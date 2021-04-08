package ox.softeng.metadatacatalogue.plugins.xsd

import ox.softeng.metadatacatalogue.core.spi.MetadataCataloguePlugin
import ox.softeng.metadatacatalogue.plugins.xsd.wrapper.RestrictionKind

/**
 * @since 14/09/2017
 */
abstract class XsdPlugin implements MetadataCataloguePlugin {

    public static final String METADATA_LABEL_PREFIX = "XSD "
    public static final String METADATA_LABEL_RESTRICTION_PREFIX = METADATA_LABEL_PREFIX + "Restriction "

    public static final String METADATA_NAMESPACE = XsdPlugin.getPackage().getName()

    public static final String METADATA_XSD_TARGET_NAMESPACE = METADATA_LABEL_PREFIX + "XML Target Namespace"
    public static final String METADATA_XSD_TARGET_NAMESPACE_PREFIX = METADATA_LABEL_PREFIX + "XML Target Namespace Prefix"
    public static final String METADATA_XSD_RESTRICTION_BASE = METADATA_LABEL_RESTRICTION_PREFIX + "Restriction Base"
    public static final String METADATA_XSD_EXTENSION_BASE = METADATA_LABEL_PREFIX + "Extension Base"
    public static final String METADATA_XSD_DEFAULT = METADATA_LABEL_PREFIX + "Default"
    public static final String METADATA_XSD_FIXED = METADATA_LABEL_PREFIX + "Fixed"
    public static final String METADATA_XSD_CHOICE = METADATA_LABEL_PREFIX + "Choice Group"
    public static final String METADATA_XSD_ALL = METADATA_LABEL_PREFIX + "All Group"

    @Override
    Boolean allowsExtraMetadataKeys() {
        false
    }

    @Override
    Set<String> getKnownMetadataKeys() {
        ([METADATA_XSD_TARGET_NAMESPACE,
          METADATA_XSD_TARGET_NAMESPACE_PREFIX,
          METADATA_XSD_RESTRICTION_BASE,
          METADATA_XSD_EXTENSION_BASE,
          METADATA_XSD_DEFAULT,
          METADATA_XSD_FIXED,
          METADATA_XSD_CHOICE,
          METADATA_XSD_ALL] +
         RestrictionKind.values().collect {it.displayText}) as HashSet
    }
}
