package org.matsim.contrib.simulated_annealing;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.shifts.shift.*;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShiftOptimizer implements IterationEndsListener {

    private static final Logger log = Logger.getLogger( ControlerConfigGroup.class );

    private final RejectionTracker rejectionTracker;
    private final DrtShifts initialSolution;
    private DrtShifts currentSolution;
    private DrtShifts newSolution;
    private DrtShifts bestSolution;
	private double temp = SimulatedAnnealing.INITIAL_TEMPERATURE;
    public DrtShifts getNewSolution() {
        return newSolution;
    }

    public DrtShifts getBestSolution() {
        return bestSolution;
    }

    @Inject
    public ShiftOptimizer(Scenario scenario, RejectionTracker rejectionTracker) {
        this.initialSolution = DrtShiftUtils.getOrCreateShifts(scenario);
        this.currentSolution = initialSolution;
        this.bestSolution = currentSolution;
        this.rejectionTracker = rejectionTracker;
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {
        Map<Double, Double> rejectionRates = rejectionTracker.getRejectionRatePerTimeBin();
		Map<Double, Double> rejections = rejectionTracker.getRejectionsPerTimeBin();
		Map<Double, Double> submitted = rejectionTracker.getSubmittedPerTimeBin();
        //optimization
        double newIndividualCost, currentIndividualCost, bestIndividualCost;
        temp = temp / (iterationEndsEvent.getIteration() + 1);
        Individual currentIndividual = getIndividualFromDrtShifts(currentSolution);
        Individual bestIndividual = getIndividualFromDrtShifts(bestSolution);
        if (initialSolution == null)
            log.warn("initial solution is null");
        else {
			Individual newIndividual = SimulatedAnnealing.perturb(currentIndividual);
            newSolution = getDrtShiftsFromIndividual(newIndividual);
            newIndividualCost = SimulatedAnnealing.getCostOfSolution(newIndividual, rejections);
            currentIndividualCost = SimulatedAnnealing.getCostOfSolution(currentIndividual, rejections);
            bestIndividualCost = SimulatedAnnealing.getCostOfSolution(bestIndividual, rejections);
//            log.info(String.format("The current Shift Solution Cost for iteration %d: %f",
//                    iterationEndsEvent.getIteration(), currentIndividualCost));
            log.info(String.format("The best Shift Solution Cost for iteration %d: %f",
                    iterationEndsEvent.getIteration(), bestIndividualCost));
//            log.info(String.format("The new Shift Solution Cost for iteration %d: %f",
//                    iterationEndsEvent.getIteration(), newIndividualCost));
            double prob = SimulatedAnnealing.acceptanceProbability(currentIndividualCost, newIndividualCost, temp);
            log.info("prob"+ prob);
            if (SimulatedAnnealing.acceptanceProbability(currentIndividualCost, newIndividualCost, temp) > SimulatedAnnealing.random.nextDouble()) {
                currentIndividual = newIndividual.deepCopy();
                currentSolution = newSolution;
            }
            if (currentIndividualCost < bestIndividualCost) {
                bestIndividual = currentIndividual.deepCopy();
                bestSolution = currentSolution;
            }
        }
        getIndividualFromDrtShifts(bestSolution).getShifts().forEach(shift -> log.info(SimulatedAnnealing.printMap(shift.getEncodedShift())));
		log.info("Temperature: " + temp);
        log.info("Number of shifts: " + bestSolution.getShifts().size());
		log.info("Number of submitted requests: " + SimulatedAnnealing.getSumOfValues(submitted));
		log.info("Number of rejected requests: " + SimulatedAnnealing.getSumOfValues(rejections));
		log.info("Average rejection rate: " + (SimulatedAnnealing.getSumOfValues(rejectionRates) / rejectionRates.size()));
		FileWriter csvwriter = null;
		try {
			csvwriter = new FileWriter("test/output/holzkirchen_shifts/shift_log.txt", true);
//			csvwriter.write("iteration");
//			csvwriter.write(",");
//			csvwriter.write("shifts");
//			csvwriter.write(",");
//			csvwriter.write("submitted");
//			csvwriter.write(",");
//			csvwriter.write("rejected");
//			csvwriter.write(",");
//			csvwriter.write("average_rejection_rate");
//			csvwriter.write(",");
//			csvwriter.write("cost");
//			csvwriter.write("\n");

			csvwriter.write(""+iterationEndsEvent.getIteration()+"");
			csvwriter.write(",");
			csvwriter.write(""+bestSolution.getShifts().size()+"");
			csvwriter.write(",");
			csvwriter.write(""+(int) SimulatedAnnealing.getSumOfValues(submitted)+"");
			csvwriter.write(",");
			csvwriter.write(""+(int) SimulatedAnnealing.getSumOfValues(rejections)+"");
			csvwriter.write(",");
			csvwriter.write(""+SimulatedAnnealing.getSumOfValues(rejectionRates) / rejectionRates.size()+"");
			csvwriter.write(",");
			csvwriter.write(""+(int) SimulatedAnnealing.getCostOfSolution(bestIndividual, rejections)+"");
			csvwriter.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				assert csvwriter != null;
				csvwriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				csvwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

    public static Individual getIndividualFromDrtShifts(DrtShifts drtShifts) {
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
        return solution;
    }

    public static DrtShifts getDrtShiftsFromIndividual(Individual individual) {
        DrtShifts solution = new DrtShiftsImpl();
        DefautShiftBreakImpl shiftBreak;
        for (var shift: individual.getShifts()) {
            SAShift decodedSAShift = shift.deepCopy().decodeShiftV2(shift, shift.getId());
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
}
