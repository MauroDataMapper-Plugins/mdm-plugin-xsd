/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ExplicitGroup
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Facet
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.NoFixedFacet
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.NumFacet
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Pattern
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Restriction
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.RestrictionType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.TotalDigits
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.WhiteSpace
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.ExtensionCapable
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.JaxbHandler

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.enumeration
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.fractionDigits
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.length
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.maxExclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.maxInclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.maxLength
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.minExclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.minInclusive
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.minLength
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.pattern
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.totalDigits
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind.whiteSpace

/**
 * Can be
 * ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Restriction or
 * ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.RestrictionType
 *
 * both extend Annotated and both are used interchangeably
 * @since 25/08/2017
 */
class RestrictionWrapper extends ComplexContentWrapper<Annotated> implements JaxbHandler, ExtensionCapable {

    RestrictionWrapper(XsdSchemaService xsdSchemaService, Annotated wrappedElement) {
        super(xsdSchemaService, wrappedElement)
    }

    @Override
    String getName() {
        "Restriction[${getExtensionName()}]"
    }

    @Override
    QName getBase() {
        if (wrappedElement instanceof Restriction) {
            return wrappedElement.getBase()
        }
        if (wrappedElement instanceof RestrictionType) {
            return wrappedElement.getBase()
        }
        warn('Unknown restriction type inside wrapper {}', wrappedElement.getClass().getCanonicalName())
        null
    }

    @Override
    List<Annotated> getAttributesAndAttributeGroups() {
        if (wrappedElement instanceof RestrictionType) {
            return wrappedElement.getAttributesAndAttributeGroups()
        }
        warn('Unknown restriction type inside wrapper {}', wrappedElement.getClass().getCanonicalName())
        Collections.emptyList()
    }

    @Override
    ExplicitGroup getAll() {
        if (wrappedElement instanceof RestrictionType) {
            return wrappedElement.getAll()
        }
        warn('Unknown restriction type inside wrapper {}', wrappedElement.getClass().getCanonicalName())
        null
    }

    @Override
    ExplicitGroup getChoice() {
        if (wrappedElement instanceof RestrictionType) {
            return wrappedElement.getChoice()
        }
        warn('Unknown restriction type inside wrapper {}', wrappedElement.getClass())
        null
    }

    @Override
    ExplicitGroup getSequence() {
        if (wrappedElement instanceof RestrictionType) {
            return wrappedElement.getSequence()
        }
        warn('Unknown restriction type inside wrapper {}', wrappedElement.getClass().getCanonicalName())
        null
    }

    List<JAXBElement> findRestrictionsOfKind(RestrictionKind kind) {
        getRestrictions().findAll {e -> isJaxbElementWithName(e, kind.name())} as List<JAXBElement>
    }

    List<Object> findAllRestrictionsNotOfKind(RestrictionKind kind) {
        getRestrictions().findAll {e -> !isJaxbElementWithName(e, kind.name())}
    }

    List<JAXBElement> findRestrictionsWithKind(RestrictionKind kind) {
        return getMinExclusivesAndMinInclusivesAndMaxExclusives()
                .findAll {e -> e instanceof JAXBElement && isJaxbElementWithName( (JAXBElement) e, kind.name())}
    }

    List<Object> findAllRestrictionsWithoutKind(RestrictionKind kind) {
        return getMinExclusivesAndMinInclusivesAndMaxExclusives().findAll {
            e -> !(e instanceof JAXBElement) || (!isJaxbElementWithName( (JAXBElement) e, kind.name()))
        }
    }

    private List<Object> getMinExclusivesAndMinInclusivesAndMaxExclusives() {
        if (wrappedElement instanceof Restriction) {
            return ((Restriction) wrappedElement).getMinExclusivesAndMinInclusivesAndMaxExclusives()
        }
        if (wrappedElement instanceof RestrictionType) {
            return ((RestrictionType) wrappedElement).getMinExclusivesAndMinInclusivesAndMaxExclusives()
        }
        if (wrappedElement == null) {
            warn("No restriction type inside wrapper")
        } else warn("Unknown restriction type inside wrapper {}", wrappedElement.getClass().getCanonicalName())
        return Collections.emptyList()
    }



    List<Object> getRestrictions() {
        if (wrappedElement instanceof Restriction) {
            return wrappedElement.getMinExclusivesAndMinInclusivesAndMaxExclusives()
        }
        if (wrappedElement instanceof RestrictionType) {
            return wrappedElement.getMinExclusivesAndMinInclusivesAndMaxExclusives()
        }

        if (wrappedElement == null) warn('No restriction type inside wrapper')
        else warn('Unknown restriction type inside wrapper {}', wrappedElement.getClass().getCanonicalName())

        Collections.emptyList()
    }

   JAXBElement findRestrictionOfKind(RestrictionKind kind) {
       getRestrictions().find {e -> isJaxbElementWithName(e, kind.name())} as JAXBElement
   }


   static Object createRestrictionElement(RestrictionKind restrictionKind, String value) {
       createRestrictionElement(restrictionKind, value, null)
   }

   static Object createRestrictionElement(RestrictionKind restrictionKind, String value, Annotation annotation) {
       switch (restrictionKind) {
           case minLength:
           case length:
           case maxLength:
           case fractionDigits:
               return createJaxbElement(NumFacet, restrictionKind, value, annotation)
           case enumeration:
               return createJaxbElement(NoFixedFacet, restrictionKind, value, annotation)
           case maxExclusive:
           case maxInclusive:
           case minExclusive:
           case minInclusive:
               return createJaxbElement(Facet, restrictionKind, value, annotation)
           case pattern:
               return createFacet(Pattern, value, annotation)
           case whiteSpace:
               return createFacet(WhiteSpace, value, annotation)
           case totalDigits:
               return createFacet(TotalDigits, value, annotation)
       }

       null
   }

   private static <T extends Facet> T createFacet(Class<T> declaredType, String value, Annotation annotation) {
       try {
           T facet = declaredType.newInstance()
           facet.setValue(value)
           facet.setAnnotation(annotation)
           return facet
       } catch (InstantiationException | IllegalAccessException e) {
           Logger logger = LoggerFactory.getLogger(RestrictionWrapper)
           logger.error('Something went very wrong trying to create a facet of type ' + declaredType, e)
       }
       null
   }

   private static <T extends Facet> JAXBElement<T> createJaxbElement(Class<T> declaredType, RestrictionKind restrictionKind, String value,
                                                                     Annotation annotation) {
       new JAXBElement<>(new QName(XS_NAMESPACE, restrictionKind.name()), declaredType, createFacet(declaredType, value, annotation))
   }
}
