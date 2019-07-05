package convoice.server.channel;


// Java imports
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Project imports
import convoice.server.connection.ConnectionManager;
import convoice.server.gui.GUIController;
import convoice.server.logger.LogManager;
import convoice.server.server.Server;
import convoice.server.user.UserManager;

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
	
	/** The next assignable channel ID. */
	private static int m_idGenerator;					
	
	/** ID of the default channel. */
	public static final int DEFAULT_CHANNEL_ID = 0;		
	
	/**
	 * Initializes the map of channels and the ID generator.
	 * For faster access to a specific channel, the container is mapped
	 * with the channel's ID. The ID generator field gets incremented
	 * when a new channel is created, ensuring uniqueness of IDs.
	 */
	static {
		// Initializing members
		m_channels = new HashMap<Integer, Channel>();
		m_idGenerator = 1;
	}
	
	/**
	 * Resets the ChannelManager by clearing the map of channels,
	 * and resetting the channel ID generator. No shutdown operations
	 * are performed by this method and may be only used when the server
	 * is not running.
	 */
	public static void reset() {
		// Clearing the map of channels
		synchronized(m_channels) {
			m_channels.clear();
		}
		
		// Resetting the ID generator
		m_idGenerator = 1;
	}
		
	/**
	 * Loads channel configuration from the "channels.xml" file
	 * of the specified directory, resets the manager and creates
	 * a the new default channel.
	 * @param configPath The path of the configuration directory.
	 */
	public static void loadConfiguration(String configPath) {
		// Performing manager reset
		reset();
		
		// Creating default channel
		createDefaultChannel();
		
		// Loading channels from file
		loadChannels(configPath + "/channels.xml");
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Channels configuration loaded.");
	}
	
	/**
	 * Saves the list of channels to "channels.xml" file of the specified directory.
	 * @param configPath The path of the configuration directory.
	 */
	public static void saveConfiguration(String configPath) {
		// Saving channels to file
		saveChannels(configPath + "/channels.xml");
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Channels configuration saved.");
	}
	
	/**
	 * Reads the settings from the server properties and
	 * creates the default channel.
	 */
	private static void createDefaultChannel() {
		// Getting default channel properties
		String name = Server.getProperties().getProperty("defaultChannelName", "Default Channel");
		String topic = Server.getProperties().getProperty("defaultChannelTopic", "");
		String description = Server.getProperties().getProperty("defaultChannelDescription", "The default channel of the server.");
		
		// Creating the channel data
		ChannelData channelData = new ChannelData(name, topic, description, false, null, Integer.MAX_VALUE);
		
		// Creating the channel
		Channel channel = new Channel(channelData, DEFAULT_CHANNEL_ID, false);
		
		// Adding the channel
		synchronized(m_channels) {
			m_channels.put(0, channel);
		}
		
		// Updating user interface
		GUIController.channelCreatedUpdate(channel);
		
		// Logging
		LogManager.addMainLog(Level.CONFIG, "Default channel created.");
	}
	
	/**
	 * Creates a new Channel and adds it to the manager.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword Does the channel have a password?
	 * @param password The password of the channel.
	 * @param maxClients The maximum number of clients on the channel.
	 * @param permanent Is the channel permanent?
	 * @return The ID of the created channel.
	 */
	public static int createChannel(String name, String topic, String description, boolean hasPassword,
							  		String password, int maxClients, boolean permanent) {
		// Creating the channel data
		ChannelData channelData = new ChannelData(name, topic, description, hasPassword, password, maxClients);
		
		// Creating the channel
		int id = generateID();
		Channel channel = new Channel(channelData, id, permanent);
		
		// Adding the channel
		synchronized(m_channels) {
			m_channels.put(id, channel);
		}
		
		// Requesting user notifications
		ConnectionManager.channelCreatedNotify(id, name, topic, description, hasPassword, maxClients, permanent);
		
		// Updating user interface
		GUIController.channelCreatedUpdate(channel);
		
		// Logging
		LogManager.addMainLog(Level.INFO, "Channel ID: " + id + " created.");
		
		// Returning the ID of the channel
		return id;
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
	 * @param password The new password of the channel.
	 * @param maxClients The new maximum number of clients.
	 * @param permanent Is the channel permanent?
	 */
	public static void modifyChannel(int channelID, String name, String topic, String description, boolean hasPassword,
									 String password, int maxClients, boolean permanent) {
		// Getting the channel to modify
		Channel channel = getChannel(channelID);
		
		// Creating new channel data
		ChannelData channelData = new ChannelData(name, topic, description, hasPassword, password, maxClients);
		
		synchronized(channel) {
			// Assigning new channel data
			channel.setChannelData(channelData);
			
			// Setting permanent field
			channel.setPermanent(permanent);
		}
		
		// Requesting user notifications
		ConnectionManager.channelModifiedNotify(channelID, name, topic, description, hasPassword, maxClients, permanent);
		
		// Updating user interface
		GUIController.channelModifiedUpdate(channel);
		
		// Logging
		LogManager.addMainLog(Level.INFO, "Channel ID: " + channelID + " modified.");
	}
	
	/**
	 * Deletes the specified channel. Users currently in the channel
	 * will be moved to the default channel.
	 * @param channelID The ID of the channel to delete.
	 */
	public static void deleteChannel(int channelID) {
		// Getting the channel
		Channel channel = getChannel(channelID);
		
		synchronized(channel) {
			// Getting users on the channel
			Set<Integer> usersInChannel = channel.getUsers();
			
			// Moving users to the default channel
			for(int userID : usersInChannel) {
				UserManager.moveUser(userID, DEFAULT_CHANNEL_ID, null);
			}
		}
		
		// Requesting user notifications
		ConnectionManager.channelDeletedNotify(channelID);
		
		// Updating user interface
		GUIController.channelDeletedUpdate(channelID);
		
		// Deleting the channel
		synchronized(m_channels) {
			m_channels.remove(channelID);
		}
		
		// Logging
		LogManager.addMainLog(Level.INFO, "Channel ID: " + channelID + " deleted.");
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
	 * Gets all Channel objects from the manager.
	 * @return An unmodifiable map of channels.
	 */
	public static Map<Integer, Channel> getAllChannels() {
		synchronized(m_channels) {
			return Collections.unmodifiableMap(m_channels);
		}
	}
	
	/**
	 * Loads the list of channels from an XML-file.
	 * Loaded channels are assumed to be permanent.
	 * @param fileName The name of the channel file.
	 */
	private static void loadChannels(String fileName) {
		try {
			// Creating XML-objects for deserialization
			FileInputStream fis = new FileInputStream(fileName);
			JAXBContext xmlContext = JAXBContext.newInstance(ChannelSerializer.class);
			Unmarshaller xmlUnmarshaller = xmlContext.createUnmarshaller();
			
			// Deserializing channel data
			ChannelSerializer xmlSerializer = (ChannelSerializer) xmlUnmarshaller.unmarshal(fis);
			
			// Feeding deserialized data to the ChannelManager
			for(ChannelData data : xmlSerializer.getData()) {
				// Creating channel from read data
				createChannel(data.getName(), data.getTopic(), data.getDescription(), data.hasPassword(), 
							  data.getPassword(), data.getMaxClients(), true);
			}
			
			// Closing the file
			fis.close();
			
			// Logging
			LogManager.addMainLog(Level.CONFIG, "Channels loaded.");
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(JAXBException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the list of channels to an XML-file.
	 * Only permanent channels are being serialized.
	 * @param fileName The name of the file.
	 */
	private static void saveChannels(String fileName) {
		try {
			// Creating serializer
			ChannelSerializer xmlSerializer = new ChannelSerializer();
			
			// Feeding channel data to the serializer
			synchronized(m_channels) {
				for(Channel channel : m_channels.values()) {
					if(channel.isPermanent()) {
						xmlSerializer.getData().add(channel.getChannelData());
					}
				}
			}
			
			// Serializing data to XML-file
			FileOutputStream fos = new FileOutputStream(fileName);
			JAXBContext xmlContext = JAXBContext.newInstance(ChannelSerializer.class);
			Marshaller xmlMarshaller = xmlContext.createMarshaller();
			xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			xmlMarshaller.marshal(xmlSerializer, fos);
			
			// Closing the file
			fos.close();
			
			// Logging
			LogManager.addMainLog(Level.CONFIG, "Channels saved.");
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(JAXBException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
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
