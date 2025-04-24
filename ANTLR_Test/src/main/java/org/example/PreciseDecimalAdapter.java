package org.example;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.example.util.PreciseDecimal;

import java.io.IOException;

public class PreciseDecimalAdapter extends JsonDeserializer<PreciseDecimal> {
    @Override
    public PreciseDecimal deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        String text = parser.getText();
        try {
            return new PreciseDecimal(text);
        } catch (Exception e) {
            throw new IOException("Unable to parse PreciseDecimal: " + text, e);
        }
    }
}
