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
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Any
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.BaseAttribute
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ExplicitGroup

import javax.xml.bind.JAXBElement

/**
 * @since 05/09/2017
 */
abstract class ComplexContentWrapper<K extends Annotated> extends AnnotatedWrapper<K> {

    ComplexContentWrapper(XsdSchemaService xsdSchemaService, K wrappedElement) {
        super(xsdSchemaService, wrappedElement)
    }

    abstract List<Annotated> getAttributesAndAttributeGroups()

    abstract ExplicitGroup getAll()

    abstract ExplicitGroup getChoice()

    abstract ExplicitGroup getSequence()

    List<BaseAttribute> getBaseAttributes() {
        getAttributesAndAttributeGroups().findAll {it instanceof BaseAttribute && it.use != 'prohibited'} as List<BaseAttribute>
    }

    Boolean hasBaseAttributes() {
        getBaseAttributes()
    }

    List<ElementWrapper> getElements() {
        if (getSequence()) {
            return getSubElementsOfGroup(getSequence())
        }
        if (getChoice()) {
            List<ElementWrapper> elements = getSubElementsOfGroup(getChoice())
            elements.each {e -> e.setChoiceGroup('choice')}
            return elements
        }
        if (getAll()) {
            List<ElementWrapper> elements = getSubElementsOfGroup(getAll())
            elements.each {it.setAllElement()}
            return elements
        }
        if (hasBaseAttributes()) trace('complex content has attribute based content')
        else trace('complex content has no elements')
        Collections.emptyList()
    }

    List<ElementWrapper> getSubElementsOfGroup(ExplicitGroup group) {
        List<ElementWrapper> elements = []

        int groupMarker = 0
        for (Object o : group.getElementsAndGroupsAndAlls()) {

            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o
                if (el.getValue() instanceof AbstractElement) {
                    elements.add(new ElementWrapper(xsdSchemaService, (AbstractElement) el.getValue()))
                } else if (el.getValue() instanceof ExplicitGroup) {
                    List<ElementWrapper> subElements = getSubElementsOfGroup((ExplicitGroup) el.getValue())
                    int finalGroupMarker = groupMarker
                    subElements.each {e ->
                        e.setMaxOccurs(((ExplicitGroup) el.getValue()).getMaxOccurs())
                        e.setMinOccurs(((ExplicitGroup) el.getValue()).getMinOccurs())
                        switch (el.getName().getLocalPart()) {
                            case 'choice':
                                e.setChoiceGroup('choice-' + finalGroupMarker)
                                break
                            case 'all':
                                e.setAllElement()
                                break
                            case 'sequence':
                                break
                            default:
                                warn('Handling sub element group with unknown type {}', el.getName().getLocalPart())
                        }
                    }
                    groupMarker++
                    elements.addAll(subElements)
                } else {
                    warn('complex content jaxb element with value {}', el.getValue().getClass().getSimpleName())
                }
            } else if (!(o instanceof Any)) {
                warn('complex content encountered sub element of type {}', o.getClass().getSimpleName())
            }
        }
        elements.findAll {it.maxOccurs != 0}
    }

}
