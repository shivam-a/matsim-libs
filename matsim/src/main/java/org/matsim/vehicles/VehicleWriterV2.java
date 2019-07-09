/* *********************************************************************** *
 * project: org.matsim.*
 * VehicleDefinitionsWriterV1
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.vehicles;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.utils.objectattributes.AttributeConverter;
import org.matsim.utils.objectattributes.attributable.AttributesXmlWriterDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dgrether
 * @author jwjoubert
 * @author kturner
 */

public class VehicleWriterV2 extends MatsimXmlWriter {

	private static final Logger log = Logger.getLogger(VehicleWriterV2.class);
	private AttributesXmlWriterDelegate attributesWriter = new AttributesXmlWriterDelegate();

	private List<Tuple<String, String>> atts = new ArrayList<Tuple<String, String>>();
	private Map<Id<VehicleType>, VehicleType> vehicleTypes;
	private Map<Id<Vehicle>, Vehicle> vehicles;


	public VehicleWriterV2(Vehicles vehicles) {
		this.vehicleTypes = vehicles.getVehicleTypes();
		this.vehicles = vehicles.getVehicles();
//		Map<Class<?>, AttributeConverter<?>> converters = new HashMap<>() ;
//		AttributeConverter<VehicleType.DoorOperationMode> converter = new AttributeConverter<VehicleType.DoorOperationMode>(){
//			@Override
//			public VehicleType.DoorOperationMode convert( String value ){
//				return VehicleType.DoorOperationMode.valueOf( value ) ;
//			}
//
//			@Override
//			public String convertToString( Object o ){
//				return ((VehicleType.DoorOperationMode)o).name() ;
//			}
//		} ;
//		converters.put( VehicleType.DoorOperationMode.class, converter ) ;
//		this.attributesWriter.putAttributeConverters( converters );
	}

	public void writeFile(String filename) throws UncheckedIOException, IOException {
		log.info( Gbl.aboutToWrite( "vehicles", filename) ) ;
		this.openFile(filename);
		this.writeXmlHead();
		this.writeRootElement();
		this.close();
	}

	private void writeRootElement() throws UncheckedIOException, IOException {
		atts.clear();
		atts.add(this.createTuple(XMLNS, MatsimXmlWriter.MATSIM_NAMESPACE));
		atts.add(this.createTuple(XMLNS + ":xsi", DEFAULTSCHEMANAMESPACELOCATION));
		atts.add(this.createTuple("xsi:schemaLocation", MATSIM_NAMESPACE + " " + DEFAULT_DTD_LOCATION + "vehicleDefinitions_v2.0.xsd"));
		this.writeStartTag(VehicleSchemaV2Names.VEHICLEDEFINITIONS, atts);
		this.writeVehicleTypes(this.vehicleTypes);
		this.writeVehicles(this.vehicles);
		this.writeEndTag(VehicleSchemaV2Names.VEHICLEDEFINITIONS);
	}

