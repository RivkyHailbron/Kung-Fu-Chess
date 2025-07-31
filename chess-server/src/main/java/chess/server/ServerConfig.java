package chess.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * מחלקה לטעינת הגדרות השרת מקובץ application.properties
 * Singleton pattern - רק instance אחד בכל האפליקציה
 */
public class ServerConfig {
    private static final String CONFIG_FILE = "/application.properties";
    private static ServerConfig instance;
    private final Properties properties;
    
    private ServerConfig() {
        this.properties = new Properties();
        loadProperties();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("✓ Configuration loaded from " + CONFIG_FILE);
            } else {
                throw new RuntimeException("Configuration file " + CONFIG_FILE + " not found! Please create src/main/resources/application.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration from " + CONFIG_FILE + ": " + e.getMessage(), e);
        }
    }
    
    public int getPort() {
        String portStr = properties.getProperty("server.port");
        if (portStr == null) {
            throw new RuntimeException("server.port not found in application.properties");
        }
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid port in configuration: " + portStr + ". Must be a valid number.", e);
        }
    }
    
    public String getDomain() {
        String domain = properties.getProperty("server.domain");
        if (domain == null) {
            throw new RuntimeException("server.domain not found in application.properties");
        }
        return domain;
    }
    
    public int getGameTimeoutMinutes() {
        String timeoutStr = properties.getProperty("game.timeout.minutes");
        if (timeoutStr == null) {
            throw new RuntimeException("game.timeout.minutes not found in application.properties");
        }
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid game timeout in configuration: " + timeoutStr + ". Must be a valid number.", e);
        }
    }
    
    public int getMaxPlayers() {
        String maxPlayersStr = properties.getProperty("game.max.players");
        if (maxPlayersStr == null) {
            throw new RuntimeException("game.max.players not found in application.properties");
        }
        try {
            return Integer.parseInt(maxPlayersStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid max players in configuration: " + maxPlayersStr + ". Must be a valid number.", e);
        }
    }
    
    public int getMaxClients() {
        String maxClientsStr = properties.getProperty("connection.max.clients");
        if (maxClientsStr == null) {
            throw new RuntimeException("connection.max.clients not found in application.properties");
        }
        try {
            return Integer.parseInt(maxClientsStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid max clients in configuration: " + maxClientsStr + ". Must be a valid number.", e);
        }
    }
    
    public int getConnectionTimeoutSeconds() {
        String timeoutStr = properties.getProperty("connection.timeout.seconds");
        if (timeoutStr == null) {
            throw new RuntimeException("connection.timeout.seconds not found in application.properties");
        }
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid connection timeout in configuration: " + timeoutStr + ". Must be a valid number.", e);
        }
    }
    
    public boolean isDebugEnabled() {
        String debugStr = properties.getProperty("logging.enable.debug");
        if (debugStr == null) {
            throw new RuntimeException("logging.enable.debug not found in application.properties");
        }
        return Boolean.parseBoolean(debugStr);
    }

    public String getLoggingLevel() {
        String level = properties.getProperty("logging.level");
        if (level == null) {
            throw new RuntimeException("logging.level not found in application.properties");
        }
        return level;
    }
    
    public int getConnectionSetupDelayMs() {
        String delayStr = properties.getProperty("connection.setup.delay.ms");
        if (delayStr == null) {
            throw new RuntimeException("connection.setup.delay.ms not found in application.properties");
        }
        try {
            return Integer.parseInt(delayStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid connection setup delay in configuration: " + delayStr + ". Must be a valid number.", e);
        }
    }
    
    public int getConnectionStartDelayMs() {
        String delayStr = properties.getProperty("connection.start.delay.ms");
        if (delayStr == null) {
            throw new RuntimeException("connection.start.delay.ms not found in application.properties");
        }
        try {
            return Integer.parseInt(delayStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid connection start delay in configuration: " + delayStr + ". Must be a valid number.", e);
        }
    }
    
    public String getPlayer1Color() {
        String color = properties.getProperty("game.player1.color");
        if (color == null) {
            throw new RuntimeException("game.player1.color not found in application.properties");
        }
        return color;
    }
    
    public String getPlayer2Color() {
        String color = properties.getProperty("game.player2.color");
        if (color == null) {
            throw new RuntimeException("game.player2.color not found in application.properties");
        }
        return color;
    }
    
    public String getWaitingMessage() {
        String message = properties.getProperty("game.waiting.message");
        if (message == null) {
            throw new RuntimeException("game.waiting.message not found in application.properties");
        }
        return message;
    }
    
    public int getConnectionClearDelayMs() {
        String delayStr = properties.getProperty("connection.clear.delay.ms");
        if (delayStr == null) {
            throw new RuntimeException("connection.clear.delay.ms not found in application.properties");
        }
        try {
            return Integer.parseInt(delayStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid connection clear delay in configuration: " + delayStr + ". Must be a valid number.", e);
        }
    }    /**
     * Get any custom property from application.properties
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get property with default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void printConfiguration() {
        System.out.println("=== Server Configuration ===");
        System.out.println("Port: " + getPort());
        System.out.println("Domain: " + getDomain());
        System.out.println("Game Timeout: " + getGameTimeoutMinutes() + " minutes");
        System.out.println("Max Players: " + getMaxPlayers());
        System.out.println("Max Clients: " + getMaxClients());
        System.out.println("Player 1 Color: " + getPlayer1Color());
        System.out.println("Player 2 Color: " + getPlayer2Color());
        System.out.println("Waiting Message: " + getWaitingMessage());
        System.out.println("Connection Timeout: " + getConnectionTimeoutSeconds() + " seconds");
        System.out.println("Connection Setup Delay: " + getConnectionSetupDelayMs() + " ms");
        System.out.println("Connection Start Delay: " + getConnectionStartDelayMs() + " ms");
        System.out.println("Connection Clear Delay: " + getConnectionClearDelayMs() + " ms");
        System.out.println("Debug Enabled: " + isDebugEnabled());
        System.out.println("Logging Level: " + getLoggingLevel());
        System.out.println("============================");
    }
}
