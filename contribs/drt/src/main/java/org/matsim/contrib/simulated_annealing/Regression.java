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
		List<Double> x1 = new LinkedList<>();
		List<Double> x2 = new LinkedList<>();
		List<Double> y = new LinkedList<>();
		Scanner activeShifts = new Scanner(new File(String.format("test/output/shifts_optimization/config%s/active_shifts_per_hour.csv", 1)));
		Scanner submittedRequests = new Scanner(new File(String.format("test/output/shifts_optimization/config%s/submitted_requests_per_hour.csv", 1)));
		Scanner rejectedRates = new Scanner(new File(String.format("test/output/shifts_optimization/config%s/rejected_rate_per_hour.csv", 1)));
		activeShifts.nextLine();
		int countRowsAS = 0;
		int countRowsSR = 0;
		int countRowsRR = 0;
		while (activeShifts.hasNextLine())
		{
			String[] activeShifts1 = activeShifts.nextLine().split("\t");
			Object[] activeShifts2 = Arrays.stream(activeShifts1[0].split(",")).toArray();
			Object[] activeShiftsArray = Arrays.copyOfRange(activeShifts2, 1, activeShifts2.length);
			for (Object o : activeShiftsArray) {
				x1.add(Double.valueOf((String) o));
			}
			countRowsAS += 1;
		}
		System.out.println("\n\n");
		submittedRequests.nextLine();
		while (submittedRequests.hasNextLine())
		{
			String[] submittedRequests1 = submittedRequests.nextLine().split("\t");
			Object[] submittedRequests2 = Arrays.stream(submittedRequests1[0].split(",")).toArray();
			Object[] submittedRequestsArray = Arrays.copyOfRange(submittedRequests2, 1, submittedRequests2.length);
			for (Object o : submittedRequestsArray) {
				x2.add(Double.valueOf((String) o));
			}
			countRowsSR += 1;
		}
		rejectedRates.nextLine();
		while (rejectedRates.hasNextLine())
		{
			String[] rejectedRates1 = rejectedRates.nextLine().split("\t");
			Object[] rejectedRates2 = Arrays.stream(rejectedRates1[0].split(",")).toArray();
			Object[] rejectedRatesArray = Arrays.copyOfRange(rejectedRates2, 1, rejectedRates2.length);
			for (Object o : rejectedRatesArray) {
				y.add(Double.valueOf((String) o));
			}
			countRowsRR += 1;
		}
		if (countRowsAS == countRowsRR && countRowsSR == countRowsRR) {
			countRowsAS = 200;
			countRowsRR = 200;
		}
		double[] dependentVariable = new double[countRowsRR];
		double[][] independentVariable = new double[countRowsAS][3];

		for (int i = 0; i < countRowsAS; i++) {
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
		double[] betas = model.estimateRegressionParameters();
		System.out.println(Arrays.toString(model.estimateRegressionParameters()));
		System.out.println(Arrays.toString(model.estimateResiduals()));
		System.out.println(model.calculateRSquared());
		System.out.println(model.calculateAdjustedRSquared());
	}
}
