package com.md.frequencytable;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class FrequencyCounter implements Callable<Map<Character, Long>> {
    private File file;
    private long start;
    private long length;

    public FrequencyCounter(File file, long start, long length) {
        this.file = file;
        this.start = start;
        this.length = length;
    }

    public Map<Character, Long> call() {
        Map<Character, Long> frequencyTable = new HashMap<Character, Long>();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
            reader.skip(start);

            for(long i = 0; i < length; i++) {
                char ch = (char)reader.read();

                Long currentCount = frequencyTable.get(ch);
                if (currentCount == null) {
                    currentCount = 0l;
                }

                frequencyTable.put(ch, currentCount + 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } 
 
        return frequencyTable;
    }

    public static Map<Character, Long> parallelCount(File file, int numberOfThreads) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        Collection<Future<Map<Character, Long>>> frequencyTables = executor.invokeAll(buildCallables(file, numberOfThreads));

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

        executor.shutdown();

        return accumulatedFrequencyTable;
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
}
