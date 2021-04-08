package ox.softeng.metadatacatalogue.plugins.xsd

import ox.softeng.metadatacatalogue.core.api.exception.ApiBadRequestException
import ox.softeng.metadatacatalogue.core.api.exception.ApiException
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModelService
import ox.softeng.metadatacatalogue.core.spi.exporter.DataModelExporterPlugin
import ox.softeng.metadatacatalogue.core.user.CatalogueUser
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Schema
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService
import ox.softeng.metadatacatalogue.plugins.xsd.wrapper.SchemaWrapper

import org.springframework.beans.factory.annotation.Autowired

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller

/**
 * @since 11/09/2017
 */
class XsdExporterService extends XsdPlugin implements DataModelExporterPlugin {

    @Autowired
    XsdSchemaService xsdSchemaService

    @Autowired
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
        '2.0.0'
    }

    @Override
    ByteArrayOutputStream exportDataModel(CatalogueUser currentUser, DataModel dataModel) throws ApiException {
        logger.info('Exporting DataModel {}', dataModel.getLabel())
        SchemaWrapper sw = new SchemaWrapper(xsdSchemaService, dataModel.getLabel())
        sw.populateSchemaFromDataModel(dataModel, getDefaultTargetNamespace())

        logger.debug('DataModel exported to schema wrapper')
        exportSchema(sw)
    }

    @Override
    ByteArrayOutputStream exportDataModels(CatalogueUser currentUser, List<DataModel> dataModel) throws ApiException {
        throw new ApiBadRequestException('XDES02', "${getName()} cannot export multiple DataModels")
    }

    private ByteArrayOutputStream exportSchema(SchemaWrapper schemaWrapper) throws ApiBadRequestException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Schema)
            logger.debug('Marshalling Schema to XML')
            Marshaller marshaller = jc.createMarshaller()
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            ByteArrayOutputStream os = new ByteArrayOutputStream()
            marshaller.marshal(schemaWrapper.getWrappedElement(), os)
            logger.info('DataModel exported to XSD')
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
