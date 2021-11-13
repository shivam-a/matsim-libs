package org.matsim.contrib.simulated_annealing;

public class SABreak {

    public void setEarliestStart(double earliestStart) {
        this.earliestStart = earliestStart;
    }

    public void setLatestEnd(double latestEnd) {
        this.latestEnd = latestEnd;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    private double earliestStart;
    private double latestEnd;
    private double duration;

    public SABreak(double earliestStart, double latestEnd, double duration) {
        this.earliestStart = earliestStart;
        this.latestEnd = latestEnd;
        this.duration = duration;
    }


    public double getEarliestStart() {
        return earliestStart;
    }

    public double getLatestEnd() {
        return latestEnd;
    }

    public double getDuration() {
        return duration;
    }

}
