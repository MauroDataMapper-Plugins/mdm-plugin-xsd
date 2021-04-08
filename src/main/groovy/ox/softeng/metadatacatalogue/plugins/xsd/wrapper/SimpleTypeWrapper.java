package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.DataType;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.EnumerationType;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.PrimitiveType;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel;
import ox.softeng.metadatacatalogue.core.facet.Metadata;
import ox.softeng.metadatacatalogue.core.user.CatalogueUser;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractSimpleType;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.NoFixedFacet;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Restriction;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.SimpleType;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;

import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_NAMESPACE;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_RESTRICTION_BASE;
import static ox.softeng.metadatacatalogue.plugins.xsd.wrapper.AnnotationContentWrapper.APPINFO_CONTENT;

/**
 * @since 24/08/2017
 */
public class SimpleTypeWrapper extends AnnotatedWrapper<AbstractSimpleType> {

    public static final List<String> PRIMITIVE_XML_TYPES = Arrays.asList(
        // Primitive Types
        "string",
        "boolean",
        "decimal",
        "float",
        "double",
        "duration",
        "dateTime",
        "time",
        "date",
        "gYearMonth",
        "gYear",
        "gMonthDay",
        "gDay",
        "gMonth",
        "hexBinary",
        "base64Binary",
        "anyURI",
        "QName",
        "NOTATION",
        // Derived Types
        "normalizedString",
        "token",
        "language",
        "IDREFS",
        "ENTITIES",
        "NMTOKEN",
        "NMTOKENS",
        "Name",
        "NCName",
        "ID",
        "IDREF",
        "ENTITY",
        "integer",
        "nonPositiveInteger",
        "negativeInteger",
        "long",
        "int",
        "short",
        "byte",
        "nonNegativeInteger",
        "unsignedLong",
        "unsignedInt",
        "unsignedShort",
        "unsignedByte",
        "positiveInteger");

