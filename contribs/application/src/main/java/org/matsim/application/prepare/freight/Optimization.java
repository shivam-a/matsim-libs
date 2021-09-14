package org.matsim.application.prepare.freight;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Optimization {

    private static final String OPTIMIZED_TRAFFIC_COUNT_PATH = "D:/Work/" + "optimized-traffic-count.csv";

     public void optimization(Network network, Map<String, Map<String, Double>> freightTraffic, Map<String, Id<Link>> zoneIdToLinkIdMap, LeastCostPathCalculator router, Map<Id<Link>, Double> actualCount, Map<Id<Link>, Double> trafficCountReference, double score){

       // Map<Id<Link>, Double> actualCountForReference;
        Set<Id<Link>> linkIdSet = actualCount.keySet();
        //actualCountForReference = linkIdSet.stream().collect(Collectors.toMap(link -> link, actualCount::get, (a, b) -> b, ConcurrentHashMap::new));
        AtomicReference<Double> totalTrips = new AtomicReference<>(0.0);
        AtomicInteger totalTripsChanged = new AtomicInteger();
        freightTraffic.keySet().forEach(from -> {
            //System.out.println("New source");
            freightTraffic.get(from).keySet().forEach(to -> {
                double numOfVehicles = freightTraffic.get(from).get(to);
                totalTrips.updateAndGet(v -> v + numOfVehicles);
                if(numOfVehicles > 5){
                Node fromNode = network.getLinks().get(zoneIdToLinkIdMap.get(from)).getToNode();
                Node toNode = network.getLinks().get(zoneIdToLinkIdMap.get(to)).getToNode();
                Set<Id<Link>> linksInActualCount = actualCount.keySet();
                List<Link> linksInRoute = router.calcLeastCostPath(fromNode, toNode, 0, null, null).links;
                List<Id<Link>> linkIdsInRoute = new ArrayList<>();
                linksInRoute.forEach(link -> linkIdsInRoute.add(link.getId()));
                //System.out.println("New destination");
                Map<Id<Link>, Double> actualCountDecrease = new ConcurrentHashMap<>();
                Map<Id<Link>, Double> actualCountIncrease = new ConcurrentHashMap<>();
                Set<Id<Link>> stations = actualCount.keySet();
                for(Id<Link> link : stations){
                    actualCountDecrease.put(link, actualCount.get(link));
                    actualCountIncrease.put(link, actualCount.get(link));
                }
                int increment = 1;
                boolean hasStationInRoute = false;
                for (Id<Link> link : linksInActualCount) {
                    if (linkIdsInRoute.contains(link)) {
                        hasStationInRoute = true;
                        double decreaseTrip = actualCountDecrease.get(link) - 1;
                        double increaseTrip = actualCountIncrease.get(link) + 1;
                        actualCountDecrease.put(link, decreaseTrip);
                        actualCountIncrease.put(link, increaseTrip);
                    }
                }
                double optimizedScoreAfterDecrease = calculateScore(actualCountDecrease, trafficCountReference);
                double optimizedScoreAfterIncrease = calculateScore(actualCountIncrease, trafficCountReference);
                double percentage;
                double optimizedPreviousScore = 0.0;
                if((score > optimizedScoreAfterDecrease || score > optimizedScoreAfterIncrease) && hasStationInRoute){
                    Map<Id<Link>, Double> modifiedActualCount = new ConcurrentHashMap<>();
              //      if(optimizedScoreAfterDecrease < optimizedScoreAfterIncrease){
                        Set<Id<Link>> actualCountDecreaseStations = actualCountDecrease.keySet();
                        for(Id<Link> link : actualCountDecreaseStations){
                            modifiedActualCount.put(link, actualCountDecrease.get(link));
                        }
                        double vehicleCount = numOfVehicles;
                        int changeInTrip;
                        while(increment > 0) {
                            if(optimizedScoreAfterDecrease < optimizedScoreAfterIncrease){
                                vehicleCount -= 1;
                                percentage = vehicleCount/numOfVehicles;
                                changeInTrip = -1;
                                if((percentage)*100 <= 75){
                                    //System.out.println("count went below 75% "+ vehicleCount+"/"+numOfVehicles);
                                    break;
                                }
                            }else{
                                vehicleCount += 1;
                                percentage = vehicleCount/numOfVehicles;
                                changeInTrip = 1;
                                if((percentage)*100 >= 125) {
                                    //System.out.println("count went above 125% " + vehicleCount + "/" + numOfVehicles);
                                    break;
                                }
                            }

                            Set<Id<Link>> countStations1 = actualCount.keySet();
                            for (Id<Link> link : countStations1) {
                                if (linkIdsInRoute.contains(link)) {
                                    double decreaseTrip = modifiedActualCount.get(link) + changeInTrip;
                                    modifiedActualCount.put(link, decreaseTrip);
                                }
                            }

                            //score --- check optimized score is less than the optimized score of previous step
                            double optimizedScore = calculateScore(modifiedActualCount, trafficCountReference);
                            if((optimizedPreviousScore < optimizedScore) && optimizedPreviousScore != 0)
                                break;
                            else
                                optimizedPreviousScore = optimizedScore;

                            if (optimizedScore < score){
                                //System.out.println("Optimized Score is "+optimizedScore+" and initial score is "+score);
                                Set<Id<Link>> countStations = actualCount.keySet();
                                totalTripsChanged.addAndGet(Math.abs(changeInTrip));
                                for(Id<Link> link : countStations){
                                    actualCount.put(link, modifiedActualCount.get(link));
                                }
                            }else
                                increment = 0;
                        }
                }
            }
            });
        });
        System.out.println("Final optimized score is "+calculateScore(actualCount, trafficCountReference));
        System.out.println("Total number of trips "+totalTrips);
        System.out.println("Total number of trips changed "+totalTripsChanged);
        try {
            writeOptimizedActualCount(actualCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double calculateScore(Map<Id<Link>, Double> actualCount, Map<Id<Link>, Double> trafficCountReference) {
        double score = 0.0;
        for (Id<Link> linkId : trafficCountReference.keySet()) {
            score += Math.pow(actualCount.get(linkId) - trafficCountReference.get(linkId), 2);
        }
        return score;
    }

    private static void writeOptimizedActualCount(Map<Id<Link>, Double> actualCount) throws IOException {

        {
            // Otherwise, calculate the initial traffic count and write it as a csv file
            System.out.println("Writing optimized traffic count");
            FileWriter csvWriter = new FileWriter(OPTIMIZED_TRAFFIC_COUNT_PATH);
            csvWriter.append("counting-station-link");
            csvWriter.append(",");
            csvWriter.append("count");
            csvWriter.append("\n");
            for (Id<Link> linkId : actualCount.keySet()) {
                csvWriter.append(linkId.toString());
                csvWriter.append(",");
                csvWriter.append(Double.toString(actualCount.get(linkId)));
                csvWriter.append("\n");
            }
            csvWriter.close();
        }

    }
}