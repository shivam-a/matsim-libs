package org.matsim.contrib.simulated_annealing;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.shifts.shift.DrtShift;

import java.util.*;

/**
 * An map object that represents a binary sequence of availability of drivers per time stamp
 */
public class SAShift {
	private SABreak saBreak;
	private Map<Double, Double> encodedShift;
	private Id<DrtShift> id;
	private double start;
	private double end;
	private static final Logger log = Logger.getLogger(SAShift.class);
	public Id<DrtShift> getId() {
		return id;
	}

	public void setId(Id<DrtShift> id) {
		this.id = id;
	}

	public SAShift() {
	}


	public void setStartTime(double time) {
		if ((time % 1) != 0) {
			throw new RuntimeException("Cannot use fractions of seconds!");
		}
		this.start = time;
	}



	public void setEndTime(double time) {
		if ((time % 1) != 0) {
			throw new RuntimeException("Cannot use fractions of seconds!");
		}
		this.end = time;
	}


	public double getStartTime() {
		return start;
	}


	public double getEndTime() {
		return end;
	}

	public String toString() {
		return "Shift " + id.toString() + " ["+start+"-"+end+"]";
	}

	public SABreak getSABreak() {
		return saBreak;
	}

	public void setSABreak(SABreak saBreak) {
		this.saBreak = saBreak;
	}

	public SAShift deepCopy() {
		SAShift newSAShift = new SAShift();
		Map<Double, Double> newEncodedShift = new LinkedHashMap<>();
		this.getEncodedShift().forEach(newEncodedShift::put);
		newSAShift.setId(this.getId());
		newSAShift.setStartTime(this.getStartTime());
		newSAShift.setEndTime(this.getEndTime());
		newSAShift.setSABreak(this.getSABreak());
		newSAShift.setEncodedShift(newEncodedShift);
		return newSAShift;
	}

	public Map<Double, Double> getEncodedShift() {
		return encodedShift;
	}

	public void setEncodedShift(Map<Double, Double> encodedShift) {
		this.encodedShift = encodedShift;
	}

	public SAShift(Id<DrtShift> id, double start, double end) {
		this.id = id;
		this.start = start;
		this.end = end;
		encodeShiftV2();
	}

	public void encodeShiftV2() {
		encodedShift = new LinkedHashMap<>();
		initializeEncoding(encodedShift);
		for (var entry: encodedShift.entrySet()) {
			if (entry.getKey() > start && entry.getKey() <= end) {
				encodedShift.put(entry.getKey(), 1.0);
			}
		}
		List<Double> timeBins = new LinkedList<>(encodedShift.keySet());
		for (double timeBin: timeBins) {
			if (getSABreak() != null) {
				if (timeBin > getSABreak().getEarliestStart() && timeBin <= getSABreak().getLatestEnd())
					encodedShift.put(timeBin, 2.0);
			}
		}
	}

	public SAShift decodeShiftV2() {
		SAShift decodedSAShift = new SAShift();
		SABreak saBreak;
		Double start = this.getEncodedShift().entrySet().stream().filter(entry -> entry.getValue() == 1.0).map(Map.Entry::getKey).findFirst().orElse(null);
		Double end = this.getEncodedShift().entrySet().stream().filter(entry -> entry.getValue().intValue() == 1.0).map(Map.Entry::getKey).reduce((__, last) -> last).orElse(null);
		Double earliestStart = this.deepCopy().getEncodedShift().entrySet().stream().filter(entry -> entry.getValue() == 2.0).map(Map.Entry::getKey).findFirst().orElse(null);
		Double latestEnd = this.deepCopy().getEncodedShift().entrySet().stream().filter(entry -> entry.getValue().intValue() == 2.0).map(Map.Entry::getKey).reduce((__, last) -> last).orElse(null);
		double breakDuration = 0;
		if (end != null && start != null && latestEnd != null && earliestStart != null) {
			start = start - SimulatedAnnealing.TIME_INTERVAL;
			earliestStart = earliestStart - SimulatedAnnealing.TIME_INTERVAL;
			if ((end > start) && (latestEnd > earliestStart)) {
				if (((latestEnd + SimulatedAnnealing.TIME_INTERVAL - earliestStart) > 0) &&
						((latestEnd + SimulatedAnnealing.TIME_INTERVAL - earliestStart) <= SimulatedAnnealing.BREAK_CORRIDOR_MINIMUM_LENGTH) &&
						((end - start) > SimulatedAnnealing.SHIFT_TIMINGS_MINIMUM_LENGTH) &&
						((end - start) <= SimulatedAnnealing.SHIFT_TIMINGS_MAXIMUM_LENGTH)) {
					breakDuration = 1800;
				} else if (((latestEnd + SimulatedAnnealing.TIME_INTERVAL - earliestStart) > 0) &&
						((latestEnd + SimulatedAnnealing.TIME_INTERVAL - earliestStart) <= SimulatedAnnealing.BREAK_CORRIDOR_MINIMUM_LENGTH) &&
						((end - start) > 0) &&
						((end - start) <= SimulatedAnnealing.SHIFT_TIMINGS_MINIMUM_LENGTH)) {
					breakDuration = 0;
				}
			}
			saBreak = new SABreak(earliestStart, latestEnd, breakDuration);
			decodedSAShift.setStartTime(start);
			decodedSAShift.setEndTime(end);
			decodedSAShift.setSABreak(saBreak);
			decodedSAShift.setId(this.getId());
		}
		else throw new NullPointerException("Shift is null");
		return decodedSAShift;
	}
	/**
	 * Makes the initial values of all time stamps in a day's supply (number of available drivers) schedule to 0
	 * (no work for any driver)
	 * @param encodedShift a map where keys are time stamps and values are day's supply scheduleGeneSequence
	 */
	public void initializeEncoding (Map<Double, Double> encodedShift) {
		double timeInterval = SimulatedAnnealing.TIME_INTERVAL;
		double startScheduleTime = SimulatedAnnealing.START_SCHEDULE_TIME;
		double endScheduleTime = SimulatedAnnealing.END_SCHEDULE_TIME;
		for (double i = startScheduleTime; i < endScheduleTime; i += timeInterval) {
			encodedShift.put(i, 0.0);
		}
	}
}
