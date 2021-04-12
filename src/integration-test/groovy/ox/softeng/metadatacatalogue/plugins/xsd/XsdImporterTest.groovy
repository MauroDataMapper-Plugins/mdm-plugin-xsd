/*
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package ox.softeng.metadatacatalogue.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImportParameters

import ox.softeng.metadatacatalogue.core.api.exception.ApiException
import ox.softeng.metadatacatalogue.core.catalogue.CatalogueItem
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataClass
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataElement
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.DataType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.EnumerationType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel
import ox.softeng.metadatacatalogue.core.facet.Metadata

import org.apache.commons.lang3.tuple.Pair
import org.junit.Test

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_ALL
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_CHOICE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_DEFAULT
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_EXTENSION_BASE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_FIXED
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_RESTRICTION_BASE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_TARGET_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.OpenAttrsWrapper.XS_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.fractionDigits
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.length
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.maxExclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.maxInclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.maxLength
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.minExclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.minInclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.minLength
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.pattern
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.totalDigits
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.whiteSpace

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * @since 08/09/2017
 */
public class XsdImporterTest extends XsdTest {

    @Test
    public void testImportComplex() throws IOException, ApiException {
        XsdImportParameters params = createImportParameters("complex.xsd", "XSD Test: Complex model");

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params);

        verifyDataModelValues(dataModel, params, "XSD Test: Complex model", "https://metadatacatalogue.com/xsd/test/complex/1.0");

        /*
       DataType checking
        */

