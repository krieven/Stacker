package io.github.krieven.stacker.common.config.router.nsRouterConfig;

import java.util.Map;

public class FlowConfig {
    private String name;
    private String title;
    private AccessType access;
    private String description;
    private String address;
    private Map<String, String> mapping;
    private Map<String, String> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public AccessType getAccess() {
        return access;
    }

    public void setAccess(AccessType access) {
        this.access = access;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }


    public boolean isPublic() {
        return this.access == AccessType.EXPORT;
    }

    public boolean isImport() {
        return this.access == AccessType.IMPORT;
    }

    public enum AccessType {
        IMPORT,
        EXPORT
    }

}
