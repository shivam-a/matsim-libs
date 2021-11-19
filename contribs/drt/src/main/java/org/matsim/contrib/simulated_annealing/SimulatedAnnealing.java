package org.matsim.contrib.simulated_annealing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SimulatedAnnealing{
	// configuration 1
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 10;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 30;

	public static final int SHIFTS_MINIMUM = 15;
	public static final int SHIFTS_MAXIMUM = 45;
	public static final int SHIFTS_REMOVAL = 2;
	public static final int SHIFTS_INSERTION = 2;
	public static final int[] PERTURBATION_WEIGHTAGE = {10, 10, 25, 25, 15, 15};

	/* // configuration 2
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 10;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 30;

	public static final int SHIFTS_MINIMUM = 1;
	public static final int SHIFTS_MAXIMUM = 1000;
	public static final int SHIFTS_REMOVAL = 10;
	public static final int SHIFTS_INSERTION = 10;
	public static final int[] PERTURBATION_WEIGHTAGE = {10, 10, 25, 25, 15, 15};*/

	/*// configuration 3
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2.5 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 10;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 30;

	public static final int SHIFTS_MINIMUM = 10;
	public static final int SHIFTS_MAXIMUM = 100;
	public static final int SHIFTS_REMOVAL = 20;
	public static final int SHIFTS_INSERTION = 20;
	public static final int[] PERTURBATION_WEIGHTAGE = {5, 5, 25, 25, 20, 20};*/

	/*// configuration 4
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 20;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 20;

	public static final int SHIFTS_MINIMUM = 15;
	public static final int SHIFTS_MAXIMUM = 45;
	public static final int SHIFTS_REMOVAL = 2;
	public static final int SHIFTS_INSERTION = 2;
	public static final int[] PERTURBATION_WEIGHTAGE = {10, 10, 25, 25, 15, 15};*/


	/* // configuration 5
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 20;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 20;

	public static final int SHIFTS_MINIMUM = 1;
	public static final int SHIFTS_MAXIMUM = 1000;
	public static final int SHIFTS_REMOVAL = 10;
	public static final int SHIFTS_INSERTION = 10;
	public static final int[] PERTURBATION_WEIGHTAGE = {10, 10, 25, 25, 15, 15};*/

	/* // configuration 6
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 20;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 20;

	public static final int SHIFTS_MINIMUM = 10;
	public static final int SHIFTS_MAXIMUM = 100;
	public static final int SHIFTS_REMOVAL = 20;
	public static final int SHIFTS_INSERTION = 20;
	public static final int[] PERTURBATION_WEIGHTAGE = {5, 5, 25, 25, 20, 20};
	*/

	/* // configuration 7
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 30;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 10;

	public static final int SHIFTS_MINIMUM = 15;
	public static final int SHIFTS_MAXIMUM = 45;
	public static final int SHIFTS_REMOVAL = 2;
	public static final int SHIFTS_INSERTION = 2;
	public static final int[] PERTURBATION_WEIGHTAGE = {10, 10, 25, 25, 15, 15};
	*/

	/* // configuration 8
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 30;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 10;

	public static final int SHIFTS_MINIMUM = 1;
	public static final int SHIFTS_MAXIMUM = 1000;
	public static final int SHIFTS_REMOVAL = 10;
	public static final int SHIFTS_INSERTION = 10;
	public static final int[] PERTURBATION_WEIGHTAGE = {10, 10, 25, 25, 15, 15};
	*/

	/* // configuration 9
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 2 * 60 * 60;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 60 * 60;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 60 * 60;

	public static final int SHIFT_TIMINGS_BUFFER = 0;
	public static final double SHIFT_TIMINGS_MINIMUM_LENGTH = 5.5 * 60 * 60;
	public static final double SHIFT_TIMINGS_MAXIMUM_LENGTH = 8.5 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION_PER_HOUR = 30;
	public static final  double PENALTY = 1000;
	public static final double DRIVER_COST_PER_HOUR = 10;

	public static final int SHIFTS_MINIMUM = 10;
	public static final int SHIFTS_MAXIMUM = 100;
	public static final int SHIFTS_REMOVAL = 20;
	public static final int SHIFTS_INSERTION = 20;
	public static final int[] PERTURBATION_WEIGHTAGE = {5, 5, 25, 25, 20, 20};
	*/

	public static final int ITERATIONS = 400;

	public static Random random = new Random();
	public static PerturbationType perturbationType = PerturbationType.WEIGHTED_PERTURB_V2;


    public static void main(String[] args) {
        ReadShift readShift = new ReadShift(new File("examples/scenarios/holzkirchen/holzkirchenShifts.xml"));
        Individual individual = new Individual(readShift.getShifts());
//		try {
//			regression();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
        individual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
//        double sum = 0;
		Individual mutatedIndividual = individual.deepCopy();
		for (int i = 0; i < random.nextInt(30); i++)
			mutatedIndividual = perturb(mutatedIndividual);
//		mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
//		Individual mutatedIndividual2 = Perturbation.moveSAShiftTimings(mutatedIndividual);
//		for (int i = 0; i < 100; i++)
//			mutatedIndividual2 = perturb(mutatedIndividual2);
//		mutatedIndividual2.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
//		for (SAShift SAShift : mutatedIndividual.getShifts()) {
//			System.out.println((SAShift.getEndTime()- SAShift.getStartTime()) / 3600);
//		}
//        System.out.println(individual.getShifts().size());
//        Individual mutatedIndividual = Perturbation.moveSABreakCorridor(individual).deepCopy();
//        for (int i = 0; i < 100; i++)
//            mutatedIndividual = Perturbation.moveSABreakCorridor(mutatedIndividual);
//        mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
//        System.out.println(mutatedIndividual.getShifts().size());
//        individual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
//        System.out.println(individual.getShifts().size());
//        for (var shift: mutatedIndividual.getShifts()) {
//            shift = shift.decodeShiftV2(shift, shift.getId());
//            System.out.println(shift.getId() + " " + shift.getStartTime() + " " + shift.getEndTime());
//            System.out.println(shift.getSABreak().getLatestEnd() - shift.getSABreak().getEarliestStart() + " " + shift.getSABreak().getDuration());
//        }
//        int smallIndex = random.nextInt(individual.getShifts().size() - 1);
//        int largeIndex = 0;
//        while (smallIndex > largeIndex)
//        	largeIndex = random.nextInt(individual.getShifts().size());
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

    public static Individual perturb(Individual individual) {
        if (perturbationType == PerturbationType.MOVE_BREAK_CORRIDOR) {
            return Perturbation.moveSABreakCorridor(individual);
        }
        else if (perturbationType == PerturbationType.INSERT_SHIFT) {

            return Perturbation.insertSAShifts(individual);
        }
        else if (perturbationType == PerturbationType.MOVE_SHIFT_TIMINGS) {

            return Perturbation.moveSAShiftTimings(individual);
        }
        else if (perturbationType == PerturbationType.REMOVE_SHIFT) {

            return Perturbation.removeSAShifts(individual);
        }
        else if (perturbationType == PerturbationType.INCREASE_SHIFT_TIMINGS) {

            return Perturbation.increaseSAShiftTimings(individual);
        }
        else if (perturbationType == PerturbationType.DECREASE_SHIFT_TIMINGS) {

            return Perturbation.decreaseSAShiftTimings(individual);
        }
        else if (perturbationType == PerturbationType.RANDOM_PERTURB) {
            int num = random.nextInt(8);
            switch(num) {
                case 0:
                    return Perturbation.removeSAShifts(individual);
                case 1:
                    return Perturbation.moveSABreakCorridor(individual);
                case 2:
                    return Perturbation.moveSAShiftTimings(individual);
                case 3:
                    return Perturbation.insertSAShifts(individual);
                case 4:
                    return Perturbation.increaseSAShiftTimings(individual);
                case 5:
                    return Perturbation.decreaseSAShiftTimings(individual);
            }
        }
        else if (perturbationType == PerturbationType.WEIGHTED_PERTURB) {
			RandomCollection<Object> rc = new RandomCollection<>()
					.add(PERTURBATION_WEIGHTAGE[0], Perturbation.insertSAShifts(individual))
					.add(PERTURBATION_WEIGHTAGE[1], Perturbation.removeSAShifts(individual))
					.add(PERTURBATION_WEIGHTAGE[2], Perturbation.moveSABreakCorridor(individual))
					.add(PERTURBATION_WEIGHTAGE[3], Perturbation.moveSAShiftTimings(individual))
					.add(PERTURBATION_WEIGHTAGE[4], Perturbation.increaseSAShiftTimings(individual))
					.add(PERTURBATION_WEIGHTAGE[5], Perturbation.decreaseSAShiftTimings(individual));
			return (Individual) rc.next();
		}
		else if (perturbationType == PerturbationType.WEIGHTED_PERTURB_V2) {
			int num = random.nextInt(100);
			if (num <= PERTURBATION_WEIGHTAGE[0]) {
				return Perturbation.removeSAShifts(individual);
			} else if (num <= (PERTURBATION_WEIGHTAGE[0] + PERTURBATION_WEIGHTAGE[1])) {
				return Perturbation.insertSAShifts(individual);
			} else if (num <= (PERTURBATION_WEIGHTAGE[0] + PERTURBATION_WEIGHTAGE[1] + PERTURBATION_WEIGHTAGE[2])) {
				return Perturbation.moveSABreakCorridor(individual);
			} else if (num <= (PERTURBATION_WEIGHTAGE[0] + PERTURBATION_WEIGHTAGE[1] + PERTURBATION_WEIGHTAGE[2] + PERTURBATION_WEIGHTAGE[3])) {
				return Perturbation.moveSAShiftTimings(individual);
			} else if (num <= (PERTURBATION_WEIGHTAGE[0] + PERTURBATION_WEIGHTAGE[1] + PERTURBATION_WEIGHTAGE[2] + PERTURBATION_WEIGHTAGE[3] + PERTURBATION_WEIGHTAGE[4])) {
				return Perturbation.increaseSAShiftTimings(individual);
			} else {
				return Perturbation.decreaseSAShiftTimings(individual);
			}
		}
        return individual;
    }

    public static double acceptanceProbability(double currentCost, double newCost, double temperature) {
        // If the new solution is better, accept it
        if (newCost < currentCost) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        return Math.exp((currentCost - newCost) / temperature);
    }

    public static String printMap(Map<Double, Double> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (var entry : map.entrySet()) {
            stringBuilder.append(entry.getValue().intValue()).append("");
        }
//        stringBuilder.append("\n");
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
        /*
        // soft constraint
        double costOfDriverHours = getSumOfValues(driversPerTimeBin(individual)) * COST_PER_DRIVER;
        double costOfRejecting = getSumOfValues(rejectionRate);
        double fitness;
        double penalties = 0;
        // hard constraint
        for (var entry: rejectionRate.entrySet()) {
            double rate = entry.getValue();
            if (rate >= DESIRED_REJECTION_RATE)
                penalties += PENALTY;
        }
        fitness =  costOfDriverHours + costOfRejecting + penalties;
        */
        // soft constraint
        double costOfDriverHours = eachShiftDriverHour(individual) * DRIVER_COST_PER_HOUR;
        double costOfRejecting = getSumOfValues(rejections) * COST_PER_REJECTION_PER_HOUR;
        double cost;
        double penalties = 0;
        // hard constraint
        for (var entry: rejections.entrySet()) {
            double rate = entry.getValue();
            if (rate >= DESIRED_REJECTION_RATE)
                penalties += PENALTY;
        }
        cost =  costOfDriverHours + costOfRejecting + penalties;
        return cost;
    }


    public static double eachShiftDriverHour(Individual individual) {
        double sum = 0;
        for (SAShift SAShift : individual.getShifts()) {
            sum += (SAShift.getEndTime() - SAShift.getStartTime()) / 3600;
        }
        return sum;
    }

    public enum PerturbationType {
        REMOVE_SHIFT,
        INSERT_SHIFT,
		MOVE_BREAK_CORRIDOR,
		MOVE_SHIFT_TIMINGS,
        INCREASE_SHIFT_TIMINGS,
        DECREASE_SHIFT_TIMINGS,
        INCREASE_BREAK_CORRIDOR,
        DECREASE_BREAK_CORRIDOR,
        RANDOM_PERTURB,
        WEIGHTED_PERTURB,
		WEIGHTED_PERTURB_V2;
	}
}

class RandomCollection<E> {
	private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
	private final Random random;
	private double total = 0;

	public RandomCollection() {
		this(new Random());
	}

	public RandomCollection(Random random) {
		this.random = random;
	}

	public RandomCollection<E> add(double weight, E result) {
		if (weight <= 0) return this;
		total += weight;
		map.put(total, result);
		return this;
	}

	public E next() {
		double value = random.nextDouble() * total;
		return map.higherEntry(value).getValue();
	}
}
