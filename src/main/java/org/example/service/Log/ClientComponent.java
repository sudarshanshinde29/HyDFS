package org.example.service.Log;

import org.example.config.CLIPrinter;
import org.example.entities.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class contains the multithreaded code for connecting to the Server
 */
public class ClientComponent extends Thread {

    String ipAddress;
    int port;
    String command;
    String machineName;
    private static final Logger logger = LoggerFactory.getLogger(ClientComponent.class);

    public ClientComponent(String ipAddress, int port, String machineName, String command) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.machineName = machineName;
            this.command = command;
    }

    /**
     * This functions connects to Server and passes the command and then prints and save the result.
     */
    public void run() {
        String threadName = Thread.currentThread().getName() + ": ";
        logger.info( threadName + "establishing a connection to machine : " + machineName + " " + ipAddress + " " + port);

        // establish a connection
        try {
            Socket socket = null;
            ObjectInputStream ois = null;

            try {
                socket = new Socket(ipAddress, port);
                socket.setSoTimeout(600000);
                logger.info(machineName + " " + "Connected");
            }
            catch (Exception e) {
                throw new RuntimeException("Unable to connect the machine with Machine Name " + machineName + " IP Address " + ipAddress + " and Port " + port + " due to " + e.getMessage());
            }

            try {
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeUTF(command);
            } catch (Exception e) {
                throw new RuntimeException("Not able to send command " + command);
            }

            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            String response = "";
            while(!response.equals("Query Completed")) {
                response = dataInputStream.readUTF();
                if(response.contains("Query Failed")) {
                    throw new RuntimeException(response);
                }
                logger.info(machineName + " " + response);
            }

            Command c = CommandProcessor.processCommand(command);

            try {
                ois = new ObjectInputStream(socket.getInputStream());
                List<String> obj = (List<String>) ois.readObject();
                CLIPrinter cliPrinter = new CLIPrinter();
                cliPrinter.printResult(obj, c.getOptionsList(), machineName);

                Path filePath = Paths.get(machineName + "GrepOutput.txt");
                // Write the list to the file, creating it if it doesn't exist
                Files.write(filePath, obj);
                logger.info("File written successfully for machine : " + machineName);
            }catch (Exception e) {
                throw new RuntimeException("Error occured while processing the result");
            }

            //close the connection
            socket.close();
        }
        catch (Exception i) {
            logger.error(machineName + " " + i.getMessage());
        }

    }
}
