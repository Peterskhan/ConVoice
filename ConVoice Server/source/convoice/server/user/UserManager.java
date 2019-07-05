package convoice.server.user;


// Java imports
import java.util.Map;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.net.Socket;

// Project imports
import convoice.server.channel.Channel;
import convoice.server.channel.ChannelManager;
import convoice.server.connection.ConnectionManager;
import convoice.server.gui.GUIController;
import convoice.server.logger.LogManager;
import convoice.server.permission.PermissionManager;
import convoice.server.user.User;
import convoice.server.user.UserData;

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
	/** The created members mapped by their username. */
	private static Map<String, UserData> m_members; 
	
	/** The created users mapped by their IDs. */
	private static Map<Integer, User> m_users;		
	
	/** The next assignable user ID. */
	private static int m_idGenerator;				
	
	/**
	 * Initializes the map of users, members and the ID generator.
	 * For faster access to a specific user, the container is mapped
	 * with the user's ID. The container of server members is mapped
	 * with their username. The ID generator field gets incremented
	 * when a new user is created, ensuring uniqueness.
	 */
	static {
		// Initializing members
		m_members = new HashMap<String, UserData>();
		m_users = new HashMap<Integer, User>();
		m_idGenerator = 1;
	}
	
	/**
	 * Resets the UserManager by clearing the map of members and users,
	 * and resetting the user ID generator. No shutdown operations are
	 * performed by this method and may be only used when the server 
	 * is not running.
	 */
	public static void reset() {
		// Clearing the map of members
		synchronized(m_members) {
			m_members.clear();
		}
		
		// Clearing the map of users
		synchronized(m_users) {
			m_users.clear();
		}
		
		// Resetting the id generator
		m_idGenerator = 1;
	}
	
	/**
	 * Resets the UserManager and loads server members from the "members.xml"
	 * file of the specified directory.
	 * @param configPath The path of the configuration directory.
	 */
	public static void loadConfiguration(String configPath) {
		// Performing manager reset
		reset();
		
		// Loading members from file
		loadMembers(configPath + "/members.xml");
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Users configuration loaded.");
	}
	
	/**
	 * Saves the list of members to the "members.xml" file of the specified directory.
	 * @param configPath The path of the configuration directory.
	 */
	public static void saveConfiguration(String configPath) {
		// Saving members to file
		saveMembers(configPath + "/members.xml");
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Users configuration saved.");
	}
		
	/**
	 * Creates a new User and adds it to the manager.
	 * @param username The username.
	 * @param nickname The nickname.
	 * @param password The password.
	 * @param socket The TCP socket for network communication.
	 * @return The ID of the created user.
	 */
	public static int createUser(String username, String nickname, String password, Socket socket) {
		// Creating the user data
		UserData userData = new UserData(username, nickname, password);
		
		// Creating the user
		int id = generateID();
		User user = new User(userData, id, ChannelManager.DEFAULT_CHANNEL_ID, socket);
		
		// Modifying the default channel
		Channel defaultChannel = ChannelManager.getChannel(ChannelManager.DEFAULT_CHANNEL_ID);
		synchronized(defaultChannel) {
			defaultChannel.addUser(id);
		}
		
		// Requesting user notifications
		ConnectionManager.userCreatedNotify(id, username, nickname);
		
		// Updating user interface
		GUIController.userCreatedUpdate(user);
		
		// Adding the user
		synchronized(m_users) {
			m_users.put(id, user);
		}
		
		// Logging
		LogManager.addMainLog(Level.INFO, "User ID: " + id + " created.");
		
		// Returning the ID of the user
		return id;
	}
	
	/**
	 * Moves the specified user to the specified channel.
	 * @param userID The user to move.
	 * @param channelID The channel to move to.
	 * @param password The password of the channel if any.
	 */
	public static void moveUser(int userID, int channelID, String password) {
		
		// Getting the user
		User user = getUser(userID);
		
		// Getting involved channels
		Channel oldChannel = ChannelManager.getChannel(user.getChannel());
		Channel newChannel = ChannelManager.getChannel(channelID);
		
		// Checking if can move user to the new channel
		synchronized(newChannel) {
			// Checking channel password
			if(newChannel.getChannelData().hasPassword() && !newChannel.getChannelData().getPassword().equals(password)) {
				return;
			}
			
			// Checking channel capacity
			if(newChannel.getUsers().size() == newChannel.getChannelData().getMaxClients()) {
				return;
			}
		}
		
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
		
		// Updating user interface
		GUIController.userMovedUpdate(userID, channelID);
		
		// Logging
		LogManager.addMainLog(Level.INFO, "User ID: " + userID + " moved to channel ID: " + channelID);
		
		// Requesting user notifications
		ConnectionManager.userMovedNotify(userID, channelID);
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
		
		// Deleting the user 
		synchronized(m_users) {
			m_users.remove(userID);
		}
		
		// Requesting user notifications
		ConnectionManager.userDeletedNotify(userID);
		
		// Updating user interface
		GUIController.userDeletedUpdate(userID);
		
		// Logging
		LogManager.addMainLog(Level.INFO, "User ID: " + userID + " deleted.");
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
	 * Adds a UserData object to the list of members.
	 * @param username The username of the member.
	 * @param password The password of the member.
	 */
	public static void addMember(String username, String password) {
		UserData member = new UserData(username, null, password);
		synchronized(m_members) {
			m_members.put(username, member);
		}
		
		// Updating user interface
		GUIController.memberCreatedUpdate(member);
		
		// Logging
		LogManager.addMembersLog(Level.INFO, "Member: " + username + " added.");
	}
	
	/**
	 * Removes a member with the specified username.
	 * @param username The username of the member being deleted.
	 */
	public static void deleteMember(String username) {
		synchronized(m_members) {
			m_members.remove(username);
		}
		
		// Updating user interface
		GUIController.memberDeletedUpdate(username);
		
		// Logging
		LogManager.addMembersLog(Level.INFO, "Member: " + username + " deleted.");
	}
	
	/**
	 * Replaces a member with it's modified version.
	 * @param username The current username of the member.
	 * @param newUsername The new username of the member.
	 * @param password The password of the member.
	 */
	public static void modifyMember(String username, String newUsername, String password) {		
		UserData member = m_members.get(username);
		String oldUsername = member.getUsername();		
		member.setUsername(newUsername);
		member.setPassword(password);
		
		// Updating user interface
		GUIController.memberModifiedUpdate(oldUsername, member);
		
		// Logging
		LogManager.addMembersLog(Level.INFO, "Member: " + username + " modified.");
	}
	
	/**
	 * Registers a guest at the PermissionManager.
	 * @param id The assigned ID of the guest.
	 */
	public static void loginGuest(int id) {
		PermissionManager.registerGuest(id);
	}
	
	/**
	 * Registers a member at the PermissionManager.
	 * @param id The assigned ID of the member.
	 */
	public static void loginMember(int id) {
		PermissionManager.registerMember(id);
	}
	
	/**
	 * Checks whether the provided username and password matches 
	 * with the list of server members.
	 * @param username The username.
	 * @param password The password to the username.
	 * @return True if the password matches with the username.
	 */
	public static boolean validateMember(String username, String password) {
		try {
			// Checking if the password matches the username
			synchronized(m_members) {
				return m_members.get(username).getPassword().equals(password);
			}
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Loads the list of members from an XML-file.
	 * @param fileName The name of the file.
	 */
	public static void loadMembers(String fileName) {
		try {
			// Creating XML-objects for deserialization
			FileInputStream fis = new FileInputStream(fileName);
			JAXBContext xmlContext = JAXBContext.newInstance(UserSerializer.class);
			Unmarshaller xmlUnmarshaller = xmlContext.createUnmarshaller();
			
			// Deserializing member data
			UserSerializer xmlSerializer = (UserSerializer) xmlUnmarshaller.unmarshal(fis);
			
			// Feeding deserialized data to the UserManager
			for(UserData data : xmlSerializer.getData()) {
				// Adding members
				addMember(data.getUsername(), data.getPassword());
			}
			
			// Closing the file 
			fis.close();
			
			// Logging
			LogManager.addMembersLog(Level.CONFIG, "Members configuration loaded.");
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(JAXBException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the list of members to an XML-file.
	 * @param fileName The name of the file.
	 */
	public static void saveMembers(String fileName) {
		try {
			// Creating serializer
			UserSerializer xmlSerializer = new UserSerializer();
			
			// Feeding member data to the serializer
			for(UserData member : m_members.values()) {
				xmlSerializer.getData().add(member);
			}
			
			// Serializing data to XML-file
			FileOutputStream fos = new FileOutputStream(fileName);
			JAXBContext xmlContext = JAXBContext.newInstance(UserSerializer.class);
			Marshaller xmlMarshaller = xmlContext.createMarshaller();
			xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			xmlMarshaller.marshal(xmlSerializer, fos);
			
			// Closing the file
			fos.close();
			
			// Logging
			LogManager.addMembersLog(Level.CONFIG, "Members configuration saved.");
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(JAXBException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets all User objects from the manager.
	 * @return An unmodifiable map containing the users.
	 */
	public static Map<Integer, User> getAllUsers() {
		synchronized(m_users) {
			return Collections.unmodifiableMap(m_users);
		}
	}
	
	/**
	 * Gets all UserData objects associated with members.
	 * @return An unmodifiable map containing the data of members.
	 */
	public static Map<String, UserData> getAllMembers() {
		synchronized(m_members) {
			return Collections.unmodifiableMap(m_members);
		}	
	}
	
	/**
	 * Generates a new unique ID.
	 * @return The generated ID.
	 */
	private static int generateID() {
		return m_idGenerator++;
	}

};
