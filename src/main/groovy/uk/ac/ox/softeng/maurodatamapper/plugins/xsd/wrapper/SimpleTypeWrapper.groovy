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

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.NoFixedFacet
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Restriction
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleType
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings

import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.AnnotationContentWrapper.APPINFO_CONTENT

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

    DataType createDataType(User user, DataModel dm, SchemaWrapper schema) {
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
                wrappedElement.getUnion().getMemberTypes().each {mem -> members.append(mem.getLocalPart()).append(" ")}
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

        enums.each {noFixedFacet ->
            xsdSchemaService.addEnumerationValueToEnumerationType(et, noFixedFacet.getValue(),
                                                                  extractDescriptionFromAnnotations(noFixedFacet, APPINFO_CONTENT),
                                                                  user)
        }
        return et;

    }

    boolean matchesName(String name) {
        return !Strings.isNullOrEmpty(name) && (name.equals(getName()) || name.equals(wrappedElement.getName()));
    }

    private Restriction buildRestriction(Collection<Metadata> xsdMetadata, String restrictionType, DataType dataType) {
        Restriction restriction = new Restriction();
        restriction.setBase(getTypeForName(restrictionType));

        xsdMetadata.findAll {md -> !md.getKey().equalsIgnoreCase(XsdPlugin.METADATA_XSD_RESTRICTION_BASE)}.each {md ->
            RestrictionKind rk = RestrictionKind.findFromDisplayText(md.getKey());

            if (rk != null) {
                trace("Defining restriction {} : {}", rk, md.getValue());
                Object element = RestrictionWrapper.createRestrictionElement(rk, md.getValue());
                if (element != null) restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().add(element);
            } else {
                warn("Unknown restriction type {}", md.getKey());
            }
        }

        if (dataType.instanceOf(EnumerationType.class)) {
            trace("DataType is enumerationType with {} values", ((EnumerationType) dataType).getEnumerationValues().size());

            ((EnumerationType) dataType).getEnumerationValues().sort().each {ev ->
                Object element = RestrictionWrapper.createRestrictionElement(RestrictionKind.enumeration, ev.getKey(),
                                                                             createAnnotation(ev.getValue()));
                if (element != null) restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().add(element);
            }
        }


        return restriction;
    }

    private void checkBaseTypeExists(User user, DataModel dataModel, SchemaWrapper schema, QName baseType) {
        debug("Find or create base type '{}'", baseType.getLocalPart());
        SimpleTypeWrapper base = schema.getSimpleTypeByName(getRestriction().getBase().getLocalPart());
        if (base == null) return;
        base.createDataType(user, dataModel, schema);
    }

    private String findMetadata(Set<Metadata> metadata, String key) {
        Metadata optional = metadata.find {md -> md.getKey().equalsIgnoreCase(key)}
        return optional?.getValue()
    }

    private List<NoFixedFacet> getEnumerations(SchemaWrapper schema) {
        List<NoFixedFacet> enums = new ArrayList<>();
        if (getRestriction() != null) {

            enums.addAll(getRestriction()
                             .findRestrictionsWithKind(RestrictionKind.enumeration)
                             .collect {e -> (NoFixedFacet) (e.getValue())})


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

        Set<Metadata> xsdMetadata = dataType.findMetadataByNamespace(XsdPlugin.METADATA_NAMESPACE);

        if (xsdMetadata.isEmpty()) {
            warn("SimpleType cannot be reliably created as no defined XSD data in {}", dataType.getLabel());
            return;
        }

        String restrictionType = findMetadata(xsdMetadata, XsdPlugin.METADATA_XSD_RESTRICTION_BASE);
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