	private void writeVehicles(Map<Id<Vehicle>, Vehicle> veh) throws UncheckedIOException {
		for (Vehicle v : veh.values()) {
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.ID, v.getId().toString()));
			atts.add(this.createTuple(VehicleSchemaV2Names.TYPE, v.getType().getId().toString()));
			this.writeStartTag(VehicleSchemaV2Names.VEHICLE, atts, true);
		}
	}

	private void writeVehicleTypes(Map<Id<VehicleType>, VehicleType> vts) throws UncheckedIOException, IOException {
		for (VehicleType vt : vts.values()) {
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.ID, vt.getId().toString()));
			this.writeStartTag(VehicleSchemaV2Names.VEHICLETYPE, atts);

			this.writer.write("\n");
			attributesWriter.writeAttributes( "\t\t" , this.writer , vt.getAttributes() );

			if (vt.getDescription() != null) {
				this.writeStartTag(VehicleSchemaV2Names.DESCRIPTION, null);
				this.writeContent(vt.getDescription(), true);
				this.writeEndTag(VehicleSchemaV2Names.DESCRIPTION);
			}
			if (vt.getCapacity() != null) {
				this.writeCapacity(vt.getCapacity());
			}
			if (!Double.isNaN(vt.getLength())){
				atts.clear();
				atts.add(this.createTuple(VehicleSchemaV2Names.METER, Double.toString(vt.getLength())));
				this.writeStartTag(VehicleSchemaV2Names.LENGTH, atts, true);
			}
			if (!Double.isNaN(vt.getWidth())){
				atts.clear();
				atts.add(this.createTuple(VehicleSchemaV2Names.METER, Double.toString(vt.getWidth())));
				this.writeStartTag(VehicleSchemaV2Names.WIDTH, atts, true);
			}
			if (!Double.isNaN(vt.getMaximumVelocity()) && !Double.isInfinite(vt.getMaximumVelocity())){
				atts.clear();
				atts.add(this.createTuple(VehicleSchemaV2Names.METERPERSECOND, Double.toString(vt.getMaximumVelocity())));
				this.writeStartTag(VehicleSchemaV2Names.MAXIMUMVELOCITY, atts, true);
			}
			if (vt.getEngineInformation() != null) {
				this.writeEngineInformation(vt.getEngineInformation());
			}
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.PCE, vt.getPcuEquivalents()));
			this.writeStartTag(VehicleSchemaV2Names.PASSENGERCAREQUIVALENTS, atts, true);
			if (vt.getNetworkMode() != null) {
				atts.clear();
				atts.add(this.createTuple(VehicleSchemaV2Names.NETWORKMODE, vt.getNetworkMode()));
				this.writeStartTag(VehicleSchemaV2Names.NETWORKMODE, atts, true);
			}


			if (!Double.isNaN(vt.getFlowEfficiencyFactor())) {
                atts.clear();
                atts.add(this.createTuple(VehicleSchemaV2Names.FACTOR, vt.getFlowEfficiencyFactor()));
                this.writeStartTag(VehicleSchemaV2Names.FLOWEFFICIENCYFACTOR, atts, true);
            }
			this.writeEndTag(VehicleSchemaV2Names.VEHICLETYPE);
			this.writer.write("\n");
		}
	}

	private void writeEngineInformation(EngineInformation ei) throws UncheckedIOException {
		this.writeStartTag(VehicleSchemaV2Names.ENGINEINFORMATION, null);
		this.writeStartTag(VehicleSchemaV2Names.FUELTYPE, null);
		this.writeContent(ei.getFuelType().toString(), false);
		this.writeEndTag(VehicleSchemaV2Names.FUELTYPE);
		atts.clear();
		this.writeEndTag(VehicleSchemaV2Names.ENGINEINFORMATION);
	}

	private void writeCapacity(VehicleCapacity cap) throws UncheckedIOException {
		this.writeStartTag(VehicleSchemaV2Names.CAPACITY, null);
		if (cap.getSeats() != null) {
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.PERSONS, cap.getSeats()));
			this.writeStartTag(VehicleSchemaV2Names.SEATS, atts, true);
		}
		if (cap.getStandingRoom() != null) {
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.PERSONS, cap.getStandingRoom()));
			this.writeStartTag(VehicleSchemaV2Names.STANDINGROOM, atts, true);
		}
		if (cap.getFreightCapacity() != null) {
			this.writeFreightCapacity(cap.getFreightCapacity());
		}
		this.writeEndTag(VehicleSchemaV2Names.CAPACITY);
	}

	private void writeFreightCapacity(FreightCapacity fc) throws UncheckedIOException {
		this.writeStartTag(VehicleSchemaV2Names.FREIGHTCAPACITY, null);
		if ( fc.getVolume() != FreightCapacity.UNDEFINED_VOLUME ) {
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.VOLUME, Double.toString(fc.getVolume())));
			this.writeStartTag(VehicleSchemaV2Names.VOLUME, atts, true);
		}
		if (fc.getWeight() != FreightCapacity.UNDEFINED_WEIGHT ) {
			atts.clear();
			atts.add(this.createTuple(VehicleSchemaV2Names.WEIGHT, Double.toString(fc.getWeight())));
			this.writeStartTag(VehicleSchemaV2Names.WEIGHT, atts, true);
		}
		this.writeEndTag(VehicleSchemaV2Names.FREIGHTCAPACITY);
	}

	public void putAttributeConverters(Map<Class<?>, AttributeConverter<?>> converters) {
		this.attributesWriter.putAttributeConverters(converters);
	}
}
