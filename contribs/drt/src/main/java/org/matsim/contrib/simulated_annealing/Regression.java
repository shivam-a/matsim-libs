package org.matsim.contrib.simulated_annealing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.matsim.contrib.shifts.run.RunShiftOptimizerScenario;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Regression {
	public static void main(String[] args) {
		try {
			regression();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void regression () throws FileNotFoundException {
//		ArrayList<Double>[] y = new ArrayList[];
		ArrayList<Double>[] x1 = new ArrayList[400];
		ArrayList<Double>[] x2 = new ArrayList[400];
//		ArrayList<Double>[][] x = new ArrayList[][];
		Map<String, double[]> rows = new LinkedHashMap<>();

		Scanner activeShifts = new Scanner(new File(String.format("test/output/shifts_optimization/config%s/active_shifts_per_hour.csv", 1)));
		Scanner submittedRequests = new Scanner(new File(String.format("test/output/shifts_optimization/config%s/submitted_requests_per_hour.csv", 1)));
		activeShifts.nextLine();
		int countRowsAS = 0;
		while (activeShifts.hasNextLine())
		{
			String[] activeShifts1 = activeShifts.nextLine().split("\t");
			Object[] activeShifts2 = Arrays.stream(activeShifts1[0].split(",")).toArray();
			Object[] activeShiftsArray = Arrays.copyOfRange(activeShifts2, 1, activeShifts2.length);
//			System.out.println(Arrays.toString(activeShiftsArray));
			countRowsAS += 1;
			x1[countRowsAS].add(Double.valueOf(Arrays.toString(activeShiftsArray)));
		}
		System.out.println("\n\n");
		int countRowsSR = 0;
		submittedRequests.nextLine();
		while (submittedRequests.hasNextLine())
		{
			String[] submittedRequests1 = submittedRequests.nextLine().split("\t");
			Object[] submittedRequests2 = Arrays.stream(submittedRequests1[0].split(",")).toArray();
			Object[] submittedRequestsArray = Arrays.copyOfRange(submittedRequests2, 1, submittedRequests2.length);
//			System.out.println(Arrays.toString(submittedRequestsArray));
			countRowsSR += 1;
			x2[countRowsAS].add(Double.parseDouble(Arrays.toString(submittedRequestsArray)));
		}
		System.out.println(x1);
//		countRows=50;
//		double[] dependentVariable = new double[countRows];
//		double[][] independentVariable = new double[countRows][2];
//		List<String> rowIndex = new ArrayList<>(rows.keySet());
//
//		for (int i=0; i<countRows; i++) {
//			dependentVariable[i] = rows.get(rowIndex.get(i))[3];
//			independentVariable[i][0] = rows.get(rowIndex.get(i))[1];
//			independentVariable[i][1] = rows.get(rowIndex.get(i))[2];
////			independentVariable[i][2] = rows.get(rowIndex.get(i))[6];
//			System.out.println(Arrays.toString(rows.get(rowIndex.get(i))));
//		}

		OLSMultipleLinearRegression model = new OLSMultipleLinearRegression();
//		model.newSampleData(dependentVariable, independentVariable);
//		System.out.println(Arrays.toString(model.estimateRegressionParameters()));
//		System.out.println(Arrays.toString(model.estimateResiduals()));
//		System.out.println(model.calculateRSquared());
//		System.out.println(model.calculateAdjustedRSquared());
	}
}
