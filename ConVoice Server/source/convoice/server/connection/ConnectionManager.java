package convoice.server.connection;


// Java imports
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// Project imports
import convoice.server.gui.GUIController;
import convoice.server.logger.LogManager;
import convoice.server.server.Server;
import convoice.server.user.User;
import convoice.server.user.UserManager;

/**
 * The ConnectionManager class provides an interface for all network
 * operations within the application. 
 * It is responsible for creating the listener which will handle incoming 
 * connections, as well as managing handlers which will handle existing 
 * connections. It handles several kinds of notifications that are meant 
 * to be delivered to all clients as well. The ConnectionManager is 
 * synchronized with all the other managers.
 */
public class ConnectionManager {
	/** The ConnectionListener of the server. */
	private static ConnectionListener m_listener;
	
	/** The list of ConnectionHandlers. */
	private static List<ConnectionHandler> m_handlers;	
	
	/** The running state-flag. */
	private static boolean m_running; 
	
	/** The port number of the ConnectionListener. */
	private static int m_port;
	
	/** The maximum number of connection handlers. */
	private static int m_maxHandlers;
	
	/** The maximum number of users per connection handler. */
	private static int m_maxUserPerHandler;				
	
	/**
	 * Initializes the ConnectionHandler.
	 */
	static {
		// Initializing members
		m_listener = null; 
		m_handlers = new ArrayList<ConnectionHandler>();
		m_running = false;
	}
	
	/**
	 * Resets the ConnectionManager by clearing the list of handlers 
	 * and deleting the listener object. No shutdown operations are 
	 * performed by this method and may only be used when the server
	 * is not running.
	 */
	public static void reset() {
		// Clearing the list of handlers
		synchronized(m_handlers) {
			m_handlers.clear();
		}
		
		// Stopping the listener
		if(m_listener != null) {
			m_listener.stop();
		}
	}
	
	/**
	 * Loads connection configuration from the global Server properties,
	 * and resets the manager.
	 */
	public static void loadConfiguration() {
		// Performing manager reset
		reset();
		
		// Getting manager properties
		m_port = Integer.parseInt(Server.getProperties().getProperty("port", "6969"));
		m_maxHandlers = Integer.parseInt(Server.getProperties().getProperty("maxHandlers", "4"));
		m_maxUserPerHandler = Integer.parseInt(Server.getProperties().getProperty("maxUserPerHandler","25"));
		
		// Logging
		LogManager.addConnectionsLog(Level.CONFIG, "Connections configuration loaded.");
	}
	
	/**
	 * Loads connection configuration from the global Server properties
	 * without resetting the manager.
	 */
	public static void reloadConfiguration() {
		// Getting manager properties
		m_port = Integer.parseInt(Server.getProperties().getProperty("port", "6969"));
		m_maxHandlers = Integer.parseInt(Server.getProperties().getProperty("maxHandlers", "4"));
		m_maxUserPerHandler = Integer.parseInt(Server.getProperties().getProperty("maxUserPerHandler","25"));
		
		// Logging
		LogManager.addConnectionsLog(Level.CONFIG, "Connections configuration reloaded.");
	}
	
	/**
	 * Creates a new ConnectionHandler, and starts it on 
	 * a separate thread.
	 * @return The created ConnectionHandler.
	 */
	private static ConnectionHandler createHandler() {
		// Creating handler on a new thread
		ConnectionHandler handler = new ConnectionHandler();
		Thread thread = new Thread(handler);
		
		// Adding the handler
		synchronized(m_handlers) {
			m_handlers.add(handler);
		}
		
		// Updating the user interface
		GUIController.setConnectionsHandlersCount(m_handlers.size());
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "Connection handler created.");
		
		// Starting the handler
		thread.start();
		
