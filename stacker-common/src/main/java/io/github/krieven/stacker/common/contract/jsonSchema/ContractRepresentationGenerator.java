package io.github.krieven.stacker.common.contract.jsonSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaDraft;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

public class ContractRepresentationGenerator implements ContractRepresentationGeneratorInterface {

    private static final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(
            new ObjectMapper(),
            JsonSchemaConfig.vanillaJsonSchemaDraft4().withJsonSchemaDraft(JsonSchemaDraft.DRAFT_2019_09)
    );


    @Override
    public ContractRepresentation generate(Class question, Class answer, String contentType) {
        JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4().withJsonSchemaDraft(JsonSchemaDraft.DRAFT_07);

        return new ContractRepresentation(classToJson(question), classToJson(answer), contentType);
    }

    private JsonNode classToJson(Class clazz) {
        return jsonSchemaGenerator.generateJsonSchema(clazz);
    }


}
