package com.md.frequencytable;

import org.apache.commons.cli.*;

public class Arguments { 
    private String filepath;
    private int numberOfThreads;
    private boolean isQuiet;

    public Arguments(String args[]) throws ParseException {
        Options options = new Options();
        options.addOption("q", false, "Suppress extra output");
        options.addOption("t", true, "Number of threads");
        options.addOption("f", true, "File path to read from");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        this.numberOfThreads = Integer.parseInt(cmd.getOptionValue("t"));
        this.filepath = cmd.getOptionValue("f");
    }

    public boolean isQuiet() {
        return isQuiet;
    }

    public String filepath() {
        return filepath;
    }

    public int numberOfThreads() {
        return numberOfThreads;
    }
}
