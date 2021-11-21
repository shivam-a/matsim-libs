package org.matsim.contrib.simulated_annealing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.matsim.contrib.shifts.run.RunShiftOptimizerScenario;

import java.io.*;
import java.util.*;

public class SimulatedAnnealing{
	public static final String COOLING_SCHEDULE = RunShiftOptimizerScenario.configMap.get("coolingSchedule");
	public static Random random = new Random();
	public static String PERTURBATION_TYPE = RunShiftOptimizerScenario.configMap.get("perturbationType");

	public static void main(String[] args) {
		ReadShift readShift = new ReadShift(new File("examples/scenarios/holzkirchen/holzkirchenShifts.xml"));
		Individual individual = new Individual(readShift.getShifts());
//		try {
//			regression();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		individual.getShifts().forEach(shift -> System.out.println(joinMapValues(shift.getEncodedShift(), " ")));
		System.out.println(joinMapValues(activeShiftsPerHour(individual), " "));
//		Individual mutatedIndividual = individual.deepCopy();
//		for (int i = 0; i < 100; i++)
//			perturb(mutatedIndividual);
//		mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
	}
	public static void regression () throws FileNotFoundException {
		double[] columnDouble;
		Map<String, double[]> rows = new LinkedHashMap<>();

		Scanner sc = new Scanner(new File("test/output/saved/shift_log1_1.csv"));

		int countRows = 0;
		while (sc.hasNextLine())
		{
			String[] columnString = sc.nextLine().split(",");
			columnDouble = Arrays.stream(columnString).mapToDouble(Double::valueOf).toArray();
			rows.put(columnString[0], columnDouble);
			countRows += 1;
		}

		countRows=50;
		double[] dependentVariable = new double[countRows];
		double[][] independentVariable = new double[countRows][2];
		List<String> rowIndex = new ArrayList<>(rows.keySet());

		for (int i=0; i<countRows; i++) {
			dependentVariable[i] = rows.get(rowIndex.get(i))[3];
			independentVariable[i][0] = rows.get(rowIndex.get(i))[1];
			independentVariable[i][1] = rows.get(rowIndex.get(i))[2];
//			independentVariable[i][2] = rows.get(rowIndex.get(i))[6];
			System.out.println(Arrays.toString(rows.get(rowIndex.get(i))));
		}

		OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
		model.newSampleData(dependentVariable, independentVariable);
		System.out.println(Arrays.toString(model.estimateRegressionParameters()));
		System.out.println(Arrays.toString(model.estimateResiduals()));
		System.out.println(model.calculateRSquared());
		System.out.println(model.calculateAdjustedRSquared());
	}
	public static double coolingTemperature(int iteration, double alpha) {
		if (COOLING_SCHEDULE.equalsIgnoreCase(RunShiftOptimizerScenario.configMap.get("LINEAR"))) {
			return Double.parseDouble(RunShiftOptimizerScenario.configMap.get("INITIAL_TEMPERATURE")) / (1 + alpha * iteration);
		} else {
			return alpha * iteration;
		}
	}
	public static void perturb(Individual individual) {
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
			if (num <= Double.parseDouble(RunShiftOptimizerScenario.configMap.get("REMOVE_SHIFT_WEIGHT"))) {
				Perturbation.removeSAShifts(individual);
			} else if (num <= Double.parseDouble(RunShiftOptimizerScenario.configMap.get("INSERT_SHIFT_WEIGHT"))) {
				Perturbation.insertSAShifts(individual);
			} else if (num <= Double.parseDouble(RunShiftOptimizerScenario.configMap.get("MOVE_BREAK_CORRIDOR_WEIGHT"))) {
				Perturbation.moveSABreakCorridor(individual);
			} else if (num <= Double.parseDouble(RunShiftOptimizerScenario.configMap.get("MOVE_SHIFT_TIMINGS_WEIGHT"))) {
				Perturbation.moveSAShiftTimings(individual);
			} else if (num <= Double.parseDouble(RunShiftOptimizerScenario.configMap.get("INCREASE_SHIFT_TIMINGS_WEIGHT"))) {
				Perturbation.increaseSAShiftTimings(individual);
			} else {
				Perturbation.decreaseSAShiftTimings(individual);
			}
		}
	}

	public static double acceptanceProbability(double currentCost, double newCost, double temperature) {
		// If the new solution is better, accept it
		if (newCost < currentCost) {
			return  1.0;
		}
		// If the new solution is worse, calculate an acceptance probability
		return Math.exp((currentCost - newCost) / temperature);
	}

	public static String joinMapValues(Map<Double, Double> map, String delimiter) {
		StringBuilder stringBuilder = new StringBuilder();
		for (var entry : map.entrySet()) {
			stringBuilder.append(entry.getValue().intValue()).append(delimiter);
		}
		return stringBuilder.toString();
	}

	public static double getSumOfValues(Map<Double, Double> map) {
		double sum = 0;
		for (var entry : map.entrySet())
			sum = sum + entry.getValue();
		return sum;
	}
	//decrease cost
	public static double getCostOfSolution(Individual individual, Map<Double, Double> rejections) {
		// soft constraint
		double costOfDriverHours = totalDriverHours(individual) * Double.parseDouble(RunShiftOptimizerScenario.configMap.get("DRIVER_COST_PER_HOUR"));
		double costOfRejecting = getSumOfValues(rejections) * Double.parseDouble(RunShiftOptimizerScenario.configMap.get("COST_PER_REJECTION_PER_HOUR"));
		double cost;
		double penalties = 0;
		// hard constraint
		for (var entry: rejections.entrySet()) {
			double rate = entry.getValue();
			if (rate >= Double.parseDouble(RunShiftOptimizerScenario.configMap.get("DESIRED_REJECTION_RATE")))
				penalties += Double.parseDouble(RunShiftOptimizerScenario.configMap.get("PENALTY"));
		}
		cost =  costOfDriverHours + costOfRejecting + penalties;
		return cost;
	}

	public static double totalDriverHours (Individual individual) {
		double sum = 0;
		for (SAShift SAShift : individual.getShifts()) {
			sum += (SAShift.getEndTime() - SAShift.getStartTime()) / 3600;
		}
		return sum;
	}

	public static Map<Double, Double> activeShiftsPerHour(Individual individual) {
		Map<Double, Double> activeShiftsPerTimeInterval = new LinkedHashMap<>();
		initializeEncodingPerTimeInterval(activeShiftsPerTimeInterval);
		for (SAShift shift: individual.getShifts()) {
			for (Map.Entry<Double, Double> entry : shift.getEncodedShift().entrySet()) {
				double existingValue = entry.getValue();
				double newValue = activeShiftsPerTimeInterval.get(entry.getKey());
				if (existingValue == 1) {
					newValue = newValue + existingValue;
				}
				activeShiftsPerTimeInterval.put(entry.getKey(),  newValue);
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
	/**
	 * Makes the initial values of all time stamps in a day's supply (number of available drivers) schedule to 0
	 * (no work for any driver)
	 * @param encodedShift a map where keys are time stamps and values are day's supply scheduleGeneSequence
	 */
	public static void initializeEncodingPerTimeInterval(Map<Double, Double> encodedShift) {
		for (double i = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("START_SCHEDULE_TIME")); i < Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SCHEDULE_TIME")); i += Double.parseDouble(RunShiftOptimizerScenario.configMap.get("TIME_INTERVAL"))) {
			encodedShift.put(i, 0.0);
		}
	}

	public static void initializeEncodingPerHour(Map<Double, Double> encodedShift) {
		for (double i = Double.parseDouble(RunShiftOptimizerScenario.configMap.get("START_SCHEDULE_TIME")); i < Double.parseDouble(RunShiftOptimizerScenario.configMap.get("END_SCHEDULE_TIME")); i += 3600) {
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

