package org.example;

import org.example.entities.FDProperties;
import org.example.service.FailureDetector.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is main entry point
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.debug("---->Starting Main");
        FDProperties.initialize();
        FDProperties.printFDProperties();
        CommandLine commandLine = new CommandLine();
        commandLine.run();
        System.out.println("Server started");
    }
}