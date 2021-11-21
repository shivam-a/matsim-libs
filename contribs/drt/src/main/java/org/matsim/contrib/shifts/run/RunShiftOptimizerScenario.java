package org.matsim.contrib.shifts.run;

import com.google.inject.Singleton;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.drt.analysis.zonal.DrtZonalSystemParams;
import org.matsim.contrib.drt.optimizer.insertion.ExtensiveInsertionSearchParams;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingParams;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.MinCostFlowRebalancingStrategyParams;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.shifts.config.ShiftDrtConfigGroup;
import org.matsim.contrib.shifts.optimizer.ShiftVehicleDataEntryFactory;
import org.matsim.contrib.simulated_annealing.RejectionTracker;
import org.matsim.contrib.simulated_annealing.ShiftOptimizer;
import org.matsim.contrib.simulated_annealing.SimulatedAnnealing;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class RunShiftOptimizerScenario {

	private static final boolean infield = true;
	private static final boolean rebalancing = false;
	public static final Map<String, String> configMap = new LinkedHashMap<>();
	public static void main(String[] args) {

		MultiModeDrtConfigGroup multiModeDrtConfigGroup = new MultiModeDrtConfigGroup();

		/*
		URL fleet = IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("holzkirchen"), "holzkirchenFleet.xml");
		URL plans = IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("holzkirchen"), "holzkirchenPlans.xml.gz");
		URL network = IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("holzkirchen"), "holzkirchenNetwork.xml.gz");
		URL opFacilities = IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("holzkirchen"), "holzkirchenOperationFacilities.xml");
		URL shifts = IOUtils.extendUrl(ExamplesUtils.getTestScenarioURL("holzkirchen"), "holzkirchenShifts.xml");
		*/
		File fleet = new File("examples/scenarios/holzkirchen/holzkirchenFleet.xml");
		File plans = new File("examples/scenarios/holzkirchen/holzkirchenPlans.xml.gz");
		File network = new File("examples/scenarios/holzkirchen/holzkirchenNetwork.xml.gz");
		File opFacilities = new File("examples/scenarios/holzkirchen/holzkirchenOperationFacilities.xml");
		File shifts = new File("examples/scenarios/holzkirchen/holzkirchenShifts.xml");


		List<String> keys = new LinkedList<>();
		List<String> values = new LinkedList<>();
		try {
			Scanner scanner = new Scanner(new FileReader("test/output/shifts_optimization/configurations.csv"));
			String[] columns = scanner.nextLine().split("\t");
			String[] columnsSplit = columns[0].split(",");
			keys.addAll(Arrays.asList(columnsSplit));
			int configNumber = 2;
			for (int i = 1; i < configNumber; i++) {
				scanner.nextLine();
			}
			String[] configs = scanner.nextLine().split("\t");
			String[] configsSplit = configs[0].split(",");
			values.addAll(Arrays.asList(configsSplit));
			for (int i = 0; i < values.size(); i++) {
				configMap.put(keys.get(i), values.get(i));
			}
		}
		catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		}

		DrtConfigGroup drtConfigGroup = new DrtConfigGroup().setMode(TransportMode.drt)
				.setMaxTravelTimeAlpha(1.5)
				.setMaxTravelTimeBeta(10. * 60.)
				.setStopDuration(30.)
				.setMaxWaitTime(600.)
				.setRejectRequestIfMaxWaitOrTravelTimeViolated(true)
				.setUseModeFilteredSubnetwork(false)
				.setVehiclesFile(fleet.getPath())
				.setOperationalScheme(DrtConfigGroup.OperationalScheme.door2door)
				.setPlotDetailedCustomerStats(true)
				.setMaxWalkDistance(1000.)
				.setNumberOfThreads(8)
				.setIdleVehiclesReturnToDepots(false);

		drtConfigGroup.addParameterSet(new ExtensiveInsertionSearchParams());

		if (rebalancing) {

			ConfigGroup rebalancing = drtConfigGroup.createParameterSet("rebalancing");
			drtConfigGroup.addParameterSet(rebalancing);
			((RebalancingParams) rebalancing).setInterval(600);

			MinCostFlowRebalancingStrategyParams strategyParams = new MinCostFlowRebalancingStrategyParams();
			strategyParams.setTargetAlpha(0.3);
			strategyParams.setTargetBeta(0.3);

			drtConfigGroup.getRebalancingParams().get().addParameterSet((ConfigGroup) strategyParams);

			DrtZonalSystemParams drtZonalSystemParams = new DrtZonalSystemParams();
			drtZonalSystemParams.setZonesGeneration(DrtZonalSystemParams.ZoneGeneration.GridFromNetwork);
			drtZonalSystemParams.setCellSize(500.);
			drtZonalSystemParams.setTargetLinkSelection(DrtZonalSystemParams.TargetLinkSelection.mostCentral);
			drtConfigGroup.addParameterSet(drtZonalSystemParams);
		}

		multiModeDrtConfigGroup.addParameterSet(drtConfigGroup);

		final Config config = ConfigUtils.createConfig(multiModeDrtConfigGroup,
				new DvrpConfigGroup());

		Set<String> modes = new HashSet<>();
		modes.add("drt");
		config.travelTimeCalculator().setAnalyzedModes(modes);

		PlanCalcScoreConfigGroup.ModeParams scoreParams = new PlanCalcScoreConfigGroup.ModeParams("drt");
		config.planCalcScore().addModeParams(scoreParams);
		PlanCalcScoreConfigGroup.ModeParams scoreParams2 = new PlanCalcScoreConfigGroup.ModeParams("walk");
		config.planCalcScore().addModeParams(scoreParams2);

		config.plans().setInputFile(plans.getPath());
		config.network().setInputFile(network.getPath());

		config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
		config.qsim().setSimEndtimeInterpretation(QSimConfigGroup.EndtimeInterpretation.minOfEndtimeAndMobsimFinished);


		final PlanCalcScoreConfigGroup.ActivityParams home = new PlanCalcScoreConfigGroup.ActivityParams("home");
		home.setTypicalDuration(8 * 3600);
		final PlanCalcScoreConfigGroup.ActivityParams other = new PlanCalcScoreConfigGroup.ActivityParams("other");
		other.setTypicalDuration(4 * 3600);
		final PlanCalcScoreConfigGroup.ActivityParams education = new PlanCalcScoreConfigGroup.ActivityParams("education");
		education.setTypicalDuration(6 * 3600);
		final PlanCalcScoreConfigGroup.ActivityParams shopping = new PlanCalcScoreConfigGroup.ActivityParams("shopping");
		shopping.setTypicalDuration(2 * 3600);
		final PlanCalcScoreConfigGroup.ActivityParams work = new PlanCalcScoreConfigGroup.ActivityParams("work");
		work.setTypicalDuration(2 * 3600);

		config.planCalcScore().addActivityParams(home);
		config.planCalcScore().addActivityParams(other);
		config.planCalcScore().addActivityParams(education);
		config.planCalcScore().addActivityParams(shopping);
		config.planCalcScore().addActivityParams(work);

		final StrategyConfigGroup.StrategySettings stratSets = new StrategyConfigGroup.StrategySettings();
		stratSets.setWeight(1);
		stratSets.setStrategyName("ChangeExpBeta");
		config.strategy().addStrategySettings(stratSets);

		config.controler().setLastIteration(Integer.parseInt(RunShiftOptimizerScenario.configMap.get("ITERATIONS")));
		config.controler().setWriteEventsInterval(1);

		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory("test/output/holzkirchen_shifts");

		ShiftDrtConfigGroup shiftDrtConfigGroup = ConfigUtils.addOrGetModule(config, ShiftDrtConfigGroup.class);
		shiftDrtConfigGroup.setOperationFacilityInputFile(opFacilities.getPath());
		shiftDrtConfigGroup.setShiftInputFile(shifts.getPath());
		shiftDrtConfigGroup.setAllowInFieldChangeover(infield);

		final Controler run = ShiftDrtControlerCreator.createControler(config, false);

		run.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				bind(RejectionTracker.class).in(Singleton.class);
				this.addEventHandlerBinding().to(RejectionTracker.class);
				this.addMobsimListenerBinding().to(RejectionTracker.class);
				this.addControlerListenerBinding().to(RejectionTracker.class);
				bind(ShiftOptimizer.class).in(Singleton.class);
				this.addControlerListenerBinding().to(ShiftOptimizer.class);
			}
		});

		for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
			run.addOverridingModule(new AbstractDvrpModeModule(drtCfg.getMode()) {
				@Override
				public void install() {
					bind(ShiftVehicleDataEntryFactory.ShiftVehicleDataEntryFactoryProvider.class).toInstance(
							new ShiftVehicleDataEntryFactory.ShiftVehicleDataEntryFactoryProvider());
				}
			});
		}
		run.run();
	}
}
