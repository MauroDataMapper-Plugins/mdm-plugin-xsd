/**
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.wrapper

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.AbstractElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.ComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.FormChoice
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.Include
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.OpenAttrs

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.Element
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.Schema
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.org.w3.xmlschema.SimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.spi.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings
import org.apache.commons.lang3.tuple.Pair

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.function.Function
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.wrapper.SimpleTypeWrapper.PRIMITIVE_XML_TYPES

import static java.util.stream.Collectors.toList
import static java.util.stream.Collectors.toSet

/**
 * @since 24/08/2017
 */
class SchemaWrapper extends OpenAttrsWrapper<Schema> {

    private static Unmarshaller jaxbUnmarshallerInstance
    private final List<SchemaWrapper> importedSchemas
    private String dataModelName
    private DataStore dataStore

    SchemaWrapper(XsdSchemaService xsdSchemaService, String name) {
        this(xsdSchemaService, new Schema(), name)
    }

    SchemaWrapper(XsdSchemaService xsdSchemaService, Schema schema, String name) {
        super(xsdSchemaService, schema, name)
        importedSchemas = new ArrayList<>()
    }

    private SchemaWrapper(XsdSchemaService xsdSchemaService, InputStream inputStream, String name) throws JAXBException {
        this(xsdSchemaService, (Schema) getJaxbUnmarshaller().unmarshal(inputStream), name)
    }

    @Override
    String getLoggingName() {
        return getDataModelName()
    }

    @Override
    String getName() {
        return givenName
    }

    DataModel loadIntoDataModel(DataModel dataModel, User createdBy, String rootElement,
                                       boolean createLinksRatherThanReferences) {

        dataModelName = dataModel.getLabel()

        dataModel.addToMetadata(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_TARGET_NAMESPACE, wrappedElement.getTargetNamespace(), createdBy)

        dataStore = new DataStore(createdBy, xsdSchemaService.getSemanticLinkService(), createLinksRatherThanReferences)
        dataStore.putAllDataTypes(createXsdBasePrimitiveTypes(createdBy, dataModel))

        createSimpleTypes(createdBy, dataModel)

        createElements(createdBy, dataModel, rootElement)

        debug("Created DataModel")
        return dataModel
    }

    void populateSchemaFromDataModel(DataModel dataModel, String defaultTargetNamespace) {
        info("Populating from {}", dataModel)
        Metadata tn = dataModel.findMetadataByNamespaceAndKey(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_TARGET_NAMESPACE)
        if (tn != null) wrappedElement.setTargetNamespace(tn.getValue())
        else wrappedElement.setTargetNamespace(defaultTargetNamespace)

        wrappedElement.getOtherAttributes().put(new QName("xmlns"), wrappedElement.getTargetNamespace())
        wrappedElement.setElementFormDefault(FormChoice.QUALIFIED)

        debug("Adding schema annotation")
        Annotation annotationDocumentation = createAnnotationDocumentation(
            Pair.of("Name", dataModel.getLabel()),
            Pair.of("Author", dataModel.getAuthor()),
            Pair.of("Organisation", dataModel.getOrganisation()),
            Pair.of("Description", dataModel.getDescription()),
            Pair.of("Last Updated", dataModel.getLastUpdated()
                .withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))

        if (annotationDocumentation != null) {
            getIncludesAndImportsAndRedefines().add(annotationDocumentation)
        }

        debug("Adding in XSD elements")
        addXsdElements(dataModel.getChildDataClasses().stream()
                           .filter({ dc ->
                               dc.getMaxMultiplicity() != null &&
                               dc.getMinMultiplicity() != null
                           }
                                  ).collect(toSet()))
    }

    DataType computeIfDataTypeAbsent(String key,
                                     Function<? super String, ? extends DataType> mappingFunction) {
        return getDataStore().computeIfDataTypeAbsent(key, mappingFunction)
    }

    DataClass createAndStoreDataClass(User user, DataModel parentDataModel, DataClass parentDataClass,
                                      ComplexTypeWrapper complexType, QName type, Integer minOccurs, Integer maxOccurs) {
        debug("Creating DataClass for type {}", complexType.getName())
        if (!dataStore.addToConstruction(complexType.getName())) {
            warn("Trying to create DataClass {} however it is already under construction", complexType.getName())
            return null
        }
        DataClass dataClass = complexType.initialiseDataClass(user, parentDataModel, parentDataClass, type, minOccurs, maxOccurs)
        dataStore.putDataClass(complexType.getName(), dataClass)
        dataClass = complexType.populateDataClass(user, parentDataModel, parentDataClass, this, dataClass)
        dataStore.putDataClass(complexType.getName(), dataClass)
        dataStore.removeFromConstruction(complexType.getName())
        return dataClass
    }

