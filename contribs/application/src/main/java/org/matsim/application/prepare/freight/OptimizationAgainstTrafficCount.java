package org.matsim.application.prepare.freight;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.costcalculators.RandomizingTimeDistanceTravelDisutilityFactory;
import org.matsim.core.router.speedy.SpeedyALTFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class OptimizationAgainstTrafficCount {
    private static final String ROOT_FOLDER = "/Users/luchengqi/Documents/SVN/shared-svn/projects/german-wide-freight/original-data/"; //TODO
    private static final String NETWORK_PATH = ROOT_FOLDER + "german-primary-road.network.xml.gz";
    private static final String SHAPEFILE_PATH = ROOT_FOLDER + "NUTS3/NUTS3_2010_DE.shp";
    private static final String LOOKUP_TABLE_PATH = ROOT_FOLDER + "lookup-table.csv";
    private static final String INTERNATIONAL_REGION = ROOT_FOLDER + "international-regions.csv";
    private static final String BOUNDARY_LINKS = ROOT_FOLDER + "boundary-links.csv";
    private static final String COUNTING_DATA_PATH = ROOT_FOLDER + "countData.csv";
    private static final String FREIGHT_DATA_PATH = ROOT_FOLDER + "freight-data.csv";

    public static void main(String[] args) throws IOException {
        // Load config, scenario and network
        Config config = ConfigUtils.createConfig();
        config.global().setCoordinateSystem("EPSG:5677");
        config.network().setInputFile(NETWORK_PATH);
        config.plansCalcRoute().setRoutingRandomness(0);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        // Load Shapefile
        Collection<SimpleFeature> features = getFeatures(SHAPEFILE_PATH);
        // Read look up table
        Map<String, String> lookUpTable = getLookUpTable(LOOKUP_TABLE_PATH);
        // Create zone to link map for regions within Germany
        Map<String, Id<Link>> zoneIdToLinkIdMap = createZoneIdToLinkIdMap(lookUpTable, features, network);
        // Add international regions to the table
        addInternationalRegions(zoneIdToLinkIdMap, INTERNATIONAL_REGION, BOUNDARY_LINKS);
        // Load traffic count reference
        Map<Id<Link>, Double> trafficCountReference = readAutobahnCountingData(COUNTING_DATA_PATH, network);
        // Create Router
        LeastCostPathCalculator router = generateRouter(config, network);
        // Read Freight Data
        Map<String, Map<String, Double>> freightTraffic = readFreightTraffic(FREIGHT_DATA_PATH, 7.0);

        // Optimization
        System.out.println("Begin optimization process");
        Map<Id<Link>, Double> actualCount = calculateActualCount(freightTraffic, trafficCountReference.keySet(),
                router, network, zoneIdToLinkIdMap);

        double score = calculateScore(actualCount, trafficCountReference);
        System.out.println("Initial score is " + score);

        // Write results

    }


    // Below are functions
    //##################################################################################################################
    // Optimization
    private static Map<Id<Link>, Double> calculateActualCount
    (Map<String, Map<String, Double>> freightTraffic, Set<Id<Link>> countingStations, LeastCostPathCalculator router,
     Network network, Map<String, Id<Link>> zoneIdToLinkIdMap) {
        // Initialization
        Map<Id<Link>, Double> actualCount = new HashMap<>();
        for (Id<Link> linkId : countingStations) {
            actualCount.put(linkId, 0.0);
        }
        // Calculate actual traffic count
        int processed = 0;
        System.out.println("Calculating initial actual counting data...");
        for (String from : freightTraffic.keySet()) {
            for (String to : freightTraffic.get(from).keySet()) {
                double numOfVehicles = freightTraffic.get(from).get(to);
                Node fromNode = network.getLinks().get(zoneIdToLinkIdMap.get(from)).getToNode();
                Node toNode = network.getLinks().get(zoneIdToLinkIdMap.get(to)).getToNode();
                List<Link> linksInRoute =
                        router.calcLeastCostPath(fromNode, toNode, 0, null, null).links;
                for (Link link : linksInRoute) {
                    if (actualCount.keySet().contains(link.getId())) {
                        double updatedValue = actualCount.get(link.getId()) + numOfVehicles;
                        actualCount.put(link.getId(), updatedValue);
                    }
                }
                processed += 1;
                if (processed % 10000 == 0) {
                    System.out.println("In progress: " + processed);
                }
            }
        }
        return actualCount;
    }

    private static double calculateScore(Map<Id<Link>, Double> actualCount, Map<Id<Link>, Double> trafficCountReference) {
        double score = 0.0;
        for (Id<Link> linkId : trafficCountReference.keySet()) {
            score += Math.pow(actualCount.get(linkId) - trafficCountReference.get(linkId), 2);
        }
        return score;
    }

    //##################################################################################################################
    private static Collection<SimpleFeature> getFeatures(String pathToShapeFile) {
        System.out.println("Reading shape file...");
        if (pathToShapeFile != null) {
            Collection<SimpleFeature> features;
            if (pathToShapeFile.startsWith("http")) {
                URL shapeFileAsURL = null;
                try {
                    shapeFileAsURL = new URL(pathToShapeFile);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                features = ShapeFileReader.getAllFeatures(shapeFileAsURL);
            } else {
                features = ShapeFileReader.getAllFeatures(pathToShapeFile);
            }
            return features;
        } else {
            System.err.println("Warning: No features in shapefile!");
            return null;
        }
    }

    private static Map<String, String> getLookUpTable(String lookupTablePath) throws IOException {
        System.out.println("Reading lookup table...");
        Map<String, String> lookUpTable = new HashMap<>();
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(lookupTablePath)),
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                lookUpTable.put(record.get(0), record.get(3));
            }
        }
        return lookUpTable;
    }

    private static Map<String, Id<Link>> createZoneIdToLinkIdMap
            (Map<String, String> lookUpTable, Collection<SimpleFeature> features, Network network) {
        System.out.println("Creating zone ID to Link ID map...");
        Map<String, Id<Link>> zoneIdToLinkIdMap = new HashMap<>();
        Map<String, Id<Link>> nutsIdToLinkIdMap = new HashMap<>();
        for (SimpleFeature feature : features) {
            String nutsId = feature.getAttribute("NUTS_ID").toString();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Id<Link> linkId = NetworkUtils.getNearestLink(network, MGC.point2Coord(geometry.getCentroid())).getId();
            nutsIdToLinkIdMap.put(nutsId, linkId);
        }
        System.out.println("There are " + nutsIdToLinkIdMap.keySet().size() + " NUTS3 regions within Germany");
        for (String zoneId : lookUpTable.keySet()) {
            zoneIdToLinkIdMap.put(zoneId, nutsIdToLinkIdMap.get(lookUpTable.get(zoneId)));
        }
        System.out.println("There are " + zoneIdToLinkIdMap.keySet().size() + " regions within Germany (including harbour zones)");
        return zoneIdToLinkIdMap;
    }

    private static void addInternationalRegions
            (Map<String, Id<Link>> zoneIdToLinkIdMap, String internationalRegionPath, String boudaryLinksPath) throws IOException {
        System.out.println("Adding International regions to the map...");
        // Preparation
        Random rnd = new Random(1234);
        String[] orientations = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

        // Read boundary links
        Map<String, List<Id<Link>>> orientationMap = new HashMap<>();
        List<Id<Link>> boundaryLinks = new ArrayList<>();
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(boudaryLinksPath)),
                CSVFormat.DEFAULT.withDelimiter(',').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                orientationMap.computeIfAbsent(record.get(1), k -> new ArrayList<>()).add(Id.createLinkId(record.get(0)));
                boundaryLinks.add(Id.createLinkId(record.get(0)));
            }
        }

        // Add entries to the zone ID to Link ID Map
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(internationalRegionPath)),
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                Id<Link> linkId = null;
                if (record.size() >= 3) {
                    List<Id<Link>> links = orientationMap.getOrDefault(record.get(2), boundaryLinks);
                    linkId = links.get(rnd.nextInt(links.size()));
                } else {
                    linkId = boundaryLinks.get(rnd.nextInt(boundaryLinks.size()));
                }
                zoneIdToLinkIdMap.put(record.get(0), linkId);
            }
        }
        System.out.println("There are in total " + zoneIdToLinkIdMap.keySet().size() + " regions (including international regions)");

    }

    private static Map<Id<Link>, Double> readAutobahnCountingData(String countingDataPath, Network network) throws IOException {
        System.out.println("Reading traffic count data...");
        Map<Id<Link>, Double> trafficCount = new HashMap<>();
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(countingDataPath)),
                CSVFormat.DEFAULT.withDelimiter(',').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                if (record.get(2).equals("A")) {
                    Coord coord = new Coord(Double.parseDouble(record.get(3)), Double.parseDouble(record.get(4)));
                    Id<Link> linkId = NetworkUtils.getNearestLink(network, coord).getId();
                    double count = Double.parseDouble(record.get(5)) / 2;
                    trafficCount.put(linkId, count);
                }
            }
        }
        System.out.println("There are " + trafficCount.keySet().size() + " entries in the Autobahn traffic count");
        return trafficCount;
    }

    private static LeastCostPathCalculator generateRouter(Config config, Network network) {
        FreeSpeedTravelTime travelTime = new FreeSpeedTravelTime();
        LeastCostPathCalculatorFactory fastAStarLandmarksFactory = new SpeedyALTFactory();
        RandomizingTimeDistanceTravelDisutilityFactory disutilityFactory = new RandomizingTimeDistanceTravelDisutilityFactory(
                "car", config);
        TravelDisutility travelDisutility = disutilityFactory.createTravelDisutility(travelTime);
        LeastCostPathCalculator router = fastAStarLandmarksFactory.createPathCalculator(network, travelDisutility,
                travelTime);
        return router;
    }

    private static Map<String, Map<String, Double>> readFreightTraffic(String freightDataPath, double initialAverageLoad) throws IOException {
        System.out.println("Reading freight traffic...");
        Map<String, Map<String, Double>> freightTraffic = new HashMap<>();
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(freightDataPath)),
                CSVFormat.DEFAULT.withDelimiter(',').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                String from = record.get(0);
                String to = record.get(1);
                double numberOfTrucks = Double.parseDouble(record.get(2)) / initialAverageLoad / 260; // 260 working days per year
                double updatedValue = freightTraffic.computeIfAbsent(from, k -> new HashMap<>()).computeIfAbsent(to, l -> 0.0) + numberOfTrucks;
                freightTraffic.get(from).put(to, updatedValue);
            }
        }
        return freightTraffic;
    }
}
