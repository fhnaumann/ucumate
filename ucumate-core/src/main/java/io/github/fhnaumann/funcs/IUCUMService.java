package io.github.fhnaumann.funcs;

/**
 * @author Felix Naumann
 */
public interface IUCUMService extends ValidatorService, CanonicalizerService, ConverterService, RelationCheckerService, PrinterService {

    public CanonicalizerService getCanonicalizerService();
    public void setCanonicalizerService(CanonicalizerService canonicalizerService);
    public ConverterService getConverterService();
    public void setConverterService(ConverterService converterService);
    public ValidatorService getValidatorService();
    public void setValidatorService(ValidatorService validatorService);
    public RelationCheckerService getRelationCheckerService();
    public void setRelationCheckerService(RelationCheckerService relationCheckerService);
}
