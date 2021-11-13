package org.matsim.contrib.simulated_annealing;

import org.apache.log4j.Logger;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerRequestRejectedEvent;
import org.matsim.contrib.dvrp.passenger.PassengerRequestRejectedEventHandler;
import org.matsim.core.controler.listener.ControlerListener;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import java.util.LinkedHashMap;
import java.util.Map;

public class RejectionTracker implements PassengerRequestRejectedEventHandler, DrtRequestSubmittedEventHandler, MobsimBeforeSimStepListener, ControlerListener {
    private final Map<Double, Double> rejectionRatePerTimeBin = new LinkedHashMap<>();
	private final Map<Double, Double> rejectionsPerTimeBin = new LinkedHashMap<>();
	private final Map<Double, Double> submittedPerTimeBin = new LinkedHashMap<>();
	private double submittedCounter = 0;
	private double rejectedCounter = 0;
	private final static double timeBinSize = SimulatedAnnealing.TIME_INTERVAL;
	private final static Logger logger = Logger.getLogger(RejectionTracker.class);

	public Map<Double, Double> getRejectionsPerTimeBin() {
		return rejectionsPerTimeBin;
	}

    public Map<Double, Double> getRejectionRatePerTimeBin() {
        return rejectionRatePerTimeBin;
    }

    @Override
    public void handleEvent(PassengerRequestRejectedEvent passengerRequestRejectedEvent) {
        rejectedCounter++;
    }

    @Override
    public void reset(int iteration) {
        PassengerRequestRejectedEventHandler.super.reset(iteration);
    }

    @Override
    public void handleEvent(DrtRequestSubmittedEvent drtRequestSubmittedEvent) {
        submittedCounter++;
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent mobsimBeforeSimStepEvent) {
        if (mobsimBeforeSimStepEvent.getSimulationTime() % timeBinSize == 0) {
            double rejectionRate;
            if (Double.isNaN(rejectedCounter / submittedCounter))
                rejectionRate = 0;
            else rejectionRate = rejectedCounter / submittedCounter;
            double bin = mobsimBeforeSimStepEvent.getSimulationTime() / timeBinSize;
            rejectionRatePerTimeBin.put(bin, rejectionRate);
            rejectionsPerTimeBin.put(bin, rejectedCounter);
            submittedPerTimeBin.put(bin, submittedCounter);
			logger.info("The time bin: " + bin + " ; The rejection rate: " + rejectionRate);
			logger.info("The time bin: " + bin + " ; The rejected requests: " + rejectedCounter);
			logger.info("The time bin: " + bin + " ; The submitted requests: " + submittedCounter);
            rejectedCounter = 0;
            submittedCounter = 0;
        }
    }

	public Map<Double, Double> getSubmittedPerTimeBin() {
		return submittedPerTimeBin;
	}
}
