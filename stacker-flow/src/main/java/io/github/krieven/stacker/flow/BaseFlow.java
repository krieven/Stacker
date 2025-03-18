package io.github.krieven.stacker.flow;

import java.util.*;
import java.util.function.BiConsumer;

import io.github.krieven.stacker.common.ICallback;
import io.github.krieven.stacker.common.IParser;
import io.github.krieven.stacker.common.ParsingException;
import io.github.krieven.stacker.common.SerializingException;
import io.github.krieven.stacker.flow.resource.ResourceLeaf;
import io.github.krieven.stacker.flow.resource.ResourceTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.krieven.stacker.common.dto.Command;


import javax.validation.constraints.NotNull;

import static org.junit.Assert.*;

/**
 * Base class for any plain Workflow.
 * Each Workflow implements Role part of BPMN schema,
 * can receive argument of type Q and return some value of type A.
 * Workflow consists of States,
 * all data, that should be passed through Workflow between states,
 * should be stored in the instance of your FlowData class.
 *
 * @param <Q> argument type
 * @param <A> returns type
 * @param <F> flowData type
 */
public abstract class BaseFlow<Q, A, F> {
    private static final Logger log = LoggerFactory.getLogger(BaseFlow.class);

    /**
     * The Map of Resource controllers registered by states
     */
    private final Map<String, ResourceTree<ResourceController<? super F>>>
            resourceControllers = new HashMap<>();

    /**
     * The Contract of flow
     */
    private final Contract<Q, A> flowContract;
    /**
     * The class of FlowData, collected by flow
     */
    private final Class<F> flowDataClass;
    /**
     * the parser of FlowData
     */
    private final IParser flowDataParser;

    /**
     * The registry of States in the Flow
     */
    private final Map<String, State<? super F, ?>> states = new HashMap<>();

    /**
     * The Map of incoming Commands handlers
     */
    private final Map<Command.Type, BiConsumer<Command, FlowContext<F>>> incomingHandlers = new HashMap<>();

    /**
     * The entering State of Flow
     */
    private String enterState;

    {
        incomingHandlers.put(Command.Type.OPEN, (command, context) ->
                start(parseRq(command.getContentBody()), context));

        incomingHandlers.put(Command.Type.ANSWER, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RETURN, (command, context) ->
                getState(command.getState())
                        .handle(command.getContentBody(), context));

        incomingHandlers.put(Command.Type.RESOURCE, new ResourceHandler<>());
    }

    /**
     * Constructor of BaseFlow, you should call it in the constructor of your concrete Workflow class.
     *
     * @param contract       - the contract of your Workflow - Question type, Answer type and serializing format,
     *                       defined by parser.
     * @param flowDataClass  - the class of the FlowData
     * @param flowDataParser - parser of FlowData
     */
    protected BaseFlow(
            Contract<Q, A> contract,
            Class<F> flowDataClass,
            IParser flowDataParser) {

        this.flowContract = contract;
        this.flowDataClass = flowDataClass;
        this.flowDataParser = flowDataParser;
    }

    /**
     * get contract of Flow - Question class, Answer class and serialization format
     *
     * @return the Contract
     */
    public final Contract<?, ?> getContract() {
        return flowContract;
    }

    /**
     * Each Flow should define its entering State in the configure() method
     *
     * @param enterState the entering State
     */
    protected final void setEnterState(String enterState) {
        this.enterState = enterState.trim().toUpperCase();
    }

    /**
     * In this method you should configure Workflow schema by adding States and defining transitions
     */
    protected abstract void configure();

    /**
     * Sometimes the same Answer of some Workflow needed many times.
     * If your Workflow should store its FlowData in the context of current user session
     * for next call, this method should return true, otherwise it should return false.
     *
     * @param context - the context of this flow
     * @return boolean true, if FlowData should be stored for current user session
     */
    protected abstract boolean isDaemon(FlowContext<F> context);

    /**
     * When the Workflow starts, it requires the FlowData to store collected data
     *
     * @param arg     - the Question on which this Workflow should answer
     * @param context - the context of the current Workflow
     * @return new instance of your FlowData for the current call of Workflow
     */
    protected abstract F createFlowData(Q arg, FlowContext<F> context);

    /**
     * This method should extract Answer of current Workflow from its context
     *
     * @param context - the context of the current Workflow
     * @return the instance of Answer class or null
     */
    protected abstract A makeReturn(FlowContext<F> context);

