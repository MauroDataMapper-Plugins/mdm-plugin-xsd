/**
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper;

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ExplicitGroup;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Facet;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.NoFixedFacet;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.NumFacet;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Pattern;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Restriction;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.RestrictionType;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.TotalDigits;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.WhiteSpace;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * @since 25/08/2017
 */
public class RestrictionWrapper extends ComplexContentWrapper<Annotated> {

    RestrictionWrapper(XsdSchemaService xsdSchemaService, Annotated wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    public JAXBElement findRestrictionWithKind(RestrictionKind kind) {
        for (Object e : getMinExclusivesAndMinInclusivesAndMaxExclusives()) {
            if (e instanceof JAXBElement && isElementWithName(kind.name(), (JAXBElement) e)) return (JAXBElement) e;
        }
        return null;
    }

    public ExplicitGroup getAll() {
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getAll();
        }
        warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass().getCanonicalName());
        return null;
    }

    public List<Annotated> getAttributesAndAttributeGroups() {
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getAttributesAndAttributeGroups();
        }
        warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass().getCanonicalName());
        return Collections.emptyList();
    }

    public ExplicitGroup getChoice() {
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getChoice();
        }
        warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass());
        return null;
    }

    public ExplicitGroup getSequence() {
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getSequence();
        }
        warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass().getCanonicalName());
        return null;
    }

    public QName getBase() {
        if (wrappedElement instanceof Restriction) {
            return ((Restriction) wrappedElement).getBase();
        }
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getBase();
        }
        warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass().getCanonicalName());
        return null;
    }

    @Override
    public String getName() {
        return Strings.isNullOrEmpty(givenName) ? "Restriction[" + getBase().getLocalPart() + "]" : givenName;
    }

    @Override
    public RestrictionWrapper getRestriction() {
        return null;
    }

    List<Object> findAllRestrictionsWithoutKind(RestrictionKind kind) {
        return getMinExclusivesAndMinInclusivesAndMaxExclusives().stream()
            .filter(e -> !(e instanceof JAXBElement) || (!isElementWithName(kind.name(), (JAXBElement) e)))
            .collect(Collectors.toList());
    }

    List<JAXBElement> findRestrictionsWithKind(RestrictionKind kind) {
        return getMinExclusivesAndMinInclusivesAndMaxExclusives().stream()
            .filter(e -> e instanceof JAXBElement && isElementWithName(kind.name(), (JAXBElement) e))
            .map(e -> (JAXBElement) e).collect(Collectors.toList());
    }

    private List<Object> getMinExclusivesAndMinInclusivesAndMaxExclusives() {
        if (wrappedElement instanceof Restriction) {
            return ((Restriction) wrappedElement).getMinExclusivesAndMinInclusivesAndMaxExclusives();
        }
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getMinExclusivesAndMinInclusivesAndMaxExclusives();
        }
        if (wrappedElement == null) {
            warn("No restriction type inside wrapper");
        } else warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass().getCanonicalName());
        return Collections.emptyList();
    }

    static Object createRestrictionElement(RestrictionKind restrictionKind, String value) {
        return createRestrictionElement(restrictionKind, value, null);
    }

    static Object createRestrictionElement(RestrictionKind restrictionKind, String value, Annotation annotation) {
        switch (restrictionKind) {
            case minLength:
            case length:
            case maxLength:
            case fractionDigits:
                return createJaxbElement(NumFacet.class, restrictionKind, value, annotation);
            case enumeration:
                return createJaxbElement(NoFixedFacet.class, restrictionKind, value, annotation);
            case maxExclusive:
            case maxInclusive:
            case minExclusive:
            case minInclusive:
                return createJaxbElement(Facet.class, restrictionKind, value, annotation);
            case pattern:
                return createFacet(Pattern.class, value, annotation);
            case whiteSpace:
                return createFacet(WhiteSpace.class, value, annotation);
            case totalDigits:
                return createFacet(TotalDigits.class, value, annotation);
        }

        return null;
    }

    private static <T extends Facet> T createFacet(Class<T> declaredType, String value, Annotation annotation) {
        try {
            T facet = declaredType.newInstance();
            facet.setValue(value);
            facet.setAnnotation(annotation);
            return facet;
        } catch (InstantiationException | IllegalAccessException e) {
            Logger logger = LoggerFactory.getLogger(RestrictionWrapper.class);
            logger.error("Something went very wrong trying to create a facet of type " + declaredType, e);
        }
        return null;
    }

    private static <T extends Facet> JAXBElement<T> createJaxbElement(Class<T> declaredType, RestrictionKind restrictionKind, String value,
                                                                      Annotation annotation) {
        return new JAXBElement<>(new QName(XS_NAMESPACE, restrictionKind.name()), declaredType, createFacet(declaredType, value, annotation));
    }
}
