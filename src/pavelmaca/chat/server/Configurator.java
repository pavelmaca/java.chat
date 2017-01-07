package pavelmaca.chat.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Configurator {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * @return valid listening port
     */
    public static int getListeningPort() {
        System.out.println("Enter listening port:");
        int port = 0;
        boolean first = true;
        while (port >= 65535 || port <= 0) {
            if (!first) {
                System.out.println("Port number must be between 1 and 65535");
            } else {
                first = false;
            }
            try {
                port = scanner.nextInt();
            } catch (InputMismatchException e) {
                scanner.next();
                //e.printStackTrace();
            }
        }
        return port;
    }

    /**
     * Try load saved DB config, or ask user for input
     *
     * @param configFile
     * @return Database configuration
     */
    public static Properties getDatabaseConfig(String configFile) {
        Properties properties = new Properties();
        try {
            // load connection from config, if exists
            FileInputStream inputStream = new FileInputStream(configFile);
            properties.load(inputStream);
        } catch (IOException e) {
            // Setup connection
            System.out.println("Can not load database configuration.");

            System.out.println("host:");
            properties.setProperty("host", scanner.next());

            System.out.println("port:");
            properties.setProperty("port", scanner.next());

            System.out.println("username:");
            properties.setProperty("user", scanner.next());

            System.out.println("password:");
            properties.setProperty("password", scanner.next());

            System.out.println("database:");
            properties.setProperty("database", scanner.next());
            try {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                properties.store(outputStream, null);
            } catch (IOException e1) {
                //e1.printStackTrace();
                System.out.println("Can not save database configuration.");
            }
        }

        return properties;
    }
}
