/*
 * Copyright 2020-2021 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.BaseAttribute
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleExtensionType
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings

import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_LABEL_PREFIX
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_ATTRIBUTE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_DEFAULT
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_FIXED

/**
 * @since 05/09/2017
 */
class BaseAttributeWrapper extends ElementBasedWrapper<BaseAttribute> {

    BaseAttributeWrapper(XsdSchemaService xsdSchemaService, BaseAttribute wrappedElement) {
        super(xsdSchemaService, wrappedElement)
    }

    @Override
    String getName() {
        givenName ?: wrappedElement.getName()
    }

    @Override
    QName getRef() {
        wrappedElement.getRef()
    }

    @Override
    AbstractSimpleType getSimpleType() {
        wrappedElement.getSimpleType()
    }

    @Override
    QName getType() {
        wrappedElement.getType()
    }

    @Override
    Integer getMaxOccurs() {
        1
    }

    @Override
    Integer getMinOccurs() {
        wrappedElement.getUse() == 'required' ? 1 : 0
    }

    Boolean isProhibited() {
        wrappedElement.getUse() == 'prohibited'
    }

    @Override
    DataElement createDataTypeElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                      SimpleTypeWrapper simpleTypeWrapper) {
        if (isProhibited()) {
            warn('Is prohibited therefore not creating')
            return null
        }

        additionalMetadata[METADATA_XSD_ATTRIBUTE] = 'true'
        if (wrappedElement.getDefault()) additionalMetadata[METADATA_XSD_DEFAULT] = wrappedElement.getDefault()
        if (wrappedElement.getForm()) additionalMetadata[METADATA_LABEL_PREFIX + 'Form'] = wrappedElement.getForm().value()
        if (wrappedElement.getFixed()) additionalMetadata[METADATA_XSD_FIXED] = wrappedElement.getFixed()

        super.createDataTypeElement(user, parentDataModel, parentDataClass, schema, simpleTypeWrapper)
    }

    @Override
    RestrictionWrapper getRestriction() {
        null
    }

    @Override
    SimpleExtensionType getComplexSimpleContentExtension() {
        null
    }

    @Override
    AbstractComplexType getComplexType() {
        null
    }
}
