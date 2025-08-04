package io.github.fhnaumann.adapters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.fhnaumann.util.PreciseDecimal;

import java.io.IOException;

public class PreciseDecimalAdapter extends JsonDeserializer<PreciseDecimal> {
    @Override
    public PreciseDecimal deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String text = parser.getText();
        try {
            /*
            Handling decimal precision is still not implemented/decided. So for now, just use unlimited precision everywhere.
            Let's see if someone really needs the "correct scientific rounding".
             */
            return new PreciseDecimal(text, false);
        } catch (Exception e) {
            throw new IOException("Unable to parse PreciseDecimal: " + text, e);
        }
    }
}
