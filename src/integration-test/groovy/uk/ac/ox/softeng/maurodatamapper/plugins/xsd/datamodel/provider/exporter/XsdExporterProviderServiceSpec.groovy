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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.exporter

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ReferenceType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.BaseXsdImportorExporterProviderServiceSpec

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.*

@Integration
@Rollback
@Slf4j
class XsdExporterProviderServiceSpec extends BaseXsdImportorExporterProviderServiceSpec {

    DataModelService dataModelService
    XsdExporterProviderService xsdExporterProviderService

    @Override
    XsdExporterProviderService getXsdExporterProviderService() {
        xsdExporterProviderService
    }


    void "test export simple"() {
        given:
        setupData()

        //Create Simple Data Model : TODO Replace with import once importer is completed
        Metadata targetNamespace = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_TARGET_NAMESPACE, value: ' https://metadatacatalogue.com/xsd/test/simple/1.0', multiFacetAwareItemId: UUID.randomUUID())
        Metadata namespace = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_NAMESPACE, value: ' https://metadatacatalogue.com/xsd/test/simple/1.0', multiFacetAwareItemId: UUID.randomUUID())
        dataModel.addToMetadata(targetNamespace)
        dataModel.addToMetadata(namespace)

        checkAndSave(dataModel)

        EnumerationType et = new EnumerationType(label: "element C", createdByUser: reader1)
        et.addToEnumerationValues(key: '1', value: 'Possible', index: 0)
        et.addToEnumerationValues(key: '2', value: 'Not Possible', index: 1)
        et.addToEnumerationValues(key: '3', value: 'Probable', index: 2)
        dataModel.addToDataTypes(et)
        checkAndSave(et)

        EnumerationType enumeratedStringType = new EnumerationType(description: "An enumeration", label: "enumerated String", id: UUID.randomUUID(), createdByUser: reader1)
        enumeratedStringType.addToEnumerationValues(key: 'Y', value: 'Yes')
        enumeratedStringType.addToEnumerationValues(key: 'N', value: 'No')
        enumeratedStringType.addToEnumerationValues(key: 'U', value: 'Unknown')
        dataModel.addToDataTypes(enumeratedStringType)
        checkAndSave(enumeratedStringType)

        PrimitiveType mandatoryStringType = new PrimitiveType(label: "mandatory String", description: "A mandatory string type", createdByUser: reader1)
        dataModel.addToDataTypes(mandatoryStringType)
        checkAndSave(mandatoryStringType)


        Metadata restrictionBaseString = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_RESTRICTION_BASE, value: 'string', multiFacetAwareItemId: UUID.randomUUID())
        et.addToMetadata(restrictionBaseString)
        enumeratedStringType.addToMetadata(restrictionBaseString)
        mandatoryStringType.addToMetadata(restrictionBaseString)

        Metadata restrictionPrefixMinLength1 = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "MIN LENGTH", value: '1', multiFacetAwareItemId: UUID.randomUUID())
        mandatoryStringType.addToMetadata(restrictionPrefixMinLength1)

        DataClass topElementClass = new DataClass(createdByUser: reader1, label: 'top Element', description: 'The top element complex type', minMultiplicity: 1, maxMultiplicity: 1)
        dataModel.addToDataClasses(topElementClass)
        checkAndSave(topElementClass)

        DataClass subDataClass = new DataClass(createdByUser: reader1, label: 'complex Type B', description: 'A choice complex type', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataClasses(subDataClass)
        checkAndSave(subDataClass)

        Metadata choice = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_CHOICE, value: 'xs:choice')

        ReferenceType complexTypeB = new ReferenceType(referenceClass: subDataClass, description: "A choice complex type", label: "complex Type B Type", createdByUser: reader1)
        dataModel.addToDataTypes(complexTypeB)
        checkAndSave(complexTypeB)

        PrimitiveType dateTimeType = new PrimitiveType(createdByUser: reader1, label: 'dateTime')
        dataModel.addToDataTypes(dateTimeType)
        checkAndSave(dateTimeType)

        PrimitiveType dateType = new PrimitiveType(createdByUser: reader1, label: 'date')
        dataModel.addToDataTypes(dateType)
        checkAndSave(dateType)

        DataElement dataElementE = new DataElement(createdByUser: reader1, dataType: dateType, label: 'element E', maxMultiplicity: 1, minMultiplicity: 1, description: 'The choice for date')
        subDataClass.addToDataElements(dataElementE)
        dataElementE.setId(UUID.randomUUID())
        dataElementE.addToMetadata(choice)
        checkAndSave(dataElementE)

        PrimitiveType stringType = new PrimitiveType(createdByUser: reader1, label: 'string')
        dataModel.addToDataTypes(stringType)
        checkAndSave(stringType)


        DataElement dataElementA = new DataElement(createdByUser: reader1, dataType: stringType, label: 'element A', maxMultiplicity: 1, minMultiplicity: 1, description: 'This is a simple string')
        topElementClass.addToDataElements(dataElementA)
        checkAndSave(dataElementA)

        DataElement enumeratedString = new DataElement(createdByUser: reader1, dataType: enumeratedStringType, label: 'element B', maxMultiplicity: 1, minMultiplicity: 1, description: 'This is an enumerated string')
        topElementClass.addToDataElements(enumeratedString)
        checkAndSave(enumeratedString)

        DataElement dataElementC = new DataElement(createdByUser: reader1, dataType: et, label: 'element C', maxMultiplicity: 1, minMultiplicity: 0, description: 'This is an optional local enumerated string')
        topElementClass.addToDataElements(dataElementC)
        checkAndSave(dataElementC)

        DataElement dataElementG = new DataElement(createdByUser: reader1, dataType: mandatoryStringType, label: 'element G', maxMultiplicity: 1, minMultiplicity: 1, description: 'This is a string entry where there must be contents in the element')
        topElementClass.addToDataElements(dataElementG)
        dataElementG.setId(UUID.randomUUID())
        dataElementG.addToMetadata(restrictionBaseString)
        checkAndSave(dataElementG)

        DataElement dataElementF = new DataElement(createdByUser: reader1, dataType: dateTimeType, label: 'element F', maxMultiplicity: 1, minMultiplicity: 1, description: 'The choice for datetime')
        subDataClass.addToDataElements(dataElementF)
        dataElementF.setId(UUID.randomUUID())
        dataElementF.addToMetadata(choice)
        checkAndSave(dataElementF)

        DataElement dataElementD = new DataElement(createdByUser: reader1, dataType: complexTypeB, label: 'element D', minMultiplicity: 1, maxMultiplicity: -1, description: 'This is a list of a new complex type which provides a choice of date or datetime')
        topElementClass.addToDataElements(dataElementD)
        checkAndSave(dataElementD)

        checkAndSave(dataModel)

        when:
        ByteArrayOutputStream byteArrayOutputStream = getXsdExporterProviderService().exportDomain(reader1, dataModel.getId())

        String exportedXsd = byteArrayOutputStream.toString('ISO-8859-1')

        Path p = Paths.get('build/tmp/', 'simple_test.xsd')
        Files.write(p, exportedXsd.getBytes('ISO-8859-1'))

        Path expPath = Paths.get('src/integration-test/resources/expected/simple.xsd')
        String expected = new String(Files.readAllBytes(expPath), 'ISO-8859-1')

        then:
        completeCompareXml(fudgeDates(expected), fudgeDates(exportedXsd))

    }

    void "test export complex"() {
        given:
        setupData()

        //Create Complex Data Model : TODO Replace with import once importer is completed
        Metadata targetNamespace = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_TARGET_NAMESPACE, value: 'https://metadatacatalogue.com/xsd/test/complex/1.0', multiFacetAwareItemId: UUID.randomUUID())
        Metadata namespace = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_NAMESPACE, value: 'https://metadatacatalogue.com/xsd/test/complex/1.0', multiFacetAwareItemId: UUID.randomUUID())
        dataModel.setLabel("XSD Test: Complex model")
        dataModel.addToMetadata(targetNamespace)
        dataModel.addToMetadata(namespace)

        checkAndSave(dataModel)

        EnumerationType elementCType = new EnumerationType(label: "element C", createdByUser: reader1)
        elementCType.addToEnumerationValues(key: '1', value: 'Possible')
        elementCType.addToEnumerationValues(key: '2', value: 'Not Possible')
        elementCType.addToEnumerationValues(key: '3', value: 'Probable')
        dataModel.addToDataTypes(elementCType)
        checkAndSave(elementCType)

        EnumerationType enumeratedStringType = new EnumerationType(description: "An enumeration", label: "enumerated String", id: UUID.randomUUID(), createdByUser: reader1)
        enumeratedStringType.addToEnumerationValues(key: 'Y', value: 'Yes')
        enumeratedStringType.addToEnumerationValues(key: 'N', value: 'No')
        enumeratedStringType.addToEnumerationValues(key: 'U', value: 'Unknown')
        dataModel.addToDataTypes(enumeratedStringType)
        checkAndSave(enumeratedStringType)

        PrimitiveType mandatoryStringType = new PrimitiveType(label: "mandatory String", description: "A mandatory string type", createdByUser: reader1)
        dataModel.addToDataTypes(mandatoryStringType)
        checkAndSave(mandatoryStringType)

        Metadata restrictionBaseString = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_RESTRICTION_BASE, value: 'string', multiFacetAwareItemId: UUID.randomUUID())
        elementCType.addToMetadata(restrictionBaseString)
        enumeratedStringType.addToMetadata(restrictionBaseString)
        mandatoryStringType.addToMetadata(restrictionBaseString)

        Metadata restrictionPrefixMinLength1 = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "MIN LENGTH", value: '1', multiFacetAwareItemId: UUID.randomUUID())
        mandatoryStringType.addToMetadata(restrictionPrefixMinLength1)

        DataClass topElementClass = new DataClass(createdByUser: reader1, label: 'top Element', description: 'The top element complex type', minMultiplicity: 1, maxMultiplicity: 1)
        dataModel.addToDataClasses(topElementClass)
        checkAndSave(topElementClass)

        DataClass subDataClass = new DataClass(createdByUser: reader1, label: 'choice B', description: 'A choice complex type', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataClasses(subDataClass)
        checkAndSave(subDataClass)

        DataClass allClass = new DataClass(createdByUser: reader1, label: 'all', description: 'An all complex type', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataClasses(allClass)
        checkAndSave(allClass)

        DataClass complexM = new DataClass(createdByUser: reader1, label: 'complex M', description: 'A complex element which extends another', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataClasses(complexM)
        checkAndSave(complexM)

        DataClass elementP = new DataClass(createdByUser: reader1, label: 'element P', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataClasses(elementP)
        checkAndSave(elementP)


        Metadata choice = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_CHOICE, value: '1')
        Metadata choice2 = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_CHOICE, value: '2')

        ReferenceType complexTypeB = new ReferenceType(referenceClass: subDataClass, description: "A choice complex type", label: "choice B", createdByUser: reader1)
        dataModel.addToDataTypes(complexTypeB)
        checkAndSave(complexTypeB)

        ReferenceType allType = new ReferenceType(referenceClass: allClass, description: "An all complex type", label: "All", createdByUser: reader1)
        dataModel.addToDataTypes(allType)
        checkAndSave(allType)

        ReferenceType complexMType = new ReferenceType(referenceClass: complexM, description: "A complex element which extends another", label: "complex m", createdByUser: reader1)
        dataModel.addToDataTypes(complexMType)
        checkAndSave(complexMType)

        ReferenceType elementPType = new ReferenceType(referenceClass: elementP, label: "element p", createdByUser: reader1)
        dataModel.addToDataTypes(elementPType)
        checkAndSave(elementPType)


        PrimitiveType dateTimeType = new PrimitiveType(createdByUser: reader1, label: 'dateTime')
        dataModel.addToDataTypes(dateTimeType)
        checkAndSave(dateTimeType)

        PrimitiveType dateType = new PrimitiveType(createdByUser: reader1, label: 'date')
        dataModel.addToDataTypes(dateType)
        checkAndSave(dateType)

        PrimitiveType decimalType = new PrimitiveType(createdByUser: reader1, label: 'decimal')
        dataModel.addToDataTypes(decimalType)
        checkAndSave(decimalType)

        Metadata intRestrict = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_RESTRICTION_BASE, value: 'integer', multiFacetAwareItemId: UUID.randomUUID())
        Metadata minInclusive = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "min Inclusive", value: '1', multiFacetAwareItemId: UUID.randomUUID())
        Metadata maxExclusive = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "max exclusive", value: '100', multiFacetAwareItemId: UUID.randomUUID())
        Metadata totalDigits = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "total Digits", value: '3', multiFacetAwareItemId: UUID.randomUUID())

        PrimitiveType numberWithRestrictionsType = new PrimitiveType(createdByUser: reader1, label: 'number With Restrictions')
        dataModel.addToDataTypes(numberWithRestrictionsType)
        checkAndSave(numberWithRestrictionsType)

        numberWithRestrictionsType.addToMetadata(intRestrict)
        numberWithRestrictionsType.addToMetadata(minInclusive)
        numberWithRestrictionsType.addToMetadata(maxExclusive)
        numberWithRestrictionsType.addToMetadata(totalDigits)

        Metadata anotherNumberRestrict = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_RESTRICTION_BASE, value: 'decimal', multiFacetAwareItemId: UUID.randomUUID())
        Metadata anotherNumberminInclusive = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "min Inclusive", value: '0', multiFacetAwareItemId: UUID.randomUUID())
        Metadata anotherNumbermaxExclusive = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "max exclusive", value: '100', multiFacetAwareItemId: UUID.randomUUID())
        Metadata anotherNumbertotalDigits = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "total Digits", value: '2', multiFacetAwareItemId: UUID.randomUUID())
        Metadata anotherNumberfractionDigits = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "Number of Fraction Digits", value: '2', multiFacetAwareItemId: UUID.randomUUID())


        PrimitiveType anotherNumberWithRestrictionsType = new PrimitiveType(createdByUser: reader1, label: 'another number With Restrictions')
        dataModel.addToDataTypes(anotherNumberWithRestrictionsType)
        checkAndSave(anotherNumberWithRestrictionsType)

        anotherNumberWithRestrictionsType.addToMetadata(anotherNumberRestrict)
        anotherNumberWithRestrictionsType.addToMetadata(anotherNumbertotalDigits)
        anotherNumberWithRestrictionsType.addToMetadata(anotherNumberfractionDigits)
        anotherNumberWithRestrictionsType.addToMetadata(anotherNumberminInclusive)
        anotherNumberWithRestrictionsType.addToMetadata(anotherNumbermaxExclusive)


        Metadata stringRestriction = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_RESTRICTION_BASE, value: 'string', multiFacetAwareItemId: UUID.randomUUID())
        Metadata lengthMeta = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "length", value: '15', multiFacetAwareItemId: UUID.randomUUID())
        Metadata whiteSpace = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "Whitespace Handling", value: 'replace', multiFacetAwareItemId: UUID.randomUUID())

        PrimitiveType stringWithRestrictionsType = new PrimitiveType(createdByUser: reader1, label: 'string With Restrictions')
        dataModel.addToDataTypes(stringWithRestrictionsType)
        checkAndSave(stringWithRestrictionsType)

        stringWithRestrictionsType.addToMetadata(stringRestriction)
        stringWithRestrictionsType.addToMetadata(lengthMeta)
        stringWithRestrictionsType.addToMetadata(whiteSpace)

        Metadata patternedStringTypeMinLength = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "min Length", value: '3', multiFacetAwareItemId: UUID.randomUUID())
        Metadata patternedStringTypeMaxLength = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "max Length", value: '20', multiFacetAwareItemId: UUID.randomUUID())
        Metadata patternedStringTypePattern = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "Pattern/Regex", value: "\\w+\\.\\w+", multiFacetAwareItemId: UUID.randomUUID())

        PrimitiveType patternedStringType = new PrimitiveType(createdByUser: reader1, label: 'patterned String')
        dataModel.addToDataTypes(patternedStringType)
        checkAndSave(patternedStringType)

        patternedStringType.addToMetadata(stringRestriction)
        patternedStringType.addToMetadata(patternedStringTypePattern)
        patternedStringType.addToMetadata(patternedStringTypeMinLength)
        patternedStringType.addToMetadata(patternedStringTypeMaxLength)

        Metadata gUIDPattern = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_LABEL_RESTRICTION_PREFIX + "Pattern/Regex", value: '([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})|(\\{[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\})', multiFacetAwareItemId: UUID.randomUUID())

        PrimitiveType gUIDType = new PrimitiveType(createdByUser: reader1, label: 'gUID')
        dataModel.addToDataTypes(gUIDType)
        checkAndSave(gUIDType)

        gUIDType.addToMetadata(stringRestriction)
        gUIDType.addToMetadata(gUIDPattern)

        DataElement dataElementN = new DataElement(createdByUser: reader1, dataType: mandatoryStringType, label: 'element N', maxMultiplicity: 1, minMultiplicity: 1)
        complexM.addToDataElements(dataElementN)
        checkAndSave(dataElementN)

        DataClass complexTypeQTypeClass = new DataClass(createdByUser: reader1, label: 'complex Type Q', minMultiplicity: 1, maxMultiplicity: 1)
        complexTypeQTypeClass.setId(UUID.randomUUID())
        topElementClass.addToDataClasses(complexTypeQTypeClass)
        checkAndSave(complexTypeQTypeClass)

        ReferenceType complexTypeQ = new ReferenceType(referenceClass: complexTypeQTypeClass, label: "complex Type Q Type", createdByUser: reader1)
        dataModel.addToDataTypes(complexTypeQ)
        checkAndSave(complexTypeQ)

        DataElement mappingId = new DataElement(createdByUser: reader1, dataType: gUIDType, label: 'mapping Id', minMultiplicity: 0, maxMultiplicity: 1)
        subDataClass.addToDataElements(mappingId)
        checkAndSave(mappingId)

        DataElement mappingId1 = new DataElement(createdByUser: reader1, dataType: gUIDType, label: 'mapping Id', minMultiplicity: 0, maxMultiplicity: 1)
        complexM.addToDataElements(mappingId1)
        checkAndSave(mappingId1)

        DataElement dataElementE = new DataElement(createdByUser: reader1, dataType: dateType, label: 'element E', maxMultiplicity: 1, minMultiplicity: 1, description: 'The choice for date')
        subDataClass.addToDataElements(dataElementE)
        checkAndSave(dataElementE)
        dataElementE.addToMetadata(choice)

        DataElement dataElementE1 = new DataElement(createdByUser: reader1, dataType: dateType, label: 'element E', maxMultiplicity: 1, minMultiplicity: 1, description: 'The choice for date')
        complexM.addToDataElements(dataElementE1)
        checkAndSave(dataElementE1)
        dataElementE1.addToMetadata(choice)

        DataElement dataElementR = new DataElement(createdByUser: reader1, dataType: complexTypeB, label: 'element R', maxMultiplicity: 1, minMultiplicity: 1)
        complexTypeQTypeClass.addToDataElements(dataElementR)
        checkAndSave(dataElementR)

        DataElement dataElementS = new DataElement(createdByUser: reader1, dataType: mandatoryStringType, label: 'element S', maxMultiplicity: 1, minMultiplicity: 0)
        complexTypeQTypeClass.addToDataElements(dataElementS)
        checkAndSave(dataElementS)

        DataElement dataElementY = new DataElement(createdByUser: reader1, dataType: complexTypeB, label: 'element Y', maxMultiplicity: 1, minMultiplicity: 0)
        complexTypeQTypeClass.addToDataElements(dataElementY)
        checkAndSave(dataElementY)

        DataElement dataElementT = new DataElement(createdByUser: reader1, dataType: decimalType, label: 'element T', maxMultiplicity: 1, minMultiplicity: 1)
        dataElementT.setId(UUID.randomUUID())
        complexTypeQTypeClass.addToDataElements(dataElementT)
        dataElementT.addToMetadata(choice)
        checkAndSave(dataElementT)

        DataElement dataElementU = new DataElement(createdByUser: reader1, dataType: numberWithRestrictionsType, label: 'element U', maxMultiplicity: 1, minMultiplicity: 1)
        dataElementU.setId(UUID.randomUUID())
        complexTypeQTypeClass.addToDataElements(dataElementU)
        dataElementU.addToMetadata(choice)
        checkAndSave(dataElementU)

        DataElement dataElementW = new DataElement(createdByUser: reader1, dataType: stringWithRestrictionsType, label: 'element W', maxMultiplicity: -1, minMultiplicity: 1)
        dataElementW.setId(UUID.randomUUID())
        complexTypeQTypeClass.addToDataElements(dataElementW)
        dataElementW.addToMetadata(choice2)
        checkAndSave(dataElementW)

        DataElement dataElementX = new DataElement(createdByUser: reader1, dataType: patternedStringType, label: 'element X', maxMultiplicity: -1, minMultiplicity: 1)
        dataElementX.setId(UUID.randomUUID())
        complexTypeQTypeClass.addToDataElements(dataElementX)
        dataElementX.addToMetadata(choice2)
        checkAndSave(dataElementX)

        PrimitiveType stringType = new PrimitiveType(createdByUser: reader1, label: 'string')
        dataModel.addToDataTypes(stringType)
        checkAndSave(stringType)

        Metadata simpleStringDefault = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_DEFAULT, value: 'Hello', multiFacetAwareItemId: UUID.randomUUID())

        DataElement dataElementA = new DataElement(createdByUser: reader1, dataType: stringType, label: 'element A', maxMultiplicity: 1, minMultiplicity: 1, description: 'This is a simple string which is fixed to \'Hello\'')

        elementP.addToDataElements(dataElementA)

        checkAndSave(dataElementA)
        dataElementA.addToMetadata(simpleStringDefault)

        Metadata enumeratedStringDefault = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_DEFAULT, value: 'Y', multiFacetAwareItemId: UUID.randomUUID())

        DataElement enumeratedString = new DataElement(createdByUser: reader1, dataType: enumeratedStringType, label: 'element B', maxMultiplicity: 1, minMultiplicity: 1, description: 'This is an enumerated string')
        elementP.addToDataElements(enumeratedString)
        checkAndSave(enumeratedString)

        enumeratedString.addToMetadata(enumeratedStringDefault)

        DataElement dataElementC = new DataElement(createdByUser: reader1, dataType: elementCType, label: 'element C', maxMultiplicity: 1, minMultiplicity: 0, description: 'This is an optional local enumerated string')
        elementP.addToDataElements(dataElementC)
        checkAndSave(dataElementC)

        DataElement dataElementG = new DataElement(createdByUser: reader1, dataType: mandatoryStringType, label: 'element G', maxMultiplicity: 1, minMultiplicity: 1, description: 'This is a string entry where there must be contents in the element')
        elementP.addToDataElements(dataElementG)
        dataElementG.setId(UUID.randomUUID())
        dataElementG.addToMetadata(restrictionBaseString)
        checkAndSave(dataElementG)

        DataElement dataElementF = new DataElement(createdByUser: reader1, dataType: dateTimeType, label: 'element F', maxMultiplicity: 1, minMultiplicity: 1, description: 'The choice for datetime')
        subDataClass.addToDataElements(dataElementF)
        checkAndSave(dataElementF)
        dataElementF.addToMetadata(choice)

        DataElement dataElementF1 = new DataElement(createdByUser: reader1, dataType: dateTimeType, label: 'element F', maxMultiplicity: 1, minMultiplicity: 1, description: 'The choice for datetime')
        complexM.addToDataElements(dataElementF1)
        checkAndSave(dataElementF1)
        dataElementF1.addToMetadata(choice)

        DataElement dataElementD = new DataElement(createdByUser: reader1, dataType: complexTypeB, label: 'element D', minMultiplicity: 1, maxMultiplicity: -1, description: 'This is a list of a new complex type which provides a choice of date or datetime')
        topElementClass.addToDataElements(dataElementD)
        elementP.addToDataElements(dataElementD)
        checkAndSave(dataElementD)

        Metadata allGroup1 = new Metadata(createdByUser: reader1, namespace: METADATA_NAMESPACE, key: METADATA_XSD_ALL, value: '1', multiFacetAwareItemId: UUID.randomUUID())

        DataElement elementJDecimal = new DataElement(createdByUser: reader1, dataType: anotherNumberWithRestrictionsType, label: 'Element_J_Decimal', minMultiplicity: 1, maxMultiplicity: 1)
        allClass.addToDataElements(elementJDecimal)
        checkAndSave(elementJDecimal)
        elementJDecimal.addToMetadata(allGroup1)

        DataElement elementJnumber = new DataElement(createdByUser: reader1, dataType: numberWithRestrictionsType, label: 'element-J-number', minMultiplicity: 1, maxMultiplicity: 1)
        allClass.addToDataElements(elementJnumber)
        checkAndSave(elementJnumber)
        elementJnumber.addToMetadata(allGroup1)

        DataElement dataElementK = new DataElement(createdByUser: reader1, dataType: allType, label: 'element k', minMultiplicity: 1, maxMultiplicity: 1, description: 'An element with an all complex element')
        topElementClass.addToDataElements(dataElementK)
        checkAndSave(dataElementK)

        DataElement dataElementO = new DataElement(createdByUser: reader1, dataType: complexMType, label: 'element o', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataElements(dataElementO)
        checkAndSave(dataElementO)

        DataElement dataElementP = new DataElement(createdByUser: reader1, dataType: elementPType, label: 'element p', minMultiplicity: 1, maxMultiplicity: 1, description: "Element with a local complex type")
        topElementClass.addToDataElements(dataElementP)
        checkAndSave(dataElementP)

        DataElement dataElementV = new DataElement(createdByUser: reader1, dataType: complexTypeQ, label: 'element v', minMultiplicity: 1, maxMultiplicity: 1)
        topElementClass.addToDataElements(dataElementV)
        checkAndSave(dataElementV)


        DataElement dataElementH = new DataElement(createdByUser: reader1, dataType: patternedStringType, label: 'element_H', minMultiplicity: 1, maxMultiplicity: 1, description: 'regex string')
        topElementClass.addToDataElements(dataElementH)
        checkAndSave(dataElementH)

        DataElement dataElementI = new DataElement(createdByUser: reader1, dataType: stringWithRestrictionsType, label: "element-i", minMultiplicity: 1, maxMultiplicity: 1, description: 'remaining restrictions applied to a string')
        topElementClass.addToDataElements(dataElementI)
        checkAndSave(dataElementI)


        checkAndSave(dataModel)

        when:
        ByteArrayOutputStream byteArrayOutputStream = getXsdExporterProviderService().exportDomain(reader1, dataModel.getId())

        String exportedXsd = byteArrayOutputStream.toString('ISO-8859-1')

        Path p = Paths.get('build/tmp/', 'complex_test.xsd')
        Files.write(p, exportedXsd.getBytes('ISO-8859-1'))

        Path expPath = Paths.get('src/integration-test/resources/expected/complex.xsd')
        String expected = new String(Files.readAllBytes(expPath), 'ISO-8859-1')

        then:
        completeCompareXml(fudgeDates(expected), fudgeDates(exportedXsd))
    }

    void "test export hepatitis"() {
        given:
        setupData()

        byte[] file = loadTestFile('hic__hepatitis_v2.0.0', 'json')
        DataModel dm = importModel(file)
        dataModelService.validate(dm)
        dataModelService.saveModelWithContent(dm)

        when:


        ByteArrayOutputStream byteArrayOutputStream = getXsdExporterProviderService().exportDomain(reader1, dm.getId())

        String exportedXsd = byteArrayOutputStream.toString('ISO-8859-1')

        Path p = Paths.get('build/tmp/', 'hic__hepatitis_v2.0.0.xsd')
        Files.write(p, exportedXsd.getBytes('ISO-8859-1'))

        Path expPath = Paths.get('src/integration-test/resources/expected/hic__hepatitis_v2.0.0.xsd')
        String expected = new String(Files.readAllBytes(expPath), 'ISO-8859-1')

        then:
        completeCompareXml(fudgeDates(expected), fudgeDates(exportedXsd))
    }

}
