/* *********************************************************************** *
 * project: org.matsim.*
 * InitialIdentifierImpl.java
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

package playground.christoph.withinday.replanning.identifiers;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PersonAgent;
import org.matsim.ptproject.qsim.QSim;

import playground.christoph.withinday.mobsim.WithinDayPersonAgent;
import playground.christoph.withinday.replanning.identifiers.interfaces.InitialIdentifier;

public class InitialIdentifierImpl extends InitialIdentifier {

	protected QSim qsim;

	// use the Factory!
	/*package*/ InitialIdentifierImpl(QSim qsim) {
		this.qsim = qsim;
	}
		
	public Set<PersonAgent> getAgentsToReplan(double time, Id withinDayReplannerId) {
		Set<PersonAgent> agentsToReplan = new HashSet<PersonAgent>();
		
		for (MobsimAgent mobsimAgent : this.qsim.getAgents()) {
			if (mobsimAgent instanceof WithinDayPersonAgent) {
				WithinDayPersonAgent withinDayPersonAgent = (WithinDayPersonAgent) mobsimAgent;
				
				if (withinDayPersonAgent.getReplannerAdministrator().getWithinDayReplannerIds().contains(withinDayReplannerId)) {
					agentsToReplan.add(withinDayPersonAgent);
				}
			}
		}
		
		return agentsToReplan;
	}

}
