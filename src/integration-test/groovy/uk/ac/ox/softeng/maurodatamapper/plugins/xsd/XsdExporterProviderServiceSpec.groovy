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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.core.authority.Authority
import uk.ac.ox.softeng.maurodatamapper.core.bootstrap.StandardEmailAddress
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.exporter.XsdExporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.evaluator.IgnoreNameAttributeDifferenceEvaluator
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind
import uk.ac.ox.softeng.maurodatamapper.security.User
import uk.ac.ox.softeng.maurodatamapper.test.integration.BaseIntegrationSpec
import uk.ac.ox.softeng.maurodatamapper.test.xml.evalutator.IgnoreOrderDifferenceEvaluator
import uk.ac.ox.softeng.maurodatamapper.test.xml.selector.CustomElementSelector

import com.google.common.base.Strings
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import groovy.xml.XmlUtil
import org.junit.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.Diff
import org.xmlunit.diff.DifferenceEvaluator
import org.xmlunit.diff.DifferenceEvaluators
import org.xmlunit.diff.ElementSelector
import org.xmlunit.input.CommentLessSource

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.transform.Source

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_RESTRICTION_BASE

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@Integration
@Rollback
@Slf4j
class XsdExporterProviderServiceSpec extends BaseIntegrationSpec {

    DataModelService dataModelService
    XsdExporterProviderService xsdExporterProviderService
    Folder folder
    String testUser = reader1.emailAddress
    Authority testAuthority

    @Override
    void setupDomainData() {
        log.debug('Setting up DataModel for ExporterSpec unit')
    }

    void testExportModelWithSpacesInNames() throws Exception {

        folder = new Folder(label: 'xsdTestFolder', createdBy: reader1.emailAddress)
        checkAndSave(folder)
        String testUser = reader1.emailAddress
        testAuthority = new Authority(label: 'XsdTestAuthority', url: 'http://localhost', createdBy: testUser)
        checkAndSave(testAuthority)

        DataModel dataModel = new DataModel(createdByUser: reader1, label: 'test XSD exporter', author: 'author', organisation:'org',
                                            description:'XSD exporter test model description', type: DataModelType.DATA_STANDARD,
                                            folder: folder, authority: testAuthority)
        dataModel.save()
        dataModel

        DataClass dataClass = createDataClass('class with label', dataModel)
        createDataClass('2 class with number', dataModel)
        createDataClass('class 3 with number', dataModel)
        createDataClass('Properly Named', dataModel)
        createDataClass('camelCase', dataModel)
        createDataClass('2CamelCase', dataModel)
        createDataClass('camel3Case', dataModel)
        createDataClass('hyphen-named', dataModel)
        createDataClass('Hyphen-Named-With-Caps', dataModel)
        createDataClass('2-hyphen-named', dataModel)
        createDataClass('hyphen-3-named', dataModel)
        createDataClass('underscore_named', dataModel)
        createDataClass('2_underscore_named', dataModel)
        createDataClass('underscore_3_named', dataModel)
        createDataClass('Underscore_Named_With_Caps', dataModel)

        DataType dataType = createDataType(dataModel)

        createDataElement('2 element with number', dataClass, dataType)
        createDataElement('element 3 with number', dataClass, dataType)
        createDataElement('Properly Named', dataClass, dataType)
        createDataElement('camelCase', dataClass, dataType)
        createDataElement('2CamelCase', dataClass, dataType)
        createDataElement('camel3Case', dataClass, dataType)
        createDataElement('hyphen-named', dataClass, dataType)
        createDataElement('Hyphen-Named-With-Caps', dataClass, dataType)
        createDataElement('2-hyphen-named', dataClass, dataType)
        createDataElement('hyphen-3-named', dataClass, dataType)
        createDataElement('underscore_named', dataClass, dataType)
        createDataElement('2_underscore_named', dataClass, dataType)
        createDataElement('underscore_3_named', dataClass, dataType)
        createDataElement('Underscore_Named_With_Caps', dataClass, dataType)

        checkAndSave(dataModel)

        expect:
        testExport(dataModel.getId(), 'simple_space_test.xsd', 'simple_space_test.xsd')
    }

