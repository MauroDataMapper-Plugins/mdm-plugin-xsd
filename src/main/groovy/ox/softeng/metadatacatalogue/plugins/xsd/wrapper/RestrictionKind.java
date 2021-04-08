package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Facet;

import com.google.common.base.CaseFormat;

import java.util.Arrays;
import javax.xml.bind.JAXBElement;

import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_LABEL_RESTRICTION_PREFIX;

/**
 * @since 12/09/2017
 */
public enum RestrictionKind {

    maxExclusive("Max Exclusive"),
    minInclusive("Min Exclusive"),
    minLength("Min Length"),
    enumeration("Enumeration"),
    length("Length"),
    pattern("Pattern/Regex"),
    fractionDigits("Number of Fraction Digits"),
    whiteSpace("Whitespace Handling"),
    totalDigits("Total Digits"),
    maxLength("Max Length"),
    minExclusive("Min Exclusive"),
    maxInclusive("Max Exclusive");

    public final String displayText;

    RestrictionKind(String displayText) {
        this.displayText = METADATA_LABEL_RESTRICTION_PREFIX + displayText;
    }

    public static RestrictionKind findFromDisplayText(String displayText) {
        return Arrays.stream(values()).filter(v -> v.displayText.equalsIgnoreCase(displayText)).findFirst().orElse(null);
    }

    public static RestrictionKind findFromElement(JAXBElement element) {
        return RestrictionKind.valueOf(element.getName().getLocalPart());
    }

    public static RestrictionKind findFromFacet(Facet facet) {
        return RestrictionKind.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, facet.getClass().getSimpleName()));
    }
}
