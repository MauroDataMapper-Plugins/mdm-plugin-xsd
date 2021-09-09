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


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import groovy.xml.XmlUtil
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.Diff
import org.xmlunit.diff.DifferenceEvaluator
import org.xmlunit.diff.DifferenceEvaluators
import org.xmlunit.diff.ElementSelector
import org.xmlunit.input.CommentLessSource
import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.core.authority.Authority
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ReferenceType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.evaluator.IgnoreNameAttributeDifferenceEvaluator

import uk.ac.ox.softeng.maurodatamapper.test.integration.BaseIntegrationSpec
import uk.ac.ox.softeng.maurodatamapper.test.xml.evalutator.IgnoreOrderDifferenceEvaluator
import uk.ac.ox.softeng.maurodatamapper.test.xml.selector.CustomElementSelector

import javax.xml.transform.Source
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_LABEL_RESTRICTION_PREFIX
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_CHOICE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_MIN_LENGTH
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_RESTRICTION_BASE

@Slf4j
@Integration
@Rollback
class BaseXsdImportorExporterProviderServiceSpec extends BaseIntegrationSpec {


    DataModel dataModel
    Folder folder

    @Override
    void setupDomainData() {
        folder = new Folder(label: 'xsdTestFolder', createdBy: reader1.emailAddress)
        checkAndSave(folder)

        String testUser = reader1.emailAddress

        def testAuthority = new Authority(label: 'XsdTestAuthority', url: 'http://localhost', createdBy: testUser)
        checkAndSave(testAuthority)

        dataModel = new DataModel(createdByUser: reader1, label: 'XSD Test: Simple model', author: 'Test Author', organisation: 'Test Org',
                description: 'Test description', type: DataModelType.DATA_STANDARD,
                folder: folder, authority: testAuthority)
        dataModel.save()

    }


    def testExport(UUID dataModelId, String filename, String outFileName) throws IOException, ApiException {
        log.info('------------ Exporting -------------')
        ByteArrayOutputStream byteArrayOutputStream = xsdExporterProviderService.exportDomain(reader1, dataModelId)

        String exportedXsd = byteArrayOutputStream.toString('ISO-8859-1')

        Path p = Paths.get('build/tmp/', outFileName)
        Files.write(p, exportedXsd.getBytes('ISO-8859-1'))

        Path expPath = Paths.get('src/integration-test/resources/expected/' + filename)
        String expected = new String(Files.readAllBytes(expPath), 'ISO-8859-1')
    }

    boolean completeCompareXml(String expected, String actual) {
        compareXml(expected, actual)
    }

    boolean compareXml(String expected, String actual) {

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

    String failureReason

    Source getCommentLess(String xml) {
        return new CommentLessSource(getSource(xml))
    }

    Source getSource(Object object) {
        return Input.from(object).build()
    }

    DifferenceEvaluator getDifferenceEvaluator() {
        return DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                new IgnoreOrderDifferenceEvaluator(),
                new IgnoreNameAttributeDifferenceEvaluator()
        )
    }

    ElementSelector getElementSelector() {
        return new CustomElementSelector()
    }

    String fudgeDates(String s) {
        return s.replaceAll('Last Updated:.+?<br\\s*/>', 'Last Updated:<br/>')
    }


}
