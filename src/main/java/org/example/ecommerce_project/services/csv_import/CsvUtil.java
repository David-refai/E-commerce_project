package org.example.ecommerce_project.services.csv_import;



import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class CsvUtil {

    public static List<CSVRecord> read(Path path) {
        try (Reader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {

            CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            return parser.getRecords();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read CSV: " + path + " (" + e.getMessage() + ")", e);
        }
    }

}
