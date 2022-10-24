package io.github.krieven.stacker.common.config.router.nsRouterConfig;

import java.util.*;
import java.util.stream.Collectors;


public class NSRouterConfig implements io.github.krieven.stacker.common.config.router.RouterConfig {

    private final String PATH_SPLITTER = "/";

    private String title;
    private String description;
    private String mainFlow;
    private Map<String, NameSpaceConfig> nameSpaceMap;

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getMainFlow() {
        return mainFlow;
    }

    public void setMainFlow(String mainFlow) {
        this.mainFlow = mainFlow;
    }

    public Map<String, NameSpaceConfig> getNameSpaceMap() {
        return nameSpaceMap;
    }

    public void setNameSpaceMap(Map<String, NameSpaceConfig> nameSpaceMap) {
        this.nameSpaceMap = nameSpaceMap;
    }

    //MODEL LOGIC

    @Override
    public String resolveSubFlow(String callerFlow, String name) {
        String[] path = toPath(callerFlow);
        FlowConfig flowConfig = resolveFlow(path);
        String flowName;
        if (flowConfig == null ||
                flowConfig.getMapping() == null ||
                (flowName = flowConfig.getMapping().get(name)) == null) {
            return null;
        }

        FlowConfig foundFlowConfig = resolveFlow(new String[]{path[0], flowName});
        if (foundFlowConfig == null) {
            return null;
        }
        if (!foundFlowConfig.isImport()) {
            return path[0] + PATH_SPLITTER + flowName;
        }

        FlowConfig targetFlow;
        if ((targetFlow = resolveFlow(toPath(foundFlowConfig.getAddress()))) != null && targetFlow.isPublic()) {
            return foundFlowConfig.getAddress();
        }

        return null;
    }

    @Override
    public String resolveAddress(String fullFlowName) {
        String[] path = toPath(fullFlowName);
        FlowConfig flowConfig = resolveFlow(path);
        if (flowConfig == null || flowConfig.isImport()) {
            return null;
        }
        return flowConfig.getAddress();
    }

    @Override
    public Map<String, String> resolveProperties(String fullFlowName) {
        String[] path = toPath(fullFlowName);
        FlowConfig flowConfig = resolveFlow(path);
        if (flowConfig == null || flowConfig.isImport()) {
            return null;
        }
        return flowConfig.getProperties();
    }

    @Override
    public List<String> resolveSubFlows(String fullFlowName) {
        String[] path = toPath(fullFlowName);
        FlowConfig flowConfig = resolveFlow(path);
        if (flowConfig == null || flowConfig.isImport()) {
            return null;
        }
        return Optional.ofNullable(flowConfig.getMapping())
                .orElse(new HashMap<>()).keySet().stream()
                .map(name -> resolveSubFlow(fullFlowName, name))
                .collect(Collectors.toList());
    }

    private FlowConfig resolveFlow(String[] path) {
        NameSpaceConfig nameSpace;
        if (path == null ||
                getNameSpaceMap() == null ||
                (nameSpace = getNameSpaceMap().get(path[0])) == null ||
                nameSpace.getFlowConfigMap() == null
        ) {
            return null;
        }

        return nameSpace.getFlowConfigMap().get(path[1]);

    }

    private String[] toPath(String flowFullName) {
        String[] path;
        if (flowFullName == null ||
                (path = flowFullName.split(PATH_SPLITTER)).length < 2
        ) {
            return null;
        }
        return path;
    }

}
