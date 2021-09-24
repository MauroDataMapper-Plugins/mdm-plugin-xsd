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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiInternalException
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Element
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.FormChoice
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Include
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.OpenAttrs
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Schema
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleType
import uk.ac.ox.softeng.maurodatamapper.security.User
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.namespace.QName
import java.util.function.BiFunction

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_TARGET_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.METADATA_XSD_TARGET_NAMESPACE_PREFIX
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.PRIMITIVE_XML_TYPES

/**
 * @since 24/08/2017
 */
class SchemaWrapper extends OpenAttrsWrapper<Schema> {

    private static Unmarshaller jaxbUnmarshallerInstance
    final List<SchemaWrapper> importedSchemas
    DataStore dataStore
    private String dataModelName

    SchemaWrapper(XsdSchemaService xsdSchemaService, String name) {
        this(xsdSchemaService, new Schema(), name)
    }

    SchemaWrapper(XsdSchemaService xsdSchemaService, Schema schema, String name) {
        super(xsdSchemaService, schema)
        givenName = name
        importedSchemas = []
    }

    SchemaWrapper(XsdSchemaService xsdSchemaService, InputStream inputStream, String name) throws JAXBException {
        this(xsdSchemaService, (Schema) getJaxbUnmarshaller().unmarshal(inputStream), name)
    }

    @Override
    String getName() {
        givenName
    }

    @Override
    String getLoggingName() {
        dataModelName ?: givenName
    }

    List<OpenAttrs> getContent() {
        wrappedElement.getIncludesAndImportsAndRedefines()
    }

    List<Include> getIncludes() {
        getContent().findAll { it instanceof Include } as List<Include>
    }

    Set<SimpleTypeWrapper> getSimpleTypes() {
        List<SimpleTypeWrapper> wrappers = getContent().findAll { it instanceof SimpleType }.collect {
            new SimpleTypeWrapper(xsdSchemaService, it as SimpleType)
        }
        importedSchemas.each { wrappers.addAll(it.getSimpleTypes()) }
        wrappers
    }

    SimpleTypeWrapper getSimpleTypeByName(String name) {
        if (!name) return null
        simpleTypes.find { it.matchesName(name) }
    }

    List<ElementWrapper> getElements() {
        List<ElementWrapper> wrappers = getContent().findAll { it instanceof Element }.collect {
            new ElementWrapper(xsdSchemaService, it as Element)
        }
        importedSchemas.each { wrappers.addAll(it.getElements()) }
        wrappers
    }

    ElementWrapper getElementByName(String name) {
        if (!name) return null
        elements.find { name == it.getName() }
    }

    List<ComplexTypeWrapper> getComplexTypes() {
        List<ComplexTypeWrapper> wrappers = getContent().findAll { it instanceof ComplexType }.collect {
            new ComplexTypeWrapper(xsdSchemaService, it as ComplexType)
        }
        importedSchemas.each { wrappers.addAll(it.getComplexTypes()) }
        wrappers
    }

    ComplexTypeWrapper getComplexTypeByName(String name) {
        if (!name) return null
        complexTypes.find { it.matchesName(name) }
    }

    DataType getDataType(String key) {
        dataStore.getDataType(key)
    }

    /*
     * Importing DataModel
     */

