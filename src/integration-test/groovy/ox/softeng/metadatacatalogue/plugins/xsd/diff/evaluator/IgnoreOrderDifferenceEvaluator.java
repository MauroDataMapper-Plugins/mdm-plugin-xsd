package ox.softeng.metadatacatalogue.plugins.xsd.diff.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

/**
 * @since 21/02/2017
 */
public class IgnoreOrderDifferenceEvaluator implements DifferenceEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(IgnoreOrderDifferenceEvaluator.class);

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.SIMILAR && comparison.getType() == ComparisonType.CHILD_NODELIST_SEQUENCE) {
            logger.trace("Found similar but nodelist sequence is wrong: {}", comparison);
            return ComparisonResult.EQUAL;
        }
        return outcome;
    }
}
