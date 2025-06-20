package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public interface RelationCheckerService extends QuickParse {

    /**
     * Checks the relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @param allowMolMassConversion If true, allows mol->g, otherwise mol->1. If the property ucumate.enableMolMassConversion=false then this value is ignored.
     * @return A RelationResult containing information about the relation between the two terms.
     *
     */
    public RelationChecker.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean allowMolMassConversion);

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation as a string. Will be validated first.
     * @param term2 The second term in the relation. Will be validated first.
     * @param allowMolMassConversion If true, allows mol->g, otherwise mol->1. If the property ucumate.enableMolMassConversion=false then this value is ignored.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see RelationChecker.CommensurableResult
     */
    public default RelationChecker.CommensurableResult checkCommensurable(String term1, String term2, boolean allowMolMassConversion) {
        try {
            return checkCommensurable(parseOrError(term1), parseOrError(term2), allowMolMassConversion);
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
        }
    }

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @param allowMolMassConversion If true, allows mol->g, otherwise mol->1. If the property ucumate.enableMolMassConversion=false then this value is ignored.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see RelationChecker.CommensurableResult
     */
    public RelationChecker.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean allowMolMassConversion);

}
