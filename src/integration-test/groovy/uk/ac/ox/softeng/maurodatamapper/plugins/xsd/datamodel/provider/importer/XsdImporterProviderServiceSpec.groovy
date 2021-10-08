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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.BaseXsdImporterExporterProviderServiceSpec
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.Verification
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.exporter.XsdExporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImporterProviderServiceParameters

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import org.junit.Ignore
import org.junit.Test
import spock.lang.PendingFeature

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_LABEL_PREFIX
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_ALL
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_ATTRIBUTE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_ATTRIBUTE_NAME
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_CHOICE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_DEFAULT
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_EXTENSION_BASE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_FIXED
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_RESTRICTION_BASE
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
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

/**
 * @since 08/09/2017
 */
@Integration
@Rollback
@Slf4j
class XsdImporterProviderServiceSpec extends BaseXsdImporterExporterProviderServiceSpec implements Verification {

    XsdExporterProviderService xsdExporterProviderService
    XsdImporterProviderService xsdImporterProviderService

    void testImportEmpty() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('empty.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'https://metadatacatalogue.com/xsd/test/empty/1.0')

        /*
       DataType checking
        */

        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 45, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())
    }


    void testImportSimpleDataTypesOnly() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('simpleDataTypesOnly.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'https://metadatacatalogue.com/xsd/test/sdto/1.0')

        /*
       DataType checking
        */
        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 45, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())
    }


    void testImportMixedDataTypesOnly() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('mixedDataTypesOnly.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'https://genomicsengland.co.uk/xsd/cancer/3.1.2')

        /*
       DataType checking
        */
then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 45, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        /*
        DataClass checking
         */
        assertTrue('No DataClasses have been created', dataModel.childDataClasses.empty)
    }


    void testImportSimple() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('simple.xsd', 'XSD Test: Simple model')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test: Simple model', 'https://metadatacatalogue.com/xsd/test/simple/1.0')

        /*
        DataType checking
         */

        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 49, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 4, specificTypes.size())

        verifyDataType(specificTypes, 'mandatoryString', 'A mandatory string type', 'PrimitiveType',
                       [(minLength.displayText)        : '1',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])
        verifyDataType(specificTypes, 'complexTypeB', 'This is a list of a new complex type which provides a choice of date or datetime',
                       'ReferenceType')
        verifyEnumerationType(specificTypes, 'enumeratedString', 'An enumeration',
                              ['Y': 'Yes',
                               'N': 'No',
                               'U': 'Unknown'])
        verifyEnumerationType(specificTypes, 'elementCType', null,
                              ['1': 'Possible',
                               '2': 'Not Possible',
                               '3': 'Probable'])

        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses()
        assertEquals('Number of dataclasses', 2, dataClasses.size())

        // complexTypeA is only used by the topElement, which automatically makes a new dataclass to match the top level element
        verifyDataClass(dataClasses, 'topElement', 'The top element complex type', 5, 1, 1, 'XSD Test: Simple model')
        verifyDataClass(dataClasses, 'complexTypeB', 'A choice complex type', 2, 'topElement')

        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 7, elements.size())

        verifyDataElement(elements, 'elementA', 'This is a simple string', 'string', 'PrimitiveType', 'topElement')
        verifyDataElement(elements, 'elementB', 'This is an enumerated string', 'enumeratedString', 'EnumerationType', 'topElement')
        verifyDataElement(elements, 'elementC', 'This is an optional local enumerated string', 'elementCType', 'EnumerationType', 0, 1,
                          'topElement')
        verifyDataElement(elements, 'elementD', 'This is a list of a new complex type which provides a choice of date or datetime',
                          'complexTypeB', 'ReferenceType', 1, -1, 'topElement')
        verifyDataElement(elements, 'elementG', 'This is a string entry where there must be contents in the element', 'mandatoryString',
                          'PrimitiveType', 'topElement')

        verifyDataElement(elements, 'elementE', 'The choice for date', 'date', 'PrimitiveType',
                          'complexTypeB', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementF', 'The choice for datetime', 'dateTime', 'PrimitiveType', 'complexTypeB',
                          [(METADATA_XSD_CHOICE): 'choice'])
    }


    void testImportLocalSimpleTypesInsideElementsWithSameNames() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('localSimpleTypes.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)
        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'https://metadatacatalogue.com/xsd/test/simple/1.0')

        /*
        DataType checking
         */
        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 51, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 6, specificTypes.size())

        verifyDataType(specificTypes, 'elementAType', 'PrimitiveType',
                       [(minLength.displayText)        : '1',
                        (maxLength.displayText)        : '10',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])
        verifyEnumerationType(specificTypes, 'elementBType', null,
                              ['Y': 'Yes',
                               'N': 'No',
                               'U': 'Unknown'])
        verifyDataType(specificTypes, 'elementCType', 'PrimitiveType',
                       [(minLength.displayText)        : '1',
                        (maxLength.displayText)        : '10',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])
        verifyDataType(specificTypes, 'complexTypeB', 'ReferenceType')
        verifyDataType(specificTypes, 'elementAType.1', 'PrimitiveType',
                       [(pattern.displayText)          : '[a-z]+',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])
        verifyDataType(specificTypes, 'elementCType.1', 'PrimitiveType',
                       [(minLength.displayText)        : '1',
                        (maxLength.displayText)        : '10',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])

        /*
       DataClass checking
        */
        Set<DataClass> dataClasses = dataModel.getDataClasses()
        assertEquals('Number of dataclasses', 2, dataClasses.size())

        // complexTypeA is only used by the topElement, which automatically makes a new dataclass to match the top level element
        verifyDataClass(dataClasses, 'topElement', 4, 1, 1, 'XSD Test')
        verifyDataClass(dataClasses, 'complexTypeB', 3, 'topElement')

        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 7, elements.size())

        verifyDataElement(elements, 'elementA', 'elementAType', 'PrimitiveType', 'topElement')
        verifyDataElement(elements, 'elementB', 'elementBType', 'EnumerationType', 'topElement')
        verifyDataElement(elements, 'elementC', 'elementCType', 'PrimitiveType', 'topElement')
        verifyDataElement(elements, 'elementD', 'complexTypeB', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'elementA', 'elementAType.1', 'PrimitiveType', 'complexTypeB')
        verifyDataElement(elements, 'elementB', 'elementBType', 'EnumerationType', 'complexTypeB')
        verifyDataElement(elements, 'elementC', 'elementCType.1', 'PrimitiveType', 'complexTypeB')

    }

    void testImportComplex() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('complex.xsd', 'XSD Test: Complex model')

        when:
        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        then:
        verifyDataModelValues(dataModel, params, 'XSD Test: Complex model', 'https://metadatacatalogue.com/xsd/test/complex/1.0')

        /*
       DataType checking
        */

        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 58, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, 'http://www.w3.org/2001/XMLSchema')
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 13, specificTypes.size())

        verifyDataType(specificTypes, 'guid',
                       [(METADATA_XSD_RESTRICTION_BASE): 'string',
                        (pattern.displayText)          : '([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})|' +
                                                         '(\\{[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\})'])

        verifyDataType(specificTypes, 'numberWithRestrictions',
                       [(METADATA_XSD_RESTRICTION_BASE): 'integer',
                        (totalDigits.displayText)      : '3',
                        (minInclusive.displayText)     : '1',
                        (maxInclusive.displayText)     : '100'])

        verifyDataType(specificTypes, 'mandatoryString', 'A mandatory string type', 'PrimitiveType',
                       [(METADATA_XSD_RESTRICTION_BASE): 'string',
                        (minLength.displayText)        : '1'])

        verifyDataType(specificTypes, 'patternedString',
                       [(METADATA_XSD_RESTRICTION_BASE): 'string',
                        (minLength.displayText)        : '3',
                        (pattern.displayText)          : '\\w+\\.\\w+',
                        (maxLength.displayText)        : '20'])

        verifyDataType(specificTypes, 'stringWithRestrictions',
                       [(METADATA_XSD_RESTRICTION_BASE): 'string',
                        (length.displayText)           : '15',
                        (whiteSpace.displayText)       : 'replace'])

        verifyDataType(specificTypes, 'anotherNumberWithRestrictions',
                       [(METADATA_XSD_RESTRICTION_BASE): 'decimal',
                        (totalDigits.displayText)      : '2',
                        (fractionDigits.displayText)   : '2',
                        (minExclusive.displayText)     : '0',
                        (maxExclusive.displayText)     : '100'])

        verifyEnumerationType(specificTypes, 'enumeratedString', 'An enumeration',
                              ['Y': 'Yes',
                               'N': 'No',
                               'U': 'Unknown'])
        verifyEnumerationType(specificTypes, 'elementCType', null,
                              ['1': 'Possible',
                               '2': 'Not Possible',
                               '3': 'Probable'])

        verifyDataType(specificTypes, 'choiceB', 'This is a list of a new complex type which provides a choice of date or datetime',
                       'ReferenceType')
        verifyDataType(specificTypes, 'elementP', 'Element with a local complex type', 'ReferenceType')
        verifyDataType(specificTypes, 'complexM', 'ReferenceType')
        verifyDataType(specificTypes, 'allL', 'An element with an all complex element', 'ReferenceType')
        verifyDataType(specificTypes, 'complexTypeQ', 'ReferenceType')

        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses()
        // 17 in the XSDs however we only create the ones which are actually used
        assertEquals('Number of dataclasses', 6, dataClasses.size())

        // complexTypeA is only used by the topElement, which automatically makes a new dataclass to match the top level element
        verifyDataClass(dataClasses, 'topElement', 'The top element complex type', 6, 1, 1, 'XSD Test: Complex model')
        verifyDataClass(dataClasses, 'elementP', null, 5, 'topElement')
        verifyDataClass(dataClasses, 'complexTypeQ', null, 7, 'topElement')
        verifyDataClass(dataClasses, 'allL', 'An all complex type', 2, 'topElement')
        verifyDataClass(dataClasses, 'choiceB', 'A choice complex type', 3, 'XSD Test: Complex model')
        verifyDataClass(dataClasses, 'complexM', 'A complex element which extends another', 4, 'topElement',
                        [(METADATA_XSD_EXTENSION_BASE): 'choiceB'])

        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 27, elements.size())

        // topElement
        verifyDataElement(elements, 'elementP', 'Element with a local complex type', 'elementP', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'element_H', 'regex string', 'patternedString', 'PrimitiveType', 'topElement')
        verifyDataElement(elements, 'element-i', 'remaining restrictions applied to a string', 'stringWithRestrictions', 'PrimitiveType',
                          'topElement')
        verifyDataElement(elements, 'ElementK', 'An element with an all complex element', 'allL', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'elementO', 'complexM', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'elementV', 'complexTypeQ', 'ReferenceType', 'topElement')

        // elementP
        verifyDataElement(elements, 'elementA', 'This is a simple string which is fixed to \'Hello\'', 'string', 'PrimitiveType', 'elementP',
                          [(METADATA_XSD_FIXED): 'Hello'])
        verifyDataElement(elements, 'elementB', 'This is an enumerated string', 'enumeratedString', 'EnumerationType', 'elementP',
                          [(METADATA_XSD_DEFAULT): 'Y'])
        verifyDataElement(elements, 'elementC', 'This is an optional local enumerated string', 'elementCType', 'EnumerationType', 0, 1, 'elementP')
        verifyDataElement(elements, 'elementG', 'This is a string entry where there must be contents in the element', 'mandatoryString',
                          'PrimitiveType', 'elementP')
        verifyDataElement(elements, 'elementD', 'This is a list of a new complex type which provides a choice of date or datetime', 'choiceB',
                          'ReferenceType', 1, -1, 'elementP')

        //complexTypeQ
        verifyDataElement(elements, 'elementR', 'choiceB', 'ReferenceType', 'complexTypeQ')
        verifyDataElement(elements, 'elementS', 'mandatoryString', 'PrimitiveType', 0, 1, 'complexTypeQ')
        verifyDataElement(elements, 'elementY', 'choiceB', 'ReferenceType', 0, 1, 'complexTypeQ')
        verifyDataElement(elements, 'elementT', 'decimal', 'PrimitiveType', 'complexTypeQ', [(METADATA_XSD_CHOICE): 'choice-1'])
        verifyDataElement(elements, 'elementU', 'numberWithRestrictions', 'PrimitiveType', 'complexTypeQ', [(METADATA_XSD_CHOICE): 'choice-1'])

        verifyDataElement(elements, 'elementW', 'stringWithRestrictions', 'PrimitiveType', 1, -1, 'complexTypeQ', [(METADATA_XSD_CHOICE): 'choice-2'])
        verifyDataElement(elements, 'elementX', 'patternedString', 'PrimitiveType', 1, -1, 'complexTypeQ', [(METADATA_XSD_CHOICE): 'choice-2'])

        //all
        verifyDataElement(elements, 'element-J-number', 'numberWithRestrictions', 'PrimitiveType', 'allL', [(METADATA_XSD_ALL): 'true'])
        verifyDataElement(elements, 'Element_J_Decimal', 'anotherNumberWithRestrictions', 'PrimitiveType', 'allL', [(METADATA_XSD_ALL): 'true'])

        //choiceB
        verifyDataElement(elements, 'mappingId', 'guid', 'PrimitiveType', 0, 1, 'choiceB', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'elementE', 'The choice for date', 'date', 'PrimitiveType', 'choiceB', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementF', 'The choice for datetime', 'dateTime', 'PrimitiveType', 'choiceB', [(METADATA_XSD_CHOICE): 'choice'])

        //complexM
        verifyDataElement(elements, 'mappingId', 'guid', 'PrimitiveType', 0, 1, 'complexM', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'elementE', 'The choice for date', 'date', 'PrimitiveType', 'complexM', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementF', 'The choice for datetime', 'dateTime', 'PrimitiveType', 'complexM', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementN', 'mandatoryString', 'PrimitiveType', 'complexM')
    }


    void testImportRestrictionAndExtensionComplexTypes() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('restrictionAndExtensionComplexTypes.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'https://metadatacatalogue.com/xsd/test/raect/1.0')

        /*
       DataType checking
        */
        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 55, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, 'http://www.w3.org/2001/XMLSchema')
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 10, specificTypes.size())

        verifyDataType(specificTypes, 'extensionType', 'PrimitiveType',
                       [(minLength.displayText)        : '1',
                        (maxLength.displayText)        : '20',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])

        verifyDataType(specificTypes, 'iiGbEnNhsIdentifierType6', 'PrimitiveType',
                       [(minLength.displayText)        : '1',
                        (maxLength.displayText)        : '20',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])

        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses()
        assertEquals('Number of dataclasses', 8, dataClasses.size())

        verifyDataClass(dataClasses, 'topElement', 5, 1, 1, 'XSD Test')
        verifyDataClass(dataClasses, 'anyAlternative', 2, 'iiAlternative', [(METADATA_LABEL_PREFIX + 'Abstract'): 'true'])
        verifyDataClass(dataClasses, 'iiAlternative', 6, 'topElement', [(METADATA_XSD_EXTENSION_BASE): 'anyAlternative'])
        verifyDataClass(dataClasses, 'iiGbEnNhsIdentifierType6Alternative', 1, 'topElement',
                        [(METADATA_XSD_RESTRICTION_BASE): 'iiAlternative'])
        verifyDataClass(dataClasses, 'any', 2, 'ii', [(METADATA_LABEL_PREFIX + 'Abstract'): 'true'])
        verifyDataClass(dataClasses, 'ii', 6, 'topElement', [(METADATA_XSD_EXTENSION_BASE): 'any'])
        verifyDataClass(dataClasses, 'choiceB', 3, 'extendedChoiceB')
        verifyDataClass(dataClasses, 'extendedChoiceB', 4, 'topElement', [(METADATA_XSD_EXTENSION_BASE): 'choiceB'])

        /*
        DataElement checking
         */
        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 29, elements.size())

        // topElement
        verifyDataElement(elements, 'elementA', 'extendedChoiceB', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'elementB', 'iiGbEnNhsIdentifierType6', 'PrimitiveType', 'topElement',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'elementC', 'iiGbEnNhsIdentifierType6Alternative', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'elementD', 'iiAlternative', 'ReferenceType', 'topElement')
        verifyDataElement(elements, 'elementE', 'ii', 'ReferenceType', 'topElement')

        //choiceB
        verifyDataElement(elements, 'mappingId', 'string', 'PrimitiveType', 0, 1, 'choiceB', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'elementE', 'date', 'PrimitiveType', 'choiceB', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementF', 'dateTime', 'PrimitiveType', 'choiceB', [(METADATA_XSD_CHOICE): 'choice'])

        //extendedChoiceB
        verifyDataElement(elements, 'mappingId', 'string', 'PrimitiveType', 0, 1, 'extendedChoiceB', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'elementE', 'date', 'PrimitiveType', 'extendedChoiceB', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementF', 'dateTime', 'PrimitiveType', 'extendedChoiceB', [(METADATA_XSD_CHOICE): 'choice'])
        verifyDataElement(elements, 'elementN', 'string', 'PrimitiveType', 'extendedChoiceB')

        //ii
        verifyDataElement(elements, 'root', 'uid', 'PrimitiveType', 0, 1, 'ii', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'extension', 'string', 'PrimitiveType', 0, 1, 'ii', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'assigningAuthorityName', 'string', 'PrimitiveType', 0, 1, 'ii', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'displayable', 'bl', 'PrimitiveType', 0, 1, 'ii', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'nullFlavor', 'csNullFlavor', 'EnumerationType', 0, 1, 'ii', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'updateMode', 'csUpdateMode', 'EnumerationType', 0, 1, 'ii', [(METADATA_XSD_ATTRIBUTE): 'true'])

        //any
        verifyDataElement(elements, 'nullFlavor', 'csNullFlavor', 'EnumerationType', 0, 1, 'any', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'updateMode', 'csUpdateMode', 'EnumerationType', 0, 1, 'any', [(METADATA_XSD_ATTRIBUTE): 'true'])

        //iiAlternative
        verifyDataElement(elements, 'root', 'uid', 'PrimitiveType', 0, 1, 'iiAlternative')
        verifyDataElement(elements, 'extension', 'string', 'PrimitiveType', 0, 1, 'iiAlternative')
        verifyDataElement(elements, 'assigningAuthorityName', 'string', 'PrimitiveType', 0, 1, 'iiAlternative')
        verifyDataElement(elements, 'displayable', 'bl', 'PrimitiveType', 0, 1, 'iiAlternative')
        verifyDataElement(elements, 'nullFlavor', 'csNullFlavor', 'EnumerationType', 0, 1, 'iiAlternative')
        verifyDataElement(elements, 'updateMode', 'csUpdateMode', 'EnumerationType', 0, 1, 'iiAlternative')

        //anyAlternative
        verifyDataElement(elements, 'nullFlavor', 'csNullFlavor', 'EnumerationType', 0, 1, 'anyAlternative')
        verifyDataElement(elements, 'updateMode', 'csUpdateMode', 'EnumerationType', 0, 1, 'anyAlternative')

        //iiGbEnNhsIdentifierType6Alternative
        verifyDataElement(elements, 'extension', 'extensionType', 'PrimitiveType', 1, 1, 'iiGbEnNhsIdentifierType6Alternative')
    }


    void testImportGelCancerRac() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('RegistrationAndConsentsCancer-v3.1.2.xsd', 'XSD Test: GEL CAN RAC')

        when:
        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        then:
        verifyDataModelValues(dataModel, params, 'XSD Test: GEL CAN RAC', 'https://genomicsengland.co.uk/xsd/cancer/3.1.2')

        /*
        DataType checking
         */

        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 90, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, 'http://www.w3.org/2001/XMLSchema')
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 45, specificTypes.size())

        /*
       DataClass checking
        */

        Set<DataClass> dataClasses = dataModel.getDataClasses()
        // 17 in the XSDs however we only create the ones which are actually used
        assertEquals('Number of dataclasses', 12, dataClasses.size())

        verifyDataClass(dataClasses, 'registration-and-consents', null, 2, 1, 1, 'XSD Test: GEL CAN RAC')

        verifyDataClass(dataClasses, 'metadata', 6, 'registration-and-consents')
        verifyDataClass(dataClasses, 'subject29260', 3, 'registration-and-consents')

        verifyDataClass(dataClasses, 'eventDetails40374', 2, 'subject29260')

        verifyDataClass(dataClasses, 'participantIdentifiers39047', 5, 'subject29260')
        verifyDataClass(dataClasses, 'personIdentifier42125', 3, 'participantIdentifiers39047')

        verifyDataClass(dataClasses, 'registration12831', 10, 'subject29260')
        verifyDataClass(dataClasses, 'participantContactDetails12528', 9, 'registration12831')
        verifyDataClass(dataClasses, 'diseaseInformationTumourSample33073', 2, 'registration12831')
        verifyDataClass(dataClasses, 'consultantDetails14515', 5, 'registration12831')

        verifyDataClass(dataClasses, 'consent12541', 10, 'subject29260')
        verifyDataClass(dataClasses, 'consentDetails29742', 2, 'consent12541')

        /*
        DataElement checking
         */

        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 59, elements.size())
    }


    void testImportCosdMixedDataTypesOnly() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('cosd/COSD_XMLDataTypes-v8-1.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'http://www.datadictionary.nhs.uk/messages/COSD-v8-1')

        /*
       DataType checking
        */

        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 45, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        /*
        DataClass checking
         */
        assertTrue('No DataClasses exist', dataModel.childDataClasses.empty)
    }

    /**
     * This includes extended and restricted local complex types with only 1 attribute which should be collapsed down to the owning element name.
     */
    void testImportCosdBreastCoreCancerCarePlan() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('cosd/COSDBreastCoreCancerCarePlan_XMLSchema-v8-1.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'http://www.datadictionary.nhs.uk/messages/COSD-v8-1')

        /*
       DataType checking
        */

        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 51, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 6, specificTypes.size())

        specificTypes.findAll {it.label.contains('StringType')}.each {
            Metadata md = it.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_RESTRICTION_BASE)
            assertNotNull('Restriction metadata exists', md)
            assertEquals('Overrides string not st', 'string', md.value)
        }

        assertFalse('Override datatypes do not exist', specificTypes.any {it.label in ['st', 'int', 'bin']})

        verifyDataType(specificTypes, 'stGbEnNhsStringType1', 'This data type supports the representation of dictionary dates.', 'PrimitiveType',
                       [(METADATA_XSD_RESTRICTION_BASE): 'string',
                        (pattern.displayText)          : '(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])'])

        verifyDataType(specificTypes, 'consultantCodeMultidisciplinaryTeamLeadType', 'PrimitiveType',
                       [(length.displayText)           : '8',
                        (METADATA_XSD_RESTRICTION_BASE): 'string'])

        verifyEnumerationType(specificTypes, 'cancerCarePlanIntentType', ['C', 'Z', 'X', '9'], 'cs')
        verifyEnumerationType(specificTypes, 'plannedCancerTreatmentTypeType',
                              ['01', '02', '03', '04', '05', '06', '07', '10', '11', '12', '13', '14', '99'], 'cs')
        verifyEnumerationType(specificTypes, 'noCancerTreatmentReasonType',
                              ['01', '02', '03', '04', '05', '06', '07', '08', '10', '99'], 'cs')
        verifyEnumerationType(specificTypes, 'adultComorbidityEvaluationType', ['0', '1', '2', '3', '9'], 'cs')

        /*
        DataClass checking
         */
        Set<DataClass> dataClasses = dataModel.getDataClasses()
        assertEquals('Number of DataClasses', 1, dataClasses.size())

        verifyDataClass(dataClasses, 'BreastCoreCancerCarePlan', 6, 1, 1, 'XSD Test')

        /*
       DataElement checking
        */
        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 6, elements.size())

        verifyDataElement(elements, 'CancerMultiTeamDiscussionDate', 'MULTIDISCIPLINARY TEAM DISCUSSION DATE (CANCER)',
                          'stGbEnNhsStringType1', 'PrimitiveType', 0, 1, 'BreastCoreCancerCarePlan')
        verifyDataElement(elements, 'ConsultantCodeMultidisciplinaryTeamLead', 'CONSULTANT CODE (MULTIDISCIPLINARY TEAM LEAD)',
                          'consultantCodeMultidisciplinaryTeamLeadType', 'PrimitiveType', 0, 1, 'BreastCoreCancerCarePlan',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'CancerCarePlanIntent', 'CANCER CARE PLAN INTENT',
                          'cancerCarePlanIntentType', 'EnumerationType', 0, 1, 'BreastCoreCancerCarePlan',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'PlannedCancerTreatmentType', 'PLANNED CANCER TREATMENT TYPE',
                          'plannedCancerTreatmentTypeType', 'EnumerationType', 0, -1, 'BreastCoreCancerCarePlan',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'NoCancerTreatmentReason', 'NO CANCER TREATMENT REASON',
                          'noCancerTreatmentReasonType', 'EnumerationType', 0, 1, 'BreastCoreCancerCarePlan',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'AdultComorbidityEvaluation', 'ADULT COMORBIDITY EVALUATION - 27 SCORE',
                          'adultComorbidityEvaluationType', 'EnumerationType', 0, 1, 'BreastCoreCancerCarePlan',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
    }

    /**
     * This includes elements with 0 max occurs, which should act like prohibited attributes
     */
    void testImportCosdBreastCoreDemographics() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('cosd/COSDBreastCoreDemographics_XMLSchema-v8-1.xsd', 'XSD Test')

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'http://www.datadictionary.nhs.uk/messages/COSD-v8-1')

        then:
        Set<DataType> types = dataModel.getDataTypes()
        assertEquals('Number of datatypes', 63, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        assertEquals('Number of specific datatypes', 18, specificTypes.size())

/*        specificTypes.findAll {it.label.contains('StringType')}.each {
            Metadata md = it.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_RESTRICTION_BASE)
            assertNotNull('Restriction metadata exists', md)
            assertEquals('Overrides string not st', 'string', md.value)
        }*/

        assertFalse('Override datatypes do not exist', specificTypes.any {it.label in ['st', 'int', 'bin']})

        /*
       DataClass checking
        */
        Set<DataClass> dataClasses = dataModel.getDataClasses()
        assertEquals('Number of DataClasses', 8, dataClasses.size())

        verifyDataClass(dataClasses, 'BreastCoreDemographics', 10, 1, 1, 'XSD Test')
        verifyDataClass(dataClasses, 'adGbEnNhsAddressType4', 'This data type supports the representation of a structured or unstructured address.',
                        2, 'BreastCoreDemographics', ['XSD Mixed': 'true'])
        verifyDataClass(dataClasses, 'adGbEnNhsAddressType1',
                        'This data type supports the representation of an address represented with a postcode attribute.', 3,
                        'BreastCoreDemographics', [(METADATA_XSD_EXTENSION_BASE): 'any'])
        verifyDataClass(dataClasses, 'pnGbEnNhsPersonNameType4', 'This data type supports the representation of a family name.', 1,
                        'BreastCoreDemographics')
        verifyDataClass(dataClasses, 'pnGbEnNhsPersonNameType5', 'This data type supports the representation of a given name.', 1,
                        'BreastCoreDemographics')
        verifyDataClass(dataClasses, 'structuredAddress', 1, 'adGbEnNhsAddressType4')
        verifyDataClass(dataClasses, 'unstructuredAddress', 1, 'adGbEnNhsAddressType4')

        verifyDataClass(dataClasses, 'any', 'This data type represents an abstract type from which more specific data types are derived.', 2,
                        'adGbEnNhsAddressType1', ['XSD Abstract': 'true'])

        /*
       DataElement checking
        */
        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        assertEquals('Number of elements', 21, elements.size())

        // BreastCoreDemographics
        verifyDataElement(elements, 'PersonFamilyName', 'PERSON FAMILY NAME',
                          'pnGbEnNhsPersonNameType4', 'ReferenceType', 0, 1, 'BreastCoreDemographics')
        verifyDataElement(elements, 'PersonGivenName', 'PERSON GIVEN NAME',
                          'pnGbEnNhsPersonNameType5', 'ReferenceType', 0, 1, 'BreastCoreDemographics')
        verifyDataElement(elements, 'Address', 'PATIENT USUAL ADDRESS (AT DIAGNOSIS)',
                          'adGbEnNhsAddressType4', 'ReferenceType', 0, 1, 'BreastCoreDemographics')
        verifyDataElement(elements, 'Postcode', 'POSTCODE OF USUAL ADDRESS (AT DIAGNOSIS)',
                          'adGbEnNhsAddressType1', 'ReferenceType', 0, 1, 'BreastCoreDemographics')
        verifyDataElement(elements, 'Gender', 'PERSON STATED GENDER CODE',
                          'genderType', 'EnumerationType', 0, 1, 'BreastCoreDemographics', [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'PersonStatedSexualOrientationCodeAtDiagnosis', 'PERSON STATED SEXUAL ORIENTATION CODE (AT DIAGNOSIS)',
                          'personStatedSexualOrientationCodeAtDiagnosisType', 'EnumerationType', 0, 1, 'BreastCoreDemographics',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'GMPSpecified', 'GENERAL MEDICAL PRACTITIONER (SPECIFIED)',
                          'gmpSpecifiedType', 'PrimitiveType', 0, 1, 'BreastCoreDemographics', [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'GMPRegistration', 'GENERAL MEDICAL PRACTICE CODE (PATIENT REGISTRATION)',
                          'gmpRegistrationType', 'PrimitiveType', 0, 1, 'BreastCoreDemographics', [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'PersonBirthFamilyName', 'PERSON FAMILY NAME (AT BIRTH)',
                          'pnGbEnNhsPersonNameType4', 'ReferenceType', 0, 1, 'BreastCoreDemographics')
        verifyDataElement(elements, 'Ethnicity', 'ETHNIC CATEGORY',
                          'ethnicityType', 'EnumerationType', 0, 1, 'BreastCoreDemographics', [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])

        // pNGbEnNhsPersonNameType
        verifyDataElement(elements, 'family', 'familyType', 'PrimitiveType', 'pnGbEnNhsPersonNameType4')
        verifyDataElement(elements, 'given', 'givenType', 'PrimitiveType', 'pnGbEnNhsPersonNameType5')

        // adGbEnNhsAddressType1
        verifyDataElement(elements, 'UnstructuredAddress', 'unstructuredAddress', 'ReferenceType', 'adGbEnNhsAddressType4',
                          [(METADATA_XSD_CHOICE): 'choice-0'])
        verifyDataElement(elements, 'StructuredAddress', 'structuredAddress', 'ReferenceType', 'adGbEnNhsAddressType4',
                          [(METADATA_XSD_CHOICE): 'choice-0'])
        verifyDataElement(elements, 'streetAddressLine', 'streetAddressLineType', 'PrimitiveType', 'unstructuredAddress')
        verifyDataElement(elements, 'streetAddressLine', 'streetAddressLineType.1', 'PrimitiveType', 5, 5, 'structuredAddress')

        // adGbEnNhsAddressType4
        verifyDataElement(elements, 'postalCode', 'postalcode', 'PrimitiveType', 'adGbEnNhsAddressType1')
        verifyDataElement(elements, 'nullFlavor', 'csNullFlavor', 'EnumerationType', 0, 1, 'adGbEnNhsAddressType1',
                          [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'updateMode', 'csUpdateMode', 'EnumerationType', 0, 1, 'adGbEnNhsAddressType1',
                          [(METADATA_XSD_ATTRIBUTE): 'true'])

        verifyDataElement(elements, 'nullFlavor', 'csNullFlavor', 'EnumerationType', 0, 1, 'any', [(METADATA_XSD_ATTRIBUTE): 'true'])
        verifyDataElement(elements, 'updateMode', 'csUpdateMode', 'EnumerationType', 0, 1, 'any', [(METADATA_XSD_ATTRIBUTE): 'true'])
    }

    /**
     * This includes elements which are so named that they could produce a cyclical datatype reference when complex types are renamed
     */

    void testImportCosdWithCoreOnly() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('cosd/COSDCOSD_XMLSchema-v8-1.xsd', 'XSD Test')
        params.rootElement = 'COSD'

        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        when:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'http://www.datadictionary.nhs.uk/messages/COSD-v8-1')

        then:
        Set<DataType> types = dataModel.getDataTypes()
        log.info('{} datatypes', types.size())
        // assertEquals('Number of datatypes', 58, types.size())

        Set<DataType> wsTypes = findDataTypesWithNamespace(types, XS_NAMESPACE)
        assertEquals('Number of xs datatypes', 45, wsTypes.size())

        Set<DataType> specificTypes = findDataTypesWithNamespace(types, null)
        //assertEquals('Number of specific datatypes', 14, specificTypes.size())

        specificTypes.findAll {it.label.contains('StringType')}.each {
            Metadata md = it.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_RESTRICTION_BASE)
            assertNotNull('Restriction metadata exists', md)
            assertEquals('Overrides string not st', 'string', md.value)
        }

        assertFalse('Override datatypes do not exist', specificTypes.any {it.label in ['st', 'int', 'bin']})

        /*
      DataClass checking
       */
        Set<DataClass> dataClasses = dataModel.getDataClasses()
        log.info('{} dataclasses', dataClasses.size())
        assertEquals('Number of DataClasses', 6, dataClasses.size())

        verifyDataClass(dataClasses, 'COSD', 7, 1, 1, 'XSD Test')
        verifyDataClass(dataClasses, 'cosdRecordType', 2, 'COSD')
        verifyDataClass(dataClasses, 'coreType', 1, 'cosdRecordType')
        verifyDataClass(dataClasses, 'coreType.1', 2, 'coreType')
        verifyDataClass(dataClasses, 'coreLinkagePatientIdType', 5, 'coreType.1')
        verifyDataClass(dataClasses, 'coreLinkageDiagnosticDetailsType', 4, 'coreType.1')

        /*
     DataElement checking
      */
        List<DataElement> elements = dataClasses.collectMany {it.dataElements}
        log.info('{} dataelements', elements.size())
        assertEquals('Number of elements', 21, elements.size())

        //COSD
        verifyDataElement(elements, 'Id', 'COSDS SUBMISSION IDENTIFIER', 'idType', 'PrimitiveType', 'COSD',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'OrganisationIdentifierCodeOfSubmittingOrganisation',
                          'ORGANISATION IDENTIFIER (CODE OF SUBMITTING ORGANISATION)',
                          'organisationIdentifierCodeOfSubmittingOrganisationType', 'PrimitiveType', 'COSD',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'RecordCount', 'COSDS SUBMISSION RECORD COUNT', 'recordCountType', 'PrimitiveType', 'COSD',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'value'])
        verifyDataElement(elements, 'ReportingPeriodStartDate', 'REPORTING PERIOD START DATE', 'stGbEnNhsStringType1', 'PrimitiveType', 'COSD')
        verifyDataElement(elements, 'ReportingPeriodEndDate', 'REPORTING PERIOD END DATE', 'stGbEnNhsStringType1', 'PrimitiveType', 'COSD')
        verifyDataElement(elements, 'FileCreationDateTime', 'DATE AND TIME DATA SET CREATED', 'stGbEnNhsStringType4', 'PrimitiveType', 'COSD')
        verifyDataElement(elements, 'COSDRecord', 'cosdRecordType', 'ReferenceType', 1, -1, 'COSD')

        // cosdRecordType
        verifyDataElement(elements, 'Id', 'COSDS UNIQUE IDENTIFIER', 'idType.1', 'PrimitiveType', 'cosdRecordType',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'Core', 'coreType', 'ReferenceType', 'cosdRecordType', [(METADATA_XSD_CHOICE): 'choice-0'])

        // coreType
        verifyDataElement(elements, 'CoreCore', 'coreType1', 'ReferenceType', 0, 1, 'coreType')

        // coreType.1
        verifyDataElement(elements, 'CoreLinkagePatientId', 'coreLinkagePatientIdType', 'ReferenceType', 'coreType.1')
        verifyDataElement(elements, 'CoreLinkageDiagnosticDetails', 'coreLinkageDiagnosticDetailsType', 'ReferenceType', 'coreType.1')

        // coreLinkagePatientIdType
        verifyDataElement(elements, 'NHSNumber', 'NHS NUMBER', 'nhsNumberType', 'PrimitiveType', 0, 1, 'coreLinkagePatientIdType',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])
        verifyDataElement(elements, 'LocalPatientIdExtended', 'LOCAL PATIENT IDENTIFIER (EXTENDED)', 'localPatientIdExtendedType', 'PrimitiveType',
                          0, 1, 'coreLinkagePatientIdType')
        verifyDataElement(elements, 'NHSNumberStatusIndicator', 'NHS NUMBER STATUS INDICATOR CODE', 'nhsNumberStatusIndicatorType', 'EnumerationType',
                          'coreLinkagePatientIdType', [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'Birthdate', 'PERSON BIRTH DATE', 'stGbEnNhsStringType1', 'PrimitiveType', 0, 1, 'coreLinkagePatientIdType')
        verifyDataElement(elements, 'OrganisationIdentifierCodeOfProvider', 'ORGANISATION IDENTIFIER (CODE OF PROVIDER)',
                          'organisationIdentifierCodeOfProviderType', 'PrimitiveType', 'coreLinkagePatientIdType',
                          [(METADATA_XSD_ATTRIBUTE_NAME): 'extension'])


        // coreLinkageDiagnosticDetailsType
        verifyDataElement(elements, 'PrimaryDiagnosis', 'PRIMARY DIAGNOSIS (ICD)', 'primaryDiagnosisType', 'PrimitiveType',
                          'coreLinkageDiagnosticDetailsType', [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'TumourLaterality', 'TUMOUR LATERALITY', 'tumourLateralityType', 'EnumerationType',
                          'coreLinkageDiagnosticDetailsType', [(METADATA_XSD_ATTRIBUTE_NAME): 'code'])
        verifyDataElement(elements, 'ClinicalDateCancerDiagnosis', 'DATE OF PRIMARY CANCER DIAGNOSIS (CLINICALLY AGREED)',
                          'stGbEnNhsStringType1', 'PrimitiveType', 0, 1, 'coreLinkageDiagnosticDetailsType')
        verifyDataElement(elements, 'DateOfNonPrimaryCancerDiagnosisClinicallyAgreed', 'DATE OF NON PRIMARY CANCER DIAGNOSIS (CLINICALLY AGREED)',
                          'stGbEnNhsStringType1', 'PrimitiveType', 0, 1, 'coreLinkageDiagnosticDetailsType')
    }

    void testImportCosd() {
        given:
        setupData()
        XsdImporterProviderServiceParameters params = createImportParameters('cosd/COSDCOSD_XMLSchema-v8-1.xsd',
                                                                             'XSD Test')
        params.rootElement = 'COSD'

        when:
        DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

        then:
        verifyDataModelValues(dataModel, params, 'XSD Test', 'http://www.datadictionary.nhs.uk/messages/COSD-v8-1')
    }

    //Comment un comment to test zip imports, please be aware test may take a long time depending on file size
    /*    void testPerformance()
        {
            given:
            setupData()
            XsdImporterProviderServiceParameters params = createImportParametersForZip('COSDCOSD_XMLSchema-v6-0.xsd','C:\\mauro\\main_v6-0.zip' , 'XSD Test: Complex model')

            when:
            DataModel dataModel = importDataModelAndRetrieveFromDatabase(params)

            then:
            dataModel != null
        }*/
}