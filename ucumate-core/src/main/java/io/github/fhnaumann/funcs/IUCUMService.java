package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;

/**
 * @author Felix Naumann
 */
public interface IUCUMService extends ValidatorService, CanonicalizerService, ConverterService, RelationCheckerService, PrinterService, LookupService {

    public CanonicalizerService getCanonicalizerService();
    public void setCanonicalizerService(CanonicalizerService canonicalizerService);
    public ConverterService getConverterService();
    public void setConverterService(ConverterService converterService);
    public ValidatorService getValidatorService();
    public void setValidatorService(ValidatorService validatorService);
    public RelationCheckerService getRelationCheckerService();
    public void setRelationCheckerService(RelationCheckerService relationCheckerService);
    public LookupService getLookupService();
    public void setLookupService(LookupService lookupService);
}
