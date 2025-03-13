package org.example;

import org.example.config.AppConfig;
import org.example.service.Log.ClientComponent;
import org.example.service.Ping.PingSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * This class contains Client related logic
 */
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {

        try {
//            PingSender pingSender = new PingSender();
//            pingSender.sendEcho("Hello");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * This method creates thread for each Server instance
     * @param command
     */
    public void runClient(String command){

        AppConfig appConfig = new AppConfig();
        Properties properties = appConfig.readConfig();
        logger.info("Command to be executed is " + command);

        for (int nodes=0; nodes < Integer.parseInt(properties.getProperty("no.of.machines")); nodes++){
            ClientComponent client = new ClientComponent(properties.getProperty("peer.machine.ip" + nodes),
                    Integer.parseInt(properties.getProperty("peer.machine.port" + nodes)),
                    properties.getProperty("peer.machine.name" + nodes),
                    command);
            client.start();
        }

    }


}

