package config;

import java.util.HashMap;
import java.util.Map;

public class FlowConfig {
    private String address;
    private Map<String, String> outerCallMapping;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Map<String, String> getOuterCallMapping() {
        return outerCallMapping;
    }

    public void setOuterCallMapping(Map<String, String> outerCallMapping) {
        this.outerCallMapping = outerCallMapping;
    }
}
