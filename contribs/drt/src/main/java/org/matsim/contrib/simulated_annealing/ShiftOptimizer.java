package org.matsim.contrib.simulated_annealing;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.shifts.run.RunShiftOptimizerScenario;
import org.matsim.contrib.shifts.shift.*;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ShiftOptimizer implements IterationEndsListener {

    private static final Logger log = Logger.getLogger( ControlerConfigGroup.class );

    private final RejectionTracker rejectionTracker;
    private final DrtShifts initialSolution;
    private DrtShifts currentSolution;
    private DrtShifts acceptedSolution;
	private double temp;
	public double currentIndividualCost = Double.POSITIVE_INFINITY;
    public double acceptedIndividualCost = Double.POSITIVE_INFINITY;

	@Inject
    public ShiftOptimizer(Scenario scenario, RejectionTracker rejectionTracker) {
        this.initialSolution = DrtShiftUtils.getOrCreateShifts(scenario);
        this.currentSolution = initialSolution;
        this.acceptedSolution = currentSolution;
        this.rejectionTracker = rejectionTracker;
		initializeCurrentSolutionCSV();
		initializeAcceptedSolutionCSV();
		initializeSubmittedRequestsCSV();
		initializeActiveShiftsPerHourCSV();
		initializeRejectedRatePerHourCSV();
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {
        Map<Double, Double> rejectionRates = rejectionTracker.getRejectionRatePerTimeBin();
		Map<Double, Double> rejections = rejectionTracker.getRejectionsPerTimeBin();
		Map<Double, Double> submitted = rejectionTracker.getSubmittedPerTimeBin();
		log.info("Number of submitted requests: " + SimulatedAnnealing.getSumOfValues(submitted));
		log.info("Number of rejected requests: " + SimulatedAnnealing.getSumOfValues(rejections));
		log.info("Average rejection rate: " + (SimulatedAnnealing.getSumOfValues(rejectionRates) / rejectionRates.size()));
		Map.Entry<Double, Double> maximumRejectionRateAndTime = rejectionRates.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).orElse(null);
		assert maximumRejectionRateAndTime != null;
		log.info("Maximum rejection rate at " + maximumRejectionRateAndTime.getKey() + " : " + maximumRejectionRateAndTime.getValue());
		//optimization
        temp = SimulatedAnnealing.coolingTemperature(iterationEndsEvent.getIteration(), Double.parseDouble(RunShiftOptimizerScenario.configMap.get("ALPHA")));
        Individual currentIndividual = getIndividualFromDrtShifts(currentSolution);
        Individual acceptedIndividual = getIndividualFromDrtShifts(acceptedSolution);
        if (initialSolution == null)
            log.warn("initial solution is null");
        else {
			currentIndividualCost = SimulatedAnnealing.getCostOfSolution(currentIndividual, rejections);
			log.info(String.format("The current Shift Solution Cost for iteration %d: %f",
					iterationEndsEvent.getIteration(), currentIndividualCost));
			log.info(String.format("The accepted Shift Solution Cost for iteration %d: %f",
					iterationEndsEvent.getIteration(), acceptedIndividualCost));
			log.info("Number of shifts for current solution: " + currentSolution.getShifts().size());
			log.info("Number of shifts for accepted solution: " + acceptedSolution.getShifts().size());
			log.info("Temperature: " + temp);

			writeCurrentSolutionOutput(iterationEndsEvent.getIteration(), rejectionRates, rejections, submitted, maximumRejectionRateAndTime, currentIndividual);

			double acceptanceProbability = SimulatedAnnealing.acceptanceProbability(acceptedIndividualCost, currentIndividualCost, temp);

			log.info(String.format("The acceptance probability: %f", acceptanceProbability));

			if (acceptanceProbability > SimulatedAnnealing.random.nextDouble()) {
				acceptedIndividual = currentIndividual;
				acceptedSolution = getDrtShiftsFromIndividual(acceptedIndividual);
				acceptedIndividualCost = currentIndividualCost;
				log.info("The new solution was accepted");
			}
			else log.info("The new solution was rejected");

			writeAcceptedSolutionOutput(iterationEndsEvent.getIteration(), rejectionRates, rejections, submitted, maximumRejectionRateAndTime, acceptedIndividual);
			writeRejectionRatePerHourCSV(iterationEndsEvent.getIteration(), rejectionRates);
			writeSubmittedRequestsPerHourCSV(iterationEndsEvent.getIteration(), submitted);
			writeActiveShiftsPerHourCSV(iterationEndsEvent.getIteration(), SimulatedAnnealing.activeShiftsPerHour(acceptedIndividual));

			int numberOfPerturbations = SimulatedAnnealing.random.nextInt(currentIndividual.getShifts().size());

			currentIndividual = acceptedIndividual.deepCopy();

			for (int i = 0; i < numberOfPerturbations; i++) {
				SimulatedAnnealing.perturb(currentIndividual);
			}

			currentSolution = getDrtShiftsFromIndividual(currentIndividual);
        }

        getIndividualFromDrtShifts(acceptedSolution).getShifts().forEach(shift -> log.info(SimulatedAnnealing.joinMapValues(shift.getEncodedShift(), "")));

        Perturbation.notifyIterationEnds();

        Perturbation.setIteration(iterationEndsEvent.getIteration());
	}

	private void writeCurrentSolutionOutput(int iteration, Map<Double, Double> rejectionRates, Map<Double, Double> rejections, Map<Double, Double> submitted, Map.Entry<Double, Double> maximumRejectionRateAndTime, Individual currentIndividual) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/current_shift_plan.csv", RunShiftOptimizerScenario.configMap.get("configuration")), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(String.valueOf(currentSolution.getShifts().size()))
					.add(String.valueOf(SimulatedAnnealing.getSumOfValues(submitted)))
					.add(String.valueOf(SimulatedAnnealing.getSumOfValues(rejections)))
					.add(String.valueOf(SimulatedAnnealing.getSumOfValues(rejectionRates) / rejectionRates.size()))
					.add(String.valueOf(maximumRejectionRateAndTime.getValue()))
					.add(String.valueOf(SimulatedAnnealing.totalDriverHours(currentIndividual)))
					.add(String.valueOf(currentIndividualCost))
					.add(String.valueOf(temp));
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeAcceptedSolutionOutput(int iteration, Map<Double, Double> rejectionRates, Map<Double, Double> rejections, Map<Double, Double> submitted, Map.Entry<Double, Double> maximumRejectionRateAndTime, Individual acceptedIndividual) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/accepted_shift_plan.csv", RunShiftOptimizerScenario.configMap.get("configuration")), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(String.valueOf(acceptedSolution.getShifts().size()))
					.add(String.valueOf(SimulatedAnnealing.getSumOfValues(submitted)))
					.add(String.valueOf(SimulatedAnnealing.getSumOfValues(rejections)))
					.add(String.valueOf(SimulatedAnnealing.getSumOfValues(rejectionRates) / rejectionRates.size()))
					.add(String.valueOf(maximumRejectionRateAndTime.getValue()))
					.add(String.valueOf(SimulatedAnnealing.totalDriverHours(acceptedIndividual)))
					.add(String.valueOf(acceptedIndividualCost))
					.add(String.valueOf(temp));
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeActiveShiftsPerHourCSV(int iteration, Map<Double, Double> activeShiftsPerHour) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/active_shifts_per_hour.csv", RunShiftOptimizerScenario.configMap.get("configuration")), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(SimulatedAnnealing.joinMapValues(activeShiftsPerHour, ","));
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void writeSubmittedRequestsPerHourCSV(int iteration, Map<Double, Double> submitted) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/submitted_requests_per_hour.csv", RunShiftOptimizerScenario.configMap.get("configuration")), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(SimulatedAnnealing.joinMapValues(submitted, ","));
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void writeRejectionRatePerHourCSV(int iteration, Map<Double, Double> rejectedRate) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/rejected_rate_per_hour.csv", RunShiftOptimizerScenario.configMap.get("configuration")), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(SimulatedAnnealing.joinMapValues(rejectedRate, ","));
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void initializeCurrentSolutionCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/current_shift_plan.csv", RunShiftOptimizerScenario.configMap.get("configuration")), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add("iteration")
					.add("current_shift_size")
					.add("current_submitted")
					.add("current_rejected")
					.add("current_average_rejection_rate")
					.add("current_max_rejection_rate")
					.add("current_driver_hours")
					.add("current_cost")
					.add("temperature");
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void initializeAcceptedSolutionCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/accepted_shift_plan.csv", RunShiftOptimizerScenario.configMap.get("configuration")), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add("iteration")
					.add("accepted_shift_size")
					.add("accepted_submitted")
					.add("accepted_rejected")
					.add("accepted_average_rejection_rate")
					.add("accepted_max_rejection_rate")
					.add("accepted_driver_hours")
					.add("accepted_cost")
					.add("temperature");
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void initializeSubmittedRequestsCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/submitted_requests_per_hour.csv", RunShiftOptimizerScenario.configMap.get("configuration")), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SCHEDULE_TIME")) / 3600; i++) {
				stringBuilder.append(i).append(",");
			}
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add("iteration")
					.add(stringBuilder);
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void initializeRejectedRatePerHourCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/rejected_rate_per_hour.csv", RunShiftOptimizerScenario.configMap.get("configuration")), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SCHEDULE_TIME")) / 3600; i++) {
				stringBuilder.append(i).append(",");
			}
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add("iteration")
					.add(stringBuilder);
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void initializeActiveShiftsPerHourCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/config%s/active_shifts_per_hour.csv", RunShiftOptimizerScenario.configMap.get("configuration")), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SCHEDULE_TIME")) / 3600; i++) {
				stringBuilder.append(i).append(",");
			}
			stringJoiner.add("iteration")
					.add(stringBuilder);
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Individual getIndividualFromDrtShifts(DrtShifts drtShifts) {
        Individual solution = new Individual();
        List<SAShift> saShiftList = new LinkedList<>();
        SABreak saBreak;
        for (var shift: drtShifts.getShifts().values()) {
            if (shift.getBreak() != null)
                saBreak = new SABreak(shift.getBreak().getEarliestBreakStartTime(), shift.getBreak().getLatestBreakEndTime(), shift.getBreak().getDuration());
            else throw new NullPointerException("Drt Shift Break is null");
            SAShift saShift = new SAShift(shift.getId(), shift.getStartTime(), shift.getEndTime());
            saShift.setSABreak(saBreak);
            saShift.encodeShiftV2();
            saShiftList.add(saShift);
        }
        solution.setShifts(saShiftList);
        return solution;
    }

    public static DrtShifts getDrtShiftsFromIndividual(Individual individual) {
        DrtShifts solution = new DrtShiftsImpl();
        DefautShiftBreakImpl shiftBreak;
        for (var shift: individual.getShifts()) {
            SAShift decodedSAShift = shift.decodeShiftV2();
            DrtShift drtShift = new DrtShiftImpl(decodedSAShift.getId());
            if (decodedSAShift.getSABreak() != null)
                shiftBreak = new DefautShiftBreakImpl(decodedSAShift.getSABreak().getEarliestStart(), decodedSAShift.getSABreak().getLatestEnd(), decodedSAShift.getSABreak().getDuration());
            else throw new NullPointerException("SimulatedAnnealing Shift Break is null");
            drtShift.setBreak(shiftBreak);
            drtShift.setStartTime(decodedSAShift.getStartTime());
            drtShift.setEndTime(decodedSAShift.getEndTime());
            solution.addShift(drtShift);
        }
        return solution;
    }

    public DrtShifts getCurrentSolution() {
        return currentSolution;
    }
}
