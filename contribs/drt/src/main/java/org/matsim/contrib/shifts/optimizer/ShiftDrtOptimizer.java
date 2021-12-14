package org.matsim.contrib.shifts.optimizer;

import org.matsim.contrib.drt.optimizer.DefaultDrtOptimizer;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.schedule.ScheduleTimingUpdater;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.shifts.dispatcher.DrtShiftDispatcher;
import org.matsim.contrib.shifts.fleet.ShiftDvrpVehicle;
import org.matsim.contrib.shifts.schedule.ShiftBreakTask;
import org.matsim.contrib.shifts.schedule.ShiftChangeOverTask;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;

/**
 * @author nkuehnel, fzwick
 */
public class ShiftDrtOptimizer implements DrtOptimizer {

    private final DefaultDrtOptimizer optimizer;

    private DrtShiftDispatcher dispatcher;
    private final ScheduleTimingUpdater scheduleTimingUpdater;

    public ShiftDrtOptimizer(DrtConfigGroup drtConfigGroup, DefaultDrtOptimizer optimizer,
							 DrtShiftDispatcher dispatcher, ScheduleTimingUpdater scheduleTimingUpdater) {
        this.optimizer = optimizer;
        this.dispatcher = dispatcher;
        this.scheduleTimingUpdater = scheduleTimingUpdater;
    }

    @Override
    public void nextTask(DvrpVehicle vehicle) {
        scheduleTimingUpdater.updateBeforeNextTask(vehicle);
        final Task task = vehicle.getSchedule().nextTask();


        if(task instanceof ShiftChangeOverTask) {
            dispatcher.endShift((ShiftDvrpVehicle) vehicle, ((ShiftChangeOverTask) task).getLink().getId());
        } else if(task instanceof ShiftBreakTask) {
            dispatcher.startBreak((ShiftDvrpVehicle) vehicle, ((ShiftBreakTask) task).getFacility().getLinkId());
        }

        if(task != null) {
            // previous task
            int previousTaskIdx = task.getTaskIdx() - 1;
            if (previousTaskIdx >= 0) {
                Task previousTask = vehicle.getSchedule().getTasks().get(previousTaskIdx);
                if (previousTask instanceof ShiftBreakTask) {
                    dispatcher.endBreak((ShiftDvrpVehicle) vehicle, (ShiftBreakTask) previousTask);
                }
            }
        }
    }


    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
        dispatcher.dispatch(e.getSimulationTime());
        this.optimizer.notifyMobsimBeforeSimStep(e);
    }

    @Override
    public void requestSubmitted(Request request) {
        this.optimizer.requestSubmitted(request);
    }

}