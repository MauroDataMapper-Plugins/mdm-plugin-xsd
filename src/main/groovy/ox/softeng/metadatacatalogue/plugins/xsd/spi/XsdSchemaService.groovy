package ox.softeng.metadatacatalogue.plugins.xsd.spi

import ox.softeng.metadatacatalogue.core.catalogue.CatalogueItem
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataClass
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataClassService
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataElement
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataElementService
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.DataType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.EnumerationType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.EnumerationTypeService
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.PrimitiveType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.PrimitiveTypeService
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.ReferenceType
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.ReferenceTypeService
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel
import ox.softeng.metadatacatalogue.core.facet.linkable.SemanticLinkService
import ox.softeng.metadatacatalogue.core.user.CatalogueUser

import org.springframework.beans.factory.annotation.Autowired

/**
 * @since 16/01/2018
 */
class XsdSchemaService {

    @Autowired
    SemanticLinkService semanticLinkService

    @Autowired
    private DataElementService dataElementService

    @Autowired
    private DataClassService dataClassService

    @Autowired
    private PrimitiveTypeService primitiveTypeService

    @Autowired
    private EnumerationTypeService enumerationTypeService

    @Autowired
    private ReferenceTypeService referenceTypeService

    PrimitiveType createPrimitiveTypeForDataModel(DataModel dataModel, String label, String description, CatalogueUser createdBy,
                                                  String units = null) {
        primitiveTypeService.findOrCreateDataTypeForDataModel(dataModel, label, description, createdBy, units)
    }

    EnumerationType createEnumerationTypeForDataModel(DataModel dataModel, String label, String description, CatalogueUser createdBy) {
        enumerationTypeService.findOrCreateDataTypeForDataModel(dataModel, label, description, createdBy)
    }

    ReferenceType createReferenceTypeForDataModel(DataModel dataModel, String label, String description, CatalogueUser createdBy,
                                                  DataClass referenceClass) {
        referenceTypeService.findOrCreateDataTypeForDataModel(dataModel, label, description, createdBy, referenceClass)
    }

    DataElement createDataElementForDataClass(DataClass parentClass, String label, String description, CatalogueUser createdBy, DataType dataType,
                                              Integer minMultiplicity, Integer maxMultiplicity) {
        dataElementService.findOrCreateDataElementForDataClass(parentClass, label, description, createdBy, dataType, minMultiplicity, maxMultiplicity)
    }

    DataClass createDataClass(CatalogueItem parent, String label, String description, CatalogueUser createdBy, Integer minMultiplicity,
                              Integer maxMultiplicity) {
        DataClass dataClass = dataClassService.createDataClass(label, description, createdBy, minMultiplicity, maxMultiplicity)
        moveDataClassToParent(dataClass, parent)
        dataClass
    }

    EnumerationType addEnumerationValueToEnumerationType(EnumerationType enumerationType, String key, String value, CatalogueUser createdBy) {
        enumerationTypeService.addEnumerationValueToEnumerationType(enumerationType, key, value, createdBy)
    }

    CatalogueItem findCommonParent(DataClass leftDataClass, DataClass rightDataClass) {
        dataClassService.findCommonParent(leftDataClass, rightDataClass)
    }

    void moveDataClassToParent(DataClass dataClass, CatalogueItem parent) {
        dataClassService.moveDataClassToParent(dataClass, parent)
    }

    DataElement createDuplicateElementForDataClass(CatalogueUser user, DataClass parent, DataElement original) {

        DataElement duplicate = createDataElementForDataClass(parent, original.label, original.description, user, original.dataType,
                                                              original.minMultiplicity, original.maxMultiplicity)
        original.getMetadata()?.each {md ->
            duplicate.addToMetadata(md.namespace, md.key, md.value, user)
        }

        duplicate
    }

    Set<DataElement> getChildDataElements(DataClass dataClass) {
        dataElementService.findAllByDataClassIdJoinDataType(dataClass.id)
    }

}
