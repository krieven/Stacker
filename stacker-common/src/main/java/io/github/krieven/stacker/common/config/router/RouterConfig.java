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
     * The MainFlow - is the root flow of the application
     * @return String full name of the MainFlow
     */
    String getMainFlow();

    /**
     *
     * @param callerFlowFullName
     * @param name
     * @return
     */
    String resolveSubFlow(String callerFlowFullName, String name);

    /**
     *
     * @param fullFlowName
     * @return
     */
    List<String> resolveSubFlows(String fullFlowName);

    /**
     *
     * @param fullFlowName
     * @return
     */
    String resolveAddress(String fullFlowName);

    /**
     *
     * @param fullFlowName
     * @return
     */
    Map<String, String> resolveProperties(String fullFlowName);
}
