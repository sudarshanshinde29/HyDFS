package org.example.config;

import org.example.entities.FDProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This Class is used to read properties
 */
public class AppConfig {

    public Properties readConfig(){
        Properties prop = null;
        try {
            prop = readPropertiesFile("application.properties");
        }catch (IOException e){
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * This functions reads a property file and passes the values with the properties object.
     * @param fileName Name of the file with th properties
     * @return return Properties object with key value pairs.
     */
    public static Properties readPropertiesFile(String fileName) throws IOException {
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            fis.close();
        }
        return prop;
    }
}
