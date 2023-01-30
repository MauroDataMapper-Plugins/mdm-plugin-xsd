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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd


import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImporterProviderServiceParameters

import static XsdMetadata.METADATA_NAMESPACE
import static XsdMetadata.METADATA_XSD_RESTRICTION_BASE
import static XsdMetadata.METADATA_XSD_TARGET_NAMESPACE
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * @since 10/10/2018
 */
trait Verification {

    DataElement findDataElement(Collection<DataElement> components, String name, String parent) {
        components.find {it.label == name && parent == it.dataClass.label}
    }

    def <K extends CatalogueItem> K findDataModelComponentByName(Collection<K> components, String name) {
        components.find {it.label == name}
    }

    Set<DataType> findDataTypesWithNamespace(Collection<DataType> allDataTypes, String targetNamespace) {
        allDataTypes.findAll {dt ->
            Metadata tn = dt.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE)
            (targetNamespace == null && tn == null) || (tn != null && tn.getValue() == targetNamespace)
        }.toSet()
    }

    void verifyCatalogueItem(CatalogueItem catalogueItem, String label, String description, Map<String, String> metadata = [:]) {
        assertNotNull('Should have ' + label, catalogueItem)
        assertEquals(label + ' description', description, catalogueItem.getDescription())

        if (catalogueItem.getMetadata() == null) {
            assertEquals(label + ' number of metadata', metadata.size(), 0)
        } else {
            assertEquals(label + ' number of metadata', metadata.size(), catalogueItem.getMetadata().size())
        }
        metadata.each {k, v -> verifyMetadata(catalogueItem, METADATA_NAMESPACE, k, v)}
    }

    void verifyDataClass(Collection<DataClass> dataClasses, String label, String description, int numberOfElements, String parentName,
                         Map<String, String> metadata = [:]) {
        verifyDataClass(dataClasses, label, description, numberOfElements, null, null, parentName, metadata)

    }

    void verifyDataClass(Collection<DataClass> dataClasses, String label, int numberOfElements, String parentName,
                         Map<String, String> metadata = [:]) {
        verifyDataClass(dataClasses, label, null, numberOfElements, null, null, parentName, metadata)

    }

    void verifyDataClass(Collection<DataClass> dataClasses, String label, int numberOfElements,
                         Integer minOccurs, Integer maxOccurs, String parentName, Map<String, String> metadata = [:]) {
        verifyDataClass(dataClasses, label, null, numberOfElements, minOccurs, maxOccurs, parentName, metadata)
    }

    void verifyDataClass(Collection<DataClass> dataClasses, String label, String description, int numberOfElements,
                         Integer minOccurs, Integer maxOccurs, String parentName, Map<String, String> metadata = [:]) {
        DataClass dataClass = findDataModelComponentByName(dataClasses, label)
        verifyCatalogueItem(dataClass, label, description, metadata)
        assertEquals(label + ' has child elements', numberOfElements, dataClass.getDataElements().size())
        assertEquals(label + ' max occurs', maxOccurs, dataClass.getMaxMultiplicity())
        assertEquals(label + ' min occurs', minOccurs, dataClass.getMinMultiplicity())
        assertEquals(label + ' parent', parentName, dataClass.getParent().getLabel())
    }

    void verifyDataElement(Collection<DataElement> elements, String label, String dataTypeLabel, String dataTypeType,
                           String parentName, Map<String, String> metadata = [:]) {
        verifyDataElement(elements, label, null, dataTypeLabel, dataTypeType, parentName, metadata)
    }

    void verifyDataElement(Collection<DataElement> elements, String label, String description, String dataTypeLabel,
                           String dataTypeType,
                           String parentName, Map<String, String> metadata = [:]) {
        verifyDataElement(elements, label, description, dataTypeLabel, dataTypeType, 1, 1, parentName, metadata)
    }

    void verifyDataElement(Collection<DataElement> elements, String label, String dataTypeLabel, String dataTypeType,
                           Integer minOccurs, Integer maxOccurs, String parentName, Map<String, String> metadata = [:]) {
        verifyDataElement(elements, label, null, dataTypeLabel, dataTypeType, minOccurs, maxOccurs, parentName, metadata)
    }

    void verifyDataElement(Collection<DataElement> elements, String label, String description, String dataTypeLabel,
                           String dataTypeType,
                           Integer minOccurs, Integer maxOccurs, String parentName, Map<String, String> metadata = [:]) {
        DataElement element = findDataElement(elements, label, parentName)
        verifyCatalogueItem(element, label, description, metadata)
        assertEquals(label + ' max occurs', maxOccurs, element.getMaxMultiplicity())
        assertEquals(label + ' min occurs', minOccurs, element.getMinMultiplicity())
        assertEquals(label + ' parent', parentName, element.getDataClass().getLabel())
        assertEquals(label + ' datatype', dataTypeLabel, element.getDataType().getLabel())
        assertEquals(label + ' datatype type', dataTypeType, element.getDataType().getDomainType())

    }

    void verifyDataModelValues(DataModel dataModel, XsdImporterProviderServiceParameters params, String modelName, String targetNamespace) {
        assertNotNull('Should have the datamodel', dataModel)
        assertEquals('Datamodel author', params.getAuthor(), dataModel.getAuthor())
        assertEquals('Datamodel organisation', params.getOrganisation(), dataModel.getOrganisation())
        assertEquals('Datamodel description', params.getDescription(), dataModel.getDescription())
        assertEquals('Datamodel finalised', params.getFinalised(), dataModel.getFinalised())
        assertEquals('Datamodel name', modelName, dataModel.getLabel())

        Metadata md = dataModel.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE)
        assertNotNull(modelName + ' should define a target namespace', md)
        assertEquals('Datamodel targetnamespace', targetNamespace, md.getValue())
    }

    void verifyDataType(Collection<DataType> dataTypes, String label, String type, Map<String, String> metadata = [:]) {
        verifyDataType(dataTypes, label, null, type, metadata)
    }

    void verifyDataType(Collection<DataType> dataTypes, String label, Map<String, String> metadata = [:]) {
        verifyDataType(dataTypes, label, null, 'PrimitiveType', metadata)
    }

    DataType verifyDataType(Collection<DataType> dataTypes, String label, String description, String type, Map<String, String> metadata = [:]) {
        DataType dataType = findDataModelComponentByName(dataTypes, label)
        verifyCatalogueItem(dataType, label, description, metadata)
        assertEquals(label + ' type', type, dataType.getDomainType())
        dataType
    }

    void verifyEnumerationType(Collection<DataType> dataTypes, String label, Map enumerations) {
        verifyEnumerationType(dataTypes, label, null, enumerations)
    }

    void verifyEnumerationType(Collection<DataType> dataTypes, String label, List enumerations, String base = 'string') {
        verifyEnumerationType(dataTypes, label, null, enumerations.collectEntries {[it, it]}, base)
    }

    void verifyEnumerationType(Collection<DataType> dataTypes, String label, String description,
                               Map<String, String> enumerations = [:], String base = 'string') {
        EnumerationType type = (EnumerationType) verifyDataType(dataTypes, label, description, 'EnumerationType',
                                                                [(METADATA_XSD_RESTRICTION_BASE): base])
        assertEquals(label + ' number of enumerations', enumerations.size(), type.getEnumerationValues().size())

        enumerations.each {k, v ->
            assertTrue(label + ' has enumeration ' + k, type.getEnumerationValues()
                .any {ev -> k == ev.getKey() && v == ev.getValue()
            })
        }
    }

    void verifyMetadata(CatalogueItem catalogueItem, String namespace, String key, String value) {
        Metadata metadata = catalogueItem.findMetadataByNamespaceAndKey(namespace, key)
        assertNotNull(catalogueItem.getLabel() + ' metadata ' + key + ' should exist', metadata)
        assertEquals(catalogueItem.getLabel() + ' metadata ' + key, value, metadata.getValue())
    }

}