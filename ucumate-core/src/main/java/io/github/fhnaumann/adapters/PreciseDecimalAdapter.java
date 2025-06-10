package io.github.fhnaumann.adapters;

import io.github.fhnaumann.util.PreciseDecimal;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class PreciseDecimalAdapter extends JsonDeserializer<PreciseDecimal> {
    @Override
    public PreciseDecimal deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String text = parser.getText();
        try {
            return new PreciseDecimal(text, false); // todo assume unlimited precision, change later once I have a list of consts and units that use limited precision (pi, grav constant, proton mass, etc.)
        } catch (Exception e) {
            throw new IOException("Unable to parse PreciseDecimal: " + text, e);
        }
    }
}