    ComplexTypeWrapper findOrCreateComplexType(DataClass dataClass) {
        String typeName = createComplexTypeName(dataClass)
        debug("Find or create complexType {}", typeName)
        ComplexTypeWrapper complexTypeWrapper = getComplexTypeByName(typeName)
        if (complexTypeWrapper != null) {
            complexTypeWrapper.setName(typeName)
            trace("Found complexType {} matching {}", complexTypeWrapper.getName(), typeName)
            return complexTypeWrapper
        }

        ComplexTypeWrapper wrapper = ComplexTypeWrapper.createComplexType(this, dataClass)
        getIncludesAndImportsAndRedefines().add(wrapper.wrappedElement)
        return wrapper
    }

    DataClass findOrCreateDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, ComplexTypeWrapper complexType) {
        debug("Find or create DataClass for {}", complexType)
        DataClass dataClass = dataStore.getDataClass(complexType.getName())

        if (!isCreateLinksRatherThanReferences() && dataClass != null) {
            CatalogueItem commonParent = parentDataClass == null ? parentDataModel :
                                         xsdSchemaService.findCommonParent(dataClass, parentDataClass)

            // If found dataclass parent isn't the expected parent, then its already in existence and needs to be set to the common parent of the
            // existing parent and the desired parent class
            if (!commonParent.equals(dataClass.getParent())) {
                debug("Changing existing {} parent from {} to {}", dataClass, dataClass.getParent(), commonParent)
                xsdSchemaService.moveDataClassToParent(dataClass, commonParent)
                dataStore.putDataClass(complexType.getName(), dataClass)
            }

            return dataClass
        }
        return createAndStoreDataClass(user, parentDataModel, parentDataClass, complexType, null, null, null)
    }

    SimpleTypeWrapper findOrCreateSimpleType(DataType dataType) {
        String typeName = createSimpleTypeName(dataType)
        debug("Find or create simpleType {}", typeName)
        SimpleTypeWrapper wrapper = getSimpleTypeByName(typeName)
        if (wrapper != null) {
            wrapper.setName(typeName)
            debug("Found simpleType {} matching {}", wrapper.getName(), typeName)
            return wrapper
        }

        wrapper = SimpleTypeWrapper.createSimpleType(xsdSchemaService, dataType, typeName)
        if (!PRIMITIVE_XML_TYPES.contains(wrapper.getName())) {
            debug("Adding {} to schema as not base XSD type", typeName)
            getIncludesAndImportsAndRedefines().add(wrapper.wrappedElement)
        }
        return wrapper
    }

    ComplexTypeWrapper getComplexTypeByName(String name) {
        if (name == null) return null
        for (ComplexTypeWrapper st : getComplexTypes()) {
            if (st.matchesName(name)) {
                return st
            }
        }
        for (SchemaWrapper s : getImportedSchemas()) {
            ComplexTypeWrapper act = s.getComplexTypeByName(name)
            if (act != null) {
                return act
            }
        }
        return null
    }

    DataType getDataType(String key) {
        return getDataStore().getDataType(key)
    }

    ElementWrapper getElementByName(String name) {
        if (name == null) return null
        for (ElementWrapper el : getElements()) {
            if (name.equals(el.getName())) {
                return el
            }
        }
        for (SchemaWrapper s : getImportedSchemas()) {
            ElementWrapper ae = s.getElementByName(name)
            if (ae != null) {
                return ae
            }
        }
        return null
    }

    SimpleTypeWrapper getSimpleTypeByName(String name) {
        if (name == null) return null
        for (SimpleTypeWrapper st : getSimpleTypes()) {
            if (st.matchesName(name)) {
                return st
            }
        }
        for (SchemaWrapper s : getImportedSchemas()) {
            SimpleTypeWrapper ast = s.getSimpleTypeByName(name)
            if (ast != null) {
                return ast
            }
        }
        return null
    }

    boolean isCreateLinksRatherThanReferences() {
        return dataStore.isCreateLinksRatherThanReferences()
    }

    private void addImportedSchema(SchemaWrapper importedSchema) {
        importedSchemas.add(importedSchema)
    }

    private void addXsdElements(Set<DataClass> rootDataClasses) {

        // These are all DataClasses which directly belong to the DataModel and have a multiplicity
        // We need to create a complex type for each and an element,
        // This method will recurse through the model creating only the elements into the XSD which are used to create the top level elements
        rootDataClasses.forEach({ dc -> getIncludesAndImportsAndRedefines().add(ElementWrapper.createElement(this, dc).wrappedElement) })

        getIncludesAndImportsAndRedefines().sort({ o1, o2 ->
            // Elements at top
            if (o1 instanceof AbstractElement) {
                if (o2 instanceof AbstractElement) return ((AbstractElement) o1).getName().compareTo(((AbstractElement) o2).getName())
                if (o2 instanceof AbstractComplexType || o2 instanceof AbstractSimpleType) return -1
            }

            // Complex types next
            if (o1 instanceof AbstractComplexType) {
                if (o2 instanceof AbstractElement) return 1
                if (o2 instanceof AbstractComplexType) return ((AbstractComplexType) o1).getName().compareTo(((AbstractComplexType) o2).getName())
                if (o2 instanceof AbstractSimpleType) return -1
            }

            // Simple types last
            if (o1 instanceof AbstractSimpleType) {
                if (o2 instanceof AbstractElement || o2 instanceof AbstractComplexType) return 1
                if (o2 instanceof AbstractSimpleType) return ((AbstractSimpleType) o1).getName().compareTo(((AbstractSimpleType) o2).getName())
            }

            // Anything else should be at the top
            return 1
        })
    }

    private void createComplexTypes(User user, DataModel dataModel) {


        // Hopefully the order they are provided from the schema class is the order they are inside the actual schema
        List<ComplexTypeWrapper> complexTypes = getComplexTypes()
        info("Adding {} complex types, {} known complex types", complexTypes.size())
        complexTypes.forEach({ ct -> findOrCreateDataClass(user, dataModel, ct) })

        // Complex types need to belong in the correct place in the datamodel, so we need to create all "dependent types first"
        // This is easy by adding all the imported schemas first.
        // Issue will be the order of addition inside the schema
        importedSchemas.forEach({ schema -> schema.createComplexTypes(user, dataModel, this.dataStore) })

    }

    private void createComplexTypes(User user, DataModel dataModel, DataStore dataStore) {
        this.dataStore = dataStore
        createComplexTypes(user, dataModel)
    }

    private void createElements(User user, DataModel dataModel, String rootElement) {

        List<ElementWrapper> elements = getElements()
        if (!elements.isEmpty()) {

            // If no defined root element then import all elements
            if (Strings.isNullOrEmpty(rootElement)) {
                info("Adding {} elements", elements.size())
                // Assuming at this point that every element defined is a data class / complex data element
                elements.forEach({ e -> e.createDataModelElement(user, dataModel, this) })
            }

            // Root element defined so want to import that only
            else {
                Optional<ElementWrapper> element = elements.stream().filter({ e -> rootElement.equalsIgnoreCase(e.getName()) }).findFirst()
                if (element.isPresent()) {
                    info("Adding root element {}", rootElement)
                    element.get().createDataModelElement(user, dataModel, this)
                    return
                } else {
                    warn("Root element '{}' specified but cannot be found in the list of available elements", rootElement)
                }
            }
        }

        importedSchemas.forEach({ schema -> schema.createElements(user, dataModel, rootElement, this.dataStore) })
    }

    private void createElements(User user, DataModel dataModel, String rootElement, DataStore dataStore) {
        this.dataStore = dataStore
        createElements(user, dataModel, rootElement)
    }

    private void createSimpleTypes(User user, DataModel dataModel) {
        // Simple types all belong to the datamodel, so point of creation is irrelevant
        Set<SimpleTypeWrapper> simpleTypes = getSimpleTypes()
        if (simpleTypes.isEmpty()) return

        info("Adding {} simple types", simpleTypes.size())
        simpleTypes.forEach({ st -> dataStore.putDataType(st.getName(), st.createDataType(user, dataModel, this)) })
    }

    private Map<String, DataType> createXsdBasePrimitiveTypes(User user, DataModel dataModel) {
        Map<String, DataType> dataTypes = new HashMap<>()
        info("Adding {} primitive base types", PRIMITIVE_XML_TYPES.size())

        PRIMITIVE_XML_TYPES.forEach({ t ->
            PrimitiveType pt = xsdSchemaService.createPrimitiveTypeForDataModel(dataModel, t, "XML primitive type: xs:" + t, user)
            pt.addToMetadata(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_TARGET_NAMESPACE, XS_NAMESPACE, user)
            pt.addToMetadata(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_TARGET_NAMESPACE_PREFIX, XS_PREFIX, user)
            dataTypes.put(t, pt)
        })
        return dataTypes
    }

    DataClass findOrCreateDataClass(User user, DataModel parentDataModel, ComplexTypeWrapper complexType) {
        return findOrCreateDataClass(user, parentDataModel, null, complexType)
    }

    private List<Include> getAllIncludes() {
        return getIncludesAndImportsAndRedefines().stream()
            .filter({ oa -> oa instanceof Include })
            .map({ oa -> (Include) oa })
            .collect(toList())
    }

    private List<ComplexTypeWrapper> getComplexTypes() {
        return getIncludesAndImportsAndRedefines().stream()
            .filter({ oa -> oa instanceof ComplexType })
            .map({ element -> new ComplexTypeWrapper(xsdSchemaService, (ComplexType) element) })
            .collect(toList())
    }

    private String getDataModelName() {
        return Strings.isNullOrEmpty(dataModelName) ? getName() : dataModelName
    }

    private DataStore getDataStore() {
        return dataStore
    }

    private List<ElementWrapper> getElements() {
        return getIncludesAndImportsAndRedefines().stream()
            .filter({ oa -> oa instanceof Element })
            .map({ element -> new ElementWrapper(xsdSchemaService, (Element) element) })
            .collect(toList())
    }

    private List<SchemaWrapper> getImportedSchemas() {
        return importedSchemas
    }

    private List<OpenAttrs> getIncludesAndImportsAndRedefines() {
        return wrappedElement.getIncludesAndImportsAndRedefines()
    }

    private Set<SimpleTypeWrapper> getSimpleTypes() {
        return getIncludesAndImportsAndRedefines().stream()
            .filter({ oa -> oa instanceof SimpleType })
            .map({ element -> new SimpleTypeWrapper(xsdSchemaService, (SimpleType) element) })
            .collect(toSet())
    }

    private void loadIncludedSchemas(XsdSchemaService xsdSchemaService, String directory, Map<String, SchemaWrapper> loadedSchemas)
        throws FileNotFoundException, JAXBException {

        List<Include> includes = getAllIncludes()
        if (includes.isEmpty()) return

        trace("Loading {} included schemas", includes.size())
        for (Include inc : includes) {
            String schemaLocation = inc.getSchemaLocation()
            SchemaWrapper included = loadedSchemas.get(schemaLocation)
            if (included == null) {
                debug("Including schema at location {}", directory + "/" + schemaLocation)
                InputStream is = getClass().getClassLoader().getResourceAsStream(directory + "/" + schemaLocation)

                if (is == null) is = new FileInputStream(directory + "/" + schemaLocation)

                included = loadSchemaFromInputStream(xsdSchemaService, is, schemaLocation, directory, loadedSchemas)
                loadedSchemas.put(schemaLocation, included)
            }

            addImportedSchema(included)
        }

        trace("{} included schemas loaded", importedSchemas.size())
    }

    static SchemaWrapper loadSchemaFromInputStream(XsdSchemaService xsdSchemaService, InputStream is, String filename, String directory)
        throws JAXBException, FileNotFoundException {
        return loadSchemaFromInputStream(xsdSchemaService, is, filename, directory, new HashMap<>())
    }

    private static Unmarshaller getJaxbUnmarshaller() throws JAXBException {
        if (jaxbUnmarshallerInstance == null) {
            jaxbUnmarshallerInstance = JAXBContext.newInstance(Schema.class).createUnmarshaller()
        }
        return jaxbUnmarshallerInstance
    }

    static SchemaWrapper loadSchemaFromInputStream(XsdSchemaService xsdSchemaService, InputStream inputStream, String filename,
                                                           String directory,
                                                           Map<String, SchemaWrapper> loadedSchemas) throws JAXBException, FileNotFoundException {
        SchemaWrapper schemaWrapper = new SchemaWrapper(xsdSchemaService, inputStream, filename.replaceFirst("\\.xsd", ""))
        schemaWrapper.loadIncludedSchemas(xsdSchemaService, directory, loadedSchemas)
        return schemaWrapper
    }

}
