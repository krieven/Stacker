package io.github.krieven.stacker.common.contract;

public interface ContractRepresentationGeneratorInterface {
    ContractRepresentation generate(Class question, Class answer, String contentType);
}
