package org.example.service.Log;

import org.example.entities.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;
import org.unix4j.unix.grep.GrepOptionSet_Fcilnvx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains grep related operations
 */
public class GrepExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GrepExecutor.class);
    private String filePath = "";
    public GrepExecutor(String filePath) {
            this.filePath = filePath;
    }

    /**
     * This method processes the grep command and return the grep command result
     * @param request
     * @return list of strings
     */
    public List<String> executeGrep(String request){
        logger.info("---->Entering Execute Grep");
        Command command = CommandProcessor.processCommand(request);
        GrepOptionSet_Fcilnvx grepOptions = convertGrepOptions(command.getOptionsList());
        List<String> grepOutput = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (grepOptions != null) {
                grepOutput = Unix4j.grep(grepOptions, command.getPattern(), file).toStringList();
            } else {
                grepOutput = Unix4j.grep(command.getPattern(), file).toStringList();
            }
        }catch (Exception e){
            throw new RuntimeException("Unable to execute grep", e);
        }
        logger.info("<----Exiting Execute Grep");
        return grepOutput;
    }

    /**
     * This method converts parsed grep options to GrepOptionSet_Fcilnvx grep options
     * @param grepOptionsList
     * @return GrepOptionSet_Fcilnvx
     */
    private  GrepOptionSet_Fcilnvx convertGrepOptions(List<Character>grepOptionsList) {
        logger.info("---->Entering convertGrepOptions with options"+grepOptionsList);
        GrepOptionSet_Fcilnvx grepOptions = null;
        if (grepOptionsList == null || grepOptionsList.isEmpty()) {
            return grepOptions;
        }
        for (char option : grepOptionsList) {
            if (option == 'n') {
                grepOptions = (grepOptions == null) ? Grep.Options.n : grepOptions.n;
            } else if (option == 'c') {
                grepOptions = (grepOptions == null) ? Grep.Options.c : grepOptions.c;
            } else if (option == 'l') {
                grepOptions = (grepOptions == null) ? Grep.Options.l : grepOptions.l;
            } else if (option == 'x') {
                grepOptions = (grepOptions == null) ? Grep.Options.x : grepOptions.x;
            } else if (option == 'i') {
                grepOptions = (grepOptions == null) ? Grep.Options.i : grepOptions.i;
            } else if (option == 'v') {
                grepOptions = (grepOptions == null) ? Grep.Options.v : grepOptions.v;
            } else if (option == 'F') {
                grepOptions = (grepOptions == null) ? Grep.Options.F : grepOptions.F;
            }
        }
        logger.info("<----Exiting convertGrepOptions conversion completed");
        return grepOptions;
    }
}