    SimpleTypeWrapper(XsdSchemaService xsdSchemaService, AbstractSimpleType wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    SimpleTypeWrapper(XsdSchemaService xsdSchemaService, AbstractSimpleType wrappedElement, String name) {
        super(xsdSchemaService, wrappedElement, name);
    }

    @Override
    public String getName() {
        return Strings.isNullOrEmpty(givenName) ? createSimpleTypeName(wrappedElement.getName()) : givenName;
    }

    public void setName(String name) {
        givenName = name;
        wrappedElement.setName(name);
    }

    public String toString() {
        return getName();
    }

    @Override
    public RestrictionWrapper getRestriction() {
        return wrappedElement.getRestriction() == null ? null : new RestrictionWrapper(xsdSchemaService, wrappedElement.getRestriction());
    }

    public QName getType() {
        return getTypeForName(getName());
    }

    DataType createDataType(CatalogueUser user, DataModel dm, SchemaWrapper schema) {
        debug("Creating new DataType");
        String typeName = getName();

        List<NoFixedFacet> enums = getEnumerations(schema);

        if (enums.isEmpty()) {
            // Primitive type
            trace("Is a primitive type");
            PrimitiveType pt = xsdSchemaService.createPrimitiveTypeForDataModel(dm, typeName, extractDescriptionFromAnnotations(), user);

            if (getRestriction() != null) {
                addRestrictionsToMetadata(pt, user, getRestriction());
                if (getRestriction().getBase() != null) {
                    checkBaseTypeExists(user, dm, schema, getRestriction().getBase());
                }
            } else if (wrappedElement.getUnion() != null && wrappedElement.getUnion().getMemberTypes() != null) {
                StringBuilder members = new StringBuilder();
                wrappedElement.getUnion().getMemberTypes().forEach(mem -> members.append(mem.getLocalPart()).append(" "));
                addMetadataToComponent(pt, "XML Union", members.toString(), user);
            } else if (wrappedElement.getList() != null && wrappedElement.getList().getItemType() != null) {
                addMetadataToComponent(pt, "XML List", wrappedElement.getList().getItemType().getLocalPart(), user);
            } else {
                warn("No restriction: {}", typeName);
            }
            return pt;
        }
        trace("Is a simple type with {} enumerations", enums.size());
        // Enumerated type
        EnumerationType et = xsdSchemaService.createEnumerationTypeForDataModel(dm, typeName, extractDescriptionFromAnnotations(), user);
        addRestrictionsToMetadata(et, user, getRestriction());

        enums.forEach(noFixedFacet ->
                          xsdSchemaService.addEnumerationValueToEnumerationType(et, noFixedFacet.getValue(),
                                                                                extractDescriptionFromAnnotations(noFixedFacet, APPINFO_CONTENT),
                                                                                user)
                     );
        return et;

    }

    boolean matchesName(String name) {
        return !Strings.isNullOrEmpty(name) && (name.equals(getName()) || name.equals(wrappedElement.getName()));
    }

    private Restriction buildRestriction(Collection<Metadata> xsdMetadata, String restrictionType, DataType dataType) {
        Restriction restriction = new Restriction();
        restriction.setBase(getTypeForName(restrictionType));

        xsdMetadata.stream().filter(md -> !md.getKey().equalsIgnoreCase(METADATA_XSD_RESTRICTION_BASE)).forEach(md -> {
            RestrictionKind rk = RestrictionKind.findFromDisplayText(md.getKey());

            if (rk != null) {
                trace("Defining restriction {} : {}", rk, md.getValue());
                Object element = RestrictionWrapper.createRestrictionElement(rk, md.getValue());
                if (element != null) restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().add(element);
            } else {
                warn("Unknown restriction type {}", md.getKey());
            }
        });

        if (dataType.instanceOf(EnumerationType.class)) {
            trace("DataType is enumerationType with {} values", ((EnumerationType) dataType).getEnumerationValues().size());

            ((EnumerationType) dataType).getEnumerationValues().forEach(ev -> {
                Object element = RestrictionWrapper.createRestrictionElement(RestrictionKind.enumeration, ev.getKey(),
                                                                             createAnnotation(ev.getValue()));
                if (element != null) restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().add(element);
            });
        }


        return restriction;
    }

    private void checkBaseTypeExists(CatalogueUser user, DataModel dataModel, SchemaWrapper schema, QName baseType) {
        debug("Find or create base type '{}'", baseType.getLocalPart());
        SimpleTypeWrapper base = schema.getSimpleTypeByName(getRestriction().getBase().getLocalPart());
        if (base == null) return;
        base.createDataType(user, dataModel, schema);
    }

    private String findMetadata(Set<Metadata> metadata, String key) {
        Optional<Metadata> optional = metadata.stream().filter(md -> md.getKey().equalsIgnoreCase(key)).findFirst();
        return optional.isPresent() ? optional.get().getValue() : null;
    }

    private List<NoFixedFacet> getEnumerations(SchemaWrapper schema) {
        List<NoFixedFacet> enums = new ArrayList<>();
        if (getRestriction() != null) {

            enums.addAll(getRestriction()
                             .findRestrictionsWithKind(RestrictionKind.enumeration)
                             .stream()
                             .map(e -> (NoFixedFacet) (e.getValue()))
                             .collect(Collectors.toList()));


            if (getRestriction().getBase() != null) {
                SimpleTypeWrapper st = schema.getSimpleTypeByName(getRestriction().getBase().getLocalPart());
                if (st != null) {
                    enums.addAll(st.getEnumerations(schema));
                }
            }
        }

        return enums;
    }

    private void populateFromDataType(DataType dataType, String typeName) {
        setName(typeName);
        debug("Populating from {}", dataType);
        wrappedElement.setAnnotation(createAnnotation(dataType.getDescription()));

        Set<Metadata> xsdMetadata = dataType.findMetadataByNamespace(METADATA_NAMESPACE);

        if (xsdMetadata.isEmpty()) {
            warn("SimpleType cannot be reliably created as no defined XSD data in {}", dataType.getLabel());
            return;
        }

        String restrictionType = findMetadata(xsdMetadata, METADATA_XSD_RESTRICTION_BASE);
        if (Strings.isNullOrEmpty(restrictionType)) {
            warn("SimpleType cannot be reliably created as no XSD restriction base defined in {}", dataType.getLabel());
            return;
        }

        Restriction restriction = buildRestriction(xsdMetadata, restrictionType, dataType);

        // Remove unnecessary simple types which only restrict a base type but dont add anything
        if (restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().isEmpty()) {
            warn("SimpleType is unnecessary as no additional XSD metadata in {}, therefore removing", dataType.getLabel());
            wrappedElement.setName(restrictionType);
            return;
        }

        wrappedElement.setRestriction(restriction);
        // TODO List & Union
    }

    static SimpleTypeWrapper createSimpleType(XsdSchemaService xsdSchemaService, DataType dataType, String typeName) {
        SimpleTypeWrapper wrapper = new SimpleTypeWrapper(xsdSchemaService, new SimpleType());

        if (PRIMITIVE_XML_TYPES.contains(typeName)) {
            wrapper.wrappedElement.setName(typeName);
            return wrapper;
        }

        wrapper.populateFromDataType(dataType, typeName);
        return wrapper;
    }

    static String createSimpleTypeName(String name) {
        return standardiseTypeName(name);
    }
}
