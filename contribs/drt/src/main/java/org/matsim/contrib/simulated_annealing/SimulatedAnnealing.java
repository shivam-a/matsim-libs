package org.matsim.contrib.simulated_annealing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.io.*;
import java.util.*;

public class SimulatedAnnealing{
	public static Random random = new Random();

	public static final Map<String, String> configMap = new LinkedHashMap<>();
	public static void main(String[] args) throws FileNotFoundException {
//		individual.getShifts().forEach(shift -> System.out.println(joinMapValues(shift.getEncodedShift(), " ")));
//		System.out.println(joinMapValues(activeShiftsPerHour(individual), " "));
//		Individual mutatedIndividual = individual.deepCopy();
//		for (int i = 0; i < 100; i++)
//			perturb(mutatedIndividual);
//		mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
		List<Double> x1 = new LinkedList<>();
		List<Double> x2 = new LinkedList<>();
		List<Double> y = new LinkedList<>();
		Scanner activeShifts;
		Scanner submittedRequests;
		Scanner rejectedRates;
		String[] initialShiftSize = {"5_shifts", "30_shifts", "60_shifts"};
		initializeRegressionResultCSV();
		for (String size : initialShiftSize) {
			for (int configuration = 1; configuration < 19; configuration++) {
				x1.clear();
				x2.clear();
				y.clear();
//		int j = 1;
//		String s = initialShiftSize[0];
				activeShifts = new Scanner(new File(String.format("test/output/shifts_optimization/400_iterations/%s/config%s/active_shifts_per_hour.csv", size, configuration)));
				submittedRequests = new Scanner(new File(String.format("test/output/shifts_optimization/400_iterations/%s/config%s/submitted_requests_per_hour.csv", size, configuration)));
				rejectedRates = new Scanner(new File(String.format("test/output/shifts_optimization/400_iterations/%s/config%s/rejected_rate_per_hour.csv", size, configuration)));
				activeShifts.nextLine();
				System.out.println("Initial Shift Size: " + size + " and Configuration Number: " + configuration);
				while (activeShifts.hasNextLine()) {
					String[] activeShifts1 = activeShifts.nextLine().split("\t");
					Object[] activeShifts2 = Arrays.stream(activeShifts1[0].split(",")).toArray();
					Object[] activeShiftsArray = Arrays.copyOfRange(activeShifts2, 1, activeShifts2.length);
					for (Object o : activeShiftsArray) {

						x1.add(Double.valueOf((String) o));
					}
				}

				submittedRequests.nextLine();
				while (submittedRequests.hasNextLine()) {
					String[] submittedRequests1 = submittedRequests.nextLine().split("\t");
					Object[] submittedRequests2 = Arrays.stream(submittedRequests1[0].split(",")).toArray();
					Object[] submittedRequestsArray = Arrays.copyOfRange(submittedRequests2, 1, submittedRequests2.length);
					for (Object o : submittedRequestsArray) {
						x2.add(Double.valueOf((String) o));
					}
				}
				rejectedRates.nextLine();
				while (rejectedRates.hasNextLine()) {
					String[] rejectedRates1 = rejectedRates.nextLine().split("\t");
					Object[] rejectedRates2 = Arrays.stream(rejectedRates1[0].split(",")).toArray();
					Object[] rejectedRatesArray = Arrays.copyOfRange(rejectedRates2, 1, rejectedRates2.length);
					for (Object o : rejectedRatesArray) {
						y.add(Double.valueOf((String) o));
					}
				}
//			if (countRowsAS == countRowsRR && countRowsSR == countRowsRR) {
//				countRowsAS = 200;
//				countRowsRR = 200;
//			}
				double[] dependentVariable = new double[y.size()];
				double[][] independentVariable;
				if (x1.size() <= x2.size())
					independentVariable = new double[x1.size()][3];
				else independentVariable = new double[x2.size()][3];
				for (int i = 0; i < dependentVariable.length; i++) {
					if (x2.get(i) == 0 || y.get(i) == 0 || x1.get(i) == 0)
						continue;
					dependentVariable[i] = y.get(i);
					independentVariable[i][0] = (1 / Math.exp(x1.get(i)));
					independentVariable[i][1] = Math.log(x2.get(i));
					independentVariable[i][2] = (1 / x1.get(i)) * x2.get(i);
				}

				OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
				model.setNoIntercept(true);
				model.newSampleData(dependentVariable, independentVariable);
				System.out.println(Arrays.toString(model.estimateRegressionParameters()));
//			System.out.println(Arrays.toString(model.estimateResiduals()));
				System.out.println(model.calculateRSquared());
				System.out.println(model.calculateAdjustedRSquared());
				System.out.println("\n\n");
				writeRegressionResultCSV(configuration, size, model.calculateRSquared());
			}
		}

	}

	public static void initializeRegressionResultCSV() {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter("test/output/shifts_optimization/regression_results.csv", false);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add("iteration")
					.add("initial_shift_size")
					.add("r_squared");
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
	public static void writeRegressionResultCSV(int iteration, String size, double rSquared) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			csvwriter = new FileWriter("test/output/shifts_optimization/regression_results.csv", true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner.add(String.valueOf(iteration))
					.add(size)
					.add(String.valueOf(rSquared));
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
}

