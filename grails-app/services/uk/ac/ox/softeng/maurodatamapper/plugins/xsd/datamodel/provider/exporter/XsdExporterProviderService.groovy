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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.exporter

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiBadRequestException
import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService

//import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiInternalException
//import uk.ac.ox.softeng.maurodatamapper.core.provider.exporter.TemplateBasedExporter
//import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService

import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.exporter.DataModelExporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Schema
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.SchemaWrapper
import uk.ac.ox.softeng.maurodatamapper.security.User

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException

//import ox.softeng.metadatacatalogue.core.spi.exporter.DataModelExporterPlugin

import javax.xml.bind.Marshaller

//import groovy.text.Template

/**
 * @since 11/09/2017
 */
class XsdExporterProviderService extends DataModelExporterProviderService {

    XsdSchemaService xsdSchemaService

    DataModelService dataModelService

    @Override
    String getFileExtension() {
        'xsd'
    }

    @Override
    String getFileType() {
        'application/xml'
    }

    @Override
    String getDisplayName() {
        'XML Schema (XSD) Exporter'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    Boolean allowsExtraMetadataKeys() {
        XsdMetadata.allowsExtraMetadataKeys()
    }

    @Override
    Set<String> getKnownMetadataKeys() {
        XsdMetadata.getKnownMetadataKeys()
    }

    @Override
    ByteArrayOutputStream exportDataModel(User currentUser, DataModel dataModel) throws ApiException {
        exportModel(dataModel, fileType)
    }

    ByteArrayOutputStream exportModel(DataModel dataModel, String format) {
        log.info('Exporting DataModel {}', dataModel.getLabel())
        SchemaWrapper sw = new SchemaWrapper(xsdSchemaService, dataModel.getLabel())

        sw.populateSchemaFromDataModel(dataModel, getDefaultTargetNamespace())

        log.debug('DataModel exported to schema wrapper')
        exportSchema(sw)
    }

    @Override
    ByteArrayOutputStream exportDataModels(User currentUser, List<DataModel> dataModel) throws ApiException {
        throw new ApiBadRequestException('XDES02', "${getName()} cannot export multiple DataModels")
    }

    private ByteArrayOutputStream exportSchema(SchemaWrapper schemaWrapper) throws ApiBadRequestException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Schema)
            log.debug('Marshalling Schema to XML')
            Marshaller marshaller = jc.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            marshaller.marshal(schemaWrapper.getWrappedElement(), os)
            log.info('DataModel exported to XSD')
            os
        } catch (JAXBException e) {
            throw new ApiBadRequestException('XDES01', 'Could not export schema', e)
        }
    }

    private String getDefaultTargetNamespace() {
        'https://metadatacatalogue.com/' +
        getNamespace().replaceAll('\\.', '/') +
        '/' +
        getVersion()
    }
}