    private DataClass createDataClass(String label, DataModel dataModel) {
        DataClass dataClass = new DataClass()
        dataClass.setCreatedBy(testUser)
        dataClass.setLabel(label)
        dataClass.setMaxMultiplicity(1)
        dataClass.setMinMultiplicity(1)

        dataModel.addToDataClasses(dataClass)
        checkAndSave(dataClass)

        dataClass
    }

    private void createDataElement(String label, DataClass dataClass, DataType dataType) {
        DataElement dataElement = new DataElement()
        dataElement.setCreatedBy(testUser)
        dataElement.setLabel(label)
        dataElement.setMaxMultiplicity(1)
        dataElement.setMinMultiplicity(1)
        dataElement.setDataType(dataType)

        dataClass.addToDataElements(dataElement)
        checkAndSave(dataElement)
    }

    private DataType createDataType(DataModel dataModel) {
        DataType dataType = new PrimitiveType()
        dataType.setCreatedBy(testUser)
        dataType.setLabel('data type with space')

        dataModel.addToDataTypes(dataType)
        checkAndSave(dataType)

        Metadata metadata = new Metadata()
        metadata.setNamespace(METADATA_NAMESPACE)
        metadata.setKey(METADATA_XSD_RESTRICTION_BASE)
        metadata.setValue('string')
        metadata.catalogueItemId = UUID.randomUUID()
        dataType.addToMetadata(metadata)

        Metadata metadata2 = new Metadata()
        metadata2.setNamespace(METADATA_NAMESPACE)
        metadata2.setKey(RestrictionKind.minLength.displayText)
        metadata2.setValue('1')
        metadata2.setId(UUID.randomUUID())
        dataType.addToMetadata(metadata2)

        checkAndSave(dataType)
        dataType
    }

    private void testExport(UUID dataModelId, String filename, String outFileName) throws IOException, ApiException {
        log.info('------------ Exporting -------------')
        ByteArrayOutputStream byteArrayOutputStream = xsdExporterProviderService.exportDomain(reader1, dataModelId)
        assertNotNull('Should have an exported model', byteArrayOutputStream)

        String exportedXsd = byteArrayOutputStream.toString('ISO-8859-1')
        assertFalse('Should have an exported model string', Strings.isNullOrEmpty(exportedXsd))

        Path p = Paths.get('build/tmp/', outFileName)
        Files.write(p, exportedXsd.getBytes('ISO-8859-1'))

        Path expPath = Paths.get('src/integration-test/resources/expected/' + filename)
        String expected = new String(Files.readAllBytes(expPath), 'ISO-8859-1')

        completeCompareXml(fudgeDates(expected), fudgeDates(exportedXsd))
    }

    private void completeCompareXml(String expected, String actual) {
        boolean xmlMatchesSubmitted = compareXml(expected, actual)
        assertTrue(failureReason, xmlMatchesSubmitted)
    }

    private boolean compareXml(String expected, String actual) {

        Diff myDiffIdentical = DiffBuilder
            .compare(getCommentLess(expected))
            .withTest(getCommentLess(actual))
            .normalizeWhitespace().ignoreWhitespace()
            .withNodeMatcher(new DefaultNodeMatcher(getElementSelector()))
            .withDifferenceEvaluator(getDifferenceEvaluator())
            .checkForIdentical()
            .build()
        if (myDiffIdentical.hasDifferences()) {
            log.error('\n----------------------------------- expected -----------------------------------\n{}', XmlUtil.serialize(expected))
            log.error('\n----------------------------------- actual   -----------------------------------\n{}', XmlUtil.serialize(actual))
            failureReason = myDiffIdentical.toString()
            log.error(failureReason)
        }
        return !myDiffIdentical.hasDifferences()
    }

    private String failureReason

    private static Source getCommentLess(String xml) {
        return new CommentLessSource(getSource(xml))
    }

    private static Source getSource(Object object) {
        return Input.from(object).build()
    }

    private static DifferenceEvaluator getDifferenceEvaluator() {
        return DifferenceEvaluators.chain(
            DifferenceEvaluators.Default,
            new IgnoreOrderDifferenceEvaluator(),
            new IgnoreNameAttributeDifferenceEvaluator()
        )
    }

    private static ElementSelector getElementSelector() {
        return new CustomElementSelector()
    }

    private static String fudgeDates(String s) {
        return s.replaceAll('Last Updated:.+?<br\\s*/>', 'Last Updated:<br/>')
    }
}
