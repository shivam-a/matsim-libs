package org.matsim.contrib.simulated_annealing;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.shifts.shift.DrtShift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Used for changing/modifying the Individuals SupplySchedule/DriverShift.
 */
public class Perturbation {
	static Random random = SimulatedAnnealing.random;
	private static final Logger log = Logger.getLogger(Perturbation.class);
	private static int counter = 0;
	private static int iteration;
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
	static protected void insertSingleSAShift(Individual individual) {
		List<SAShift> newSAShiftList = individual.getShifts();
		if (newSAShiftList.size() > 0) {
			SAShift shift = individual.getShifts().get(random.nextInt(newSAShiftList.size()));
			// perturb the single shift that you get

			SAShift saShift = shift.deepCopy();
			saShift.setId(Id.create( shift.getId() + "_i" + iteration + "_c" + counter, DrtShift.class));
			saShift.encodeShiftV2();
			counter++;
			newSAShiftList.add(saShift);
		}
	}

	public static void insertSAShifts (Individual individual) {
		if (individual.getShifts().size() < SimulatedAnnealing.SHIFTS_MAXIMUM) {
			for (int i = 0; i < random.nextInt(SimulatedAnnealing.SHIFTS_INSERTION); i++) {
				insertSingleSAShift(individual);
			}
		}
		log.info("insert new shifts perturbation has been used");
	}

	/**
	 * Removes a random SupplySchedule from any random position to decrease the supplyOfDrivers
	 * @param individual Individual that will be modified
	 * @return modified Individual
	 */
	static protected void removeSingleSAShift(Individual individual) {
		List<SAShift> newSAShiftList = individual.getShifts();
		if (newSAShiftList.size() > SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.remove(random.nextInt(newSAShiftList.size()));
		}
	}

