package io.github.fhnaumann.adapters;

import io.github.fhnaumann.model.UCUMDefinition;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class UCUMUnitAdapter extends JsonDeserializer<UCUMDefinition.DefinedUnit> {

    @Override
    public UCUMDefinition.DefinedUnit deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        UCUMDefinition.UnitDto unitDto = mapper.readValue(jsonParser, UCUMDefinition.UnitDto.class);
        boolean metric = "yes".equals(unitDto.isMetric());
        boolean special = "yes".equals(unitDto.isSpecial());
        boolean arbitrary = "yes".equals(unitDto.isArbitrary());
        boolean dimless = "dimless".equals(unitDto.unitClass());
        if(special) {
            return new UCUMDefinition.SpecialUnit(
                    unitDto.code(),
                    unitDto.codeAlt(),
                    metric,
                    unitDto.unitClass(),
                    unitDto.names(),
                    unitDto.printSymbol(),
                    unitDto.property(),
                    unitDto.value()
            );
        }
        if(arbitrary) {
            return new UCUMDefinition.ArbitraryUnit(
                    unitDto.code(),
                    unitDto.codeAlt(),
                    metric,
                    unitDto.unitClass(),
                    unitDto.names(),
                    unitDto.printSymbol(),
                    unitDto.property(),
                    unitDto.value()
            );
        }
        if(dimless) {
            return new UCUMDefinition.DimlessUnit(
                    unitDto.code(),
                    unitDto.codeAlt(),
                    metric,
                    unitDto.names(),
                    unitDto.printSymbol(),
                    unitDto.property(),
                    unitDto.value()
            );
        }
        return new UCUMDefinition.DerivedUnit(
                unitDto.code(),
                unitDto.codeAlt(),
                metric,
                unitDto.unitClass(),
                unitDto.names(),
                unitDto.printSymbol(),
                unitDto.property(),
                unitDto.value()
        );
    }
}
