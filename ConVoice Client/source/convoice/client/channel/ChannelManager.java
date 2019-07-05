package convoice.client.channel;


// Java imports
import java.util.HashMap;
import java.util.Map;

// Project imports
import convoice.client.gui.GUIController;

/**
 * The ChannelManager class provides the most common administrative
 * functionality concerning channels. 
 * It is responsible for the management of Channel objects like creation, 
 * deletion and providing access to the rest of the application.
 * The ChannelManager class contains only static fields and methods, making
 * it easily available within the application. It's behavior is similar
 * to a Singleton. Methods of the ChannelManager are synchronized with other
 * managers, so any changes made via the ChannelManager will be reflected
 * everywhere else in the application.
 */
public class ChannelManager {
	
	/** The created channels mapped by their ID. */
	private static Map<Integer, Channel> m_channels;
	
	/** The ID of the client's current channel. */
	private static int m_ownChannelID;
	
	/** ID of the default channel. */
	public static final int DEFAULT_CHANNEL_ID = 0;
	
	/**
	 * Initializes the map of channels.
	 * For faster access to a specific channel, the container is mapped
	 * with the channel's ID.
	 */
	static {
		// Initializing members
		m_channels = new HashMap<Integer, Channel>();
	}
	
	/**
	 * Creates a new Channel and adds it to the manager.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword Does the channel have a password?
	 * @param maxClients The maximum number of clients on the channel.
	 * @param permanent Is the channel permanent?
	 * @param channelID The ID of the channel.
	 */
	public static void createChannel(String name, String topic, String description, boolean hasPassword,
	  								 int maxClients, boolean permanent, int channelID) {
		// Creating the channel data
		ChannelData channelData = new ChannelData(name, topic, description, hasPassword, maxClients);
		
		// Creating the channel
		Channel channel = new Channel(channelData, channelID, permanent);
		
		// Adding the channel
		synchronized(m_channels) {
			m_channels.put(channelID, channel);
		}
		
		// Updating the user interface
		GUIController.channelCreatedUpdate(channel);
	}
	
	/**
	 * Modifies the channel with the specified ID by setting the specified parameters.
	 * The method performs a complete reassignment of fields, so all data that should
	 * not be modified must be passed as parameter.
	 * @param channelID The ID of the channel being modified.
	 * @param name The new name of the channel.
	 * @param topic The new topic of the channel.
	 * @param description The new description of the channel.
	 * @param hasPassword Does the channel have a password?
	 * @param maxClients The new maximum number of clients.
	 * @param permanent Is the channel permanent?
	 */
	public static void modifyChannel(int channelID, String name, String topic, String description, 
									 boolean hasPassword, int maxClients, boolean permanent) {
		// Getting the channel to modify
		Channel channel = getChannel(channelID);
		
		// Creating new channel data
		ChannelData channelData = new ChannelData(name, topic, description, hasPassword, maxClients);
		
		synchronized(channel) {
			// Assigning new channel data
			channel.setChannelData(channelData);
			
			// Setting permanent field
			channel.setPermanent(permanent);
		}
	
		// Updating user interface
		GUIController.channelModifiedUpdate(channel);
	}
	
	/**
	 * Deletes the specified channel.
	 * @param channelID The ID of the channel to delete.
	 */
	public static void deleteChannel(int channelID) {
		// Updating the user interface
		GUIController.channelDeletedUpdate(channelID);
		
		// Deleting the channel
		synchronized(m_channels) {
			m_channels.remove(channelID);
		}
	}
	
	/**
	 * Returns a channel object by it's ID.
	 * @param channelID The ID of the channel.
	 * @return The channel with the specified ID.
	 */
	public static Channel getChannel(int channelID) {
		synchronized(m_channels) {
			return m_channels.get(channelID);
		}
	}
	
	/**
	 * Sets the client's current channel's ID.
	 * This method is only administrative, and is called by the
	 * UserManager, when the client is being moved.
	 * @param id The ID of the new channel moved to.
	 */
	public static void setOwnChannelID(int id) {
		m_ownChannelID = id;
	}
	
	/**
	 * Gets the client's current channel's ID.
	 * @return The ID of the current channel.
	 */
	public static int getOwnChannelID() {
		return m_ownChannelID;
	}
	
};
