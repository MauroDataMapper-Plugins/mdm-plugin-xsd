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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * memberTypes attribute must be non-empty or there must be at least one simpleType child
 *
 *
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType$gt;
 *   &lt;complexContent$gt;
 *     &lt;extension base="{http://www.w3.org/2001/XMLSchema}annotated"$gt;
 *       &lt;sequence$gt;
 *         &lt;element name="simpleType" type="{http://www.w3.org/2001/XMLSchema}localSimpleType" maxOccurs="unbounded" minOccurs="0"/$gt;
 *       &lt;/sequence$gt;
 *       &lt;attribute name="memberTypes"$gt;
 *         &lt;simpleType$gt;
 *           &lt;list itemType="{http://www.w3.org/2001/XMLSchema}QName" /$gt;
 *         &lt;/simpleType$gt;
 *       &lt;/attribute$gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/$gt;
 *     &lt;/extension$gt;
 *   &lt;/complexContent$gt;
 * &lt;/complexType$gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "simpleTypes"
})
@XmlRootElement(name = "union")
public class Union
    extends Annotated {

    @XmlAttribute(name = "memberTypes")
    protected List<QName> memberTypes;
    @XmlElement(name = "simpleType")
    protected List<LocalSimpleType> simpleTypes;

    /**
     * Gets the value of the memberTypes property.
     *
     * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the memberTypes property.
     *
     * <p> For example, to add a new item, do as follows:
     * <pre>
     *    getMemberTypes().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list {@link QName }
     */
    public List<QName> getMemberTypes() {
        if (memberTypes == null) {
            memberTypes = new ArrayList<QName>();
        }
        return this.memberTypes;
    }

    /**
     * Gets the value of the simpleTypes property.
     *
     * <p> This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the simpleTypes property.
     *
     * <p> For example, to add a new item, do as follows:
     * <pre>
     *    getSimpleTypes().add(newItem);
     * </pre>
     *
     *
     * <p> Objects of the following type(s) are allowed in the list {@link LocalSimpleType }
     */
    public List<LocalSimpleType> getSimpleTypes() {
        if (simpleTypes == null) {
            simpleTypes = new ArrayList<LocalSimpleType>();
        }
        return this.simpleTypes;
    }

}