        Set<DataType> types = dataModel.getDataTypes();
        assertEquals("Number of datatypes", 57, types.size());

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, "http://www.w3.org/2001/XMLSchema");
        assertEquals("Number of xs datatypes", 44, wsTypes.size());

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null);
        assertEquals("Number of specific datatypes", 13, specificTypes.size());

        verifyDataType(specificTypes, "gUID",
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "string"),
                       Pair.of(pattern.displayText, "([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})|" +
                                                    "(\\{[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\})"));

        verifyDataType(specificTypes, "numberWithRestrictions",
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "integer"),
                       Pair.of(totalDigits.displayText, "3"),
                       Pair.of(minInclusive.displayText, "1"),
                       Pair.of(maxInclusive.displayText, "100"));

        verifyDataType(specificTypes, "mandatoryString", "A mandatory string type", "PrimitiveType",
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "string"),
                       Pair.of(minLength.displayText, "1"));

        verifyDataType(specificTypes, "patternedString",
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "string"),
                       Pair.of(minLength.displayText, "3"),
                       Pair.of(pattern.displayText, "\\w+\\.\\w+"),
                       Pair.of(maxLength.displayText, "20"));

        verifyDataType(specificTypes, "stringWithRestrictions",
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "string"),
                       Pair.of(length.displayText, "15"),
                       Pair.of(whiteSpace.displayText, "replace"));

        verifyDataType(specificTypes, "anotherNumberWithRestrictions",
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "decimal"),
                       Pair.of(totalDigits.displayText, "2"),
                       Pair.of(fractionDigits.displayText, "2"),
                       Pair.of(minExclusive.displayText, "0"),
                       Pair.of(maxExclusive.displayText, "100"));

        verifyEnumerationType(specificTypes, "enumeratedString", "An enumeration",
                              Pair.of("Y", "Yes"),
                              Pair.of("N", "No"),
                              Pair.of("U", "Unknown"));
        verifyEnumerationType(specificTypes, "elementC", null,
                              Pair.of("1", "Possible"),
                              Pair.of("2", "Not Possible"),
                              Pair.of("3", "Probable"));

        verifyDataType(specificTypes, "choiceB", "This is a list of a new complex type which provides a choice of date or datetime",
                       "ReferenceType");
        verifyDataType(specificTypes, "elementP", "Element with a local complex type", "ReferenceType");
        verifyDataType(specificTypes, "complexComplexM", "ReferenceType");
        verifyDataType(specificTypes, "allL", "An element with an all complex element", "ReferenceType");
        verifyDataType(specificTypes, "complexTypeQ", "ReferenceType");

        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses();
        // 17 in the XSDs however we only create the ones which are actually used
        assertEquals("Number of dataclasses", 6, dataClasses.size());

        // complexTypeA is only used by the topElement, which automatically makes a new dataclass to match the top level element
        verifyDataClass(dataClasses, "topElement", "The top element complex type", 6, 1, 1, "XSD Test: Complex model");
        verifyDataClass(dataClasses, "elementP", null, 5, "topElement");
        verifyDataClass(dataClasses, "complexTypeQ", null, 7, "topElement");
        verifyDataClass(dataClasses, "allL", "An all complex type", 2, "topElement");
        verifyDataClass(dataClasses, "choiceB", "A choice complex type", 3, "XSD Test: Complex model");
        verifyDataClass(dataClasses, "complexComplexM", "A complex element which extends another", 4, "topElement",
                        Pair.of(METADATA_XSD_EXTENSION_BASE, "choiceB"));

        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collect {it.childDataElements}.flatten()
        assertEquals("Number of elements", 27, elements.size());

        // topElement
        verifyDataElement(elements, "elementP", "Element with a local complex type", "elementP", "ReferenceType", "topElement");
        verifyDataElement(elements, "element_H", "regex string", "patternedString", "PrimitiveType", "topElement");
        verifyDataElement(elements, "element-i", "remaining restrictions applied to a string", "stringWithRestrictions", "PrimitiveType",
                          "topElement");
        verifyDataElement(elements, "ElementK", "An element with an all complex element", "allL", "ReferenceType", "topElement");
        verifyDataElement(elements, "elementO", "complexComplexM", "ReferenceType", "topElement");
        verifyDataElement(elements, "elementV", "complexTypeQ", "ReferenceType", "topElement");

        // elementP
        verifyDataElement(elements, "elementA", "This is a simple string which is fixed to 'Hello'", "string", "PrimitiveType", "elementP",
                          Pair.of(METADATA_XSD_FIXED, "Hello"));
        verifyDataElement(elements, "elementB", "This is an enumerated string", "enumeratedString", "EnumerationType", "elementP",
                          Pair.of(METADATA_XSD_DEFAULT, "Y"));
        verifyDataElement(elements, "elementC", "This is an optional local enumerated string", "elementC", "EnumerationType", 0, 1, "elementP");
        verifyDataElement(elements, "elementG", "This is a string entry where there must be contents in the element", "mandatoryString",
                          "PrimitiveType", "elementP");
        verifyDataElement(elements, "elementD", "This is a list of a new complex type which provides a choice of date or datetime", "choiceB",
                          "ReferenceType", 1, -1, "elementP");

        //complexTypeQ
        verifyDataElement(elements, "elementR", "choiceB", "ReferenceType", "complexTypeQ");
        verifyDataElement(elements, "elementS", "mandatoryString", "PrimitiveType", 0, 1, "complexTypeQ");
        verifyDataElement(elements, "elementY", "choiceB", "ReferenceType", 0, 1, "complexTypeQ");
        verifyDataElement(elements, "elementT", "decimal", "PrimitiveType", "complexTypeQ",
                          Pair.of(METADATA_XSD_CHOICE, "choice-1"));
        verifyDataElement(elements, "elementU", "numberWithRestrictions", "PrimitiveType", "complexTypeQ",
                          Pair.of(METADATA_XSD_CHOICE, "choice-1"));

        verifyDataElement(elements, "elementW", "stringWithRestrictions", "PrimitiveType", 1, -1, "complexTypeQ",
                          Pair.of(METADATA_XSD_CHOICE, "choice-2"));
        verifyDataElement(elements, "elementX", "patternedString", "PrimitiveType", 1, -1, "complexTypeQ",
                          Pair.of(METADATA_XSD_CHOICE, "choice-2"));


        //allL
        verifyDataElement(elements, "element-J-number", "numberWithRestrictions", "PrimitiveType", "allL",
                          Pair.of(METADATA_XSD_ALL, "true"));
        verifyDataElement(elements, "Element_J_Decimal", "anotherNumberWithRestrictions", "PrimitiveType", "allL",
                          Pair.of(METADATA_XSD_ALL, "true"));

        //choiceB
        verifyDataElement(elements, "mappingId", "gUID", "PrimitiveType", 0, 1, "choiceB");
        verifyDataElement(elements, "elementE", "The choice for date", "date", "PrimitiveType", "choiceB",
                          Pair.of(METADATA_XSD_CHOICE, "choice"));
        verifyDataElement(elements, "elementF", "The choice for datetime", "dateTime", "PrimitiveType", "choiceB",
                          Pair.of(METADATA_XSD_CHOICE, "choice"));

        //complexM
        verifyDataElement(elements, "mappingId", "gUID", "PrimitiveType", 0, 1, "complexComplexM");
        verifyDataElement(elements, "elementE", "The choice for date", "date", "PrimitiveType", "complexComplexM",
                          Pair.of(METADATA_XSD_CHOICE, "choice"));
        verifyDataElement(elements, "elementF", "The choice for datetime", "dateTime", "PrimitiveType", "complexComplexM",
                          Pair.of(METADATA_XSD_CHOICE, "choice"));
        verifyDataElement(elements, "elementN", "mandatoryString", "PrimitiveType", "complexComplexM");
    }

    @Test
    public void testImportGelCancerRac() throws IOException {
        XsdImportParameters params = createImportParameters("RegistrationAndConsentsCancer-v3.1.2.xsd", "XSD Test: GEL CAN RAC");

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params);

        verifyDataModelValues(dataModel, params, "XSD Test: GEL CAN RAC", "https://genomicsengland.co.uk/xsd/cancer/3.1.2");

        /*
        DataType checking
         */

        Set<DataType> types = dataModel.getDataTypes();
        assertEquals("Number of datatypes", 91, types.size());

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, "http://www.w3.org/2001/XMLSchema");
        assertEquals("Number of xs datatypes", 44, wsTypes.size());

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null);
        assertEquals("Number of specific datatypes", 47, specificTypes.size());


        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses();
        // 17 in the XSDs however we only create the ones which are actually used
        assertEquals("Number of dataclasses", 12, dataClasses.size());

        verifyDataClass(dataClasses, "registration-and-consents", null, 2, 1, 1, "XSD Test: GEL CAN RAC");

        verifyDataClass(dataClasses, "metadata", 6, "registration-and-consents");
        verifyDataClass(dataClasses, "subject29260", 3, "registration-and-consents");

        verifyDataClass(dataClasses, "eventDetails40374", 2, "subject29260");

        verifyDataClass(dataClasses, "participantIdentifiers39047", 5, "subject29260");
        verifyDataClass(dataClasses, "personIdentifier42125", 3, "participantIdentifiers39047");

        verifyDataClass(dataClasses, "registration12831", 10, "subject29260");
        verifyDataClass(dataClasses, "participantContactDetails12528", 9, "registration12831");
        verifyDataClass(dataClasses, "diseaseInformationTumourSample33073", 2, "registration12831");
        verifyDataClass(dataClasses, "consultantDetails14515", 5, "registration12831");

        verifyDataClass(dataClasses, "consent12541", 10, "subject29260");
        verifyDataClass(dataClasses, "consentDetails29742", 2, "consent12541");

        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collect {it.childDataElements}.flatten()
        assertEquals("Number of elements", 59, elements.size());
    }

    @Test
    public void testImportSimple() throws IOException {
        XsdImportParameters params = createImportParameters("simple.xsd", "XSD Test: Simple model");

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params);

        verifyDataModelValues(dataModel, params, "XSD Test: Simple model", "https://metadatacatalogue.com/xsd/test/simple/1.0");

        /*
        DataType checking
         */

        Set<DataType> types = dataModel.getDataTypes();
        assertEquals("Number of datatypes", 48, types.size());

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE);
        assertEquals("Number of xs datatypes", 44, wsTypes.size());

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null);
        assertEquals("Number of specific datatypes", 4, specificTypes.size());


        verifyDataType(specificTypes, "mandatoryString", "A mandatory string type", "PrimitiveType",
                       Pair.of(minLength.displayText, "1"),
                       Pair.of(METADATA_XSD_RESTRICTION_BASE, "string"));
        verifyDataType(specificTypes, "complexTypeB", "This is a list of a new complex type which provides a choice of date or datetime",
                       "ReferenceType");
        verifyEnumerationType(specificTypes, "enumeratedString", "An enumeration",
                              Pair.of("Y", "Yes"),
                              Pair.of("N", "No"),
                              Pair.of("U", "Unknown"));
        verifyEnumerationType(specificTypes, "elementC", null,
                              Pair.of("1", "Possible"),
                              Pair.of("2", "Not Possible"),
                              Pair.of("3", "Probable"));


        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses();
        assertEquals("Number of dataclasses", 2, dataClasses.size());

        // complexTypeA is only used by the topElement, which automatically makes a new dataclass to match the top level element
        verifyDataClass(dataClasses, "topElement", "The top element complex type", 5, 1, 1, "XSD Test: Simple model");
        verifyDataClass(dataClasses, "complexTypeB", "A choice complex type", 2, "topElement");


        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collect {it.childDataElements}.flatten()
        assertEquals("Number of elements", 7, elements.size());

        verifyDataElement(elements, "elementA", "This is a simple string", "string", "PrimitiveType", "topElement");
        verifyDataElement(elements, "elementB", "This is an enumerated string", "enumeratedString", "EnumerationType", "topElement");
        verifyDataElement(elements, "elementC", "This is an optional local enumerated string", "elementC", "EnumerationType", 0, 1,
                          "topElement");
        verifyDataElement(elements, "elementD", "This is a list of a new complex type which provides a choice of date or datetime", "complexTypeB",
                          "ReferenceType", 1, -1, "topElement");
        verifyDataElement(elements, "elementG", "This is a string entry where there must be contents in the element", "mandatoryString",
                          "PrimitiveType", "topElement");

        verifyDataElement(elements, "elementE", "The choice for date", "date", "PrimitiveType", "complexTypeB",
                          Pair.of(METADATA_XSD_CHOICE, "choice"));
        verifyDataElement(elements, "elementF", "The choice for datetime", "dateTime", "PrimitiveType", "complexTypeB",
                          Pair.of(METADATA_XSD_CHOICE, "choice"));
    }

    private DataElement findDataElement(Collection<DataElement> components, String name, String parent) {
        return components.findAll {it.label == name}.find {parent == it.dataClass.label}
    }

    private <K extends CatalogueItem> K findDataModelComponentByName(Collection<K> components, String name) {
        return components.find {it.label == name}
    }

    private Set<DataType> findDataTypesWithNamespace(Collection<DataType> allDataTypes, String targetNamespace) {
        return allDataTypes.findAll {dt ->
            Metadata tn = dt.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE);
            return (targetNamespace == null && tn == null) || (tn != null && tn.getValue().equals(targetNamespace));
        }.toSet()
    }

    @SafeVarargs
    private final void verifyCatalogueItem(CatalogueItem catalogueItem, String label, String description, Pair<String, String>... metadata) {
        assertNotNull("Should have " + label, catalogueItem);
        assertEquals(label + " description", description, catalogueItem.getDescription());

        if (catalogueItem.getMetadata() == null) {
            assertEquals(label + " number of metadata", metadata.length, 0);
        } else {
            assertEquals(label + " number of metadata", metadata.length, catalogueItem.getMetadata().size());
        }
        for (Pair<String, String> pair : metadata) {
            verifyMetadata(catalogueItem, METADATA_NAMESPACE, pair.getKey(), pair.getValue());
        }
    }

    private void verifyDataClass(Collection<DataClass> dataClasses, String label, int numberOfElements, String parentName) {
        verifyDataClass(dataClasses, label, null, numberOfElements, null, null, parentName);

    }

    @SafeVarargs
    private final void verifyDataClass(Collection<DataClass> dataClasses, String label, String description, int numberOfElements, String parentName,
                                       Pair<String, String>... metadata) {
        verifyDataClass(dataClasses, label, description, numberOfElements, null, null, parentName, metadata);

    }

    @SafeVarargs
    private final void verifyDataClass(Collection<DataClass> dataClasses, String label, String description, int numberOfElements,
                                       Integer minOccurs, Integer maxOccurs, String parentName, Pair<String, String>... metadata) {
        DataClass dataClass = findDataModelComponentByName(dataClasses, label);
        verifyCatalogueItem(dataClass, label, description, metadata);
        assertEquals(label + " has child elements", numberOfElements, dataClass.getChildDataElements().size());
        assertEquals(label + " max occurs", maxOccurs, dataClass.getMaxMultiplicity());
        assertEquals(label + " min occurs", minOccurs, dataClass.getMinMultiplicity());
        assertEquals(label + " parent", parentName, dataClass.getParent().getLabel());
    }

    @SafeVarargs
    private final void verifyDataElement(Collection<DataElement> elements, String label, String dataTypeLabel, String dataTypeType,
                                         String parentName, Pair<String, String>... metadata) {
        verifyDataElement(elements, label, null, dataTypeLabel, dataTypeType, parentName, metadata);
    }

    @SafeVarargs
    private final void verifyDataElement(Collection<DataElement> elements, String label, String description, String dataTypeLabel,
                                         String dataTypeType,
                                         String parentName, Pair<String, String>... metadata) {
        verifyDataElement(elements, label, description, dataTypeLabel, dataTypeType, 1, 1, parentName, metadata);
    }

    @SafeVarargs
    private final void verifyDataElement(Collection<DataElement> elements, String label, String dataTypeLabel, String dataTypeType,
                                         Integer minOccurs, Integer maxOccurs, String parentName, Pair<String, String>... metadata) {
        verifyDataElement(elements, label, null, dataTypeLabel, dataTypeType, minOccurs, maxOccurs, parentName, metadata);
    }

    @SafeVarargs
    private final void verifyDataElement(Collection<DataElement> elements, String label, String description, String dataTypeLabel,
                                         String dataTypeType,
                                         Integer minOccurs, Integer maxOccurs, String parentName, Pair<String, String>... metadata) {
        DataElement element = findDataElement(elements, label, parentName);
        verifyCatalogueItem(element, label, description, metadata);
        assertEquals(label + " max occurs", maxOccurs, element.getMaxMultiplicity());
        assertEquals(label + " min occurs", minOccurs, element.getMinMultiplicity());
        assertEquals(label + " parent", parentName, element.getDataClass().getLabel());
        assertEquals(label + " datatype", dataTypeLabel, element.getDataType().getLabel());
        assertEquals(label + " datatype type", dataTypeType, element.getDataType().getType());

    }

    private void verifyDataModelValues(DataModel dataModel, XsdImportParameters params, String modelName, String targetNamespace) {
        assertNotNull("Should have the datamodel", dataModel);
        assertEquals("Datamodel author", params.getAuthor(), dataModel.getAuthor());
        assertEquals("Datamodel organisation", params.getOrganisation(), dataModel.getOrganisation());
        assertEquals("Datamodel description", params.getDescription(), dataModel.getDescription());
        assertEquals("Datamodel finalised", params.getFinalised(), dataModel.getFinalised());
        assertEquals("Datamodel name", modelName, dataModel.getLabel());

        Metadata md = dataModel.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE);
        assertNotNull(modelName + " should define a target namespace", md);
        assertEquals("Datamodel targetnamespace", targetNamespace, md.getValue());
    }

    @SafeVarargs
    private final void verifyDataType(Collection<DataType> dataTypes, String label, String type, Pair<String, String>... metadata) {
        verifyDataType(dataTypes, label, null, type, metadata);
    }

    @SafeVarargs
    private final void verifyDataType(Collection<DataType> dataTypes, String label, Pair<String, String>... metadata) {
        verifyDataType(dataTypes, label, null, "PrimitiveType", metadata);
    }

    @SafeVarargs
    private final DataType verifyDataType(Collection<DataType> dataTypes, String label, String description, String type,
                                          Pair<String, String>... metadata) {
        DataType dataType = findDataModelComponentByName(dataTypes, label);
        verifyCatalogueItem(dataType, label, description, metadata);
        assertEquals(label + " type", type, dataType.getType());
        return dataType;
    }

    @SafeVarargs
    private final void verifyEnumerationType(Collection<DataType> dataTypes, String label, String description, Pair<String, String>... enumerations) {
        EnumerationType type = (EnumerationType) verifyDataType(dataTypes, label, description, "EnumerationType",
                                                                Pair.of(METADATA_XSD_RESTRICTION_BASE, "string"));
        assertEquals(label + " number of enumerations", enumerations.length, type.getEnumerationValues().size());

        for (Pair<String, String> enumeration : enumerations) {
            assertTrue(label + " has enumeration " + enumeration.getKey(), type.getEnumerationValues()
                .any {ev -> enumeration.getKey().equals(ev.getKey()) && enumeration.getValue().equals(ev.getValue())
                });
        }
    }

    private void verifyMetadata(CatalogueItem catalogueItem, String namespace, String key, String value) {
        Metadata metadata = catalogueItem.findMetadataByNamespaceAndKey(namespace, key);
        assertNotNull(catalogueItem.getLabel() + " metadata " + key + " should exist", metadata);
        assertEquals(catalogueItem.getLabel() + " metadata " + key, value, metadata.getValue());
    }
}