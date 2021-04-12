package ox.softeng.metadatacatalogue.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider

import ox.softeng.metadatacatalogue.plugins.xsd.wrapper.OpenAttrsWrapper

import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_NAMESPACE
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_TARGET_NAMESPACE
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_TARGET_NAMESPACE_PREFIX
import static ox.softeng.metadatacatalogue.plugins.xsd.wrapper.SimpleTypeWrapper.PRIMITIVE_XML_TYPES

/**
 * @since 19/04/2018
 */
class XsdDefaultDataTypeProvider implements DefaultDataTypeProvider {

    @Override
    List<DataType> getDefaultListOfDataTypes() {
        PRIMITIVE_XML_TYPES.collect {t ->
            new PrimitiveType(label: t, description: "XML primitive type: xs:$t")
                .addToMetadata(namespace: METADATA_NAMESPACE, key: METADATA_XSD_TARGET_NAMESPACE, value: OpenAttrsWrapper.XS_NAMESPACE)
                .addToMetadata(namespace: METADATA_NAMESPACE, key: METADATA_XSD_TARGET_NAMESPACE_PREFIX, value: OpenAttrsWrapper.XS_PREFIX)
        }
    }

    @Override
    String getDisplayName() {
        'XSD DataTypes'
    }

    @Override
    String getVersion() {
        return null
    }

    @Override
    int getOrder() {
        -1
    }
}
