package com.md.frequencytable;
import org.apache.commons.cli.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class App {
    public static class FrequencyCounter implements Callable<Map<Character, Long>> {
        private File file;
        private long start;
        private long length;

        public FrequencyCounter(File file, long start, long length) {
            this.file = file;
            this.start = start;
            this.length = length;
        }

        public Map<Character, Long> call() {
            InputStreamReader reader = null;
            Map<Character, Long> frequencyTable = new HashMap<Character, Long>();
            try {
                reader = new InputStreamReader(new FileInputStream(file));
                reader.skip(start);

                for(long i = 0; i < length; i++) {
                    char ch = (char)reader.read();
                    if (ch < 0) {
                        System.out.println("ERROR " + (int)ch);
                    }

                    Long currentCount = frequencyTable.get(ch);
                    if (currentCount == null) {
                        currentCount = 0l;
                    }

                    frequencyTable.put(ch, currentCount + 1);
                }

                reader.close();
            } catch(IOException e) {
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch(IOException e) {
                    }
                }
            }

            return frequencyTable;
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("q", false, "Suppress extra output");
        options.addOption("t", true, "Number of threads");
        options.addOption("f", true, "File path to read from");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        int numberOfThreads = Integer.parseInt(cmd.getOptionValue("t"));
        String filepath = cmd.getOptionValue("f");

        File file = new File(filepath);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file));
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        long start = Calendar.getInstance().getTimeInMillis();
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

        long end = Calendar.getInstance().getTimeInMillis();
        System.out.println("Time taken: " + ((end - start) / 100));
        printFrequencyTable(accumulatedFrequencyTable);
        executor.shutdown();
    }

    private static Collection<Callable<Map<Character, Long>>> buildCallables(File file, int numberOfThreads) throws IOException {
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
