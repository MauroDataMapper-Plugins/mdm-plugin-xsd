package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils

import javax.xml.bind.JAXBElement

/**
 * @since 09/10/2018
 */
trait JaxbHandler {

    boolean isJaxbElementWithName(element, String name) {
        isJaxbElement(element) && getLocalName(element).equalsIgnoreCase(name)
    }

    Boolean isJaxbElement(element) {
        element instanceof JAXBElement
    }

    String getLocalName(JAXBElement element) {
        element.getName().getLocalPart()
    }
}