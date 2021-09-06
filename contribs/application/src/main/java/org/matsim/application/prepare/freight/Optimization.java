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
import java.util.stream.Collectors;

public class Optimization {

    private static final String OPTIMIZED_TRAFFIC_COUNT_PATH = "D:/Work/docs/" + "optimized-traffic-count.csv";

    static String optimization(Network network, Map<String, Map<String, Double>> freightTraffic, Map<String, Id<Link>> zoneIdToLinkIdMap, LeastCostPathCalculator router, Map<Id<Link>, Double> actualCount, Map<Id<Link>, Double> trafficCountReference, double score){

        Map<Id<Link>, Double> actualCountForReference;
        Set<Id<Link>> linkIdSet = actualCount.keySet();
        actualCountForReference = linkIdSet.stream().collect(Collectors.toMap(link -> link, actualCount::get, (a, b) -> b, ConcurrentHashMap::new));

        freightTraffic.keySet().forEach(from -> {
            System.out.println("New source");
            freightTraffic.get(from).keySet().forEach(to -> {
                Node fromNode = network.getLinks().get(zoneIdToLinkIdMap.get(from)).getToNode();
                Node toNode = network.getLinks().get(zoneIdToLinkIdMap.get(to)).getToNode();
                Set<Id<Link>> links = actualCount.keySet();
                List<Link> linksInRoute = router.calcLeastCostPath(fromNode, toNode, 0, null, null).links;
                List<Id<Link>> linkIds = new ArrayList<>();
                linksInRoute.forEach(link -> linkIds.add(link.getId()));
                System.out.println("New destination");
                Map<Id<Link>, Double> actualCountDecrease = new ConcurrentHashMap<>();
                Map<Id<Link>, Double> actualCountIncrease = new ConcurrentHashMap<>();
                Set<Id<Link>> stations = actualCount.keySet();
                for(Id<Link> link : stations){
                    actualCountDecrease.put(link, actualCount.get(link));
                    actualCountIncrease.put(link, actualCount.get(link));
                }
                int increment = 1;
                boolean hasStationInRoute = false;
                for (Id<Link> link : links) {
                    if (linkIds.contains(link)) {
                        hasStationInRoute = true;
                        double decreaseTrip = actualCountDecrease.get(link) - 1;
                        double increaseTrip = actualCountIncrease.get(link) + 1;
                        actualCountDecrease.put(link, decreaseTrip);
                        actualCountIncrease.put(link, increaseTrip);
                    }
                }
                double optimizedScoreAfterDecrease = calculateScore(actualCountDecrease, trafficCountReference);
                double optimizedScoreAfterIncrease = calculateScore(actualCountIncrease, trafficCountReference);
                double percentage = 0.0;
                double optimizedPreviousScore = 0.0;
                if((score > optimizedScoreAfterDecrease || score > optimizedScoreAfterIncrease) && hasStationInRoute){
                    Map<Id<Link>, Double> modifiedActualCount = new ConcurrentHashMap<>();
                    increment += 1;
                    if(optimizedScoreAfterDecrease < optimizedScoreAfterIncrease){
                        Set<Id<Link>> actualCountDecreaseStations = actualCountDecrease.keySet();
                        for(Id<Link> link : actualCountDecreaseStations){
                            modifiedActualCount.put(link, actualCountDecrease.get(link));
                        }

                        while(increment > 0) {
                            Set<Id<Link>> countStations1 = actualCount.keySet();
                            for (Id<Link> link : countStations1) {
                                percentage = modifiedActualCount.get(link)/actualCountForReference.get(link);
                                if (linkIds.contains(link)) {
                                    if((percentage)*100 <= 75){
                                        System.out.println("count went below 75% "+ modifiedActualCount.get(link)+"/"+actualCountForReference.get(link));
                                        break;
                                    }

                                    double decreaseTrip = modifiedActualCount.get(link) - 1;
                                    modifiedActualCount.put(link, decreaseTrip);
                                }
                            }
                            if((percentage)*100 <= 75)
                                break;
                            //score --- check optimized score is less than the optimized score of previous step
                            double optimizedScore = calculateScore(modifiedActualCount, trafficCountReference);
                            if((optimizedPreviousScore < optimizedScore) && optimizedPreviousScore != 0)
                                break;
                            else
                                optimizedPreviousScore = optimizedScore;

                            if (optimizedScore < score){
                                System.out.println("Optimized Score is "+optimizedScore+" and initial score is "+score);
                                Set<Id<Link>> countStations = actualCount.keySet();
                                for(Id<Link> link : countStations){
                                    actualCount.put(link, modifiedActualCount.get(link));
                                }
                                increment += 1;
                            }else
                                increment = 0;
                        }
                    }else{
                        Set<Id<Link>> actualCountIncreaseStations = actualCountIncrease.keySet();
                        for(Id<Link> link : actualCountIncreaseStations){
                            modifiedActualCount.put(link, actualCountIncrease.get(link));
                        }
                        while(increment > 0) {
                            Set<Id<Link>> countStations2 = actualCount.keySet();
                            for (Id<Link> link : countStations2) {
                                percentage = actualCountForReference.get(link)/modifiedActualCount.get(link);
                                if (linkIds.contains(link)) {
                                    if((percentage)*100 >= 125){
                                        System.out.println("count went above 125% "+actualCountForReference.get(link)+"/"+modifiedActualCount.get(link));
                                        break;
                                    }
                                    double increaseTrip = modifiedActualCount.get(link) + 1;
                                    modifiedActualCount.put(link, increaseTrip);
                                }
                            }
                            if((percentage)*100 >= 125)
                                break;
                            //score
                            double optimizedScore = calculateScore(modifiedActualCount, trafficCountReference);
                            if((optimizedPreviousScore < optimizedScore) && optimizedPreviousScore != 0)
                                break;
                            else
                                optimizedPreviousScore = optimizedScore;

                            if (optimizedScore < score){
                                System.out.println("Optimized score is "+optimizedScore+" and initial score is "+score);
                                Set<Id<Link>> countStations = actualCount.keySet();
                                for(Id<Link> link : countStations){
                                    actualCount.put(link, modifiedActualCount.get(link));
                                }
                                increment += 1;
                            } else
                                increment = 0;
                        }
                    }
                }
            });
        });
        System.out.println("Final optimized score is "+calculateScore(actualCount, trafficCountReference));
        try {
            writeOptimizedActualCount(actualCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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