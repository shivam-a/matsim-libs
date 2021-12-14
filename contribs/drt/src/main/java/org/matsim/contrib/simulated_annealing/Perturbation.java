package org.matsim.contrib.simulated_annealing;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.shifts.run.RunShiftOptimizerScenario;
import org.matsim.contrib.shifts.shift.DrtShift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Used for changing/modifying the Individuals SupplySchedule/DriverShift.
 */
public class Perturbation {
	public static final double SHIFTS_MAXIMUM = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFTS_MAXIMUM"));
	public static final double SHIFTS_INSERTION = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFTS_INSERTION"));
	public static final double SHIFTS_MINIMUM = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFTS_MINIMUM"));
	public static final double SHIFTS_REMOVAL = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFTS_REMOVAL"));
	public static final double END_SERVICE_TIME = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SERVICE_TIME"));
	public static final double START_SERVICE_TIME = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("START_SERVICE_TIME"));
	public static final double TIME_INTERVAL = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("TIME_INTERVAL"));
	public static final double SHIFT_TIMINGS_BUFFER = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFT_TIMINGS_BUFFER"));
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFT_TIMINGS_MAXIMUM_LENGTH"));
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("SHIFT_TIMINGS_MINIMUM_LENGTH"));
	public static final double BREAK_CORRIDOR_BUFFER = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("BREAK_CORRIDOR_BUFFER"));
	static Random random = new Random();
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
			moveSAShiftTimings(saShift);
			moveSABreakCorridor(saShift);
			counter++;
			newSAShiftList.add(saShift);
		}
	}

	public static void insertSAShifts (Individual individual) {
		if (individual.getShifts().size() < SHIFTS_MAXIMUM) {
			for (int i = 0; i < random.nextInt((int) SHIFTS_INSERTION); i++) {
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
		if (newSAShiftList.size() > SHIFTS_MINIMUM) {
			newSAShiftList.remove(random.nextInt(newSAShiftList.size()));
		}
	}

	public static void removeSAShifts (Individual individual) {
		if (individual.getShifts().size() >= SHIFTS_MINIMUM) {
			for (int i = 0; i < random.nextInt((int) SHIFTS_REMOVAL); i++) {
				removeSingleSAShift(individual);
			}
		}
		log.info("remove random shifts perturbation has been used");
	}

	public static void moveSAShiftTimings(Individual individual) {
		List<SAShift> shiftList = new ArrayList<>(individual.getShifts());
		int numberOfShiftsToBePerturbed = random.nextInt(individual.getShifts().size());
		Collections.shuffle(shiftList, random);
		if (shiftList.size() >= SHIFTS_MINIMUM) {
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
		double movableDistanceSize = (END_SERVICE_TIME - START_SERVICE_TIME + TIME_INTERVAL) - (newEnd - newStart) - (2 * SHIFT_TIMINGS_BUFFER);
		if ((movableDistanceSize / TIME_INTERVAL) > 1) {
			moveAhead = random.nextInt((int) (movableDistanceSize / TIME_INTERVAL) - 1);
			newStart = moveAhead * TIME_INTERVAL + SHIFT_TIMINGS_BUFFER;
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
		if (shiftList.size() >= SHIFTS_MINIMUM) {
			for (int i = 0; i < numberOfShiftsToBePerturbed; i++) {
				SAShift shift = shiftList.remove(0);
				moveSABreakCorridor(shift);
			}
		}
		log.info("move break corridor perturbation has been used");
	}

	private static void moveSABreakCorridor(SAShift shift) {
		double movableDistanceSize = (shift.getEndTime() - shift.getStartTime()) - (2 * BREAK_CORRIDOR_BUFFER);
		// find any value in between the movableDistanceSize and increment all values
		double newEarliestStart = shift.getSABreak().getEarliestStart(), newLatestEnd = shift.getSABreak().getLatestEnd(), moveAhead;
		if ((movableDistanceSize / TIME_INTERVAL) > 1) {
			moveAhead = random.nextInt((int) (movableDistanceSize / TIME_INTERVAL) - 1);
			newEarliestStart = shift.getStartTime() + BREAK_CORRIDOR_BUFFER + moveAhead * TIME_INTERVAL;
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
		if (shiftList.size() >= SHIFTS_MINIMUM) {
			for (int i = 0; i < numberOfShiftsToBePerturbed; i++) {
				SAShift shift = shiftList.remove(0);
				increaseSAShiftTimings(shift);
			}
		}
		log.info("increase shift timings perturbation has been used");
	}

	private static void increaseSAShiftTimings(SAShift shift) {
		// difference between the end of schedule and end of shift
		double additionalEndDistanceSize = END_SERVICE_TIME - SHIFT_TIMINGS_BUFFER - shift.getEndTime() ;
		double additionalStartDistanceSize = shift.getStartTime() - START_SERVICE_TIME + SHIFT_TIMINGS_BUFFER;
		// find any value in between the movableDistanceSize and increment all values
		double newStart = shift.getStartTime();
		double newEnd = shift.getEndTime();
		if ((newEnd - newStart) < SHIFT_TIMINGS_MAXIMUM_LENGTH) {
			if (additionalEndDistanceSize / TIME_INTERVAL > (SHIFT_TIMINGS_BUFFER / TIME_INTERVAL)) {
				newEnd = newStart + (shift.getEndTime() - shift.getStartTime()) + TIME_INTERVAL;
			} else if (additionalStartDistanceSize / TIME_INTERVAL >= (SHIFT_TIMINGS_BUFFER / TIME_INTERVAL)) {
				newStart = newStart - TIME_INTERVAL;
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
		if (shiftList.size() >= SHIFTS_MINIMUM) {
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
		boolean minimumShift = (newEnd - TIME_INTERVAL - newStart) > SHIFT_TIMINGS_MINIMUM_LENGTH;
		boolean endSideBuffer = (newEnd - TIME_INTERVAL - newLatestEnd) > BREAK_CORRIDOR_BUFFER;
		boolean startSideBuffer = (newEarliestStart - TIME_INTERVAL - newStart) > BREAK_CORRIDOR_BUFFER;
		if (minimumShift && endSideBuffer) {
			newEnd = newStart + (shift.getEndTime() - shift.getStartTime()) - TIME_INTERVAL;
		} else {
			if (minimumShift && startSideBuffer) {
				newStart = newStart + TIME_INTERVAL;
			}
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
		if (perturbedIndividual.getShifts().size() >= SHIFTS_MINIMUM) {
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
					if ((newLatestEnd - newEarliestStart) < RunShiftOptimizerScenario.configMap.get.BREAK_CORRIDOR_MAXIMUM_LENGTH) {
						if (additionalEndDistanceSize / TIME_INTERVAL > (Double.parseDouble(RunShiftOptimizerScenario.configMap.get("BREAK_CORRIDOR_BUFFER")) / TIME_INTERVAL)) {
							newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart()) + TIME_INTERVAL;
						} else if (additionalStartDistanceSize / TIME_INTERVAL > (Double.parseDouble(RunShiftOptimizerScenario.configMap.get("BREAK_CORRIDOR_BUFFER")) / TIME_INTERVAL)) {
							newEarliestStart = newEarliestStart - TIME_INTERVAL;
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
		if (perturbedIndividual.getShifts().size() >= SHIFTS_MINIMUM) {
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
					if ((newLatestEnd - newEarliestStart) >= RunShiftOptimizerScenario.configMap.get.BREAK_CORRIDOR_MINIMUM_LENGTH) {
						newLatestEnd = newEarliestStart + (oldSAShift.getSABreak().getLatestEnd() - oldSAShift.getSABreak().getEarliestStart()) - TIME_INTERVAL;
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

