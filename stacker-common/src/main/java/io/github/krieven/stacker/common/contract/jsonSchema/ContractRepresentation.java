package io.github.krieven.stacker.common.contract.jsonSchema;

import com.fasterxml.jackson.databind.JsonNode;

public class ContractRepresentation {

    private JsonNode question;
    private JsonNode answer;
    private String contentType;

    public ContractRepresentation(JsonNode question, JsonNode answer, String contentType) {
        this.question = question;
        this.answer = answer;
        this.contentType = contentType;
    }

    public JsonNode getQuestion() {
        return question;
    }

    public void setQuestion(JsonNode question) {
        this.question = question;
    }

    public JsonNode getAnswer() {
        return answer;
    }

    public void setAnswer(JsonNode answer) {
        this.answer = answer;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }


}
