/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLinkService
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClassService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElementService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationTypeService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveTypeService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ReferenceType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ReferenceTypeService
import uk.ac.ox.softeng.maurodatamapper.security.User

/**
 * @since 16/01/2018
 */
class XsdSchemaService {

    SemanticLinkService semanticLinkService
    DataElementService dataElementService
    DataClassService dataClassService
    PrimitiveTypeService primitiveTypeService
    EnumerationTypeService enumerationTypeService
    ReferenceTypeService referenceTypeService

    PrimitiveType createPrimitiveTypeForDataModel(DataModel dataModel, String label, String description, User createdBy,
                                                  String units = null) {
        primitiveTypeService.findOrCreateDataTypeForDataModel(dataModel, label, description, createdBy, units)
    }

    EnumerationType createEnumerationTypeForDataModel(DataModel dataModel, String label, String description, User createdBy) {
        enumerationTypeService.findOrCreateDataTypeForDataModel(dataModel, label, description, createdBy)
    }

    ReferenceType createReferenceTypeForDataModel(DataModel dataModel, String label, String description, User createdBy,
                                                  DataClass referenceClass) {
        referenceTypeService.findOrCreateDataTypeForDataModel(dataModel, label, description, createdBy, referenceClass)
    }

    DataElement createDataElementForDataClass(DataClass parentClass, String label, String description, User createdBy, DataType dataType,
                                              Integer minMultiplicity, Integer maxMultiplicity) {
        dataElementService.findOrCreateDataElementForDataClass(parentClass, label, description, createdBy, dataType, minMultiplicity, maxMultiplicity)
    }

    //TODO CatalogueItem or ModelItem?
    // What can the parent of a DC be???
    DataClass createDataClass(DataModel parent, String label, String description, User createdBy, Integer minMultiplicity,
                              Integer maxMultiplicity) {
        DataClass dataClass = dataClassService.findOrCreateDataClass(parent, label, description, createdBy, minMultiplicity, maxMultiplicity)
        dataClass
    }

    DataClass createDataClass(DataClass parent, String label, String description, User createdBy, Integer minMultiplicity,
                              Integer maxMultiplicity) {
        DataClass dataClass = dataClassService.findOrCreateDataClass(parent, label, description, createdBy, minMultiplicity, maxMultiplicity)
        dataClass
    }


    EnumerationType addEnumerationValueToEnumerationType(EnumerationType enumerationType, String key, String value, User createdBy) {
        enumerationTypeService.addEnumerationValueToEnumerationType(enumerationType, key, value, createdBy)
    }

    CatalogueItem findCommonParent(DataClass leftDataClass, DataClass rightDataClass) {
        dataClassService.findCommonParent(leftDataClass, rightDataClass)
    }

    void moveDataClassToParent(DataClass dataClass, CatalogueItem parent) {
        dataClassService.moveDataClassToParent(dataClass, parent)
    }

    DataElement createDuplicateElementForDataClass(User user, DataClass parent, DataElement original) {

        DataElement duplicate = createDataElementForDataClass(parent, original.label, original.description, user, original.dataType,
                original.minMultiplicity, original.maxMultiplicity)
        original.getMetadata()?.each { md ->
            duplicate.addToMetadata(md.namespace, md.key, md.value, user.emailAddress)
        }

        duplicate
    }

    Set<DataElement> getDataElements(DataClass dataClass) {
        dataElementService.findAllByDataClassIdJoinDataType(dataClass.id)
    }

}
