package ox.softeng.metadatacatalogue.plugins.xsd.diff.selector;

import com.google.common.base.Strings;
import org.w3c.dom.Element;
import org.xmlunit.diff.ElementSelector;

import static ox.softeng.metadatacatalogue.plugins.xsd.diff.evaluator.IgnoreNameAttributeDifferenceEvaluator.stripVersion;

import static org.xmlunit.diff.ElementSelectors.byName;

/**
 * @since 24/02/2017
 */
public class CustomElementSelector implements ElementSelector {

    @Override
    public boolean canBeCompared(Element controlElement, Element testElement) {
        if (!byName.canBeCompared(controlElement, testElement)) {
            return false;
        }

        String controlName = controlElement.getAttribute("name");
        String testName = testElement.getAttribute("name");
        Boolean result = !Strings.isNullOrEmpty(controlName) && !Strings.isNullOrEmpty(testName) ||
                         stripVersion(controlName).equals(stripVersion(testName));

        if (result && controlElement.getLocalName().equals("enumeration")) {
            String controlValue = controlElement.getAttribute("value");
            String testValue = testElement.getAttribute("value");
            result = !Strings.isNullOrEmpty(controlValue) && controlValue.equals(testValue);
        }

        return result;
    }


}

