package io.github.krieven.stacker.router;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseBodyWrapper {
    private static final Logger log = LoggerFactory.getLogger(ResponseBodyWrapper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private String flow;

    private String state;
    private byte[] body;

    public ResponseBodyWrapper(){}

    public ResponseBodyWrapper(String flow, String state, byte[] body) {
        this.flow = flow;
        this.state = state;
        this.body = body;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getFlow() {
        return flow;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    byte[] toBytes() {
        try {
            return mapper.writeValueAsBytes(this);
        } catch (JsonProcessingException e) {
            log.error("Error serializing QUESTION");
            return "Error serializing QUESTION".getBytes();
        }
    }
}
