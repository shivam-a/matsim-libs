/* *********************************************************************** *
 * project: org.matsim.*
 * CottbusMain
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package playground.jbischoff.BAsignals;

import org.matsim.core.controler.Controler;

import playground.jbischoff.BAsignals.controler.JBSignalControllerListenerFactory;


/**
 * @author dgrether
 *
 */
public class CottbusMain {
	

//	private String config = JbBaPaths.BASIMH+"scenario-slv/cottbusConfig.xml";
//	private String config = "/media/data/work/repos/shared-svn/studies/dgrether/cottbus/Cottbus-BA/scenario-lsa/cottbusConfig.xml";
	private String config = "/media/data/work/repos/shared-svn/studies/dgrether/cottbus/Cottbus-BA/scenario-slv/dg_cottbus_config.xml";

	public void runCottbus(){
		System.err.println("here");
		Controler controler = new Controler(config);
		JBSignalControllerListenerFactory fact = new JBSignalControllerListenerFactory();
		controler.setSignalsControllerListenerFactory(fact);
		controler.setOverwriteFiles(true);
		controler.run();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CottbusMain().runCottbus();
	}


}
