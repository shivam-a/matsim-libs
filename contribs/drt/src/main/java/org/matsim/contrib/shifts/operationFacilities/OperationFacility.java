package org.matsim.contrib.shifts.operationFacilities;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.ev.infrastructure.Charger;
import org.matsim.facilities.Facility;

import java.util.Set;

/**
 * @author nkuehnel
 */
public interface OperationFacility extends Identifiable<OperationFacility>, Facility {

	// separate the fixed specification data (preferably immutable) from the mutable state (registered vehicles)

	int getCapacity();

    boolean hasCapacity();

    boolean register(Id<DvrpVehicle> id);

    boolean deregisterVehicle(Id<DvrpVehicle> id);

    Id<Charger> getCharger();

    OperationFacilityType getType();

    Set<Id<DvrpVehicle>> getRegisteredVehicles();

    void reset();
}