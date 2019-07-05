package convoice.client.channel;


// Java imports
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

// Project imports
import convoice.client.channel.ChannelData;

/**
 * The Channel class represents a channel of communication to which 
 * users can join. 
 * It contains permanent as well as dynamic runtime 
 * data, such as the assigned ID of the channels or the set of
 * currently active users.
 */
public class Channel {
	/** The permanent data of the channel. */
	private ChannelData m_channelData;	
	
	/** The unique ID of the channel. */
	private int m_id;
	
	/** The permanence flag of the channel. */
	private boolean m_permanent;
	
	/** The set of users ID's on the channel. */
	private Set<Integer> m_users; 		
	
	
	/**
	 * Constructs a Channel object.
	 * @param channelData The channel's permanent data.
	 * @param id The channel's unique ID.
	 * @param permanent True if the channel should be saved upon server shutdown.
	 */
	public Channel(ChannelData channelData, int id, boolean permanent) {
		// Initializing members
		m_channelData = channelData;
		m_id = id;
		m_permanent = permanent;
		m_users = new HashSet<Integer>();
	}
	
	/**
	 * Gets the channel's ChannelData structure. 
	 * @return A ChannelData object.
	 */
	public ChannelData getChannelData() {
		return m_channelData;
	}
	
	/**
	 * Gets the channel's unique ID.
	 * @return The channel's unique ID.
	 */
	public int getID() {
		return m_id;
	}
	
	/**
	 * Gets whether the channel is permanent or not.
	 * Permanent channels are saved upon server shutdown and loaded
	 * on startup, while temporary channels are deleted latest at
	 * server shutdown.
	 * @return True if the channel is permanent.
	 */
	public boolean isPermanent() {
		return m_permanent;
	}
	
	/**
	 * Gets the set of the ID's of the users currently using
	 * this channel. The returned set is unmodifiable for 
	 * integration purposes.
	 * @return The list of user ID's.
	 */
	public Set<Integer> getUsers() {
		return Collections.unmodifiableSet(m_users);
	}
	
	/**
	 * Sets the channel's permanent data. Alternatively the retrieved
	 * ChannelData object from getChannelData() can be modified.
	 * @param channelData The ChannelData object to assign to this channel.
	 */
	public void setChannelData(ChannelData channelData) {
		m_channelData = channelData;
	}
	
	/**
	 * Sets the channel's permanence flag to the specified value.
	 * @param permanent A boolean value representing whether the channel
	 * should be permanent or not.
	 */
	public void setPermanent(boolean permanent) {
		m_permanent = permanent;
	}
	
	/**
	 * Adds a user to the channel.
	 * This is for administration purposes only, the user
	 * has to be modified as well.
	 * @param userID The ID of the user being added.
	 */
	public void addUser(int userID) {
		m_users.add(userID);
	}
	
	/**
	 * Removes a user from the channel.
	 * This is for administration purposes only, the user
	 * has to be modified as well.
	 * @param userID The ID of the user that is being removed.
	 */
	public void removeUser(int userID) {
		m_users.remove(userID);
	}
	
	/**
	 * Returns the name of the channel.
	 * The returned name can be used to represent the channel
	 * on the graphical user interface.
	 */
	public String toString() {
		return m_channelData.getName();
	}
		
};
