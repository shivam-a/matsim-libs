package org.matsim.contrib.simulated_annealing;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.shifts.shift.DrtShift;

import java.util.List;
import java.util.Random;

/**
 * Used for changing/modifying the Individuals SupplySchedule/DriverShift.
 */
public class Perturbation {
	static Random random = SimulatedAnnealing.random;
	private static final Logger log = Logger.getLogger(Perturbation.class);
//    static Random randomSeed = ShiftGeneticAlgorithm.randomSeed;
	/**
	 * Since all methods are static.
	 */
	private Perturbation() {
	}

	/**
	 * Adds a random SupplySchedule to the Individual to increase the supplyOfDrivers
	 * @param individual Individual that will be modified
	 * @return modified Individual
	 */
	static protected Individual insertSingleSAShift(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		SAShift newSAShift = null;
		if (newSAShiftList.size() > 0) {
			newSAShift = moveSAShiftTimings(moveSABreakCorridor(perturbedIndividual)).getShifts().get(random.nextInt(newSAShiftList.size()));
			newSAShift.setId(Id.create(newSAShift.getId() + "_" + random.nextInt(), DrtShift.class));
			newSAShift.encodeShiftV2();
		}
		newSAShiftList.add(newSAShift);
		perturbedIndividual.setShifts(newSAShiftList);
		return perturbedIndividual;
	}

	public static Individual insertSAShifts (Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		if (perturbedIndividual.getShifts().size() < SimulatedAnnealing.SHIFTS_MAXIMUM) {
			for (int i = 0; i < random.nextInt(SimulatedAnnealing.SHIFTS_INSERTION); i++) {
				perturbedIndividual = insertSingleSAShift(perturbedIndividual);
			}
		}
		log.info("insert new shifts perturbation has been used");
		return perturbedIndividual;
	}

	/**
	 * Removes a random SupplySchedule from any random position to decrease the supplyOfDrivers
	 * @param individual Individual that will be modified
	 * @return modified Individual
	 */
	static protected Individual removeSingleSAShift(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		SAShift newSAShift;
		if (newSAShiftList.size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShift = newSAShiftList.get(random.nextInt(newSAShiftList.size()));
			newSAShiftList.remove(newSAShift);
		}
		perturbedIndividual.setShifts(newSAShiftList);
		return perturbedIndividual;
	}

