package org.matsim.contrib.simulated_annealing;

import java.util.LinkedList;
import java.util.List;

/**
 * The map of ScheduleSequences (values) and Driver number (key) that represents a shift timetable
 */

public class Individual {

    private List<SAShift> SAShiftList;

    public Individual() {
    }

    public List<SAShift> getShifts() {
        return SAShiftList;
    }

    public void setShifts(List<SAShift> SAShiftList) {
        this.SAShiftList = SAShiftList;
    }
    /**
     * Makes a hard copy of the object, no changes are reflected in the copied object
     *
     * @return new copied object
     */
    public Individual deepCopy() {
        Individual newIndividual = new Individual();
        List<SAShift> newSAShiftList = new LinkedList<>();
        getShifts().forEach(shift -> newSAShiftList.add(shift.deepCopy()));
        newIndividual.setShifts(newSAShiftList);
        return newIndividual;
    }

    public Individual(List<SAShift> SAShiftList) {
        this.SAShiftList = SAShiftList;
    }



}

