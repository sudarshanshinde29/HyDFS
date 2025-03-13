package org.example.service.Log;

import org.example.entities.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains command process logic
 */
public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(GrepExecutor.class);
    public static Command processCommand(String request) {
        logger.info("---->Entering processCommand with request: " + request);
        String[] command = request.split(" ");
        if(!command[0].equals("grep")){
            logger.error("Invalid command passed");
            throw new IllegalArgumentException("First parameter should be grep");
        }
        List<Character> optionsList = new ArrayList<>();
        if(command[1].startsWith("-")){
            //handling case of -nci
            String options = command[1];
            for(int i=1;i<options.length();i++){
                optionsList.add(options.charAt(i));
            }
            //handling case of -n -c -i
            int k = 2;
            while(k<command.length){
                String option = command[k];
                if(!option.startsWith("-")){
                    break;
                } else {
                    optionsList.add(option.charAt(1));
                }
                k++;
            }
        }
        logger.info("---->Exiting processCommand");
        return new Command(command[0], optionsList, command[command.length-1].replace("\"", ""));
    }
}
