//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.1
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2022.11.08 at 05:17:10 AM UTC
//


package com.gevamu.corda.iso20022.pain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Purpose2Choice complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Purpose2Choice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Cd" type="{urn:iso:std:iso:20022:tech:xsd:pain.001.001.09}ExternalPurpose1Code"/&gt;
 *         &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:pain.001.001.09}Max35Text"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Purpose2Choice", propOrder = {
    "cd",
    "prtry"
})
public class Purpose2Choice {

    @XmlElement(name = "Cd")
    protected String cd;
    @XmlElement(name = "Prtry")
    protected String prtry;

    /**
     * Gets the value of the cd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCd() {
        return cd;
    }

    /**
     * Sets the value of the cd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCd(String value) {
        this.cd = value;
    }

    /**
     * Gets the value of the prtry property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPrtry() {
        return prtry;
    }

    /**
     * Sets the value of the prtry property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPrtry(String value) {
        this.prtry = value;
    }

}