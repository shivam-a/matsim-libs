package org.matsim.contrib.simulated_annealing;

import java.io.File;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class SimulatedAnnealing{
	public final static double TIME_INTERVAL = 1800;
	public final static double START_SCHEDULE_TIME = 0;
	public final static double END_SCHEDULE_TIME = 30 * 60 * 60;

	public final static double DESIRED_REJECTION_RATE = 0.2;

	public static final double BREAK_CORRIDOR_BUFFER = 1800;
	public static final double BREAK_CORRIDOR_MINIMUM_LENGTH = 3600;
	public static final double BREAK_CORRIDOR_MAXIMUM_LENGTH = 1800 * 10;

	public static final int SHIFT_CORRIDOR_BUFFER = 3600;
	public static final double SHIFT_CORRIDOR_MINIMUM_LENGTH = 6 * 60 * 60;
	public static final double SHIFT_CORRIDOR_MAXIMUM_LENGTH = 9 * 60 * 60;

	public static final double INITIAL_TEMPERATURE = 1000;

	private static final double COST_PER_REJECTION = 1000;
	public static final  double PENALTY = 9999;
	public static final double COST_PER_DRIVER = 100;

	public static final int SHIFTS_MINIMUM = 15;
	public static final int SHIFTS_MAXIMUM = 40;
	public static final int SHIFTS_REMOVAL = 2;
	public static final int SHIFTS_INSERTION = 2;

	public static final int ITERATIONS = 400;

	public static Random random = new Random();
	public static PerturbationType perturbationType = PerturbationType.INCREASE_SHIFT_CORRIDOR;


    public static void main(String[] args) {
        ReadShift readShift = new ReadShift(new File("examples/scenarios/holzkirchen/holzkirchenShifts.xml"));
        Individual individual = new Individual(readShift.getShifts());
        individual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
//        System.out.println(individual.getShifts().size());
        Individual mutatedIndividual = perturb(individual).deepCopy();
        for (int i = 0; i < 10000; i++)
            mutatedIndividual = perturb(mutatedIndividual);
        mutatedIndividual.getShifts().forEach(shift -> System.out.println(printMap(shift.getEncodedShift())));
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

    public static Individual perturb(Individual individual) {
        if (perturbationType == PerturbationType.CHANGE_BREAK_CORRIDOR) {
            return Perturbation.moveSABreakCorridor(individual);
        }
        else if (perturbationType == PerturbationType.INSERT_SHIFT) {

            return Perturbation.insertSAShifts(individual);
        }
        else if (perturbationType == PerturbationType.CHANGE_SHIFT_TIMINGS) {

            return Perturbation.moveSAShiftCorridor(individual);
        }
        else if (perturbationType == PerturbationType.REMOVE_SHIFT) {

            return Perturbation.removeSAShifts(individual);
        }
        else if (perturbationType == PerturbationType.INCREASE_BREAK_CORRIDOR) {

            return Perturbation.increaseSABreakCorridor(individual);
        }
        else if (perturbationType == PerturbationType.DECREASE_BREAK_CORRIDOR) {

            return Perturbation.decreaseSABreakCorridor(individual);
        }
        else if (perturbationType == PerturbationType.INCREASE_SHIFT_CORRIDOR) {

            return Perturbation.increaseSAShiftCorridor(individual);
        }
        else if (perturbationType == PerturbationType.DECREASE_SHIFT_CORRIDOR) {

            return Perturbation.decreaseSAShiftCorridor(individual);
        }
        else if (perturbationType == PerturbationType.RANDOM_PERTURB) {
            int num = random.nextInt(8);
            switch(num) {
                case 0:
                    return Perturbation.removeSAShifts(individual);
                case 1:
                    return Perturbation.moveSABreakCorridor(individual);
                case 2:
                    return Perturbation.moveSAShiftCorridor(individual);
                case 3:
                    return Perturbation.insertSAShifts(individual);
                case 4:
                    return Perturbation.increaseSAShiftCorridor(individual);
                case 5:
                    return Perturbation.increaseSABreakCorridor(individual);
                case 6:
                    return Perturbation.decreaseSAShiftCorridor(individual);
                case 7:
                    return Perturbation.decreaseSABreakCorridor(individual);

            }
        }
        else if (perturbationType == PerturbationType.WEIGHTED_PERTURB) {
			RandomCollection<Object> rc = new RandomCollection<>()
					.add(15, Perturbation.insertSAShifts(individual))
					.add(15, Perturbation.removeSAShifts(individual))
					.add(25, Perturbation.moveSABreakCorridor(individual))
					.add(25, Perturbation.moveSAShiftCorridor(individual))
					.add(5, Perturbation.increaseSABreakCorridor(individual))
					.add(5, Perturbation.decreaseSABreakCorridor(individual))
					.add(5, Perturbation.increaseSAShiftCorridor(individual))
					.add(5, Perturbation.decreaseSAShiftCorridor(individual));

			return (Individual) rc.next();
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
        double costOfDriverHours = driverCost(individual) * COST_PER_DRIVER;
        double costOfRejecting = getSumOfValues(rejections) * COST_PER_REJECTION;
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


    public static double driverCost(Individual individual) {
        double sum = 0;
        for (SAShift SAShift : individual.getShifts()) {
            sum += SAShift.getEncodedShift().size();
        }
        return sum;
    }

    public enum PerturbationType {
        REMOVE_SHIFT,
        INSERT_SHIFT,
        CHANGE_BREAK_CORRIDOR,
        CHANGE_SHIFT_TIMINGS,
        INCREASE_SHIFT_CORRIDOR,
        DECREASE_SHIFT_CORRIDOR,
        INCREASE_BREAK_CORRIDOR,
        DECREASE_BREAK_CORRIDOR,
        RANDOM_PERTURB,
        WEIGHTED_PERTURB
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
