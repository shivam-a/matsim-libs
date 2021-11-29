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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ShiftOptimizer implements IterationEndsListener {

	private final Logger log = Logger.getLogger( ControlerConfigGroup.class );
	public final double END_SCHEDULE_TIME = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SCHEDULE_TIME"));
	public final double START_SCHEDULE_TIME = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("START_SCHEDULE_TIME"));
	public final double TIME_INTERVAL = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("TIME_INTERVAL"));
	public final double ALPHA = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("ALPHA"));
	public final String SHIFT_TYPE = RunShiftOptimizerScenario.configMap.get("shiftType");
	public final String CONFIGURATION = RunShiftOptimizerScenario.configMap.get("configuration");
	public final double INITIAL_TEMPERATURE = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("INITIAL_TEMPERATURE"));
	public final double REMOVE_SHIFT_WEIGHT = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("REMOVE_SHIFT_WEIGHT"));
	public final double INSERT_SHIFT_WEIGHT = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("INSERT_SHIFT_WEIGHT"));
	public final double MOVE_BREAK_CORRIDOR_WEIGHT = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("MOVE_BREAK_CORRIDOR_WEIGHT"));
	public final double MOVE_SHIFT_TIMINGS_WEIGHT = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("MOVE_SHIFT_TIMINGS_WEIGHT"));
	public final double INCREASE_SHIFT_TIMINGS_WEIGHT = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("INCREASE_SHIFT_TIMINGS_WEIGHT"));
	public final double DRIVER_COST_PER_HOUR = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("DRIVER_COST_PER_HOUR"));
	public final double COST_PER_REJECTION_PER_HOUR = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("COST_PER_REJECTION_PER_HOUR"));
	public final double DESIRED_REJECTION_RATE = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("DESIRED_REJECTION_RATE"));
	public final double PENALTY = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("PENALTY"));
	public Random random = new Random();
	public final String COOLING_SCHEDULE = RunShiftOptimizerScenario.configMap.get("coolingSchedule");
	public String PERTURBATION_TYPE = RunShiftOptimizerScenario.configMap.get("perturbationType");
	private final RejectionTracker rejectionTracker;
	private final DrtShifts initialSolution;
	private DrtShifts currentSolution;
	private DrtShifts acceptedSolution;
	private double temp;
	public double currentIndividualCost = Double.POSITIVE_INFINITY;
	public double acceptedIndividualCost = Double.POSITIVE_INFINITY;
	public double estimatedCost = 0;
	public Regression regression = null;
	public double[] predictedRejectionRate;
	Map<Double, Double> estimatedRejectionRate = null;

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
		Map<Double, Double> rejectionRate = rejectionTracker.getRejectionRatePerTimeBin();
		Map<Double, Double> rejections = rejectionTracker.getRejectionsPerTimeBin();
		Map<Double, Double> submitted = rejectionTracker.getSubmittedPerTimeBin();

		log.info("Number of submitted requests: " + getSumOfValues(submitted));
		log.info("Number of rejected requests: " + getSumOfValues(rejections));
		log.info("Average rejection rate: " + (getSumOfValues(rejectionRate) / rejectionRate.size()));
		Map.Entry<Double, Double> maximumRejectionRateAndTime = rejectionRate.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).orElse(null);
		assert maximumRejectionRateAndTime != null;
		log.info("Maximum rejection rate at " + maximumRejectionRateAndTime.getKey() + " : " + maximumRejectionRateAndTime.getValue());
		//optimization
		temp = coolingTemperature(iterationEndsEvent.getIteration());
		Individual currentIndividual = getIndividualFromDrtShifts(currentSolution, rejectionRate);
		Individual acceptedIndividual = getIndividualFromDrtShifts(acceptedSolution, rejectionRate);

		if (initialSolution == null)
			log.warn("initial solution is null");
		else {
			currentIndividualCost = getCostOfSolution(currentIndividual, rejections);
			log.info(String.format("The current Shift Solution Cost for iteration %d: %f",
					iterationEndsEvent.getIteration(), currentIndividualCost));
			log.info(String.format("The accepted Shift Solution Cost for iteration %d: %f",
					iterationEndsEvent.getIteration(), acceptedIndividualCost));
			log.info("Number of shifts for current solution: " + currentSolution.getShifts().size());
			log.info("Number of shifts for accepted solution: " + acceptedSolution.getShifts().size());
			log.info("Temperature: " + temp);

			if (iterationEndsEvent.getIteration() >= 10){
				try {
					regression = new Regression(SHIFT_TYPE, CONFIGURATION);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				if (regression != null) {
					double[] activeShifts = Arrays.stream(activeShiftsPerHour(currentIndividual).values().toArray()).mapToDouble(value -> Double.parseDouble(value.toString())).toArray();
					double[] submittedRequests = Arrays.stream(submitted.values().toArray()).mapToDouble(value -> Double.parseDouble(value.toString())).toArray();
					predictedRejectionRate = regression.predictedRejectionRate(activeShifts, submittedRequests);
					for (int i = 0; i < predictedRejectionRate.length; i++)
						estimatedRejectionRate.put((double) i, predictedRejectionRate[i]);
					estimatedCost = getCostOfSolution(currentIndividual, estimatedRejectionRate);
				}
			}
			writeCurrentSolutionOutput(iterationEndsEvent.getIteration(), rejectionRate, rejections, submitted, currentIndividual);

			double acceptanceProbability = acceptanceProbability(acceptedIndividualCost, currentIndividualCost, temp);

			log.info(String.format("The acceptance probability: %f", acceptanceProbability));

			if (acceptanceProbability > random.nextDouble()) {
				acceptedIndividual = currentIndividual;
				acceptedSolution = getDrtShiftsFromIndividual(acceptedIndividual);
				acceptedIndividualCost = currentIndividualCost;
				log.info("The new solution was accepted");
			}
			else log.info("The new solution was rejected");

			writeAcceptedSolutionOutput(iterationEndsEvent.getIteration(), rejectionRate, rejections, submitted, acceptedIndividual);
			writeRejectionRatePerHourCSV(iterationEndsEvent.getIteration(), rejectionRate);
			writeSubmittedRequestsPerHourCSV(iterationEndsEvent.getIteration(), submitted);
			writeActiveShiftsPerHourCSV(iterationEndsEvent.getIteration(), activeShiftsPerHour(acceptedIndividual));

			int numberOfPerturbations = random.nextInt(currentIndividual.getShifts().size());

			currentIndividual = acceptedIndividual.deepCopy();

			for (int i = 0; i < numberOfPerturbations; i++) {
				perturb(currentIndividual);
			}

			currentSolution = getDrtShiftsFromIndividual(currentIndividual);
		}

		getIndividualFromDrtShifts(acceptedSolution, rejectionRate).getShifts().forEach(shift -> log.info(joinMapValues(shift.getEncodedShift(), "")));

		Perturbation.notifyIterationEnds();

		Perturbation.setIteration(iterationEndsEvent.getIteration());
	}

	private void writeCurrentSolutionOutput(int iteration, Map<Double, Double> rejectionRates, Map<Double, Double> rejections, Map<Double, Double> submitted, Individual currentIndividual) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		Map.Entry<Double, Double> maximumRejectionRateAndTime = currentIndividual.getRejectionRate().entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).orElse(null);
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/current_shift_plan.csv", SHIFT_TYPE, CONFIGURATION), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			assert maximumRejectionRateAndTime != null;
			stringJoiner.add(String.valueOf(iteration))
					.add(String.valueOf(currentSolution.getShifts().size()))
					.add(String.valueOf(getSumOfValues(submitted)))
					.add(String.valueOf(getSumOfValues(rejections)))
					.add(String.valueOf(getSumOfValues(rejectionRates) / rejectionRates.size()))
					.add(String.valueOf(maximumRejectionRateAndTime.getValue()))
					.add(String.valueOf(totalDriverHours(currentIndividual)))
					.add(String.valueOf(currentIndividualCost))
					.add(String.valueOf(estimatedCost))
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

	private void writeAcceptedSolutionOutput(int iteration, Map<Double, Double> rejectionRates, Map<Double, Double> rejections, Map<Double, Double> submitted, Individual acceptedIndividual) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		Map.Entry<Double, Double> maximumRejectionRateAndTime = acceptedIndividual.getRejectionRate().entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).orElse(null);
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/accepted_shift_plan.csv", SHIFT_TYPE, CONFIGURATION), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			assert maximumRejectionRateAndTime != null;
			stringJoiner.add(String.valueOf(iteration))
					.add(String.valueOf(acceptedSolution.getShifts().size()))
					.add(String.valueOf(getSumOfValues(submitted)))
					.add(String.valueOf(getSumOfValues(rejections)))
					.add(String.valueOf(getSumOfValues(rejectionRates) / rejectionRates.size()))
					.add(String.valueOf(maximumRejectionRateAndTime.getValue()))
					.add(String.valueOf(totalDriverHours(acceptedIndividual)))
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
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/active_shifts_per_hour.csv", SHIFT_TYPE, CONFIGURATION), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(joinMapValues(activeShiftsPerHour, ","));
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
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/submitted_requests_per_hour.csv", SHIFT_TYPE, CONFIGURATION), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(joinMapValues(submitted, ","));
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
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/rejected_rate_per_hour.csv", SHIFT_TYPE, CONFIGURATION), true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(joinMapValues(rejectedRate, ","));
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

	public void initializeCurrentSolutionCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/current_shift_plan.csv", SHIFT_TYPE, CONFIGURATION), false);
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
					.add("estimated_current_cost")
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

	public void initializeAcceptedSolutionCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/accepted_shift_plan.csv", SHIFT_TYPE, CONFIGURATION), false);
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

	public void initializeSubmittedRequestsCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/submitted_requests_per_hour.csv", SHIFT_TYPE, CONFIGURATION), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < END_SCHEDULE_TIME / 3600; i++) {
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

	public void initializeRejectedRatePerHourCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/rejected_rate_per_hour.csv", SHIFT_TYPE, CONFIGURATION), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < END_SCHEDULE_TIME / 3600; i++) {
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

	public void initializeActiveShiftsPerHourCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter(String.format("test/output/shifts_optimization/%s/config%s/active_shifts_per_hour.csv", SHIFT_TYPE, CONFIGURATION), false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < END_SCHEDULE_TIME / 3600; i++) {
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

	public Individual getIndividualFromDrtShifts(DrtShifts drtShifts, Map<Double, Double> rejectionRate) {
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
		solution.setRejectionRate(rejectionRate);
		return solution;
	}

	public DrtShifts getDrtShiftsFromIndividual(Individual individual) {
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

	public double coolingTemperature(int iteration) {
		if (COOLING_SCHEDULE.equalsIgnoreCase("LINEAR")) {
			return INITIAL_TEMPERATURE / (1 + (ALPHA * iteration));
		} else {
			return Math.pow(ALPHA, iteration) * INITIAL_TEMPERATURE;
		}
	}

	public void perturb(Individual individual) {
		if (PERTURBATION_TYPE.equalsIgnoreCase("MOVE_BREAK_CORRIDOR")) {
			Perturbation.moveSABreakCorridor(individual);
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("INSERT_SHIFT")) {

			Perturbation.insertSAShifts(individual);
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("MOVE_SHIFT_TIMINGS")) {

			Perturbation.moveSAShiftTimings(individual);
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("REMOVE_SHIFT")) {

			Perturbation.removeSAShifts(individual);
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("INCREASE_SHIFT_TIMINGS")) {

			Perturbation.increaseSAShiftTimings(individual);
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("DECREASE_SHIFT_TIMINGS")) {

			Perturbation.decreaseSAShiftTimings(individual);
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("RANDOM_PERTURB")) {
			int num = random.nextInt(8);
			switch(num) {
				case 0:
					Perturbation.removeSAShifts(individual);
				case 1:
					Perturbation.moveSABreakCorridor(individual);
				case 2:
					Perturbation.moveSAShiftTimings(individual);
				case 3:
					Perturbation.insertSAShifts(individual);
				case 4:
					Perturbation.increaseSAShiftTimings(individual);
				case 5:
					Perturbation.decreaseSAShiftTimings(individual);
			}
		}
		else if (PERTURBATION_TYPE.equalsIgnoreCase("WEIGHTED_PERTURB_V2")) {
			int num = random.nextInt(100);
			if (num <= REMOVE_SHIFT_WEIGHT) {
				Perturbation.removeSAShifts(individual);
			} else if (num <= INSERT_SHIFT_WEIGHT) {
				Perturbation.insertSAShifts(individual);
			} else if (num <= MOVE_BREAK_CORRIDOR_WEIGHT) {
				Perturbation.moveSABreakCorridor(individual);
			} else if (num <= MOVE_SHIFT_TIMINGS_WEIGHT) {
				Perturbation.moveSAShiftTimings(individual);
			} else if (num <= INCREASE_SHIFT_TIMINGS_WEIGHT) {
				Perturbation.increaseSAShiftTimings(individual);
			} else {
				Perturbation.decreaseSAShiftTimings(individual);
			}
		}
	}
	public double acceptanceProbability(double currentCost, double newCost, double temperature) {
		// If the new solution is better, accept it
		if (newCost < currentCost) {
			return 1.0;
		}
		// If the new solution is worse, calculate an acceptance probability
		return Math.exp((currentCost - newCost) / temperature);
	}

	public String joinMapValues(Map<Double, Double> map, String delimiter) {
		StringBuilder stringBuilder = new StringBuilder();
		for (var entry : map.entrySet()) {
			stringBuilder.append(entry.getValue().doubleValue()).append(delimiter);
		}
		return stringBuilder.toString();
	}

	public double getSumOfValues(Map<Double, Double> map) {
		double sum = 0;
		for (var entry : map.entrySet())
			sum = sum + entry.getValue();
		return sum;
	}
	//decrease cost
	public double getCostOfSolution(Individual individual, Map<Double, Double> rejections) {
		// soft constraint
		double costOfDriverHours = totalDriverHours(individual) * DRIVER_COST_PER_HOUR;
		double costOfRejecting = getSumOfValues(rejections) * COST_PER_REJECTION_PER_HOUR;
		double cost;
		double penalties = 0;
		// hard constraint
		for (var entry: rejections.entrySet()) {
			double rate = entry.getValue();
			if (rate >= DESIRED_REJECTION_RATE)
				penalties += PENALTY;
		}
		cost = costOfDriverHours + costOfRejecting + penalties;
		return cost;
	}

	public double totalDriverHours (Individual individual) {
		double sum = 0;
		for (SAShift SAShift : individual.getShifts()) {
			sum += (SAShift.getEndTime() - SAShift.getStartTime()) / 3600;
		}
		return sum;
	}

	public void initializeEncodingPerTimeInterval(Map<Double, Double> encodedShift) {
		for (double i = START_SCHEDULE_TIME; i < END_SCHEDULE_TIME; i += TIME_INTERVAL) {
			encodedShift.put(i, 0.0);
		}
	}

	public Map<Double, Double> activeShiftsPerHour(Individual individual) {
		Map<Double, Double> activeShiftsPerTimeInterval = new LinkedHashMap<>();
		initializeEncodingPerTimeInterval(activeShiftsPerTimeInterval);
		for (SAShift shift: individual.getShifts()) {
			for (Map.Entry<Double, Double> entry : shift.getEncodedShift().entrySet()) {
				double existingValue = entry.getValue();
				double newValue = activeShiftsPerTimeInterval.get(entry.getKey());
				if (existingValue == 1) {
					newValue = newValue + existingValue;
				}
				activeShiftsPerTimeInterval.put(entry.getKey(), newValue);
			}
		}
		Map<Double, Double> activeShiftsPerHour = new LinkedHashMap<>();
		initializeEncodingPerHour(activeShiftsPerHour);
		List<Double> values = new ArrayList<>(activeShiftsPerTimeInterval.values());
		List<Double> keys = new ArrayList<>(activeShiftsPerHour.keySet());
		for (int j = 0; j < values.size(); j += 2) {
			activeShiftsPerHour.put(keys.get(j/2), values.get(j));
		}
		for (int j = 1; j < values.size(); j += 2) {
			activeShiftsPerHour.put(keys.get(j/2), activeShiftsPerHour.get((j-1.0)/2.0) + values.get(j));
		}
		return activeShiftsPerHour;
	}

	public void initializeEncodingPerHour(Map<Double, Double> encodedShift) {
		for (double i = START_SCHEDULE_TIME; i < END_SCHEDULE_TIME; i += 3600) {
			encodedShift.put(i / 3600, 0.0);
		}
	}

	public enum PerturbationType {
		REMOVE_SHIFT,
		INSERT_SHIFT,
		MOVE_BREAK_CORRIDOR,
		MOVE_SHIFT_TIMINGS,
		INCREASE_SHIFT_TIMINGS,
		DECREASE_SHIFT_TIMINGS,
		RANDOM_PERTURB,
		WEIGHTED_PERTURB_V2
	}
	public enum CoolingSchedule {
		EXPONENTIAL,
		LINEAR
	}
}
