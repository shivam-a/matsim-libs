/* *********************************************************************** *
 * project: org.matsim.*
 * PersonStreaming.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.ciarif;

import org.matsim.gbl.Gbl;
import org.matsim.plans.MatsimPlansReader;
import org.matsim.plans.Plans;
import org.matsim.plans.PlansReaderI;
import org.matsim.plans.PlansWriter;
import org.matsim.utils.geometry.shared.Coord;

import playground.balmermi.Scenario;
import playground.balmermi.census2000.data.Household;
import playground.balmermi.census2000.data.Households;
import playground.balmermi.census2000.data.Person;
import playground.balmermi.census2000.data.Persons;
import playground.ciarif.models.subtours.PersonModeChoiceModel;

public class PersonStreaming {

	public static void run() {

		Scenario.setUpScenarioConfig();

		System.out.println("person streaming...");

		//////////////////////////////////////////////////////////////////////

//		System.out.println("  reading network xml file...");
//		NetworkLayer network = null;
//		NetworkLayerBuilder.setNetworkLayerType(NetworkLayerBuilder.NETWORK_DEFAULT);
//		network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
//		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  setting up plans objects...");
		Plans plans = new Plans(Plans.USE_STREAMING);
		PlansWriter plansWriter = new PlansWriter(plans);
		plans.setPlansWriter(plansWriter);
		PlansReaderI plansReader = new MatsimPlansReader(plans);
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  adding person modules... ");
		Household hh = new Household(0,null);
		hh.coord = new Coord(100,100);
		Person person = new Person(0,hh);
		person.age = 20;
		person.car_avail = "always";
		person.license = true;
		person.male = true;
		Households hhs = new Households(null,null);
		hhs.setHH(hh);
		Persons persons = new Persons(hhs,null);
		persons.households = hhs;
		persons.persons.put(person.p_id,person);
		
		PersonModeChoiceModel pmcm = new PersonModeChoiceModel(persons);
		plans.addAlgorithm(pmcm);
//		PersonSubTourAnalysis psta = new PersonSubTourAnalysis();
//		plans.addAlgorithm(psta);
//		PersonInitDemandSummaryTable pidst = new PersonInitDemandSummaryTable("output/output_persons.txt");
//		plans.addAlgorithm(pidst);
//		plans.addAlgorithm(new PersonCalcTripDistances());
//		PersonTripSummaryTable ptst = new PersonTripSummaryTable("output/output_trip-summary-table.txt");
//		plans.addAlgorithm(ptst);
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  reading, processing, writing plans...");
		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
		plans.printPlansCount();
		plans.runAlgorithms();
		plansWriter.write();
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  finishing algorithms... ");
//		psta.writeSubtourTripCntVsModeCnt("output/TripsPerSubtourVsModeCnt.txt");
//		psta.writeSubtourDistVsModeCnt("output/SubtourDistVsModeCnt.txt");
//		psta.writeSubtourTripCntVsSubtourCnt("output/SubtourTripCntVsSubtourCnt.txt");
//		psta.writeSubtourDistVsModeDistSum("output/SubtourDistVsModeDistSum.txt");
//		pidst.close();
//		ptst.close();
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		System.out.println("  writing network xml file... ");
//		NetworkWriter net_writer = new NetworkWriter(network);
//		net_writer.write();
//		System.out.println("  done.");
//
//		System.out.println("  writing config xml file... ");
//		ConfigWriter config_writer = new ConfigWriter(Gbl.getConfig());
//		config_writer.write();
//		System.out.println("  done.");

		System.out.println("done.");
		System.out.println();
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		Gbl.startMeasurement();
		Gbl.printElapsedTime();
		System.out.println("Funziona?");
		run();
		System.out.println("Funziona!!!!");
		Gbl.printElapsedTime();
	}
}
