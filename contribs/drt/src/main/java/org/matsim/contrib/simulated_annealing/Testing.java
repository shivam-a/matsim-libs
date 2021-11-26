package org.matsim.contrib.simulated_annealing;

import java.io.File;
import java.util.Map;

public class Testing {
	public static void main(String[] args) {
		ReadShift readShift = new ReadShift(new File("examples/scenarios/holzkirchen/holzkirchen60Shifts.xml"));
		Individual individual = new Individual(readShift.getShifts());
		individual.getShifts().forEach(shift -> System.out.println(joinMapValues(shift.getEncodedShift(), "")));
//		System.out.println(joinMapValues(activeShiftsPerHour(individual), " "));
//		Individual mutatedIndividual = individual.deepCopy();
//		for (int i = 0; i < 100; i++)
//			perturb(mutatedIndividual);
//		mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
	}
	public static String joinMapValues(Map<Double, Double> map, String delimiter) {
		StringBuilder stringBuilder = new StringBuilder();
		for (var entry : map.entrySet()) {
			stringBuilder.append(entry.getValue().intValue()).append(delimiter);
		}
		return stringBuilder.toString();
	}
}
