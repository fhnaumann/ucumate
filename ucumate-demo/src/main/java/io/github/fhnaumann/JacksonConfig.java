package io.github.fhnaumann;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.fhnaumann.funcs.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Felix Naumann
 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule validationResultPolymorphicModule() {
        return new SimpleModule() {
            {
                setMixInAnnotation(Validator.ValidationResult.class, ValidationResultMixin.class);
            }
        };
    }

    // Mixin to inject Jackson type info externally
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Validator.Success.class, name = "success"),
            @JsonSubTypes.Type(value = Validator.Failure.class, name = "failure")
    })
    public abstract static class ValidationResultMixin {}
}

