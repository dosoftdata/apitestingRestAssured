package com.apitesting.utilities;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CSVWriteUtil {

   private CSVWriter writer;

    public CSVWriteUtil(String csvWriteFile) throws IOException {
        writer = new CSVWriter(new FileWriter(csvWriteFile), ',',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
//    writer = new CSVWriter(new FileWriter(csvWriteFile),CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
        log.debug("CSV write initialized");
    }

    public CSVWriteUtil(File file) throws IOException {
        writer = new CSVWriter(new FileWriter(file), ',',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        log.debug("CSV write initialized");
    }

    public CSVWriteUtil(String csvWriteFile, char seperator) throws IOException {
        writer = new CSVWriter(new FileWriter(csvWriteFile));
        log.debug("CSV write initialized with  "+seperator+" delimiter");
    }

    /**
     * To append the csv file
     * @param csvWriteFile
     * @param append
     * @throws IOException
     */
    public CSVWriteUtil(String csvWriteFile, boolean append) throws IOException {
        writer = new CSVWriter(new FileWriter(csvWriteFile,append));
        log.debug("CSV write initialized in append mode..");
    }

    public CSVWriteUtil(File file, boolean append) throws IOException {
        writer = new CSVWriter(new FileWriter(file, append));
        log.debug("CSV write initialized in append mode");
    }

    public void addLineToCSV(String[] entries) throws IOException {
        writer.writeNext(entries);
        log.debug("wrote a line to CSV file");
    }

    public void addAllToCSV(List<String[]> allLines) throws IOException {
        writer.writeAll(allLines);
        log.debug("wrote multiple lines to CSV file");
    }

    public void addAllListToCSV(List<List<String>> allLines) throws IOException {

        List<String[]> listEntries= new ArrayList<String[]>();
        for(List<String> row : allLines) {
            String[] strArr = new String[row.size()];
            for(int i=0;i<row.size();i++) {
                strArr[i]=row.get(i);
            }
            listEntries.add(strArr);
        }

        writer.writeAll(listEntries);
        log.debug("wrote multiple lines to CSV file");
    }

    public void addAllToCSV(List<String[]> allLines,boolean quotesStatus) throws IOException {
        writer.writeAll(allLines,quotesStatus);
        log.debug("wrote multiple lines to CSV file");
    }

    public void closeWriter() throws IOException{
        writer.close();
        log.debug("CSV writer closed");
    }
}
