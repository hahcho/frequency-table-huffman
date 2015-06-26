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

        ExecutorService executor = Executors.newFixedThreadPool(arguments.numberOfThreads());

        long start = Calendar.getInstance().getTimeInMillis();

        Collection<Future<Map<Character, Long>>> frequencyTables = executor.invokeAll(buildCallables(file, arguments.numberOfThreads()));

        Map<Character, Long> accumulatedFrequencyTable = new HashMap<Character, Long>();
        for(Future<Map<Character, Long>> frequencyTableFuture : frequencyTables) {
            Map<Character, Long> frequencyTable = frequencyTableFuture.get();
            for(Character key : frequencyTable.keySet()) {
                Long frequencyForCharacter = frequencyTable.get(key);
                Long currentFrequencyForCharacter = accumulatedFrequencyTable.get(key);
                if(currentFrequencyForCharacter == null) {
                    currentFrequencyForCharacter = 0l;
                }
                accumulatedFrequencyTable.put(key, currentFrequencyForCharacter + frequencyForCharacter);
            }
        }

        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("Time taken: " + ((end - start) / 100));
        printFrequencyTable(accumulatedFrequencyTable);
        executor.shutdown();
    }

    private static Collection<Callable<Map<Character, Long>>> buildCallables(File file, int numberOfThreads) {
        long fileLength = file.length();
        long chunkSize;

        if(fileLength % numberOfThreads == 0) {
            chunkSize = fileLength / numberOfThreads;
        } else {
            chunkSize = fileLength / (numberOfThreads - 1);
        }
        List<Callable<Map<Character, Long>>> callables = new ArrayList<Callable<Map<Character, Long>>>();

        for(int i = 0; i < (numberOfThreads - 1); i++) {
            callables.add(new FrequencyCounter(file, i * chunkSize, chunkSize));
        }

        long remainingStart = (numberOfThreads - 1) * chunkSize;
        callables.add(new FrequencyCounter(file, remainingStart, fileLength - remainingStart));

        return callables;
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
