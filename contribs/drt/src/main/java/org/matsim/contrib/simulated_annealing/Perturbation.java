package org.matsim.contrib.simulated_annealing;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.shifts.shift.DrtShift;

import java.util.LinkedList;
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
			newSAShift = moveSAShiftCorridor(moveSABreakCorridor(perturbedIndividual)).getShifts().get(random.nextInt(newSAShiftList.size()));
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
		log.info("insert new shift perturbation has been used");
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
		if (newSAShiftList.size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShift = newSAShiftList.get(random.nextInt(newSAShiftList.size()));
			newSAShiftList.remove(newSAShift);
		}
		perturbedIndividual.setShifts(newSAShiftList);
		return perturbedIndividual;
	}

	public static Individual removeSAShifts (Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < random.nextInt(SimulatedAnnealing.SHIFTS_REMOVAL); i++) {
				perturbedIndividual = removeSingleSAShift(perturbedIndividual);
			}
		}
		log.info("remove random shift perturbation has been used");
		return perturbedIndividual;
	}

	public static Individual moveSAShiftCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
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
					double movableDistanceSize = SimulatedAnnealing.END_SCHEDULE_TIME - SimulatedAnnealing.START_SCHEDULE_TIME - (oldSAShift.getEndTime() - oldSAShift.getStartTime()) - (2 * SimulatedAnnealing.SHIFT_CORRIDOR_BUFFER);
					// find any value in between the movableDistanceSize and increment all values
					double moveAhead, newStart = oldSAShift.getStartTime(), newEnd = oldSAShift.getEndTime(), newEarliestStart = oldSAShift.getSABreak().getEarliestStart(), newLatestEnd = oldSAShift.getSABreak().getLatestEnd();
					if (movableDistanceSize > 0) {
						moveAhead = random.nextInt((int) (movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) - 1);
						newStart = moveAhead * SimulatedAnnealing.TIME_INTERVAL + SimulatedAnnealing.SHIFT_CORRIDOR_BUFFER;
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
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
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
					double movableDistanceSize = oldSAShift.getEndTime() - oldSAShift.getStartTime() - (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart()) - (2 * SimulatedAnnealing.BREAK_CORRIDOR_BUFFER);
					// find any value in between the movableDistanceSize and increment all values
					double newEarliestStart = oldSAShift.getSABreak().getEarliestStart(), newLatestEnd = oldSAShift.getSABreak().getLatestEnd(), moveAhead;
					if (movableDistanceSize > 0) {
						moveAhead = random.nextInt((int) (movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) - 1);
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
	public static Individual increaseSABreakCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
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
					if (additionalEndDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.BREAK_CORRIDOR_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
						newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart()) + SimulatedAnnealing.TIME_INTERVAL;
					} else if (additionalStartDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.BREAK_CORRIDOR_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
						newEarliestStart = newEarliestStart - SimulatedAnnealing.TIME_INTERVAL;
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
	}

	public static Individual decreaseSABreakCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
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
	}

	public static Individual increaseSAShiftCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
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
					double additionalEndDistanceSize = SimulatedAnnealing.END_SCHEDULE_TIME - oldSAShift.getEndTime();
					double additionalStartDistanceSize = oldSAShift.getStartTime() - SimulatedAnnealing.START_SCHEDULE_TIME;
					// find any value in between the movableDistanceSize and increment all values
					double newStart = oldSAShift.getStartTime(), newEnd = oldSAShift.getEndTime();
					if (additionalEndDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.SHIFT_CORRIDOR_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
						newEnd = newStart + (oldSAShift.getEndTime() - oldSAShift.getStartTime()) + SimulatedAnnealing.TIME_INTERVAL;
					} else if (additionalStartDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.SHIFT_CORRIDOR_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
						newStart = newStart - SimulatedAnnealing.TIME_INTERVAL;
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
		log.info("increase shift corridor size perturbation has been used");
		return perturbedIndividual;
	}

	public static Individual decreaseSAShiftCorridor(Individual individual) {
		Individual perturbedIndividual = individual.deepCopy();
		List<SAShift> newSAShiftList = perturbedIndividual.deepCopy().getShifts();
		if (perturbedIndividual.getShifts().size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift.deepCopy();
				if (i >= smallIndex && i <= largeIndex) {
					double newStart = oldSAShift.getStartTime(), newEnd = oldSAShift.getEndTime();
					if (((newEnd - newStart) >= SimulatedAnnealing.SHIFT_CORRIDOR_MINIMUM_LENGTH) && ((oldSAShift.getEndTime() - oldSAShift.getSABreak().getLatestEnd()) > SimulatedAnnealing.BREAK_CORRIDOR_BUFFER)) {
						newEnd = newStart + (oldSAShift.getEndTime() - oldSAShift.getStartTime()) - SimulatedAnnealing.TIME_INTERVAL;
					}
					else if (((newEnd - newStart) >= SimulatedAnnealing.SHIFT_CORRIDOR_MINIMUM_LENGTH) && ((oldSAShift.getSABreak().getEarliestStart() - oldSAShift.getStartTime()) > SimulatedAnnealing.BREAK_CORRIDOR_BUFFER)) {
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
		log.info("decrease shift corridor size perturbation has been used");
		return perturbedIndividual;
	}
}

