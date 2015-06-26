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
}
