package io.github.krieven.stacker.common.config.router.nsRouterConfig;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;

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
        String[] mapped = {path[0], flowName};
        FlowConfig foundFlowConfig = resolveFlow(mapped);
        if (foundFlowConfig == null) {
            return null;
        }
        if (foundFlowConfig.getType() != FlowConfig.Type.IMPORT) {
            return path[0] + PATH_SPLITTER + flowName;
        }

        String[] importPath = toPath(foundFlowConfig.getAddress());
        FlowConfig targetFlow;
        if ((targetFlow = resolveFlow(importPath)) == null) {
            return null;
        }
        if (BooleanUtils.isTrue(targetFlow.getIsPublic())) {
            return foundFlowConfig.getAddress();
        }

        return null;
    }

    @Override
    public String resolveAddress(String fullFlowName) {
        String[] path = toPath(fullFlowName);
        FlowConfig flowConfig = resolveFlow(path);
        if (flowConfig == null || flowConfig.getType() == FlowConfig.Type.IMPORT) {
            return null;
        }
        return flowConfig.getAddress();
    }

    @Override
    public Map<String, String> resolveProperties(String fullFlowName) {
        String[] path = toPath(fullFlowName);
        FlowConfig flowConfig = resolveFlow(path);
        if (flowConfig == null || flowConfig.getType() == FlowConfig.Type.IMPORT) {
            return null;
        }
        return flowConfig.getProperties();
    }

    @Override
    public List<String> resolveSubFlows(String fullFlowName) {
        String[] path = toPath(fullFlowName);
        FlowConfig flowConfig = resolveFlow(path);
        if (flowConfig == null || flowConfig.getType() == FlowConfig.Type.IMPORT) {
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
                (nameSpace = getNameSpaceMap().get(path[0])) == null
        ) {
            return null;
        }

        return nameSpace.get(path[1]);

    }

    private String[] toPath(String flowFullName) {
        String[] path;
        if (flowFullName == null ||
                (path = flowFullName.trim().split(PATH_SPLITTER)).length < 2 ||
                path[0] == null || path[1] == null
        ) {
            return null;
        }
        return path;
    }

}
