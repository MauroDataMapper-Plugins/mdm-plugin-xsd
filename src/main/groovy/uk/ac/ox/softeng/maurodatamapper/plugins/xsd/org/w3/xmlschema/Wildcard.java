/**
 * Copyright 2020-2023 University of Oxford and NHS England
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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2015.11.19 at 04:44:22 PM GMT
//


package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for wildcard complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="wildcard"$gt;
 *   &lt;complexContent$gt;
 *     &lt;extension base="{http://www.w3.org/2001/XMLSchema}annotated"$gt;
 *       &lt;attribute name="namespace" type="{http://www.w3.org/2001/XMLSchema}namespaceList" default="##any" /$gt;
 *       &lt;attribute name="processContents" default="strict"$gt;
 *         &lt;simpleType$gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"$gt;
 *             &lt;enumeration value="skip"/$gt;
 *             &lt;enumeration value="lax"/$gt;
 *             &lt;enumeration value="strict"/$gt;
 *           &lt;/restriction$gt;
 *         &lt;/simpleType$gt;
 *       &lt;/attribute$gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/$gt;
 *     &lt;/extension$gt;
 *   &lt;/complexContent$gt;
 * &lt;/complexType$gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wildcard")
@XmlSeeAlso({
                Any.class
            })
public class Wildcard
    extends Annotated {

    @XmlAttribute(name = "namespace")
    @XmlSchemaType(name = "namespaceList")
    protected List<String> namespaces;
    @XmlAttribute(name = "processContents")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String processContents;

    /**
     * Gets the value of the namespaces property.
     *
     * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the namespaces property.
     *
     * <p> For example, to add a new item, do as follows:
     * <pre>
     *    getNamespaces().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getNamespaces() {
        if (namespaces == null) {
            namespaces = new ArrayList<String>();
        }
        return this.namespaces;
    }

    /**
     * Gets the value of the processContents property.
     *
     * @return possible object is {@link String }
     */
    public String getProcessContents() {
        if (processContents == null) {
            return "strict";
        } else {
            return processContents;
        }
    }

    /**
     * Sets the value of the processContents property.
     *
     * @param value allowed object is {@link String }
     */
    public void setProcessContents(String value) {
        this.processContents = value;
    }

}