    /**
     * @param name    - the name of the entering State in the schema
     * @param context - the context of this Workflow
     * @return StateCompletion
     */
    @NotNull
    protected StateCompletion enterState(@NotNull String name, @NotNull FlowContext<F> context) {
        FlowContext<F> transContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        name,
                        context.getRqUid(),
                        context.getFlowData(),
                        context.getProperties(),
                        context.getCallback()
                );
        State<? super F, ?> state = getState(name);
        if (state == null) {
            throw new IllegalStateException("State \"" + name + "\" was not found");
        }
        log.info("enter State {}", name);
        return state.onEnter(transContext);
    }

    /**
     * Adds State to Workflow schema
     *
     * @param name  - the name of adding State
     * @param state - the instance of adding State class
     */
    protected void addState(@NotNull String name, @NotNull State<? super F, ?> state) {
        assertNotNull("The NAME should not be null", name);
        name = name.trim().toUpperCase();
        assertNotEquals("The NAME should not be empty string", "", name);
        assertFalse("State with name '" + name + "' already registered", states.containsKey(name));
        assertNotNull("State should not be null", state);
        assertFalse("this State already added into the Flow", states.containsValue(state));

        states.put(name, state);
        state.setName(name);

        if (!state.resourceControllers.isEmpty()) {
            ResourceTree<ResourceController<? super F>> handlers = new ResourceTree<>();
            resourceControllers.put(name, handlers);
            String finalName = name;
            state.resourceControllers.forEach(
                    (key, value) -> {
                        log.info("State [{}]", finalName);
                        addResourceController(handlers, key, value);
                    }
            );
        }
    }

    final void handleCommand(@NotNull Command command, @NotNull ICallback<Command> callback) {
        log.info("Handling request type [{}] rqUid [{}] flow [{}]",
                command.getType(), command.getRqUid(), command.getFlow());

        F flowData = null;
        if (command.getFlowData() != null) {
            try {
                flowData = parseFlowData(command.getFlowData());
            } catch (ParsingException e) {
                log.error("Error parsing request flowData", e);
                callback.reject(e);
                return;
            }
        }

        FlowContext<F> context =
                new FlowContext<>(
                        this,
                        command.getFlow(),
                        command.getState(),
                        command.getRqUid(),
                        flowData,
                        command.getProperties(),
                        callback);

        try {
            incomingHandlers.get(command.getType()).accept(command, context);
        } catch (Exception e) {
            log.error("Error handling request", e);
            callback.reject(e);
        }
    }


    byte[] serializeFlowData(Object o) throws SerializingException {
        return flowDataParser.serialize(o);
    }

    final void configureAndVerify() {
        this.configure();
        this.validate();
    }

    private void start(Q argument, FlowContext<F> context) {
        F flowData = context.getFlowData();
        if (flowData == null) {
            flowData = createFlowData(argument, context);
        }
        FlowContext<F> newContext =
                new FlowContext<>(
                        this,
                        context.getFlowName(),
                        enterState,
                        context.getRqUid(),
                        flowData,
                        context.getProperties(),
                        context.getCallback()
                );
        getState(enterState).onEnter(newContext).doCompletion();
    }

    private State<? super F, ?> getState(String name) {
        return name == null ? null : states.get(name.trim().toUpperCase());
    }

    private Q parseRq(byte[] rq) throws ParsingException {
        return flowContract.getParser().parse(rq, flowContract.getQuestionType());
    }

    private F parseFlowData(byte[] flowData) throws ParsingException {
        return flowDataParser.parse(flowData, flowDataClass);
    }

    private void validate() {
        if (states.isEmpty()) {
            log.error("No states are defined");
        }

        if (enterState == null || !states.containsKey(enterState)) {
            throw new IllegalStateException("Enter state is not defined or not registered");
        }

        SchemaValidator validator = new SchemaValidator();
        validator.validate(getState(enterState));
        states.keySet().stream()
                .filter(k -> !validator.touched.contains(getState(k.trim().toUpperCase())))
                .forEach(k -> log.warn("State [{}] is unreachable", k));

        log.info(validator.schemaView.toString());

        log.info("hasTerminator [{}]", validator.hasTerminator);

        log.info("Flow schema is valid");
    }

    private void addResourceController(ResourceTree<ResourceController<? super F>> tree, String path, ResourceController<? super F> handler) {
        assertNotNull("Path could not be null", path);
        path = path.trim();
        assertNotEquals("Path could not be empty", "", path);
        assertNotNull("Handler could not be null", handler);

        tree.add(path, handler);

        log.info("Resource handler for \"{}\" added", path);
    }

    ResourceLeaf<ResourceController<? super F>> getResourceLeaf(String stateName, String path) {
        stateName = stateName.trim().toUpperCase();
        if (!resourceControllers.containsKey(stateName)) {
            return null;
        }
        return resourceControllers.get(stateName).find(path);
    }

    private class SchemaValidator {
        List<State<?, ?>> touched = new ArrayList<>();
        boolean hasTerminator = false;
        int shift = 0;
        StringBuilder schemaView = new StringBuilder("\n");

        void validate(State<?, ?> state) {
            schemaView.append(state.getName());
            if (touched.contains(state)) {
                schemaView.append(" (recursion)\n");
                return;
            }
            schemaView.append("\n");
            shift++;
            touched.add(state);
            Arrays.stream(state.getExits()).forEach(
                    e -> {
                        if (state.isTerminator(e)) {
                            hasTerminator = true;
                            schemaView.append(fill(shift)).append("[").append(e).append("]").append("->* (terminator)\n");
                            return;
                        }
                        State<?, ?> target = getState(state.getTransition(e));
                        assertNotNull(
                                "exit [" + e + "] from state [" + state.getName() + "] is not specified",
                                target
                        );
                        schemaView.append(fill(shift)).append("[").append(e).append("]").append("->");
                        validate(target);
                    }
            );
            shift--;
        }

        String fill(int len) {
            return "    ".repeat(Math.max(0, len));
        }
    }
}