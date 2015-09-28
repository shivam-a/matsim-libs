//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.04 at 02:05:46 PM CEST 
//


package playground.gregor.grips.jaxb.inspire.network;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;
import net.opengis.gml.v_3_2_1.AbstractFeatureType;
import net.opengis.gml.v_3_2_1.ReferenceType;
import playground.gregor.grips.jaxb.inspire.basetypes.IdentifierPropertyType;
import playground.gregor.grips.jaxb.inspire.commontransportelements.TransportPointType;


/**
 * <p>Java class for NetworkElementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NetworkElementType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractFeatureType">
 *       &lt;sequence>
 *         &lt;element name="beginLifespanVersion">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>dateTime">
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="inspireId" type="{urn:x-inspire:specification:gmlas:BaseTypes:3.2}IdentifierPropertyType" minOccurs="0"/>
 *         &lt;element name="endLifespanVersion" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>dateTime">
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="inNetwork" type="{http://www.opengis.net/gml/3.2}ReferenceType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkElementType", propOrder = {
    "beginLifespanVersion",
    "inspireId",
    "endLifespanVersion",
    "inNetwork"
})
@XmlSeeAlso({
    GeneralisedLinkType.class,
    NodeType.class,
    NetworkAreaType.class,
    LinkSetType.class,
    TransportPointType.class,
    GradeSeparatedCrossingType.class,
    NetworkConnectionType.class
})
public abstract class NetworkElementType
    extends AbstractFeatureType
{

    @XmlElement(required = true, nillable = true)
    protected NetworkElementType.BeginLifespanVersion beginLifespanVersion;
    protected IdentifierPropertyType inspireId;
    @XmlElementRef(name = "endLifespanVersion", namespace = "urn:x-inspire:specification:gmlas:Network:3.2", type = JAXBElement.class)
    protected JAXBElement<NetworkElementType.EndLifespanVersion> endLifespanVersion;
    @XmlElement(required = true, nillable = true)
    protected List<ReferenceType> inNetwork;

    /**
     * Gets the value of the beginLifespanVersion property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkElementType.BeginLifespanVersion }
     *     
     */
    public NetworkElementType.BeginLifespanVersion getBeginLifespanVersion() {
        return beginLifespanVersion;
    }

    /**
     * Sets the value of the beginLifespanVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkElementType.BeginLifespanVersion }
     *     
     */
    public void setBeginLifespanVersion(NetworkElementType.BeginLifespanVersion value) {
        this.beginLifespanVersion = value;
    }

    public boolean isSetBeginLifespanVersion() {
        return (this.beginLifespanVersion!= null);
    }

    /**
     * Gets the value of the inspireId property.
     * 
     * @return
     *     possible object is
     *     {@link IdentifierPropertyType }
     *     
     */
    public IdentifierPropertyType getInspireId() {
        return inspireId;
    }

    /**
     * Sets the value of the inspireId property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdentifierPropertyType }
     *     
     */
    public void setInspireId(IdentifierPropertyType value) {
        this.inspireId = value;
    }

    public boolean isSetInspireId() {
        return (this.inspireId!= null);
    }

    /**
     * Gets the value of the endLifespanVersion property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link NetworkElementType.EndLifespanVersion }{@code >}
     *     
     */
    public JAXBElement<NetworkElementType.EndLifespanVersion> getEndLifespanVersion() {
        return endLifespanVersion;
    }

    /**
     * Sets the value of the endLifespanVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link NetworkElementType.EndLifespanVersion }{@code >}
     *     
     */
    public void setEndLifespanVersion(JAXBElement<NetworkElementType.EndLifespanVersion> value) {
        this.endLifespanVersion = ((JAXBElement<NetworkElementType.EndLifespanVersion> ) value);
    }

    public boolean isSetEndLifespanVersion() {
        return (this.endLifespanVersion!= null);
    }

    /**
     * Gets the value of the inNetwork property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inNetwork property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInNetwork().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReferenceType }
     * 
     * 
     */
    public List<ReferenceType> getInNetwork() {
        if (inNetwork == null) {
            inNetwork = new ArrayList<ReferenceType>();
        }
        return this.inNetwork;
    }

    public boolean isSetInNetwork() {
        return ((this.inNetwork!= null)&&(!this.inNetwork.isEmpty()));
    }

    public void unsetInNetwork() {
        this.inNetwork = null;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>dateTime">
     *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class BeginLifespanVersion {

        @XmlValue
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar value;
        @XmlAttribute
        protected List<String> nilReason;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setValue(XMLGregorianCalendar value) {
            this.value = value;
        }

        public boolean isSetValue() {
            return (this.value!= null);
        }

        /**
         * Gets the value of the nilReason property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the nilReason property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNilReason().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getNilReason() {
            if (nilReason == null) {
                nilReason = new ArrayList<String>();
            }
            return this.nilReason;
        }

        public boolean isSetNilReason() {
            return ((this.nilReason!= null)&&(!this.nilReason.isEmpty()));
        }

        public void unsetNilReason() {
            this.nilReason = null;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>dateTime">
     *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class EndLifespanVersion {

        @XmlValue
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar value;
        @XmlAttribute
        protected List<String> nilReason;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setValue(XMLGregorianCalendar value) {
            this.value = value;
        }

        public boolean isSetValue() {
            return (this.value!= null);
        }

        /**
         * Gets the value of the nilReason property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the nilReason property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNilReason().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getNilReason() {
            if (nilReason == null) {
                nilReason = new ArrayList<String>();
            }
            return this.nilReason;
        }

        public boolean isSetNilReason() {
            return ((this.nilReason!= null)&&(!this.nilReason.isEmpty()));
        }

        public void unsetNilReason() {
            this.nilReason = null;
        }

    }

}