package io.github.fhnaumann.adapters;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.UCUMRegistry;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class UCUMConceptCodeAdapter {

    private static final UCUMRegistry registry = UCUMRegistry.getInstance();

    public static class UCUMDefinitionSerializer<T extends UCUMDefinition.Concept> extends JsonSerializer<T> {

        @Override
        public void serialize(T concept, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(concept.code());
        }
    }

    public static class UCUMDefinitionDeserializer<T extends UCUMDefinition.Concept> extends JsonDeserializer<T> {
        @Override
        public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            return (T) registry.getConcept(jsonParser.getValueAsString()).orElseThrow();
        }
    }
}
