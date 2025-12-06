package com.apitesting.utilities;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CSVReadUtil {

     private CSVReader reader;

    /**
     * Creates a reader object and initializes with the csvfile provided.
     *
     * @param csvReadFile
     * @throws IOException
     */
    public CSVReadUtil(String csvReadFile) throws IOException {
        reader = new CSVReader(new FileReader(csvReadFile));
        log.debug("CSV reader initialized");
    }

    /**
     * Creates a reader object and initializes with the csvfile provided where
     * data are seperator by a custom seperator.
     *
     * @param csvReadFile
     * @throws IOException
     */
    public CSVReadUtil(String csvReadFile, char seperator) throws IOException {
        reader = new CSVReader(new FileReader(csvReadFile));
        log.debug("CSV reader initialized");
    }

    /**
     * returns the list containing arrays of strings; each array of string
     * represents a line in CSV
     *
     * @return List of arrays of strings
     * @throws IOException
     * @throws CsvException
     */
    public List<String[]> getEntriesAsList() throws IOException, CsvException {
        List<String[]> myEntries = reader.readAll();
        log.debug("Converted CSV file into List of String arrays");
        return myEntries;
    }

    public List<List<String>> getEntriesAsListOfList() throws IOException, CsvException {
        List<List<String>> listEntries= new ArrayList<List<String>>();
        List<String[]> myEntries = reader.readAll();
        for(String[] row:myEntries) {
            List<String> listRow= new ArrayList<String>();
            for(String str:row) {
                listRow.add(str);
            }
            listEntries.add(listRow);
        }
        log.debug("Converted CSV file into List of String arrays");
        return listEntries;
    }

    public String[] getEntry() throws IOException, CsvValidationException
    {
        log.debug("Extracting one entry from CSV File.");
        return reader.readNext();
    }

    public void closeReader() throws IOException {
        reader.close();
        log.debug("CSV writer closed");
    }

    public static void main(String []args){

    }
}