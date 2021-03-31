package stacker.router.server.config;

import java.util.List;

public class FlowConfig {
    private String name;
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
}
