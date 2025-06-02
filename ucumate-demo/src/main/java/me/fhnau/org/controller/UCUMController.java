package me.fhnau.org.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.Converter;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.funcs.printer.*;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.util.PreciseDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contains the REST endpoints for the demo app.
 *
 * @author Felix Naumann
 */
@RestController
@RequestMapping("/api")
public class UCUMController {

    private static final Map<RenderFormat, Printer> PRINTERS = Map.of(
            RenderFormat.ucum, new UCUMSyntaxPrinter(),
            RenderFormat.ucum_expressive, new ExpressiveUCUMSyntaxPrinter(),
            RenderFormat.common, new WolframAlphaSyntaxPrinter(),
            RenderFormat.latex, new LatexPrinter()
    );

    private Map<RenderFormat, String> createRenderedFormats(UCUMExpression parsedExpression, List<RenderFormat> requestedFormats) {
        if(requestedFormats == null) {
            requestedFormats = new ArrayList<>();
        }
        if(requestedFormats.isEmpty()) {
            // fallback to expressive ucum syntax if no requested formats given
            requestedFormats.add(RenderFormat.ucum_expressive);
        }
        return requestedFormats.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        renderFormat -> PRINTERS.get(renderFormat).print(parsedExpression)
                ));
    }

    @Operation(summary = "Validate a UCUM expression")
    @GetMapping("validate")
    public ValidationResponse validate(
            @Parameter(description = "UCUM input string") @RequestParam String input,
            @Parameter(description = "Rendering options") @RequestParam(required = false) List<RenderFormat> formats
            ) {
        return switch (UCUMService.validate(input)) {
            case Validator.Failure failure -> new ValidationResponse(false, null);
            case Validator.Success success -> new ValidationResponse(true, createRenderedFormats(success.term(), formats));
        };
    }

    @Operation(summary = "Canonicalize a UCUM expression")
    @GetMapping("canonicalize")
    public CanonicalizationResponse canonicalize(
            @Parameter(description = "UCUM input string") @RequestParam String input,
            @Parameter(description = "Rendering options") @RequestParam(required = false) List<RenderFormat> formats
    ) {
        Validator.ValidationResult validationResult = UCUMService.validate(input);
        return switch (validationResult) {
            case Validator.Failure failure -> new CanonicalizationResponse(false, null,null);
            case Validator.Success success -> switch (UCUMService.canonicalize(success.term())) {
                case Canonicalizer.FailedCanonicalization failedCanonicalization -> new CanonicalizationResponse(false, null,null);
                case Canonicalizer.Success success1 -> new CanonicalizationResponse(true, success1.magnitude().toString(), createRenderedFormats(success1.canonicalTerm(), formats));
            };
        };
    }

    @Operation(summary = "Convert a UCUM expression to another UCUM expression")
    @GetMapping("convert")
    public ConversionResponse convert(
            @Parameter(description = "from conversion factor") @RequestParam String fromFactor,
            @Parameter(description = "from conversion UCUM input string") @RequestParam String fromInput,
            @Parameter(description = "to conversion UCUM input string") @RequestParam String toInput
            ) {
        PreciseDecimal fromFactorPd;
        try {
            fromFactorPd = new PreciseDecimal(fromFactor);
        } catch (Exception e) {
            return new ConversionResponse(false, null);
        }
        Validator.ValidationResult fromValidation = UCUMService.validate(fromInput);
        Validator.ValidationResult toValidation = UCUMService.validate(toInput);
        if(!(fromValidation instanceof Validator.Success fromSuccess)) {
            return new ConversionResponse(false, null);
        }
        if(!(toValidation instanceof Validator.Success toSuccess)) {
            return new ConversionResponse(false, null);
        }
        return switch (UCUMService.convert(fromFactorPd, fromSuccess.term(), toSuccess.term())) {
            case Converter.FailedConversion failedConversion -> new ConversionResponse(false, null);
            case Converter.Success success -> new ConversionResponse(true, success.conversionFactor().toString());
        };
    }

    @Schema(description = "Conversion result from ucumate-core")
    public record ConversionResponse(
            @Schema(description = "Whether the input is valid") boolean valid,
            @Schema(description = "Resulting conversion factor") String resultingConversionFactor
    ) {}

    @Schema(description = "Canonicalization result from ucumate-core")
    public record CanonicalizationResponse(
            @Schema(description = "Whether the input is valid")
            boolean valid,
            @Schema(description = "Magnitude factor obtained during canonicalization")
            String magnitude,
            @Schema(description = "The canonical term in the requested rendered formats")
            Map<RenderFormat, String> rendered) {}

    @Schema(description = "Validation result from ucumate-core")
    public record ValidationResponse(
            @Schema(description = "Whether the input is valid")
            boolean valid,
            @Schema(description = "The parsed term in the requested rendered formats")
            Map<RenderFormat, String> rendered) {}

    @Schema(enumAsRef = true)
    public enum RenderFormat {
        ucum, ucum_expressive, common, latex
    }
}
