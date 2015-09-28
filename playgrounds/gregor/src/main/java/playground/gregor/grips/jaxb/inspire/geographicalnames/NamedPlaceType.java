//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.08.04 at 02:05:46 PM CEST
//


package playground.gregor.grips.jaxb.inspire.geographicalnames;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.datatype.XMLGregorianCalendar;
import net.opengis.gml.v_3_2_1.AbstractFeatureType;
import net.opengis.gml.v_3_2_1.AbstractMetadataPropertyType;
import net.opengis.gml.v_3_2_1.CodeType;
import net.opengis.gml.v_3_2_1.GeometryPropertyType;
import org.isotc211.iso19139.d_2007_04_17.gmd.LocalisedCharacterStringPropertyType;
import org.isotc211.iso19139.d_2007_04_17.gmd.MDResolutionType;
import playground.gregor.grips.jaxb.inspire.basetypes.IdentifierPropertyType;
import playground.gregor.grips.jaxb.inspire.basetypes.IdentifierType;


/**
 * <p>Java class for NamedPlaceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NamedPlaceType">
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
 *         &lt;element name="endLifespanVersion" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>dateTime">
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="geometry" type="{http://www.opengis.net/gml/3.2}GeometryPropertyType"/>
 *         &lt;element name="inspireId" type="{urn:x-inspire:specification:gmlas:BaseTypes:3.2}IdentifierPropertyType"/>
 *         &lt;element name="leastDetailedViewingResolution" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMetadataPropertyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.isotc211.org/2005/gmd}MD_Resolution"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="localType" type="{http://www.isotc211.org/2005/gmd}LocalisedCharacterString_PropertyType" maxOccurs="unbounded"/>
 *         &lt;element name="mostDetailedViewingResolution" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMetadataPropertyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{http://www.isotc211.org/2005/gmd}MD_Resolution"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="name" type="{urn:x-inspire:specification:gmlas:GeographicalNames:3.0}GeographicalNamePropertyType" maxOccurs="unbounded"/>
 *         &lt;element name="relatedSpatialObject" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element ref="{urn:x-inspire:specification:gmlas:BaseTypes:3.2}Identifier"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="type" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.opengis.net/gml/3.2>CodeType">
 *                 &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NamedPlaceType", propOrder = {
		"rest"
})
public class NamedPlaceType
extends AbstractFeatureType
{

	@XmlElementRefs({
		@XmlElementRef(name = "type", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "inspireId", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "name", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "geometry", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "mostDetailedViewingResolution", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "endLifespanVersion", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "beginLifespanVersion", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "localType", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "relatedSpatialObject", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class),
		@XmlElementRef(name = "leastDetailedViewingResolution", namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", type = JAXBElement.class)
	})
	protected List<JAXBElement<?>> rest;

	/**
	 * Gets the rest of the content model.
	 * 
	 * <p>
	 * You are getting this "catch-all" property because of the following reason:
	 * The field name "Name" is used by two different parts of a schema. See:
	 * line 182 of file:/Users/laemmel/Documents/workspace/playgrounds/gregor/xsd/INSPIRE/inspire-foss-read-only/schemas/inspire/v3.0.1/GeographicalNames.xsd
	 * line 42 of http://schemas.opengis.net/gml/3.2.1/gmlBase.xsd
	 * <p>
	 * To get rid of this property, apply a property customization to one
	 * of both of the following declarations to change their names:
	 * Gets the value of the rest property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the rest property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getRest().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JAXBElement }{@code <}{@link NamedPlaceType.Type }{@code >}
	 * {@link JAXBElement }{@code <}{@link GeographicalNamePropertyType }{@code >}
	 * {@link JAXBElement }{@code <}{@link IdentifierPropertyType }{@code >}
	 * {@link JAXBElement }{@code <}{@link GeometryPropertyType }{@code >}
	 * {@link JAXBElement }{@code <}{@link NamedPlaceType.EndLifespanVersion }{@code >}
	 * {@link JAXBElement }{@code <}{@link NamedPlaceType.MostDetailedViewingResolution }{@code >}
	 * {@link JAXBElement }{@code <}{@link NamedPlaceType.BeginLifespanVersion }{@code >}
	 * {@link JAXBElement }{@code <}{@link NamedPlaceType.RelatedSpatialObject }{@code >}
	 * {@link JAXBElement }{@code <}{@link LocalisedCharacterStringPropertyType }{@code >}
	 * {@link JAXBElement }{@code <}{@link NamedPlaceType.LeastDetailedViewingResolution }{@code >}
	 * 
	 * 
	 */
	public List<JAXBElement<?>> getRest() {
		if (this.rest == null) {
			this.rest = new ArrayList<JAXBElement<?>>();
		}
		return this.rest;
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
			return this.value;
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
			if (this.nilReason == null) {
				this.nilReason = new ArrayList<String>();
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
			return this.value;
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
			if (this.nilReason == null) {
				this.nilReason = new ArrayList<String>();
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
	 *   &lt;complexContent>
	 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMetadataPropertyType">
	 *       &lt;sequence>
	 *         &lt;element ref="{http://www.isotc211.org/2005/gmd}MD_Resolution"/>
	 *       &lt;/sequence>
	 *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
	 *     &lt;/extension>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
			"mdResolution"
	})
	public static class LeastDetailedViewingResolution
	extends AbstractMetadataPropertyType
	{

		@XmlElement(name = "MD_Resolution", namespace = "http://www.isotc211.org/2005/gmd", required = true)
		protected MDResolutionType mdResolution;
		@XmlAttribute
		protected List<String> nilReason;

		/**
		 * Gets the value of the mdResolution property.
		 * 
		 * @return
		 *     possible object is
		 *     {@link MDResolutionType }
		 * 
		 */
		public MDResolutionType getMDResolution() {
			return this.mdResolution;
		}

		/**
		 * Sets the value of the mdResolution property.
		 * 
		 * @param value
		 *     allowed object is
		 *     {@link MDResolutionType }
		 * 
		 */
		public void setMDResolution(MDResolutionType value) {
			this.mdResolution = value;
		}

		public boolean isSetMDResolution() {
			return (this.mdResolution!= null);
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
			if (this.nilReason == null) {
				this.nilReason = new ArrayList<String>();
			}
			return this.nilReason;
		}

		public boolean isSetNilReason() {
			return ((this.nilReason!= null)&&(!this.nilReason.isEmpty()));
		}

		public void unsetNilReason() {
			this.nilReason = null;
		}

		@Override
		public Object createNewInstance() {
			// TODO Auto-generated method stub
			return null;
		}

	}


	/**
	 * <p>Java class for anonymous complex type.
	 * 
	 * <p>The following schema fragment specifies the expected content contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractMetadataPropertyType">
	 *       &lt;sequence>
	 *         &lt;element ref="{http://www.isotc211.org/2005/gmd}MD_Resolution"/>
	 *       &lt;/sequence>
	 *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
	 *     &lt;/extension>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
			"mdResolution"
	})
	public static class MostDetailedViewingResolution
	extends AbstractMetadataPropertyType
	{

		@XmlElement(name = "MD_Resolution", namespace = "http://www.isotc211.org/2005/gmd", required = true)
		protected MDResolutionType mdResolution;
		@XmlAttribute
		protected List<String> nilReason;

		/**
		 * Gets the value of the mdResolution property.
		 * 
		 * @return
		 *     possible object is
		 *     {@link MDResolutionType }
		 * 
		 */
		public MDResolutionType getMDResolution() {
			return this.mdResolution;
		}

		/**
		 * Sets the value of the mdResolution property.
		 * 
		 * @param value
		 *     allowed object is
		 *     {@link MDResolutionType }
		 * 
		 */
		public void setMDResolution(MDResolutionType value) {
			this.mdResolution = value;
		}

		public boolean isSetMDResolution() {
			return (this.mdResolution!= null);
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
			if (this.nilReason == null) {
				this.nilReason = new ArrayList<String>();
			}
			return this.nilReason;
		}

		public boolean isSetNilReason() {
			return ((this.nilReason!= null)&&(!this.nilReason.isEmpty()));
		}

		public void unsetNilReason() {
			this.nilReason = null;
		}

		@Override
		public Object createNewInstance() {
			// TODO Auto-generated method stub
			return null;
		}

	}


	/**
	 * <p>Java class for anonymous complex type.
	 * 
	 * <p>The following schema fragment specifies the expected content contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element ref="{urn:x-inspire:specification:gmlas:BaseTypes:3.2}Identifier"/>
	 *       &lt;/sequence>
	 *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = {
			"identifier"
	})
	public static class RelatedSpatialObject {

		@XmlElement(name = "Identifier", namespace = "urn:x-inspire:specification:gmlas:BaseTypes:3.2", required = true)
		protected IdentifierType identifier;
		@XmlAttribute
		protected List<String> nilReason;

		/**
		 * Gets the value of the identifier property.
		 * 
		 * @return
		 *     possible object is
		 *     {@link IdentifierType }
		 * 
		 */
		public IdentifierType getIdentifier() {
			return this.identifier;
		}

		/**
		 * Sets the value of the identifier property.
		 * 
		 * @param value
		 *     allowed object is
		 *     {@link IdentifierType }
		 * 
		 */
		public void setIdentifier(IdentifierType value) {
			this.identifier = value;
		}

		public boolean isSetIdentifier() {
			return (this.identifier!= null);
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
			if (this.nilReason == null) {
				this.nilReason = new ArrayList<String>();
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
	 *     &lt;extension base="&lt;http://www.opengis.net/gml/3.2>CodeType">
	 *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
	 *     &lt;/extension>
	 *   &lt;/simpleContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Type
	extends CodeType
	{

		@XmlAttribute
		protected List<String> nilReason;

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
			if (this.nilReason == null) {
				this.nilReason = new ArrayList<String>();
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


	@Override
	public Object createNewInstance() {
		// TODO Auto-generated method stub
		return null;
	}

}