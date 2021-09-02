package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils

import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Facet
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionWrapper
import uk.ac.ox.softeng.maurodatamapper.security.User

import javax.xml.bind.JAXBElement


/**
 * @since 09/10/2018
 */
trait RestrictionCapable {

    abstract RestrictionWrapper getRestriction()

    abstract void addMetadataToComponent(CatalogueItem component, String key, String value, User user)

    //abstract void ensureRestrictionExtensionExists(CatalogueUser user, DataModel dataModel, SchemaWrapper schema)

    Boolean isRestricted() {
        getRestriction()
    }

    Boolean hasRestrictions() {
        restriction.restrictions
    }

    Map<String, String> getRestrictions() {
        restriction.findAllRestrictionsNotOfKind(RestrictionKind.enumeration).collectEntries {e ->
            Facet f
            RestrictionKind rk
            if (e instanceof JAXBElement) {
                f = (Facet) e.getValue()
                rk = RestrictionKind.findFromElement(e)
            } else {
                f = e as Facet
                rk = RestrictionKind.findFromFacet(f)
            }
            [rk.displayText, f.getValue()]
        }
    }

    void addRestrictionsToMetadata(CatalogueItem component, User user, CatalogueItem restrictionComponent) {
        if (restriction) {
            addMetadataToComponent(component, XsdPlugin.METADATA_XSD_RESTRICTION_BASE, restrictionComponent.label ?: restriction.extensionName, user)

            restrictions.each {k, v ->
                addMetadataToComponent(component, k, v, user)
            }
        }
    }

}