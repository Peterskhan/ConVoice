package convoice.server.logger;


// Java imports
import java.util.logging.Level;
import java.util.logging.Logger;

// Project imports
import convoice.server.gui.GUIController;

/**
 * The LogManager class is responsible for creating and maintaining
 * the state of the application Loggers, and provide an interface
 * trough which the Loggers are available to the rest of the application.
 * It also has decorator functions which forward log messages to the 
 * graphical user interface.
 */
public class LogManager {
	/** The Logger for general logs. */
	private static Logger m_mainLogger;			
	
	/** The Logger for connection logs. */
	private static Logger m_connectionsLogger;	
	
	/** The Logger for member logs. */
	private static Logger m_membersLogger;		
	
	/**
	 * Static initializer which instantiates the Loggers
	 * for the application.
	 */
	static {
		// Initializing members
		m_mainLogger = Logger.getLogger("mainLogger");
		m_connectionsLogger = Logger.getLogger("connectionsLogger");
		m_membersLogger = Logger.getLogger("membersLogger");
		
		// Setting global logging level
		setGlobalLoggingLevel(Level.SEVERE);
	}
	
	/**
	 * Gets the general purpose Logger instance.
	 * @return The general purpose Logger.
	 */
	public static Logger getMainLogger() {
		return m_mainLogger;
	}
	
	/**
	 * Gets the connections Logger instance.
	 * @return The connections Logger.
	 */
	public static Logger getConnectionsLogger() {
		return m_connectionsLogger;
	}
	
	/**
	 * Gets the members Logger instance.
	 * @return The members Logger.
	 */
	public static Logger getMembersLogger() {
		return m_membersLogger;
	}
	
	/**
	 * Adds a log message with the specified severity to the 
	 * general purpose Logger, and display it on the user interface.
	 * @param level The severity of the log.
	 * @param msg The log message.
	 */
	public static void addMainLog(Level level, String msg) {
		GUIController.addLogsOutputMessage(msg);
		synchronized(m_mainLogger) {
			m_mainLogger.log(level, msg);
		}
	}
	
	/**
	 * Adds a log message with the specified severity to the 
	 * connections Logger, and display it on the user interface.
	 * @param level The severity of the log.
	 * @param msg The log message.
	 */
	public static void addConnectionsLog(Level level, String msg) {
		GUIController.addConnectionsOutputMessage(msg);
		synchronized(m_connectionsLogger) {
			m_connectionsLogger.log(level, msg);
		}
	}
	
	/**
	 * Adds a log message with the specified severity to the 
	 * members Logger, and display it on the user interface.
	 * @param level The severity of the log.
	 * @param msg The log message.
	 */
	public static void addMembersLog(Level level, String msg) {
		GUIController.addMembersOutputMessage(msg);
		synchronized(m_membersLogger) {
			m_membersLogger.log(level, msg);
		}	
	}
	
	/**
	 * Sets the logging level for all Loggers.
	 * @param level The severity level to set for the Loggers.
	 */
	public static void setGlobalLoggingLevel(Level level) {
		m_mainLogger.setLevel(level);
		m_connectionsLogger.setLevel(level);
		m_membersLogger.setLevel(level);
	}
	
};
