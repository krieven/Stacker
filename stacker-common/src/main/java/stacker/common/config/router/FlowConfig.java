package stacker.common.config.router;

import java.util.List;

public class FlowConfig {
    private String name;
    private String title;
    private String description;
    private String address;
    private List<NameMapping> mapping;

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

    public List<NameMapping> getMapping() {
        return mapping;
    }

    public void setMapping(List<NameMapping> mapping) {
        this.mapping = mapping;
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
}
