package io.github.krieven.stacker.common.config.router;

import java.util.List;
import java.util.Map;

/**
 * Config of Stacker application
 */
public interface RouterConfig {
    /**
     * Display name of application
     * @return String title
     */
    String getTitle();

    /**
     * Description of application
     * @return String description
     */
    String getDescription();

    /**
     * The MainFlow - the root flow of the application
     * @return String full name of the MainFlow
     */
    String getMainFlow();

    /**
     * Resolves real sub flow name by caller flow name and mapped sub flow name
     *
     * @param callerFlowFullName full name of the caller flow
     * @param name mapping name of sub flow
     * @return real sub flow name
     */
    String resolveSubFlow(String callerFlowFullName, String name);

    /**
     * resolves the list of real names of mapped sub flows for given flow name
     *
     * @param fullFlowName the name of flow
     * @return list of real names of mapped sub flows
     */
    List<String> resolveSubFlows(String fullFlowName);

    /**
     * resolves URL or other kind of address of the flow for given flow name
     *
     * @param fullFlowName the name of the flow
     * @return the address of the flow
     */
    String resolveAddress(String fullFlowName);

    /**
     * resolves configuration properties of the flow instance in the context of application
     *
     * @param fullFlowName the name of the flow
     * @return the map of properties
     */
    Map<String, String> resolveProperties(String fullFlowName);
}
