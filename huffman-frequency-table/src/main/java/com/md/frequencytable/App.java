package com.md.frequencytable;

import org.apache.commons.cli.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.md.frequencytable.*;

public class App {
    public static void main(String[] args) throws Exception {
        Arguments arguments = null;
        try {
            arguments = new Arguments(args);
        } catch(ParseException e) {
            System.out.println("Invalid arguments!");
            return;
        }

        File file = new File(arguments.filepath());

        long start = Calendar.getInstance().getTimeInMillis();
        Map<Character, Long> accumulatedFrequencyTable = FrequencyCounter.parallelCount(file, arguments.numberOfThreads());
        long end = Calendar.getInstance().getTimeInMillis();

        System.out.println("Time taken: " + ((end - start) / 100));
        printFrequencyTable(accumulatedFrequencyTable);
    }


    private static void printFrequencyTable(Map<Character, Long> frequencyTable) {
        List<Character> sortedCharacters = new ArrayList<Character>(frequencyTable.keySet());
        Collections.sort(sortedCharacters);
        for(Character key : sortedCharacters){
            Long frequencyForCharacter = frequencyTable.get(key);
            System.out.println(key + " " + frequencyForCharacter);
        }
    }
}
