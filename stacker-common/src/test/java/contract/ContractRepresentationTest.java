package contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.krieven.stacker.common.contract.ContractRepresentationGenerator;
import io.github.krieven.stacker.common.dto.Command;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ContractRepresentationTest {
    private static final Logger log = LoggerFactory.getLogger(ContractRepresentationTest.class);

    @Test
    public void test() throws JsonProcessingException {
        String s = new ObjectMapper()
                .writeValueAsString(
                        new ContractRepresentationGenerator()
                                .generate(
                                        Question.class,
                                        Command.class,
                                        "application/json"
                                )
                );
        log.info(s);


    }


    private static class Question {
        @JsonProperty(required = true)
        private
        String id;

        @JsonProperty(defaultValue = "null")
        private
        String titlr;

        @JsonProperty(required = true, defaultValue = "OPEN")
        private
        Command.Type type;

        @JsonProperty(required = true)
        private
        List<Value> values;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitlr() {
            return titlr;
        }

        public void setTitlr(String titlr) {
            this.titlr = titlr;
        }

        public Command.Type getType() {
            return type;
        }

        public void setType(Command.Type type) {
            this.type = type;
        }

        public List<Value> getValues() {
            return values;
        }

        public void setValues(List<Value> values) {
            this.values = values;
        }
    }

    private static class Value {
        private String first;
        private String second;
        private int integerValue;

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getSecond() {
            return second;
        }

        public void setSecond(String second) {
            this.second = second;
        }

        public int getIntegerValue() {
            return integerValue;
        }

        public void setIntegerValue(int integerValue) {
            this.integerValue = integerValue;
        }
    }
}
