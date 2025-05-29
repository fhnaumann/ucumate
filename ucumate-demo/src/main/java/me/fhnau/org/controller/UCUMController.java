package me.fhnau.org.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.funcs.printer.Printer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Felix Naumann
 */
@RestController
@RequestMapping("/api")
public class UCUMController {

    @Operation(summary = "Validate a UCUM expression")
    @GetMapping("validate")
    public ValidationResponse validate(@Parameter(description = "UCUM input string") @RequestParam String input) {
        return switch (UCUMService.validate(input)) {
            case Validator.Failure failure -> new ValidationResponse(false, null);
            case Validator.Success success -> new ValidationResponse(true, UCUMService.print(success.term(), Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX));
        };
    }

    @Schema(description = "Validation result from ucumate-core")
    public record ValidationResponse(
            @Schema(description = "Whether the input is valid")
            boolean valid,
            @Schema(description = "Detailed form of the parsed input")
            String term) {}
}
