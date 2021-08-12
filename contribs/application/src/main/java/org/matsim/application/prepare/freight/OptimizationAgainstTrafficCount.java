package org.matsim.application.prepare.freight;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
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
    private static final String NETWORK_PATH = "/Users/luchengqi/Documents/MATSimScenarios/GermanFreight/original-data/german-primary-road.network.xml.gz"; //TODO
    private static final String SHAPEFILE_PATH = "/Users/luchengqi/Documents/MATSimScenarios/GermanFreight/original-data/NUTS3/NUTS3_2010_DE.shp"; //TODO
    private static final String LOOKUP_TABLE_PATH = ""; //TODO
    private static final String INTERNATIONAL_REGION = ""; //TODO
    private static final String BOUDARY_LINKS = ""; //TODO

    public static void main(String[] args) throws IOException {
        // Load config, scenario and network
        Config config = ConfigUtils.createConfig();
        config.global().setCoordinateSystem("EPSG:5677");
        config.network().setInputFile(NETWORK_PATH);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        // Load Shapefile
        Collection<SimpleFeature> features = getFeatures(SHAPEFILE_PATH);

        // Read look up table
        Map<String, String> lookUpTable = getLookUpTable(LOOKUP_TABLE_PATH);

        // Create zone to link map for regions within Germany
        Map<String, Id<Link>> zoneIdToLinkIdMap = createZoneIdToLinkIdMap(lookUpTable, features, network);

        // Add international regions to the table
        addInternationalRegions(zoneIdToLinkIdMap, INTERNATIONAL_REGION, BOUDARY_LINKS);

        // Load counting data

        // Create Router

        // Read Freight Data

    }

    //######################################################################################################################
    // Below are functions
    private static Collection<SimpleFeature> getFeatures(String pathToShapeFile) {
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
            return null;
        }
    }

    private static Map<String, String> getLookUpTable(String lookupTablePath) throws IOException {
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
        Map<String, Id<Link>> zoneIdToLinkIdMap = new HashMap<>();
        for (SimpleFeature feature : features) {
            String nutsId = feature.getAttribute("NUTS_ID").toString();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Id<Link> linkId = NetworkUtils.getNearestLink(network, MGC.point2Coord(geometry.getCentroid())).getId();
            zoneIdToLinkIdMap.put(nutsId, linkId);
        }
        return zoneIdToLinkIdMap;
    }

    private static void addInternationalRegions
            (Map<String, Id<Link>> zoneIdToLinkIdMap, String internationalRegionPath, String boudaryLinksPath) throws IOException {
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
                if (record.size() >= 3){
                    List<Id<Link>> links = orientationMap.getOrDefault(record.get(2), boundaryLinks);
                    linkId = links.get(rnd.nextInt(links.size()));
                } else {
                    linkId = boundaryLinks.get(rnd.nextInt(boundaryLinks.size()));
                }
                zoneIdToLinkIdMap.put(record.get(0), linkId);
            }
        }

    }
}