    DataModel loadIntoDataModel(DataModel dataModel, User createdBy, String rootElement) {

        dataModelName = dataModel.getLabel()

        dataModel.addToMetadata(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE, wrappedElement.getTargetNamespace(), createdBy.emailAddress)

        int sizeOfDataTypes = getSimpleTypes().size() + PRIMITIVE_XML_TYPES.size() + getComplexTypes().size()
        int sizeOfDataClasses = getComplexTypes().size() + getElements().size()

        dataStore = new DataStore(createdBy, xsdSchemaService.getSemanticLinkService(), sizeOfDataTypes, sizeOfDataClasses)

        createXsdBasePrimitiveTypes(createdBy, dataModel)

        createSimpleTypes(createdBy, dataModel)

        createElements(createdBy, dataModel, rootElement)

        cleanUnusedNonXsdBasePrimitiveTypes(dataModel)

        debug('Created DataModel')
        float dta = dataStore.dataTypeCreations.sum() == null ? 0 : dataStore.dataTypeCreations.sum() / dataStore.dataTypeCreations.size() == 0 ? 1 : dataStore.dataTypeCreations.size()
        float dca = dataStore.dataClassCreations.sum() == null ? 0 : dataStore.dataClassCreations.sum() / dataStore.dataClassCreations.size() == 0 ? 1 : dataStore.dataClassCreations.size()
        float dea = dataStore.dataElementCreations.sum() == null ? 0 : dataStore.dataElementCreations.sum() / dataStore.dataElementCreations.size() == 0 ? 1 : dataStore.dataElementCreations.size()
        debug('AVG Times: \n  DataTypes: {}\n  DataClasses: {}\n  DataElements: {}', Utils.getTimeString(dta.toLong()),
                Utils.getTimeString(dca.toLong()),
                Utils.getTimeString(dea.toLong()))
        dataModel
    }

    void createXsdBasePrimitiveTypes(User user, DataModel dataModel) {
        info('Adding {} primitive base types', PRIMITIVE_XML_TYPES.size())

        PRIMITIVE_XML_TYPES.each { t ->
            PrimitiveType pt = xsdSchemaService.createPrimitiveTypeForDataModel(dataModel, t, 'XML primitive type: xs:' + t, user)
            pt.addToMetadata(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE, XS_NAMESPACE, user)
            pt.addToMetadata(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE_PREFIX, XS_PREFIX, user)
            dataStore.putDataType(t, pt)
        }
    }

    void createSimpleTypes(User user, DataModel dataModel) {
        if (simpleTypes.isEmpty()) return
        info('Adding {} simple types', simpleTypes.size())
        simpleTypes.each { st -> findOrCreateDataType(st, user, dataModel) }
    }

    void createElements(User user, DataModel dataModel, String rootElement, DataStore dataStore) {
        this.dataStore = dataStore
        createElements(user, dataModel, rootElement)
    }

    void createElements(User user, DataModel dataModel, String rootElement) {

        if (!elements.isEmpty()) {

            // Root element defined so want to import that only
            if (rootElement) {
                ElementWrapper element = elements.find { e -> rootElement.equalsIgnoreCase(e.getName()) }
                if (element) {
                    info('Adding root element {}', rootElement)
                    element.createDataModelElement(user, dataModel, this)
                    return
                }
                warn('Root element "{}" specified but cannot be found in the list of available elements', rootElement)
            }
            // If no defined root element then import all elements
            else {
                info('Adding {} elements', elements.size())
                // Assuming at this point that every element defined is a data class / complex data element
                elements.each { e -> e.createDataModelElement(user, dataModel, this) }
            }
        }

        importedSchemas.each { schema -> schema.createElements(user, dataModel, rootElement, this.dataStore) }
    }

    DataType findOrCreateDataType(SimpleTypeWrapper simpleTypeWrapper, User user, DataModel dataModel) {
        long start = System.currentTimeMillis()
        try {
            DataType dataType = findOrCreateDataTypes(simpleTypeWrapper, user,dataModel)
            dataStore.dataTypeCreations << System.currentTimeMillis() - start
            return dataType
        } catch (Exception ex) {
            throw new ApiInternalException('SW01', "Cannot create DataType for ${simpleTypeWrapper.name}", ex)
        }
    }

    DataType findOrCreateDataTypes(SimpleTypeWrapper simpleTypeWrapper, User user, DataModel dataModel){
        DataType existing = dataStore.getDataType(simpleTypeWrapper.name)
        if (!existing) {
            return dataStore.putDataType(simpleTypeWrapper.name,  simpleTypeWrapper.createDataType(user, dataModel, this))
        }
        //Non-local simple types are always unique
        if (!simpleTypeWrapper.isLocalSimpleType()) return existing
        // Enumeration types with the same name will always be identical
        if (existing.instanceOf(EnumerationType)) return existing

        // Local simple type with same name
        // This is an issue as it could be the same or a new type, will occur as the containing element has the same name
        // this is very common in COSD
        if (simpleTypeWrapper.isSameAsDataType(existing, this)) existing
        simpleTypeWrapper.incrementName()
        findOrCreateDataType(simpleTypeWrapper, user, dataModel)
    }

