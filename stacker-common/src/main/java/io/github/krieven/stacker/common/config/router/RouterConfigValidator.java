package io.github.krieven.stacker.common.config.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RouterConfigValidator {
    private static final Logger LOG = LoggerFactory.getLogger(RouterConfigValidator.class);

    public static boolean isValid(RouterConfig config) {
        if (config == null || config.getMainFlow() == null || config.getMainFlow().isEmpty()) {
            LOG.error("MainFlow is not specified");
            return false;
        }
        if (config.resolveAddress(config.getMainFlow()) == null) {
            LOG.error("MainFlow is not accessible");
            return false;
        }

        return checkTree(config, config.getMainFlow(), new HashSet<>());
    }

    private static boolean checkTree(RouterConfig config, String flowName, Set<String> path) {
        if (flowName == null || config.resolveAddress(flowName) == null) {
            LOG.error("Flow {} trying to access to inaccessible flow {}", path, flowName);
            return false;
        }
        if (!path.add(flowName)) {
            LOG.error("Recursion found in the path: {} <- \"{}\"", path.toArray(), flowName);
            return false;
        }
        List<String> mappedNames;
        if ((mappedNames = config.resolveSubFlows(flowName)) == null) {
            LOG.error("flow \"{}\" not found", flowName);
            return false;
        }
        return mappedNames.isEmpty() ||
                mappedNames.stream().allMatch(name -> checkTree(config, name, new HashSet<>(path)));
    }
}
