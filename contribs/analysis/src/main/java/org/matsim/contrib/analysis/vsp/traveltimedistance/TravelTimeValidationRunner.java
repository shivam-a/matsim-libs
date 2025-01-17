/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

/**
 * 
 */
package org.matsim.contrib.analysis.vsp.traveltimedistance;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.ParallelEventsManager;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.IOUtils;

/**
 * @author jbischoff
 *
 */
public class TravelTimeValidationRunner {

	private final Network network;
	private final String eventsFile;
	private final TravelTimeDistanceValidator travelTimeValidator;
	private final int numberOfTripsToValidate;
	private final Set<Id<Person>> populationIds;
	private final String outputfolder;
	private final int eventsQueueSize = 1048576 * 32;
	private final Tuple<Double, Double> timeWindow;
	private final Predicate<CarTrip> tripFilter;

	public TravelTimeValidationRunner(Network network, Set<Id<Person>> populationIds, String eventsFile,
			String outputFolder, TravelTimeDistanceValidator travelTimeValidator, int numberOfTripsToValidate) {
		this(network, populationIds, eventsFile, outputFolder, travelTimeValidator,
				numberOfTripsToValidate, new Tuple<>((double) 0, (double) 3600 * 30), null);
	}

	public TravelTimeValidationRunner(Network network, Set<Id<Person>> populationIds, String eventsFile,
			String outputFolder, TravelTimeDistanceValidator travelTimeValidator) {
		this(network, populationIds, eventsFile, outputFolder, travelTimeValidator,
				Integer.MAX_VALUE, new Tuple<>((double) 0, (double) 3600 * 30), null);
	}

	public TravelTimeValidationRunner(Network network, Set<Id<Person>> populationIds, String eventsFile,
			String outputFolder, TravelTimeDistanceValidator travelTimeValidator, int numberOfTripsToValidate,
			Tuple<Double, Double> timeWindow) {
		this(network, populationIds, eventsFile, outputFolder, travelTimeValidator, numberOfTripsToValidate, timeWindow, null);
	}

	public TravelTimeValidationRunner(Network network, Set<Id<Person>> populationIds, String eventsFile,
	                                  String outputFolder, TravelTimeDistanceValidator travelTimeValidator, int numberOfTripsToValidate,
	                                  Tuple<Double, Double> timeWindow, Predicate<CarTrip> tripFilter) {
		this.network = network;
		this.eventsFile = eventsFile;
		this.travelTimeValidator = travelTimeValidator;
		this.numberOfTripsToValidate = numberOfTripsToValidate;
		this.outputfolder = outputFolder;
		this.populationIds = populationIds;
		this.timeWindow = timeWindow;
		this.tripFilter = tripFilter;

		if (timeWindow.getFirst() > timeWindow.getSecond()) {
			throw new IllegalArgumentException(
					"Time window is not valid (the first element should be smaller than the second element in the Time Window Tuple)");
		}
	}


	public void run() throws InterruptedException {
		ParallelEventsManager eventManager = new ParallelEventsManager(false, eventsQueueSize);
//		EventsManager events = EventsUtils.createEventsManager();
		CarTripsExtractor carTripsExtractor = new CarTripsExtractor(populationIds, network);
		eventManager.addHandler(carTripsExtractor);
		eventManager.initProcessing();
		new MatsimEventsReader(eventManager).readFile(eventsFile);
		List<CarTrip> carTrips = carTripsExtractor.getTrips();
		System.out.println("there are " + carTrips.size() + " car trips");
		Collections.shuffle(carTrips, MatsimRandom.getRandom());
		int i = 0;
		for (CarTrip trip : carTrips) {

			if (tripFilter != null && !tripFilter.test(trip))
				continue;

			if (trip.getDepartureTime() >= timeWindow.getFirst() && trip.getDepartureTime() <= timeWindow.getSecond()) {
				Tuple<Double, Double> timeDistance = travelTimeValidator.getTravelTime(trip);
				double validatedTravelTime = timeDistance.getFirst();
				trip.setValidatedTravelTime(validatedTravelTime);
				trip.setValidatedTravelDistance(timeDistance.getSecond());
				i++;
				Thread.sleep(100);
			}

			if (i >= numberOfTripsToValidate) {
				break;
			}
		}
		writeTravelTimeValidation(outputfolder, carTrips);

	}

	private void writeTravelTimeValidation(String folder, List<CarTrip> trips) {
		BufferedWriter bw = IOUtils.getBufferedWriter(folder + "/validated_trips.csv");
		XYSeriesCollection times = new XYSeriesCollection();
		XYSeriesCollection distances = new XYSeriesCollection();

		XYSeries distancess = new XYSeries("distances", true, true);
		XYSeries timess = new XYSeries("times", true, true);
		times.addSeries(timess);
		distances.addSeries(distancess);
		try {
			bw.append(
					"agent;departureTime;fromX;fromY;toX;toY;traveltimeActual;traveltimeValidated;traveledDistance;validatedDistance");
			for (CarTrip trip : trips) {
				if (trip.getValidatedTravelTime() != null) {
					bw.newLine();
					bw.append(trip.toString());
					timess.add(trip.getActualTravelTime(), trip.getValidatedTravelTime());
					distancess.add(trip.getTravelledDistance(), trip.getValidatedTravelDistance());
				}
			}

			bw.flush();
			bw.close();
			final JFreeChart chart2 = ChartFactory.createScatterPlot("Travel Times", "Simulated travel time [s]",
					"Validated travel time [s]", times);
			final JFreeChart chart = ChartFactory.createScatterPlot("Travel Distances", "Simulated travel distance [m]",
					"Validated travel distance [m]", distances);

			NumberAxis yAxis = (NumberAxis) ((XYPlot) chart2.getPlot()).getRangeAxis();
			NumberAxis xAxis = (NumberAxis) ((XYPlot) chart2.getPlot()).getDomainAxis();
			NumberAxis yAxisd = (NumberAxis) ((XYPlot) chart.getPlot()).getRangeAxis();
			NumberAxis xAxisd = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis();
			yAxisd.setUpperBound(xAxisd.getUpperBound());
			yAxis.setUpperBound(xAxis.getUpperBound());
			yAxis.setTickUnit(new NumberTickUnit(500));
			xAxis.setTickUnit(new NumberTickUnit(500));

			XYAnnotation diagonal = new XYLineAnnotation(xAxis.getRange().getLowerBound(),
					yAxis.getRange().getLowerBound(), xAxis.getRange().getUpperBound(),
					yAxis.getRange().getUpperBound());
			((XYPlot) chart2.getPlot()).addAnnotation(diagonal);

			XYAnnotation diagonald = new XYLineAnnotation(xAxisd.getRange().getLowerBound(),
					yAxisd.getRange().getLowerBound(), xAxisd.getRange().getUpperBound(),
					yAxisd.getRange().getUpperBound());
			((XYPlot) chart.getPlot()).addAnnotation(diagonald);

			ChartUtils.writeChartAsPNG(new FileOutputStream(folder + "/validated_traveltimes" + ".png"), chart2, 1500,
					1500);
			ChartUtils.writeChartAsPNG(new FileOutputStream(folder + "/validated_traveldistances.png"), chart, 1500,
					1500);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
