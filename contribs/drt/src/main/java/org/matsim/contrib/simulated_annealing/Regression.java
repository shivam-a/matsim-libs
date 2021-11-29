package org.matsim.contrib.simulated_annealing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Regression {
	public String shiftType;
	public String configuration;
	public double[] betas;

	public Regression(String shiftType, String configuration) throws FileNotFoundException {
		this.shiftType = shiftType;
		this.configuration = configuration;
		betas = getBetas();
	}


//	public static void main(String[] args) {
////		ReadShift readShift = new ReadShift(new File("examples/scenarios/holzkirchen/holzkirchenShifts.xml"));
////		Individual individual = new Individual(readShift.getShifts());
//		double[] activeShiftsArray = {0,0,0,0,2,4,5,4,4,6,6,4,5,2,1,2,3,4,4,3,3,3,3,2,2,0,0,0,0,0};
//		double[] submittedRequestsArray = {0,0,0,0,0,2,2,1,27,44,31,44,39,38,32,26,43,44,38,45,29,25,13,8,3,1,0,0,0,0};
//		// 0,0,0,0,0,0,0,0,0.222222222,0.204545455,0.064516129,0.159090909,0.333333333,0.710526316,0.75,0.769230769,0.76744186,0.318181818,0.421052632,0.444444444,0.275862069,0.4,0,0,0,0,0,0,0,0
//		double[] rejectionRate = predictedRejectionRate(activeShiftsArray, submittedRequestsArray);
//		System.out.println(Arrays.toString(activeShiftsArray));
//		System.out.println(Arrays.toString(submittedRequestsArray));
//		System.out.println(Arrays.toString(rejectionRate));
//	}

	public double[] estimateRejectionRateArray(double[] activeShiftsArray, double[] submittedRequestsArray) {
		double[] rejectionRate = new double[submittedRequestsArray.length];
		for (int i = 0; i < submittedRequestsArray.length; i ++) {
			if (estimateRejectionRate(activeShiftsArray[i], submittedRequestsArray[i]) > 1)
				rejectionRate[i] = 1.0;
			else rejectionRate[i] = estimateRejectionRate(activeShiftsArray[i], submittedRequestsArray[i]);
		}
		return rejectionRate;
	}

	private double estimateRejectionRate(double activeShift, double submittedRequest) {
		if (submittedRequest == 0 || submittedRequest == 1)
			return 0;
		else return betas[0] * (1 / Math.exp(activeShift)) +
				betas[1] * Math.log(submittedRequest) +
				betas[2] * (1 / activeShift) * submittedRequest;
	}
	private double[] getBetas() throws FileNotFoundException {
		List<Double> x1 = new LinkedList<>();
		List<Double> x2 = new LinkedList<>();
		List<Double> y = new LinkedList<>();
		Scanner activeShifts;
		Scanner submittedRequests;
		Scanner rejectedRates;
//		String[] initial_shift_size = {"5_shifts", "30_shifts", "60_shifts"};
//		for (int j = 1; j < 19; j++) {
//			for (String s : initial_shift_size) {
//				x1.clear();
//				x2.clear();
//				y.clear();
//		int j = 1;
//		String s = initial_shift_size[0];
		activeShifts = new Scanner(new File(String.format("test/output/shifts_optimization/%s/config%s/active_shifts_per_hour.csv", shiftType, configuration)));
		submittedRequests = new Scanner(new File(String.format("test/output/shifts_optimization/%s/config%s/submitted_requests_per_hour.csv", shiftType, configuration)));
		rejectedRates = new Scanner(new File(String.format("test/output/shifts_optimization/%s/config%s/rejected_rate_per_hour.csv", shiftType, configuration)));
		activeShifts.nextLine();
		System.out.println("Initial Shift Size: " + shiftType + " and Configuration Number: " + configuration);
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
//			}
//		}
		return model.estimateRegressionParameters();
	}
}
