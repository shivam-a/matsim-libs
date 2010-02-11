/* *********************************************************************** *
 * project: org.matsim.*
 * RouterVis.java
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

package org.matsim.vis.routervis;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.vis.netvis.NetVis;
import org.matsim.vis.netvis.VisConfig;
import org.matsim.vis.routervis.multipathrouter.CLogitRouter;
import org.matsim.vis.routervis.multipathrouter.PSLogitRouter;

/**
 * RouterVis is a package for router visualization. It creates NetVis compatible
 * files which shows the graph exploration procedure of a LeastCostPathCalculator.
 * To visualize the graph exploration of a particular LeastCostPathColculator it has to implement
 * VisLeastCostPathCalculator.
 *
 * @author laemmel
 */
public class RouterVis {

	private static final Logger log = Logger.getLogger(RouterVis.class);

	private final RouterNetStateWriter writer;

	private VisLeastCostPathCalculator router;

	public RouterVis(final Network network, final TravelCost costCalculator,
			final TravelTime timeCalculator, final Class<? extends VisLeastCostPathCalculator> router, Config config){
		this.writer = getNetStateWriter(network, config);
		final Class[] prototypeConstructor = { NetworkLayer.class,
				TravelCost.class, TravelTime.class, RouterNetStateWriter.class};
		Exception ex = null;
		try {
			this.router = router.getConstructor(prototypeConstructor).newInstance(new Object [] {network, costCalculator, timeCalculator, this.writer});
		} catch (final InstantiationException e) {
			ex = e;
		} catch (final IllegalAccessException e) {
			ex = e;
		} catch (final IllegalArgumentException e) {
			ex = e;
		} catch (final InvocationTargetException e) {
			ex = e;
		} catch (final SecurityException e) {
			ex = e;
		} catch (final NoSuchMethodException e) {
			ex = e;
		}
		if (ex != null) {
			throw new RuntimeException(
					"Cannot instantiate link from prototype, this should never happen, but never say never!",
					ex);
		}
	}

/**
 * Calculates the cheapest route from 'fromNode' to 'toNode' at starting time 'time' and
 * generates a NetVis file to track Dijkstra's graph exploration in the following manner:
 * While expanding a node Dijkstra's algorithm checks weather the expanded node belongs
 * to the shortest path to the succeeding node (in the sub-graph that has been explored so far).
 * If so, the link from the expanded node to the succeeding node will be colored green and
 * red otherwise. If the expanded node belongs to the shortest path to the succeeding node
 * a possible other 'green' link to this node will be colored red.
 *
 * @param fromNode
 * @param toNode
 * @param time
 *
 * @return route
 */
	public Path runRouter(final Node fromNode, final Node toNode, final double time){
		final Path path = this.router.calcLeastCostPath(fromNode, toNode, time);

		try {
			this.writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	private RouterNetStateWriter getNetStateWriter(final Network network, Config config) {
		final String snapshotFile = config.controler().getOutputDirectory() + "/Snapshot";

		int buffers = network.getLinks().size();
		final String buffString = config.findParam("vis", "buffersize");
		if (buffString == null) {
			buffers = Math.max(5, Math.min(50000/buffers, 100));
		} else {
			buffers = Integer.parseInt(buffString);
		}

		final VisConfig myVisConfig = VisConfig.newDefaultConfig();
		myVisConfig.set(VisConfig.DELAY, "100");

		final RouterNetStateWriter netStateWriter = new RouterNetStateWriter(network, config.network().getInputFile(), myVisConfig, snapshotFile, 1, buffers);
		netStateWriter.open();
		return netStateWriter;
	}

	public static void main(final String [] args){

		Id fromNodeId;
		Id toNodeId;

		log.info("starting RouterVis demo");
		final String testConfigFile = "./examples/siouxfalls/config.xml";

		Class<? extends VisLeastCostPathCalculator> router = VisDijkstra.class;
		String outputDirSuffix = "/DijkstraRouter/";

		if (args.length == 4) {

			if (args[3].equals("PSLogitRouter")) {
				router = PSLogitRouter.class;
				outputDirSuffix = "VisDijkstra/";
			} else if (args[3].equals("CLogitRouter")) {
				router = CLogitRouter.class;
			} else if (args[3].equals("DijkstraRouter")) {
				router = VisDijkstra.class;
			} else {
				throw new RuntimeException("No such router: " + args[3] + "!");
			}
			outputDirSuffix = "/" + args[3];
		}

		Config config = null;
		if (args.length >= 3) {
			config = Gbl.createConfig(new String[]{args[0], "config_v1.dtd"});
			fromNodeId = new IdImpl(args[1]);
			toNodeId = new IdImpl(args[2]);

		}	else {
			log.info(" reading default config file: " + testConfigFile);
			config = Gbl.createConfig(new String[] {testConfigFile});
			fromNodeId = new IdImpl("13");
			toNodeId = new IdImpl("7");
		}
		log.info(" done.");

		config.controler().setOutputDirectory(config.controler().getOutputDirectory() + outputDirSuffix);

		log.info("  reading the network...");
		Scenario scenario = new ScenarioImpl();
		Network network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(config.network().getInputFile());
		log.info("  done.");

		log.info("  creating output dir if needed");
		final File outputDir = new File(config.controler().getOutputDirectory());

		if (!outputDir.exists()){
			outputDir.mkdirs();
		} else if (outputDir.list().length > 0) {
			log.error("The output directory " + outputDir + " exists already but has files in it! Please delete its content or the directory and start again. We will not delete or overwrite any existing files.");
			System.exit(-1);
		}
		log.info( "done");

		log.info("  creating RouterVis object.");
		final TravelTime costCalc = new FreespeedTravelTimeCost(scenario.getConfig().charyparNagelScoring());
		final RouterVis vis = new RouterVis(network, (TravelCost) costCalc, costCalc, router, config);
		log.info("  done.");

		log.info("  running RouterVis.");
		final Node fromNode = network.getNodes().get(new IdImpl(fromNodeId.toString()));
		final Node toNode = network.getNodes().get(new IdImpl(toNodeId.toString()));
		vis.runRouter(fromNode, toNode,0.0);
		log.info("  done.");

		log.info("  starting NetVis.");
		final String [] visargs = {config.controler().getOutputDirectory() + "/Snapshot"};
		Gbl.reset();
		NetVis.main(visargs);
		log.info("  done.");
	}

}
