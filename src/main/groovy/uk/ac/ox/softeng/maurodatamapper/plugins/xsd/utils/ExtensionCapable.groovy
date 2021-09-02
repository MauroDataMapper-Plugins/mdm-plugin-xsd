package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils

import javax.xml.namespace.QName

/**
 * @since 09/10/2018
 */
trait ExtensionCapable {

    abstract QName getBase()

    Boolean isExtension() {
        getBase()
    }

    String getExtensionName() {
        getBase().getLocalPart()
    }

    String getExtensionNamespace() {
        getBase().getNamespaceURI()
    }

    String getExtensionFqdn() {
        "${getBase().prefix}:${getBase().localPart}"
    }
}