    DataType findOrCreateReferenceDataType(User user, DataModel dataModel, DataClass referencedDataClass, String description) {
        long start = System.currentTimeMillis()
        String dataTypeName = createSimpleTypeName(referencedDataClass.getLabel())
        DataType dataType = dataStore.addDataType(dataTypeName)  { key, existing ->
            if (existing) return existing
            xsdSchemaService.createReferenceTypeForDataModel(dataModel, dataTypeName, description, user, referencedDataClass)
        }
        dataStore.dataTypeCreations << System.currentTimeMillis() - start
        return dataType
    }

    DataClass findOrCreateDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, ComplexTypeWrapper complexTypeWrapper)
            throws ApiInternalException {
        debug('Find or create DataClass for {}', complexTypeWrapper)
        try {
            complexTypeWrapper.setName(complexTypeWrapper.getName())

            DataClass existing = dataStore.getDataClass(complexTypeWrapper.getName())

            // New complex type so just create
            if (!existing)
                return createAndStoreDataClass(user, parentDataModel, parentDataClass, complexTypeWrapper)

            if (complexTypeWrapper.isSameAsDataClass(existing)) {
                CatalogueItem commonParent = parentDataClass ? xsdSchemaService.findCommonParent(existing, parentDataClass) : parentDataModel

                // If found dataclass parent isn't the expected parent, then its already in existence and needs to be set to the common parent
                // of the existing parent and the desired parent class
                if (commonParent != existing.getParent()) {
                    debug('Changing existing {} parent from {} to {}', existing.label, existing.getParent().label, commonParent.label)
                    xsdSchemaService.moveDataClassToParent(existing, commonParent)
                    dataStore.putDataClass(complexTypeWrapper.getName(), existing)
                }
                return existing
            }
            complexTypeWrapper.incrementName()
            findOrCreateDataClass(user, parentDataModel, parentDataClass, complexTypeWrapper)

        } catch (Exception ex) {
            throw new ApiInternalException('SW02', "Cannot create DataClass for ${complexTypeWrapper.name}", ex)
        }
    }

    DataClass createAndStoreDataClass(User user, DataModel parentDataModel, DataClass parentDataClass,
                                      ComplexTypeWrapper complexType) {
        createAndStoreDataClass(user, parentDataModel, parentDataClass, complexType, null, null, null)
    }

    DataClass createAndStoreDataClass(User user, DataModel parentDataModel, DataClass parentDataClass,
                                      ComplexTypeWrapper complexType, QName type, Integer minOccurs, Integer maxOccurs) {
        long start = System.currentTimeMillis()
        debug('Creating DataClass for type {}', complexType.getName())
        if (!dataStore.addToConstruction(complexType.getName())) {
            warn('Trying to create DataClass {} however it is already under construction', complexType.getName())
            return null
        }
        DataClass dataClass = complexType.initialiseDataClass(user, parentDataModel, parentDataClass, type, minOccurs, maxOccurs)
        dataStore.putDataClass(complexType.getName(), dataClass)
        dataClass = complexType.populateDataClass(user, parentDataModel, parentDataClass, this, dataClass)
        dataStore.putDataClass(complexType.getName(), dataClass)
        dataStore.removeFromConstruction(complexType.getName())
        dataStore.dataClassCreations << System.currentTimeMillis() - start
        dataClass
    }

    void cleanUnusedNonXsdBasePrimitiveTypes(DataModel dataModel) {
        dataModel.dataTypes.findAll {
            !it.dataElements &&
                    it.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE)?.value != XS_NAMESPACE
        }.each {
            dataModel.removeFromDataTypes(it)
        }
    }

    /*
     * Exporting DataModel
     */

    //    void populateSchemaFromDataModel(DataModel dataModel, String defaultTargetNamespace) {
    //        info('Populating from {}', dataModel)
    //        Metadata tn = dataModel.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_TARGET_NAMESPACE)
    //        if (tn) wrappedElement.setTargetNamespace(tn.getValue())
    //        else wrappedElement.setTargetNamespace(defaultTargetNamespace)
    //
    //        wrappedElement.getOtherAttributes()[new QName('xmlns')] = wrappedElement.getTargetNamespace()
    //        wrappedElement.setElementFormDefault(FormChoice.QUALIFIED)
    //
    //        debug('Adding schema annotation')
    //        Annotation annotationDocumentation = createAnnotationDocumentation(
    //            Pair.of('Name', dataModel.getLabel()),
    //            Pair.of('Author', dataModel.getAuthor()),
    //            Pair.of('Organisation', dataModel.getOrganisation()),
    //            Pair.of('Description', dataModel.getDescription()),
    //            Pair.of('Last Updated', dataModel.getLastUpdated()
    //                .withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
    //
    //        if (annotationDocumentation) {
    //            getContent().add(annotationDocumentation)
    //        }
    //
    //        debug('Adding in XSD elements')
    //        addXsdElements(dataModel.getChildDataClasses().findAll {dc ->
    //            dc.getMaxMultiplicity() &&
    //            dc.getMinMultiplicity()
    //        })
    //    }
    //


    //    ComplexTypeWrapper findOrCreateComplexType(DataClass dataClass) {
    //        String typeName = createComplexTypeName(dataClass)
    //        debug('Find or create complexType {}', typeName)
    //        ComplexTypeWrapper complexTypeWrapper = getComplexTypeByName(typeName)
    //        if (complexTypeWrapper) {
    //            complexTypeWrapper.setName(typeName)
    //            trace('Found complexType {} matching {}', complexTypeWrapper.getName(), typeName)
    //            return complexTypeWrapper
    //        }
    //
    //        ComplexTypeWrapper wrapper = ComplexTypeWrapper.createComplexType(this, dataClass)
    //        getContent().add(wrapper.wrappedElement)
    //        wrapper
    //    }
    //
    //    SimpleTypeWrapper findOrCreateSimpleType(DataType dataType) {
    //        String typeName = createSimpleTypeName(dataType)
    //        debug('Find or create simpleType {}', typeName)
    //        SimpleTypeWrapper wrapper = getSimpleTypeByName(typeName)
    //        if (wrapper) {
    //            wrapper.setName(typeName)
    //            debug('Found simpleType {} matching {}', wrapper.getName(), typeName)
    //            return wrapper
    //        }
    //
    //        wrapper = SimpleTypeWrapper.createSimpleType(xsdSchemaService, dataType, typeName)
    //        if (!PRIMITIVE_XML_TYPES.contains(wrapper.getName())) {
    //            debug('Adding {} to schema as not base XSD type', typeName)
    //            getContent().add(wrapper.wrappedElement)
    //        }
    //        wrapper
    //    }
    //

    //

    //    private void addXsdElements(Set<DataClass> rootDataClasses) {
    //
    //        // These are all DataClasses which directly belong to the DataModel and have a multiplicity
    //        // We need to create a complex type for each and an element,
    //        // This method will recurse through the model creating only the elements into the XSD which are used to create the top level elements
    //        rootDataClasses.each {dc -> getContent().add(ElementWrapper.createElement(this, dc).wrappedElement)}
    //
    //        getContent().sort {o1, o2 ->
    //            // Elements at top
    //            if (o1 instanceof AbstractElement) {
    //                if (o2 instanceof AbstractElement) return ((AbstractElement) o1).getName() <=> ((AbstractElement) o2).getName()
    //                if (o2 instanceof AbstractComplexType || o2 instanceof AbstractSimpleType) return -1
    //            }
    //
    //            // Complex types next
    //            if (o1 instanceof AbstractComplexType) {
    //                if (o2 instanceof AbstractElement) return 1
    //                if (o2 instanceof AbstractComplexType) return ((AbstractComplexType) o1).getName() <=> ((AbstractComplexType) o2).getName()
    //                if (o2 instanceof AbstractSimpleType) return -1
    //            }
    //
    //            // Simple types last
    //            if (o1 instanceof AbstractSimpleType) {
    //                if (o2 instanceof AbstractElement || o2 instanceof AbstractComplexType) return 1
    //                if (o2 instanceof AbstractSimpleType) return ((AbstractSimpleType) o1).getName() <=> ((AbstractSimpleType) o2).getName()
    //            }
    //
    //            // Anything else should be at the top
    //            1
    //        }
    //    }
    //
    //    private void createComplexTypes(User user, DataModel dataModel) {
    //
    //        // Hopefully the order they are provided from the schema class is the order they are inside the actual schema
    //        List<ComplexTypeWrapper> complexTypes = getComplexTypes()
    //        info('Adding {} complex types, {} known complex types', complexTypes.size())
    //        complexTypes.each {ct -> findOrCreateDataClass(user, dataModel, ct)}
    //
    //        // Complex types need to belong in the correct place in the datamodel, so we need to create all "dependent types first"
    //        // This is easy by adding all the imported schemas first.
    //        // Issue will be the order of addition inside the schema
    //        importedSchemas.each {schema -> schema.createComplexTypes(user, dataModel, this.dataStore)}
    //
    //    }
    //
    //
    //    private void createComplexTypes(User user, DataModel dataModel, DataStore dataStore) {
    //        this.dataStore = dataStore
    //        createComplexTypes(user, dataModel)
    //    }
    //
    //
    //    DataClass findOrCreateDataClass(User user, DataModel parentDataModel, ComplexTypeWrapper complexType) {
    //        findOrCreateDataClass(user, parentDataModel, null, complexType)
    //    }
    //
    //    private String getDataModelName() {
    //        Strings.isNullOrEmpty(dataModelName) ? getName() : dataModelName
    //    }
    //

    /*
    Load schemas
     */

    void loadIncludedSchemas(XsdSchemaService xsdSchemaService, String directory, Map<String, SchemaWrapper> loadedSchemas)
            throws FileNotFoundException, JAXBException {

        List<Include> includes = getIncludes()
        if (includes.isEmpty()) return

        trace('Loading {} included schemas', includes.size())
        for (Include inc : includes) {
            String schemaLocation = inc.getSchemaLocation()
            SchemaWrapper included = loadedSchemas[schemaLocation]
            if (included == null) {
                debug('Including schema at location {}', directory + '/' + schemaLocation)
                InputStream is = getClass().getClassLoader().getResourceAsStream(directory + '/' + schemaLocation)

                if (is == null) is = new FileInputStream(directory + '/' + schemaLocation)

                included = createSchemaWrapperFromInputStream(xsdSchemaService, is, schemaLocation, directory, loadedSchemas)
                loadedSchemas[schemaLocation] = included
            }

            importedSchemas << included
        }

        trace('{} included schemas loaded', importedSchemas.size())
    }

    static SchemaWrapper createSchemaWrapperFromInputStream(XsdSchemaService xsdSchemaService, InputStream is, String filename, String directory)
            throws JAXBException, FileNotFoundException {
        createSchemaWrapperFromInputStream(xsdSchemaService, is, filename, directory, [:])
    }

    static SchemaWrapper createSchemaWrapperFromInputStream(XsdSchemaService xsdSchemaService, InputStream inputStream, String filename,
                                                            String directory, Map<String, SchemaWrapper> loadedSchemas)
            throws JAXBException, FileNotFoundException {

        SchemaWrapper schemaWrapper = new SchemaWrapper(xsdSchemaService, inputStream, filename.replaceFirst('\\.xsd', ''))
        schemaWrapper.loadIncludedSchemas(xsdSchemaService, directory, loadedSchemas)
        schemaWrapper
    }

    private static Unmarshaller getJaxbUnmarshaller() throws JAXBException {
        if (jaxbUnmarshallerInstance == null) {
            jaxbUnmarshallerInstance = JAXBContext.newInstance(Schema).createUnmarshaller()
        }
        jaxbUnmarshallerInstance
    }

}