	public static Individual removeSAShifts (Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < random.nextInt(SimulatedAnnealing.SHIFTS_REMOVAL); i++) {
				perturbedIndividual = removeSingleSAShift(perturbedIndividual);
			}
		}
		log.info("remove random shifts perturbation has been used");
		return perturbedIndividual;
	}

	public static Individual moveSAShiftTimings(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					// difference between the end of schedule and end of shift
					// find any value in between the movableDistanceSize and increment all values
					double moveAhead;
					double newStart = oldSAShift.getStartTime();
					double newEnd = oldSAShift.getEndTime();
					double newEarliestStart = oldSAShift.getSABreak().getEarliestStart();
					double newLatestEnd = oldSAShift.getSABreak().getLatestEnd();
					double movableDistanceSize = (SimulatedAnnealing.END_SCHEDULE_TIME - SimulatedAnnealing.START_SCHEDULE_TIME + SimulatedAnnealing.TIME_INTERVAL) - (newEnd - newStart) - (2 * SimulatedAnnealing.SHIFT_TIMINGS_BUFFER);
					if ((movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) > 0) {
						moveAhead = random.nextInt((int) (movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL));
						newStart = moveAhead * SimulatedAnnealing.TIME_INTERVAL + SimulatedAnnealing.SHIFT_TIMINGS_BUFFER;
						newEnd = oldSAShift.getEndTime() - oldSAShift.getStartTime() + newStart;
						newEarliestStart = newStart + oldSAShift.getSABreak().getEarliestStart() - oldSAShift.getStartTime();
						newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart());
					}
					SABreak newSABreak = new SABreak(newEarliestStart, newLatestEnd, oldSAShift.getSABreak().getDuration());
					newSAShift = new SAShift(oldSAShift.getId(), newStart, newEnd);
					newSAShift.setSABreak(newSABreak);
					newSAShift.encodeShiftV2();
				}
				newSAShiftList.add(newSAShift);
			}
		}
		perturbedIndividual.setShifts(newSAShiftList);
		log.info("move shift timings perturbation has been used");
		return perturbedIndividual;
	}

	public static Individual moveSABreakCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					// difference between the end of schedule and end of shift
					double movableDistanceSize = (oldSAShift.getEndTime() - oldSAShift.getStartTime()) - (2 * SimulatedAnnealing.BREAK_CORRIDOR_BUFFER);
					// find any value in between the movableDistanceSize and increment all values
					double newEarliestStart = oldSAShift.getSABreak().getEarliestStart(), newLatestEnd = oldSAShift.getSABreak().getLatestEnd(), moveAhead;
					if ((movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) > 0) {
						moveAhead = random.nextInt((int) (movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL));
						newEarliestStart = oldSAShift.getStartTime() + SimulatedAnnealing.BREAK_CORRIDOR_BUFFER + moveAhead * SimulatedAnnealing.TIME_INTERVAL;
						newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart());
					}
					SABreak newSABreak = new SABreak(newEarliestStart, newLatestEnd, oldSAShift.getSABreak().getDuration());
					newSAShift = new SAShift(oldSAShift.getId(), oldSAShift.getStartTime(), oldSAShift.getEndTime());
					newSAShift.setSABreak(newSABreak);
					newSAShift.encodeShiftV2();

				}
				newSAShiftList.add(newSAShift);
			}
		}
		perturbedIndividual.setShifts(newSAShiftList);
		log.info("move break corridor perturbation has been used");
		return perturbedIndividual;
	}
	/*public static Individual increaseSABreakCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					// difference between the end of schedule and end of shift
					double additionalEndDistanceSize = oldSAShift.getEndTime() - oldSAShift.getSABreak().getLatestEnd();
					double additionalStartDistanceSize = oldSAShift.getSABreak().getEarliestStart() - oldSAShift.getStartTime();
					// find any value in between the movableDistanceSize and increment all values
					double newEarliestStart = oldSAShift.getSABreak().getEarliestStart(), newLatestEnd = oldSAShift.getSABreak().getLatestEnd();
					if ((newLatestEnd - newEarliestStart) < SimulatedAnnealing.BREAK_CORRIDOR_MAXIMUM_LENGTH) {
						if (additionalEndDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.BREAK_CORRIDOR_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
							newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart()) + SimulatedAnnealing.TIME_INTERVAL;
						} else if (additionalStartDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.BREAK_CORRIDOR_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
							newEarliestStart = newEarliestStart - SimulatedAnnealing.TIME_INTERVAL;
						}
					}
					SABreak newSABreak = new SABreak(newEarliestStart, newLatestEnd, oldSAShift.getSABreak().getDuration());
					newSAShift = new SAShift(oldSAShift.getId(), oldSAShift.getStartTime(), oldSAShift.getEndTime());
					newSAShift.setSABreak(newSABreak);
					newSAShift.encodeShiftV2();
				}
				newSAShiftList.add(newSAShift);
			}
		}
		perturbedIndividual.setShifts(newSAShiftList);
		log.info("increase break corridor size perturbation has been used");
		return perturbedIndividual;
	}*/

	/*public static Individual decreaseSABreakCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					// difference between the end of schedule and end of shift
					// find any value in between the movableDistanceSize and increment all values
					double newEarliestStart = oldSAShift.getSABreak().getEarliestStart(), newLatestEnd = oldSAShift.getSABreak().getLatestEnd();
					if ((newLatestEnd - newEarliestStart) >= SimulatedAnnealing.BREAK_CORRIDOR_MINIMUM_LENGTH) {
						newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart()) - SimulatedAnnealing.TIME_INTERVAL;
					}
					SABreak newSABreak = new SABreak(newEarliestStart, newLatestEnd, oldSAShift.getSABreak().getDuration());
					newSAShift = new SAShift(oldSAShift.getId(), oldSAShift.getStartTime(), oldSAShift.getEndTime());
					newSAShift.setSABreak(newSABreak);
					newSAShift.encodeShiftV2();
				}
				newSAShiftList.add(newSAShift);
			}
		}
		perturbedIndividual.setShifts(newSAShiftList);
		log.info("decrease break corridor size perturbation has been used");
		return perturbedIndividual;
	}*/

	public static Individual increaseSAShiftTimings(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					// difference between the end of schedule and end of shift
					double additionalEndDistanceSize = SimulatedAnnealing.END_SCHEDULE_TIME - SimulatedAnnealing.SHIFT_TIMINGS_BUFFER - oldSAShift.getEndTime() ;
					double additionalStartDistanceSize = oldSAShift.getStartTime() - SimulatedAnnealing.START_SCHEDULE_TIME + SimulatedAnnealing.SHIFT_TIMINGS_BUFFER;
					// find any value in between the movableDistanceSize and increment all values
					double newStart = oldSAShift.getStartTime();
					double newEnd = oldSAShift.getEndTime();
					if ((newEnd - newStart) <= SimulatedAnnealing.SHIFT_TIMINGS_MAXIMUM_LENGTH) {
						if (additionalEndDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.SHIFT_TIMINGS_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
							newEnd = newStart + (oldSAShift.getEndTime() - oldSAShift.getStartTime()) + SimulatedAnnealing.TIME_INTERVAL;
						} else if (additionalStartDistanceSize / SimulatedAnnealing.TIME_INTERVAL >= (SimulatedAnnealing.SHIFT_TIMINGS_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
							newStart = newStart - SimulatedAnnealing.TIME_INTERVAL;
						}
					}
					SABreak newSABreak = new SABreak(oldSAShift.getSABreak().getEarliestStart(), oldSAShift.getSABreak().getLatestEnd(), oldSAShift.getSABreak().getDuration());
					newSAShift = new SAShift(oldSAShift.getId(), newStart, newEnd);
					newSAShift.setSABreak(newSABreak);
					newSAShift.encodeShiftV2();
				}
				newSAShiftList.add(newSAShift);
			}
		}
		perturbedIndividual.setShifts(newSAShiftList);
		log.info("increase shift timings perturbation has been used");
		return perturbedIndividual;
	}

	public static Individual decreaseSAShiftTimings(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					double newStart = oldSAShift.getStartTime();
					double newEnd = oldSAShift.getEndTime();
					double newLatestEnd = oldSAShift.getSABreak().getLatestEnd();
					double newEarliestStart = oldSAShift.getSABreak().getEarliestStart();
					if (((newEnd - newStart) > SimulatedAnnealing.SHIFT_TIMINGS_MINIMUM_LENGTH) && ((newEnd - newLatestEnd) > SimulatedAnnealing.BREAK_CORRIDOR_BUFFER)) {
						newEnd = newStart + (oldSAShift.getEndTime() - oldSAShift.getStartTime()) - SimulatedAnnealing.TIME_INTERVAL;
					}
					else if (((newEnd - SimulatedAnnealing.TIME_INTERVAL - newStart) > SimulatedAnnealing.SHIFT_TIMINGS_MINIMUM_LENGTH) && ((newEarliestStart - SimulatedAnnealing.TIME_INTERVAL - newStart) > SimulatedAnnealing.BREAK_CORRIDOR_BUFFER)) {
						newStart = newStart + SimulatedAnnealing.TIME_INTERVAL;
					}
					SABreak newSABreak = new SABreak(oldSAShift.getSABreak().getEarliestStart(), oldSAShift.getSABreak().getLatestEnd(), oldSAShift.getSABreak().getDuration());
					newSAShift = new SAShift(oldSAShift.getId(), newStart, newEnd);
					newSAShift.setSABreak(newSABreak);
					newSAShift.encodeShiftV2();
				}
				newSAShiftList.add(newSAShift);
			}
		}
		perturbedIndividual.setShifts(newSAShiftList);
		log.info("decrease shift timings perturbation has been used");
		return perturbedIndividual;
	}
}

