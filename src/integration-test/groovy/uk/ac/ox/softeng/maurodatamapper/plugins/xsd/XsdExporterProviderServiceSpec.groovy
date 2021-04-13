package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.core.authority.Authority
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

    @Override
    void setupDomainData() {

    }

    @Test
    void testExportModelWithSpacesInNames() throws Exception {
        DataModel dataModel = dataModelService.createAndSaveDataModel(user as User, getTestFolder(), DataModelType.DATA_STANDARD,
                                                                      'space test', 'model description', 'author', 'org',
                                                                      'placeholder' as Authority, true)

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

        testExport(dataModel.getId(), 'simple_space_test.xsd', 'simple_space_test.xsd')
    }

    private DataClass createDataClass(String label, DataModel dataModel) {
        DataClass dataClass = new DataClass()
        dataClass.setCreatedBy(user)
        dataClass.setLabel(label)
        dataClass.setMaxMultiplicity(1)
        dataClass.setMinMultiplicity(1)

        dataModel.addTo('dataClasses', dataClass)
        assertTrue('Dataclass must be valid', checkAndSave(dataClass) as boolean)

        dataClass
    }

    private void createDataElement(String label, DataClass dataClass, DataType dataType) {
        DataElement dataElement = new DataElement()
        dataElement.setCreatedBy(user)
        dataElement.setLabel(label)
        dataElement.setMaxMultiplicity(1)
        dataElement.setMinMultiplicity(1)
        dataElement.setDataType(dataType)

        dataClass.addTo('childDataElements', dataElement)
        assertTrue('DataElement must be valid', checkAndSave(dataElement) as boolean)
    }

    private DataType createDataType(DataModel dataModel) {
        DataType dataType = new PrimitiveType()
        dataType.setCreatedBy(user)
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

        assertTrue('DataType must be valid', checkAndSave(dataType) as boolean)
        dataType
    }

    private void testExport(UUID dataModelId, String filename, String outFileName) throws IOException, ApiException {
        log.info('------------ Exporting -------------')
        XsdExporterProviderService xsdExporterProviderService = applicationContext.getBean(XsdExporterProviderService.class)
        ByteArrayOutputStream byteArrayOutputStream = xsdExporterProviderService.exportDomain(user, dataModelId)
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
