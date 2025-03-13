package org.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class contains Printing logic with lock
 */
public class CLIPrinter {

    public ReentrantLock lock = new ReentrantLock();
    private static final Logger logger = LoggerFactory.getLogger(CLIPrinter.class);

    /**
     * This method processes the grep command and return the grep command result.
     * @param results the result returned by the Server.
     * @param optionsList options passed for grep command.
     * @param machineName Name of the machine from which the result was received.
     * @return returns true if output is successfully printed.
     */
    public boolean printResult(List<String> results, List<Character> optionsList, String machineName) {
        try{
            lock.lock();
            String threadName = Thread.currentThread().getName();
            System.out.println( "Logs for " + machineName);
            for (String result : results) {System.out.println(result);}
            if(!optionsList.isEmpty() && optionsList.contains('c'))
                logger.info( machineName + " No of lines found are : " + results.get(0));
            else
                logger.info( machineName + " No of lines found are : " + results.size());
            return true;
        }catch (Exception e){
            return false;
        }finally {
            lock.unlock();
        }
    }
}
