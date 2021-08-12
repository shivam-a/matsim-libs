package org.matsim.application.prepare.freight;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RawDataProcessing {
    private final static String RAW_DATA_PATH = "/Users/luchengqi/Documents/MATSimScenarios/GermanFreight/original-data/ketten-2010.csv";

    public static void main(String[] args) throws IOException {
        FileWriter csvWriter = new FileWriter("/Users/luchengqi/Documents/MATSimScenarios/GermanFreight/freight-data.csv");
        csvWriter.append("from");
        csvWriter.append(",");
        csvWriter.append("to");
        csvWriter.append(",");
        csvWriter.append("total-ton");
        csvWriter.append(",");
        csvWriter.append("goods-type");
        csvWriter.append("\n");

        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(RAW_DATA_PATH), StandardCharsets.ISO_8859_1),
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                // Vorlauf
                String modeVL = record.get(6);
                String tonVL = record.get(15);
                if (modeVL.equals("2") && !tonVL.equals("0")){
                    csvWriter.append(record.get(0)); // from
                    csvWriter.append(",");
                    csvWriter.append(record.get(2)); // to
                    csvWriter.append(",");
                    csvWriter.append(tonVL);
                    csvWriter.append(",");
                    csvWriter.append(record.get(9)); // goods type
                    csvWriter.append("\n");
                }

                // Hauptlauf
                String modeHL = record.get(7);
                String tonHL = record.get(16);
                if (modeHL.equals("2") && !tonHL.equals("0")){
                    csvWriter.append(record.get(2)); // from
                    csvWriter.append(",");
                    csvWriter.append(record.get(3)); // to
                    csvWriter.append(",");
                    csvWriter.append(tonHL);
                    csvWriter.append(",");
                    csvWriter.append(record.get(10)); // goods type
                    csvWriter.append("\n");
                }

                // Nachlauf
                String modeNL = record.get(8);
                String tonNL = record.get(17);

                if (modeNL.equals("2") && !tonNL.equals("0")){
                    csvWriter.append(record.get(3)); // from
                    csvWriter.append(",");
                    csvWriter.append(record.get(1)); // to
                    csvWriter.append(",");
                    csvWriter.append(tonNL);
                    csvWriter.append(",");
                    csvWriter.append(record.get(11)); // goods type
                    csvWriter.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        csvWriter.close();
    }
}
