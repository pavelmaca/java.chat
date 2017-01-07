package pavelmaca.chat.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Configuration {

    /**
     * Configuration file location
     */
    private static String configFile = "client.properties";

    /**
     * Save config to the  file
     *
     * @param properties
     */
    public static void saveConfig(Properties properties) {
        try {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            properties.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Try load config from the file if exists
     *
     * @return
     */
    public static Properties loadConfig() {
        Properties properties = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return properties;
    }
}
