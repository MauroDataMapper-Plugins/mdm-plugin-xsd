package ox.softeng.metadatacatalogue.plugins.xsd

/**
 * Created by james on 31/05/2017.
 */
class XsdImporterService extends XsdPlugin {
}

/* implements DataModelImporterPlugin<XsdImportParameters> {

   @Autowired
   XsdSchemaService xsdSchemaService

   @Autowired
   DataModelService dataModelService

   @Autowired
   FolderService folderService

   @Override
   String getVersion() {
       '2.0.0'
   }

   @Override
   String getDisplayName() {
       'XML Schema (XSD) Importer'
   }

   @Override
   DataModel importDataModel(CatalogueUser currentUser, XsdImportParameters params) {
       logger.info('Loading XSD model from {}', params.getImportFile().getFileName())
       ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(params.getImportFile().getFileContents())

       Path path = Paths.get(params.getImportFile().getFileName())
       String parent = path.getParent() == null ? '' : path.getParent().toString()
       String dataModelLabel = params.getDataModelName() ?: path.getFileName().toString()

       importDataModel(currentUser, byteArrayInputStream, path.getFileName().toString(), parent, params.getRootElement(),
                       dataModelLabel, params.description, params.author, params.organisation, folderService.get(params.folderId),
                       params.finalised)
   }

   @Override
   List<DataModel> importDataModels(CatalogueUser currentUser, XsdImportParameters params) {
       throw new ApiBadRequestException('XIS02', "${getName()} cannot import multiple DataModels")
   }

   DataModel importDataModel(CatalogueUser currentUser, InputStream byteArrayInputStream, String filename, String directory,
                             String rootElement, String label, String description, String author, String organisation, Folder folder,
                             boolean finalised, boolean createLinksRatherThanReferences = false) {
       try {
           DataModel dataModel = dataModelService.createDataModel(currentUser, label, description, author, organisation, folder)
           dataModel.finalised = finalised
           SchemaWrapper schema = SchemaWrapper.loadSchemaFromInputStream(xsdSchemaService, byteArrayInputStream, filename, directory)
           logger.debug('Creating model')
           dataModel = schema.loadIntoDataModel(dataModel, currentUser, rootElement, createLinksRatherThanReferences)
           logger.info('Model loaded from XSD')
           dataModel

       } catch (JAXBException | FileNotFoundException e) {
           logger.error('Could not load XSD model from ' + filename, e)
           throw new ApiBadRequestException('XIS01', 'Cannot read schema', e)
       }
   }

   @Override
   Boolean canImportMultipleDomains() {
       false
   }
}
*/