	public static void removeSAShifts (Individual individual) {
		if (individual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < random.nextInt(SimulatedAnnealing.SHIFTS_REMOVAL); i++) {
				removeSingleSAShift(individual);
			}
		}
		log.info("remove random shifts perturbation has been used");
	}

	public static void moveSAShiftTimings(Individual individual) {
		List<SAShift> shiftList = new ArrayList<>(individual.getShifts());
		int numberOfShiftsToBePerturbed = random.nextInt(individual.getShifts().size());
		Collections.shuffle(shiftList, random);
		if (shiftList.size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < numberOfShiftsToBePerturbed; i++) {
				SAShift shift = shiftList.remove(0);
				moveSAShiftTimings(shift);
			}
		}
		log.info("move shift timings perturbation has been used");
	}

	private static void moveSAShiftTimings(SAShift shift) {
		double moveAhead;
		double newStart = shift.getStartTime();
		double newEnd = shift.getEndTime();
		double newEarliestStart = shift.getSABreak().getEarliestStart();
		double newLatestEnd = shift.getSABreak().getLatestEnd();
		double movableDistanceSize = (SimulatedAnnealing.END_SCHEDULE_TIME - SimulatedAnnealing.START_SCHEDULE_TIME + SimulatedAnnealing.TIME_INTERVAL) - (newEnd - newStart) - (2 * SimulatedAnnealing.SHIFT_TIMINGS_BUFFER);
		if ((movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) > 1) {
			moveAhead = random.nextInt((int) (movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) - 1);
			newStart = moveAhead * SimulatedAnnealing.TIME_INTERVAL + SimulatedAnnealing.SHIFT_TIMINGS_BUFFER;
			newEnd = shift.getEndTime() - shift.getStartTime() + newStart;
			newEarliestStart = newStart + shift.getSABreak().getEarliestStart() - shift.getStartTime();
			newLatestEnd = newEarliestStart + (shift.getSABreak().getLatestEnd() - shift.getSABreak().getEarliestStart());
		}
		SABreak newSABreak = new SABreak(newEarliestStart, newLatestEnd, shift.getSABreak().getDuration());
		shift = new SAShift(shift.getId(), newStart, newEnd);
		shift.setSABreak(newSABreak);
		shift.encodeShiftV2();
	}

	public static void moveSABreakCorridor(Individual individual) {
		List<SAShift> shiftList = new ArrayList<>(individual.getShifts());
		int numberOfShiftsToBePerturbed = random.nextInt(individual.getShifts().size());
		Collections.shuffle(shiftList, random);
		if (shiftList.size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < numberOfShiftsToBePerturbed; i++) {
				SAShift shift = shiftList.remove(0);
				moveSABreakCorridor(shift);
			}
		}
		log.info("move break corridor perturbation has been used");
	}

	private static void moveSABreakCorridor(SAShift shift) {
		double movableDistanceSize = (shift.getEndTime() - shift.getStartTime()) - (2 * SimulatedAnnealing.BREAK_CORRIDOR_BUFFER);
		// find any value in between the movableDistanceSize and increment all values
		double newEarliestStart = shift.getSABreak().getEarliestStart(), newLatestEnd = shift.getSABreak().getLatestEnd(), moveAhead;
		if ((movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) > 1) {
			moveAhead = random.nextInt((int) (movableDistanceSize / SimulatedAnnealing.TIME_INTERVAL) - 1);
			newEarliestStart = shift.getStartTime() + SimulatedAnnealing.BREAK_CORRIDOR_BUFFER + moveAhead * SimulatedAnnealing.TIME_INTERVAL;
			newLatestEnd = newEarliestStart + (shift.getSABreak().getLatestEnd() - shift.getSABreak().getEarliestStart());
		}
		SABreak newSABreak = new SABreak(newEarliestStart, newLatestEnd, shift.getSABreak().getDuration());
		shift.setSABreak(newSABreak);
		shift.encodeShiftV2();
	}


	public static void increaseSAShiftTimings(Individual individual) {
		List<SAShift> shiftList = new ArrayList<>(individual.getShifts());
		int numberOfShiftsToBePerturbed = random.nextInt(individual.getShifts().size());
		Collections.shuffle(shiftList, random);
		if (shiftList.size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < numberOfShiftsToBePerturbed; i++) {
				SAShift shift = shiftList.remove(0);
				increaseSAShiftTimings(shift);
			}
		}
		log.info("increase shift timings perturbation has been used");
	}

	private static void increaseSAShiftTimings(SAShift shift) {
		// difference between the end of schedule and end of shift
		double additionalEndDistanceSize = SimulatedAnnealing.END_SCHEDULE_TIME - SimulatedAnnealing.SHIFT_TIMINGS_BUFFER - shift.getEndTime() ;
		double additionalStartDistanceSize = shift.getStartTime() - SimulatedAnnealing.START_SCHEDULE_TIME + SimulatedAnnealing.SHIFT_TIMINGS_BUFFER;
		// find any value in between the movableDistanceSize and increment all values
		double newStart = shift.getStartTime();
		double newEnd = shift.getEndTime();
		if ((newEnd - newStart) < SimulatedAnnealing.SHIFT_TIMINGS_MAXIMUM_LENGTH) {
			if (additionalEndDistanceSize / SimulatedAnnealing.TIME_INTERVAL > (SimulatedAnnealing.SHIFT_TIMINGS_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
				newEnd = newStart + (shift.getEndTime() - shift.getStartTime()) + SimulatedAnnealing.TIME_INTERVAL;
			} else if (additionalStartDistanceSize / SimulatedAnnealing.TIME_INTERVAL >= (SimulatedAnnealing.SHIFT_TIMINGS_BUFFER / SimulatedAnnealing.TIME_INTERVAL)) {
				newStart = newStart - SimulatedAnnealing.TIME_INTERVAL;
			}
		}
		SABreak newSABreak = new SABreak(shift.getSABreak().getEarliestStart(), shift.getSABreak().getLatestEnd(), shift.getSABreak().getDuration());
		shift.setStartTime(newStart);
		shift.setEndTime(newEnd);
		shift.setSABreak(newSABreak);
		shift.encodeShiftV2();
	}

	public static void decreaseSAShiftTimings(Individual individual) {
		List<SAShift> shiftList = new ArrayList<>(individual.getShifts());
		int numberOfShiftsToBePerturbed = random.nextInt(individual.getShifts().size());
		Collections.shuffle(shiftList, random);
		if (shiftList.size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			for (int i = 0; i < numberOfShiftsToBePerturbed; i++) {
				SAShift shift = shiftList.remove(0);
				decreaseSAShiftTimings(shift);
			}
		}
		log.info("decrease shift timings perturbation has been used");
	}

	private static void decreaseSAShiftTimings(SAShift shift) {
		double newStart = shift.getStartTime();
		double newEnd = shift.getEndTime();
		double newLatestEnd = shift.getSABreak().getLatestEnd();
		double newEarliestStart = shift.getSABreak().getEarliestStart();
		if (((newEnd - SimulatedAnnealing.TIME_INTERVAL - newStart) > SimulatedAnnealing.SHIFT_TIMINGS_MINIMUM_LENGTH) && ((newEnd - SimulatedAnnealing.TIME_INTERVAL - newLatestEnd) > SimulatedAnnealing.BREAK_CORRIDOR_BUFFER)) {
			newEnd = newStart + (shift.getEndTime() - shift.getStartTime()) - SimulatedAnnealing.TIME_INTERVAL;
		} else if (((newEnd - SimulatedAnnealing.TIME_INTERVAL - newStart) > SimulatedAnnealing.SHIFT_TIMINGS_MINIMUM_LENGTH) && ((newEarliestStart - SimulatedAnnealing.TIME_INTERVAL - newStart) > SimulatedAnnealing.BREAK_CORRIDOR_BUFFER)) {
			newStart = newStart + SimulatedAnnealing.TIME_INTERVAL;
		}
		SABreak newSABreak = new SABreak(shift.getSABreak().getEarliestStart(), shift.getSABreak().getLatestEnd(), shift.getSABreak().getDuration());
		shift.setStartTime(newStart);
		shift.setEndTime(newEnd);
		shift.setSABreak(newSABreak);
		shift.encodeShiftV2();
	}

	static void notifyIterationEnds() {
		counter = 0;
	}

	public static void setIteration(int iteration) {
		Perturbation.iteration = iteration;
	}

	/*public static Individual increaseSABreakCorridor(Individual individual) {
		Individual perturbedIndividual = individual;
		List<SAShift> newSAShiftList = perturbedIndividual.getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift;
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
		Individual perturbedIndividual = individual;
		List<SAShift> newSAShiftList = perturbedIndividual.getShifts();
		if (perturbedIndividual.getShifts().size() >= SimulatedAnnealing.SHIFTS_MINIMUM) {
			newSAShiftList.clear();
			int smallIndex = random.nextInt(perturbedIndividual.getShifts().size() - 1);
			int largeIndex = 0;
			while (smallIndex > largeIndex)
				largeIndex = random.nextInt(perturbedIndividual.getShifts().size());
			for (int i = 0; i < perturbedIndividual.getShifts().size(); i++) {
				SAShift oldSAShift = perturbedIndividual.getShifts().get(i);
				SAShift newSAShift = oldSAShift;
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
}

