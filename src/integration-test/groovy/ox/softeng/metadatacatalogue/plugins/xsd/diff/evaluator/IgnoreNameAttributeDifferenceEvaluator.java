package ox.softeng.metadatacatalogue.plugins.xsd.diff.evaluator;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

/**
 * @since 13/09/2017
 */
public class IgnoreNameAttributeDifferenceEvaluator implements DifferenceEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(IgnoreNameAttributeDifferenceEvaluator.class);

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) return outcome; // only evaluate differences.
        if (comparison.getType() == ComparisonType.ATTR_VALUE) {
            if (stripVersion((String) comparison.getTestDetails().getValue())
                .equals(stripVersion((String) comparison.getTestDetails().getValue()))) {
                return ComparisonResult.EQUAL;
            }
        }
        return outcome;
    }

    public static String stripVersion(String name) {
        return Strings.isNullOrEmpty(name) ? "" : name.replaceFirst("-\\d+$", "");
    }
}
