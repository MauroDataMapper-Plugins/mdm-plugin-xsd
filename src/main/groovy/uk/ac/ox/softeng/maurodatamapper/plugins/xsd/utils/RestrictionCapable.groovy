/*
 * Copyright 2020-2023 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils

import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
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
            addMetadataToComponent(component, XsdMetadata.METADATA_XSD_RESTRICTION_BASE, restrictionComponent.label ?: restriction.extensionName, user)

            restrictions.each {k, v ->
                addMetadataToComponent(component, k, v, user)
            }
        }
    }

}