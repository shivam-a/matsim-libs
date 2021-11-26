package org.matsim.contrib.simulated_annealing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.matsim.contrib.shifts.run.RunShiftOptimizerScenario;

import java.io.*;
import java.util.*;

public class SimulatedAnnealing{
	public static Random random = new Random();

	public static final Map<String, String> configMap = new LinkedHashMap<>();
	public static void main(String[] args) {
		List<String> keys = new LinkedList<>();
		List<String> values = new LinkedList<>();
		try {
			Scanner scanner = new Scanner(new FileReader("test/output/shifts_optimization/configurations.csv"));
			String[] columns = scanner.nextLine().split("\t");
			String[] columnsSplit = columns[0].split(",");
			keys.addAll(Arrays.asList(columnsSplit));
			/*
			1-19 30_shifts
			20-38 5_shifts
			39-57 60_shifts
			 */
			int configNumber = 39;
			for (int i = 1; i < configNumber; i++) {
				scanner.nextLine();
			}
			String[] configs = scanner.nextLine().split("\t");
			String[] configsSplit = configs[0].split(",");
			values.addAll(Arrays.asList(configsSplit));
			for (int i = 0; i < values.size(); i++) {
				configMap.put(keys.get(i), values.get(i));
			}
		}
		catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		}
		ReadShift readShift = new ReadShift(new File("examples/scenarios/holzkirchen/holzkirchenShifts.xml"));
		Individual individual = new Individual(readShift.getShifts());
//		individual.getShifts().forEach(shift -> System.out.println(joinMapValues(shift.getEncodedShift(), " ")));
//		System.out.println(joinMapValues(activeShiftsPerHour(individual), " "));
//		Individual mutatedIndividual = individual.deepCopy();
//		for (int i = 0; i < 100; i++)
//			perturb(mutatedIndividual);
//		mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
	}
}

