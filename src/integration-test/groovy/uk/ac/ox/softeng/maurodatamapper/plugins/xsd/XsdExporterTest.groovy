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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.exporter.XsdExporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind
import uk.ac.ox.softeng.maurodatamapper.test.xml.evalutator.IgnoreOrderDifferenceEvaluator
import uk.ac.ox.softeng.maurodatamapper.test.xml.selector.CustomElementSelector

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.evaluator.IgnoreNameAttributeDifferenceEvaluator

import com.google.common.base.Strings
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

/**
 * @since 11/09/2017
 */
class XsdExporterTest {

    @Test
    void testExportComplex() throws IOException, ApiException {
        testExportViaImport('complex.xsd', 'XSD Test: Complex model', 'complex_out.xsd')
    }

    @Test
    void testExportModelWithSpacesInNames() throws Exception {
        DataModelService dataModelService = applicationContext.getBean(DataModelService.class)
        DataModel dataModel = dataModelService.createDataModel(User,
                                                               'space test', 'model description', 'authoer', 'org',
                                                               getTestFolder(), DataModelType.DATA_STANDARD, true)

        DataClass dataClass = createDataClass('class with label', dataModel)
        createDataClass('2 class with number', dataModel)
        createDataClass('class 3 with number', dataModel)
        createDataClass('Properly Named', dataModel)
        createDataClass('camelCase', dataModel)
        createDataClass('2CamelCase', dataModel)
        createDataClass('camel3Case', dataModel)
        createDataClass('hypen-named', dataModel)
        createDataClass('Hypen-Named-With-Caps', dataModel)
        createDataClass('2-hypen-named', dataModel)
        createDataClass('hypen-3-named', dataModel)
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
        createDataElement('hypen-named', dataClass, dataType)
        createDataElement('Hypen-Named-With-Caps', dataClass, dataType)
        createDataElement('2-hypen-named', dataClass, dataType)
        createDataElement('hypen-3-named', dataClass, dataType)
        createDataElement('underscore_named', dataClass, dataType)
        createDataElement('2_underscore_named', dataClass, dataType)
        createDataElement('underscore_3_named', dataClass, dataType)
        createDataElement('Underscore_Named_With_Caps', dataClass, dataType)

        checkAndSave(dataModel)

        testExport(dataModel.getId(), 'simple_space_test.xsd', 'simple_space_test.xsd')
    }

    @Test
    void testExportSimple() throws IOException, ApiException {
        testExportViaImport('simple.xsd', 'XSD Test: Simple model', 'simple_out.xsd')
    }

    private DataClass createDataClass(String label, DataModel dataModel) {
        DataClass dataClass = new DataClass()
        dataClass.setCreatedBy(catalogueUser)
        dataClass.setLabel(label)
        dataClass.setMaxMultiplicity(1)
        dataClass.setMinMultiplicity(1)

        dataModel.addTo('dataClasses', dataClass)
        assertTrue('Dataclass must be valid', checkAndSave(dataClass))

        dataClass
    }

    private void createDataElement(String label, DataClass dataClass, DataType dataType) {
        DataElement dataElement = new DataElement()
        dataElement.setCreatedBy(catalogueUser)
        dataElement.setLabel(label)
        dataElement.setMaxMultiplicity(1)
        dataElement.setMinMultiplicity(1)
        dataElement.setDataType(dataType)

        dataClass.addTo('childDataElements', dataElement)
        assertTrue('DataElement must be valid', checkAndSave(dataElement))
    }

    private DataType createDataType(DataModel dataModel) {
        DataType dataType = new PrimitiveType()
        dataType.setCreatedBy(catalogueUser)
        dataType.setLabel('data type with space')

        Metadata metadata = new Metadata()
        metadata.setNamespace(METADATA_NAMESPACE)
        metadata.setKey(METADATA_XSD_RESTRICTION_BASE)
        metadata.setValue('string')
        dataType.addToMetadata(metadata)

        Metadata metadata2 = new Metadata()
        metadata2.setNamespace(METADATA_NAMESPACE)
        metadata2.setKey(RestrictionKind.minLength.displayText)
        metadata2.setValue('1')
        dataType.addToMetadata(metadata2)

        dataModel.addTo('dataTypes', dataType)

        assertTrue('DataType must be valid', checkAndSave(dataType))
        dataType
    }

    private String failureReason

    private Source getCommentLess(String xml) {
        return new CommentLessSource(getSource(xml))
    }

    private DifferenceEvaluator getDifferenceEvaluator() {
        return DifferenceEvaluators.chain(
            DifferenceEvaluators.Default,
            new IgnoreOrderDifferenceEvaluator(),
            new IgnoreNameAttributeDifferenceEvaluator()
        )
    }

    private ElementSelector getElementSelector() {
        return new CustomElementSelector()
    }

    private Source getSource(Object object) {
        return Input.from(object).build()
    }

    private void testExport(UUID dataModelId, String filename, String outFileName) throws IOException, ApiException {
        getLogger().info('------------ Exporting -------------')
        XsdExporterProviderService xsdExporterProviderService = applicationContext.getBean(XsdExporterProviderService.class)
        ByteArrayOutputStream byteArrayOutputStream = xsdExporterProviderService.exportDomain(catalogueUser, dataModelId)
        assertNotNull('Should have an exported model', byteArrayOutputStream)

        String exportedXsd = byteArrayOutputStream.toString('ISO-8859-1')
        assertFalse('Should have an exported model string', Strings.isNullOrEmpty(exportedXsd))

        Path p = Paths.get('build/tmp/', outFileName)
        Files.write(p, exportedXsd.getBytes('ISO-8859-1'))

        Path expPath = Paths.get('src/integration-test/resources/expected/' + filename)
        String expected = new String(Files.readAllBytes(expPath), 'ISO-8859-1')

        completeCompareXml(fudgeDates(expected), fudgeDates(exportedXsd))
    }

    private void testExportViaImport(String filename, String modelName, String outFileName) throws IOException, ApiException {
        // Import model first
        XsdImporterProviderServiceParameters params = createImportParameters(filename, modelName)
        DataModel importedModel = importDomain(params)

        getLogger().debug('DataModel to export: {}', importedModel.getId())
        // Rather than use the one returned from the import, we want to check whats actually been saved into the DB

        testExport(importedModel.getId(), filename, outFileName)
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
            getLogger().error('\n----------------------------------- expected -----------------------------------\n{}', XmlUtil.serialize(expected))
            getLogger().error('\n----------------------------------- actual   -----------------------------------\n{}', XmlUtil.serialize(actual))
            failureReason = myDiffIdentical.toString()
            getLogger().error(failureReason)
        }
        return !myDiffIdentical.hasDifferences()
    }

    private void completeCompareXml(String expected, String actual) {
        boolean xmlMatchesSubmitted = compareXml(expected, actual)
        assertTrue(failureReason, xmlMatchesSubmitted)
    }

    private String fudgeDates(String s) {
        return s.replaceAll('Last Updated:.+?<br\\s*/>', 'Last Updated:<br/>')
    }


}