package convoice.server.permission;


// Java imports
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;

// Project imports
import convoice.server.logger.LogManager;
import convoice.server.server.Server;

/**
 * The PermissionManager class is responsible for registering
 * and unregistering users for specific permission groups.
 * Users can be guests on the server, as well as members. 
 * Members generally have more rights on the server than guests,
 * but that is configurable. Permissions can be looked up by
 * user ID for specific actions.
 * The PermissionManager has only static fields and methods,
 * making it easily available to the rest of the application.
 * It's behavior is similar to a Singleton.
 */
public class PermissionManager {
	/** The set of user IDs with member rights. */
	private static Set<Integer> m_members;		
	
	/** The set of user IDs with guest rights. */
	private static Set<Integer> m_guests;				
	
	/** Channel creation right for members. */
	private static boolean m_memberCanCreateChannel;	
	
	/** Channel modification right for members. */
	private static boolean m_memberCanModifyChannel;	
	
	/** Channel deletion right for members. */
	private static boolean m_memberCanDeleteChannel;	
	
	/** Channel creation right for guests. */
	private static boolean m_guestCanCreateChannel;		

	/** Channel modification right for guests. */
	private static boolean m_guestCanModifyChannel;		
	
	/** Channel deletion right for guests. */
	private static boolean m_guestCanDeleteChannel;		
	
	/**
	 * Initializes the PermissionManager.
	 */
	static {
		// Initializing members
		m_members = new HashSet<Integer>();
		m_guests = new HashSet<Integer>();
	}
	
	/**
	 * Resets the PermissionManager by clearing the set of
	 * registered online members and guests. No shutdown operations
	 * are performed by this method and may be only used when the
	 * server is not running.
	 */
	public static void reset() {
		// Clearing the set of members
		synchronized(m_members) {
			m_members.clear();
		}
		
		// Clearing the set of guests
		synchronized(m_guests) {
			m_guests.clear();
		}
	}
	
	/**
	 * Resets the PermissionManager and loads permissions from the 
	 * global Server properties.
	 */
	public static void loadConfiguration() {
		// Performing manager reset
		reset();
		
		// Reading group permissions
		m_memberCanCreateChannel = Boolean.parseBoolean(Server.getProperties().getProperty("memberCanCreateChannel", "true"));
		m_memberCanModifyChannel = Boolean.parseBoolean(Server.getProperties().getProperty("memberCanModifyChannel", "true"));
		m_memberCanDeleteChannel = Boolean.parseBoolean(Server.getProperties().getProperty("memberCanDeleteChannel", "true"));
		m_guestCanCreateChannel = Boolean.parseBoolean(Server.getProperties().getProperty("guestCanCreateChannel", "false"));
		m_guestCanModifyChannel = Boolean.parseBoolean(Server.getProperties().getProperty("guestCanModifyChannel", "false"));
		m_guestCanDeleteChannel = Boolean.parseBoolean(Server.getProperties().getProperty("guestCanDeleteChannel", "false"));
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Permissions configuration loaded.");
	}
	
	/**
	 * Reloads permissions from the global Server properties
	 * without resetting the manager.
	 */
	public static void reloadConfiguration() {
		// Reading group permissions
		m_memberCanCreateChannel = Boolean.parseBoolean(Server.getProperties().getProperty("memberCanCreateChannel", "true"));
		m_memberCanModifyChannel = Boolean.parseBoolean(Server.getProperties().getProperty("memberCanModifyChannel", "true"));
		m_memberCanDeleteChannel = Boolean.parseBoolean(Server.getProperties().getProperty("memberCanDeleteChannel", "true"));
		m_guestCanCreateChannel = Boolean.parseBoolean(Server.getProperties().getProperty("guestCanCreateChannel", "false"));
		m_guestCanModifyChannel = Boolean.parseBoolean(Server.getProperties().getProperty("guestCanModifyChannel", "false"));
		m_guestCanDeleteChannel = Boolean.parseBoolean(Server.getProperties().getProperty("guestCanDeleteChannel", "false"));
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Permissions configuration reloaded.");
	}
	
	/**
	 * Returns whether the user with the specified ID is a guest or not.
	 * @param id The ID of the user.
	 * @return True if the user is a guest.
	 */
	public static boolean isGuest(int id) {
		synchronized(m_guests) {
			return m_guests.contains(id);
		}
	}
	
	/**
	 * Returns whether the user with the specified ID is a member or not.
	 * @param id The ID of the user.
	 * @return True if the user is a member.
	 */
	public static boolean isMember(int id) {
		synchronized(m_members) {
			return m_members.contains(id);
		}
	}
	
	/**
	 * Registers a user as a member.
	 * @param id The ID of the user.
	 */
	public static void registerMember(int id) {
		synchronized(m_members) {
			m_members.add(id);
		}
		
		// Logging
		LogManager.addMembersLog(Level.INFO, "Member ID: " + id + " registered.");
	}
	
	/**
	 * Registers a user as a guest.
	 * @param id The ID of the user.
	 */
	public static void registerGuest(int id) {
		synchronized(m_guests) {
			m_guests.add(id);
		}
	}
	
	/**
	 * Unregisters a user with the specified ID from
	 * both member and guest groups.
	 * @param id The ID of the user.
	 */
	public static void unregister(int id) {
		// Logging
		if(isMember(id)) {
			LogManager.addMembersLog(Level.INFO, "Member ID: " + id + " unregistered.");
		}

		synchronized(m_members) {
			m_members.remove(id);
		}

		synchronized(m_guests) {
			m_guests.remove(id);
		}
	}
	
	/**
	 * Returns whether the user can create a channel or not.
	 * @param id The ID of the user.
	 * @return True if the user can create a channel.
	 */
	public static boolean canCreateChannel(int id) {
		if(isMember(id)) {
			return m_memberCanCreateChannel;
		}
		else if(isGuest(id)) {
			return m_guestCanCreateChannel;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns whether the user can modify a channel or not.
	 * @param id The ID of the user.
	 * @return True if the user can modify a channel.
	 */
	public static boolean canModifyChannel(int id) {
		if(isMember(id)) {
			return m_memberCanModifyChannel;
		}
		else if(isGuest(id)) {
			return m_guestCanModifyChannel;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns whether the user can delete a channel or not.
	 * @param id The ID of the user.
	 * @return True if the user can delete a channel.
	 */
	public static boolean canDeleteChannel(int id) {
		if(isMember(id)) {
			return m_memberCanDeleteChannel;
		}
		else if(isGuest(id)) {
			return m_guestCanDeleteChannel;
		} else {
			return false;
		}
	}
	
};
