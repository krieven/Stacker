package io.github.krieven.stacker.common.contract.jsonSchema;

public interface ContractRepresentationGeneratorInterface {
    ContractRepresentation generate(Class question, Class answer, String contentType);
}
