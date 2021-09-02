package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Facet

import com.google.common.base.CaseFormat
import org.slf4j.Logger

/**
 * @since 02/10/2018
 */
trait XsdNaming {

    abstract Logger getLogger()

    String createComplexTypeName(String name) {
        standardiseTypeName(name)
    }

    String createSimpleTypeName(String name, boolean addTypeEnding = false) {
        addTypeEnding ? addTypeEndingToName(standardiseTypeName(name)) : standardiseTypeName(name)
    }

    String addTypeEndingToName(String name) {
        name.find(/Type\d+$/) ? name : "${name}Type"
    }

    String standardiseTypeName(String name, boolean collapse = true) {

        // Replace all special separators with an underscore
        String[] split = name.split(/[\s\-._]/)
        //logger.debug('split {}', split.toList())

        // Standardise from CamelCase to camel_case and split on the underscore
        List<String> standardised = split.collectMany {s ->
            if (isAllSameCase(s)) [s]
            else
                CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s).split('_').toList()
        }
        //logger.debug('standardised {}', standardised)

        // Group all standardised, this is required to collapse single letters into 1, this occurs when you have multiple uppercase letters next to
        // each other.
        List<String> grouped = []
        if (standardised.size() == 1) grouped << standardised.first()
        else {
            StringBuilder sb = new StringBuilder(standardised[0])
            for (int i = 1; i < standardised.size(); i++) {
                String current = standardised[i]
                String last = standardised[i - 1]
                if (!collapse || !current.equalsIgnoreCase(last)) {
                    if (last.length() == 1 && current.length() == 1) sb.append(current)
                    else {
                        grouped << sb.toString()
                        sb = new StringBuilder(current)
                    }
                }
            }
            grouped << sb.toString()
        }
        //        logger.debug('grouped {}', grouped)

        // Ensure no repeating sequences and join using underscore
        String built = grouped.collect {g -> g.find(/(\w{2,})\1/) {it[1]} ?: g}.join('_')
        //        logger.debug('Built {}', built)

        // Convert underscore to camelCase
        CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, built)
    }

    String getElementName(Annotated element) {
        if (element instanceof Facet) return ((Facet) element).getValue()
        if (element instanceof AbstractSimpleType) return ((AbstractSimpleType) element).getName()
        if (element instanceof AbstractElement) return ((AbstractElement) element).getName()
        element.toString()
    }

    String normaliseSpace(String str) {
        str.replaceAll('\\s{2,}', ' ').trim()
    }

    Boolean isAllSameCase(String str) {
        str == str.toUpperCase() || str == str.toLowerCase()
    }
}