		// Returning the handler
		return handler;
	}
	
	/**
	 * Starts the ConnectionListener, which will listen for incoming
	 * connections on a separate thread. Also creates the first of
	 * the ConnectionHandlers.
	 */
	public static void start() {
		// Setting state-flag
		m_running = true;
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "-----------------\nServer started.");
		
		// Starting the listener
		m_listener = new ConnectionListener(m_port);
		Thread thread = new Thread(m_listener);
		thread.start();
		
		// Starting the first handler
		createHandler();
		
		// Updating the user interface
		GUIController.setConnectionsServerStatus("Active");
	}
	
	/**
	 * Issues the ConnectionListener and all ConnectionHandlers to stop.
	 * These components will not stop functioning immediately, and will
	 * perform shutdown operations before actually stopping.
	 */
	public static void stop() {
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "Server stopped.");
		
		// Stopping the listener
		if(m_listener != null) {
			m_listener.stop();
		}
		
		// Stopping the handlers
		synchronized(m_handlers) {
			for(ConnectionHandler handler : m_handlers) {
				handler.stop();
			}
		}
		
		// Deleting the handlers
		synchronized(m_handlers) {
			m_handlers.clear();
		}
		
		// Updating the user interface
		GUIController.setConnectionsServerStatus("Inactive");
		GUIController.setConnectionsHandlersCount(m_handlers.size());
		
		// Setting state-flag
		m_running = false;
	}
	
	/**
	 * Returns whether the server is running or not.
	 * Note that the running status returned by this method is
	 * set to true when start is called, and false when stop finished,
	 * thus it does not handle the transient phase of starting wind-up,
	 * and stopping wind-down.
	 * @return True if the server is running, false otherwise.
	 */
	public static boolean isRunning() {
		return m_running;
	}
	
	/**
	 * Returns a running ConnectionHandler.
	 * The algorithm searches the present handlers for the one
	 * with the least active connections. If all handlers exceed
	 * the specified limit of connections, attempts to create
	 * a new handler. The maximum number of handlers is also
	 * limited.
	 * @return A ConnectionHandler, or null if none are available.
	 */
	public static ConnectionHandler getHandler() {
		int bestClientCount = m_maxUserPerHandler;
		int bestIndex = -1;
		
		synchronized(m_handlers) {
			// Searching for least busy handler
			for(int i = 0; i < m_handlers.size(); i++) {
				int count = m_handlers.get(i).getUserCount();
				
				if( count < bestClientCount) {
					bestClientCount = count;
					bestIndex = i;
				}
			}
			
			// Found suitable handler
			if(bestIndex != -1) {
				return m_handlers.get(bestIndex);
			} 
			else {
				// Creating new handler
				if(m_handlers.size() < m_maxHandlers) {
					return createHandler();
				}
				// No more handler can be created
				else {
					return null;
				}
			}	
		}
	}
	
	/**
	 * Returns the port number on which the listener is
	 * operating.
	 * @return The port of the listener.
	 */
	public static int getPort() {
		return m_port;
	}
	
	/**
	 * Returns the number of clients connected to the server.
	 * @return The number of clients.
	 */
	public static int getClientCount() {
		// Summing users of handlers
		int sum = 0;
		
		synchronized(m_handlers) {
			for(ConnectionHandler handler : m_handlers) {
				sum += handler.getUserCount();
			}
		}
		
		return sum;
	}
	
	/**
	 * Notifies all clients that a channel has been created.
	 * @param channelID The ID of the channel.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword Does the channel have a password?
	 * @param maxClients The maximum number of users of the channel.
	 * @param permanent Is the channel permanent?
	 */
	public static void channelCreatedNotify(int channelID, String name, String topic, String description,
											boolean hasPassword, int maxClients, boolean permanent) {
		try {
			// Getting the list of users
			Map<Integer, User> users = UserManager.getAllUsers();
			
			for(User user : users.values()) {
				// Writing channel data
				Socket socket = user.getSocket();
				synchronized(socket) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					dos.writeInt(MessageType.CHANNEL_CREATED.toInteger());
					dos.writeInt(channelID);
					dos.writeUTF(name);
					dos.writeUTF(topic);
					dos.writeUTF(description);
					dos.writeBoolean(hasPassword);
					dos.writeInt(maxClients);
					dos.writeBoolean(permanent);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Notifies all clients that a channel has been modified.
	 * @param channelID The ID of the channel.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword Does the channel have a password?
	 * @param maxClients The maximum number of users of the channel.
	 * @param permanent Is the channel permanent?
	 */
	public static void channelModifiedNotify(int channelID, String name, String topic, String description,
											 boolean hasPassword, int maxClients, boolean permanent) {
		try {
			// Getting the list of users
			Map<Integer, User> users = UserManager.getAllUsers();

			for(User user : users.values()) {
				// Writing channel data
				Socket socket = user.getSocket();
				synchronized(socket) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					dos.writeInt(MessageType.CHANNEL_MODIFIED.toInteger());
					dos.writeInt(channelID);
					dos.writeUTF(name);
					dos.writeUTF(topic);
					dos.writeUTF(description);
					dos.writeBoolean(hasPassword);
					dos.writeInt(maxClients);
					dos.writeBoolean(permanent);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Notifies all clients that a channel has been deleted.
	 * @param channelID The ID of the channel.
	 */
	public static void channelDeletedNotify(int channelID) {
		try {
			// Getting the list of users
			Map<Integer, User> users = UserManager.getAllUsers();

			for(User user : users.values()) {
				// Writing channel data
				Socket socket = user.getSocket();
				synchronized(socket) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					dos.writeInt(MessageType.CHANNEL_DELETED.toInteger());
					dos.writeInt(channelID);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Notifies all clients that a user has been created,
	 * aka. joined the server.
	 * @param userID The ID of the user.
	 * @param username The username of the user.
	 * @param nickname The nickname of the user.
	 */
	public static void userCreatedNotify(int userID, String username, String nickname) {
		try {
			// Getting the list of users
			Map<Integer, User> users = UserManager.getAllUsers();

			for(User user : users.values()) {
				// Writing user data
				Socket socket = user.getSocket();
				synchronized(socket) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					dos.writeInt(MessageType.USER_CREATED.toInteger());
					dos.writeInt(userID);
					dos.writeUTF(username);
					dos.writeUTF(nickname);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Notifies all clients that a user has moved to another channel.
	 * @param userID The ID of the user.
	 * @param channelID The ID of the channel the user moved to.
	 */
	public static void userMovedNotify(int userID, int channelID) {
		try {
			// Getting the list of users
			Map<Integer, User> users = UserManager.getAllUsers();

			for(User user : users.values()) {
				// Writing user data
				Socket socket = user.getSocket();
				synchronized(socket) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					dos.writeInt(MessageType.USER_MOVED.toInteger());
					dos.writeInt(userID);
					dos.writeInt(channelID);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Notifies all clients that a user has been deleted,
	 * aka. left the server.
	 * @param userID The ID of the user.
	 */
	public static void userDeletedNotify(int userID) {
		try {
			// Getting the list of users
			Map<Integer, User> users = UserManager.getAllUsers();

			for(User user : users.values()) {
				// Writing user data
				Socket socket = user.getSocket();
				synchronized(socket) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					dos.writeInt(MessageType.USER_DELETED.toInteger());
					dos.writeInt(userID);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
};
	
	

