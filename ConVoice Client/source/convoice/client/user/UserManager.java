package convoice.client.user;


// Java imports
import java.util.HashMap;
import java.util.Map;

// Project imports
import convoice.client.channel.ChannelManager;
import convoice.client.gui.GUIController;
import convoice.client.channel.Channel;

/**
 * The UserManager class provides the most common administrative
 * functionality concerning users. 
 * It is responsible for the management  of User objects like creation, 
 * deletion and providing access to the rest of the application.
 * The UserManager class contains only static fields and methods, making
 * it easily available within the application. It's behavior is similar
 * to a Singleton. Methods of the UserManager are synchronized with other
 * managers, so any changes made via the UserManager will be reflected
 * everywhere else in the application.
 */
public class UserManager {
	/** The created users mapped by their IDs. */
	private static Map<Integer, User> m_users;
	
	/** The assigned ID of the client */
	private static int m_ownID;
	
	/**
	 * Initializes the map of users. For faster access to a specific user, 
	 * the container is mapped  with the user's ID.
	 */
	static {
		// Initializing members
		m_users = new HashMap<Integer, User>();
	}
	
	/**
	 * Creates a new User and adds it to the manager.
	 * @param username The username.
	 * @param nickname The nickname.
	 * @param userID The ID of the user.
	 * @param channelID The ID of the user's current channel
	 */
	public static void createUser(String username, String nickname, int userID, int channelID) {
		// Creating the user data
		UserData userData = new UserData(username, nickname);
		
		// Creating the user
		User user = new User(userData, userID, channelID);
		
		// Adding the user
		synchronized(m_users) {
			m_users.put(userID, user);
		}
		
		// Modifying the user's channel
		Channel channel = ChannelManager.getChannel(channelID);
		synchronized(channel) {
			channel.addUser(userID);
		}
		
		// Updating the user interface
		GUIController.userCreatedUpdate(user);
	}
	
	/**
	 * Moves the specified user to the specified channel.
	 * @param userID The user to move.
	 * @param channelID The channel to move to.
	 */
	public static void moveUser(int userID, int channelID) {
		
		// Getting the user
		User user = getUser(userID);
				
		// Getting the involved channels
		Channel oldChannel = ChannelManager.getChannel(user.getChannel());
		Channel newChannel = ChannelManager.getChannel(channelID);
		
		// Modifying channels and user
		synchronized(oldChannel) {
			oldChannel.removeUser(userID);
		}
		synchronized(newChannel) {
			newChannel.addUser(userID);
		}
		synchronized(user) {
			user.setChannel(channelID);
		}
		
		// Checking if self move
		if(userID == m_ownID) {
			ChannelManager.setOwnChannelID(channelID);
			
			// Updating user interface
			GUIController.channelSwitchedUpdate(channelID);
		}
		
		// Updating the user interface
		GUIController.userMovedUpdate(userID, channelID);
	}
	
	/**
	 * Deletes the user from the manager. 
	 * @param userID The ID of the user to delete.
	 */
	public static void deleteUser(int userID) {
		// Modifying the user's channel
		Channel channel = ChannelManager.getChannel(getUser(userID).getChannel());
		synchronized(channel) {
			channel.removeUser(userID);
		}
		
		// Updating the user interface
		GUIController.userDeletedUpdate(userID);
		
		// Deleting the user
		synchronized(m_users) {
			m_users.remove(userID);
		}	
	}
	
	/**
	 * Returns a User object by it's ID.
	 * @param userID The ID of the user.
	 * @return The user with the specified ID.
	 */
	public static User getUser(int userID) {
		synchronized(m_users) {
			return m_users.get(userID);
		}	
	}
	
	/**
	 * Sets the client's own user ID.
	 * The ID is received from the server upon connection,
	 * and is used by the client to identify itself when
	 * communicating with the server.
	 * @param id The ID of the client.
	 */
	public static void setOwnID(int id) {
		m_ownID = id;
	}
	
	/**
	 * Gets the client's own user ID.
	 * @return The client's user ID.
	 */
	public static int getOwnID() {
		return m_ownID;
	}
	
};