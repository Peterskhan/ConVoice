package convoice.server.server;


// Java imports
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

// Project imports
import convoice.server.channel.ChannelManager;
import convoice.server.connection.ConnectionManager;
import convoice.server.logger.LogManager;
import convoice.server.permission.PermissionManager;
import convoice.server.user.UserManager;

/**
 * The Server class is responsible for managing global server 
 * properties and coordinate configuration loading/saving
 * with other managers.
 * The Server contains only static methods making it easily
 * available to the rest of the application. It's behavior 
 * is similar to a Singleton.
 */
public class Server {
	/** The name of the server. */
	private static String m_name;
	
	/** The version string of the server. */
	private static String m_version;
	
	/** The welcome message of the server. */
	private static String m_welcomeMessage;
	
	/** The properties object storing server properties. */
	private static Properties m_properties;
	
	/**
	 * Initializes the Server.
	 */
	static {
		// Creating properties object
		m_properties = new Properties();
	}
	
	/**
	 * Gets the Properties object of the server.
	 * @return The Properties object storing server properties.
	 */
	public static Properties getProperties() {
		return m_properties;
	}
	
	/**
	 * Gets the name of the server.
	 * @return The name of the server.
	 */
	public static String getName() {
		return m_name;
	}
	
	/**
	 * Gets the version of the server.
	 * @return The version of the server.
	 */
	public static String getVersion() {
		return m_version;
	}
	
	/**
	 * Gets the welcome message of the server.
	 * @return The welcome message of the server.
	 */
	public static String getWelcomeMessage() {
		return m_welcomeMessage;
	}
	
	/**
	 * Loads configuration data from the "server.prop" file of the
	 * specified directory, and initiates configuration loading for
	 * the other managers.
	 * @param configPath The path of the configuration directory.
	 */
	public static void loadConfiguration(String configPath) {
		try {
			// Loading server properties from file
			FileInputStream fis = new FileInputStream(configPath + "/server.prop");
			m_properties.load(fis);
			fis.close();
			
			// Applying configurations to members
			m_name = m_properties.getProperty("name", "conVoice Server");
			m_version = "v1.0.0";
			m_welcomeMessage = m_properties.getProperty("welcomeMessage", "Welcome to the ConVoice server!");
			
			// Loading configuration for other managers
			ChannelManager.loadConfiguration(configPath);
			ConnectionManager.loadConfiguration();
			PermissionManager.loadConfiguration();
			UserManager.loadConfiguration(configPath);
			
			// Logging
			LogManager.addMainLog(Level.CONFIG, "Server configuration loaded.");
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves current server and other manager configurations to the 
	 * specified configuration directory.
	 * @param configPath The path of the configuration directory.
	 */
	public static void saveConfiguration(String configPath) {
		try {			
			// Saving server properties to file
			FileOutputStream fos = new FileOutputStream(configPath + "/server.prop");
			m_properties.store(fos, "conVoice Server Properties");
			fos.close();
			
			// Saving configuration of other managers
			ChannelManager.saveConfiguration(configPath);
			UserManager.saveConfiguration(configPath);
			
			// Logging
			LogManager.addMainLog(Level.CONFIG, "Server configuration saved.");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

};
