package convoice.client.channel;


/**
 * The ChannelData class contains all information about a channel,
 * that is semi-permanent, and will not change during the session,
 * unless special interactions.
 */
public class ChannelData {
	/** The name of the channel. */
	private String m_name;			
	
	/** The topic of the channel. */
	private String m_topic;			
	
	/** The description of the channel. */
	private String m_description;	
	
	/** The flag of a password being used on the channel. */
	private boolean m_hasPassword;		
	
	/** The maximum number of clients on the channel. */
	private int m_maxClients;		
	
	/**
	 * Constructs a ChannelData object.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword The channel has a password?
	 * @param maxClients The maximum number of clients.
	 */
	public ChannelData(String name, String topic, String description, boolean hasPassword, int maxClients) {
		// Initializing members
		m_name = name;
		m_topic = topic;
		m_description = description;
		m_hasPassword = hasPassword;
		m_maxClients = maxClients;
	}
	
	/**
	 * Gets the name of the channel.
	 * @return The name of the channel.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Gets the topic of the channel.
	 * @return The topic of the channel.
	 */
	public String getTopic() {
		return m_topic;
	}
	
	/**
	 * Gets the description of the channel.
	 * @return The description of the channel.
	 */
	public String getDescription() {
		return m_description;
	}
	
	/**
	 * Gets whether the channel password is used or not.
	 * @return True if the channel uses the password.
	 */
	public boolean hasPassword() {
		return m_hasPassword;
	}
	
	/**
	 * Gets the maximum number of clients on the channel.
	 * @return The maximum number of clients on the channel.
	 */
	public int getMaxClients() {
		return m_maxClients;
	}
	
	/**
	 * Sets the name of the channel.
	 * @param name The new name to set for the channel.
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * Sets the topic of the channel.
	 * @param topic The new topic to set for the channel.
	 */
	public void setTopic(String topic) {
		m_topic = topic;
	}
	
	/**
	 * Sets the description of the channel.
	 * @param description The new description to set for the channel.
	 */
	public void setDescription(String description) {
		m_description = description;
	}
	
	/**
	 * Sets whether the channel uses the password or not.
	 * @param hasPassword A boolean value representing whether 
	 * the channel should use the password.
	 */
	public void setHasPassword(boolean hasPassword) {
		m_hasPassword = hasPassword;
	}
	
	/**
	 * Sets the maximum number of clients on the channel.
	 * @param maxClients The new maximum number of clients 
	 * to set for the channel.
	 */
	public void setMaxClients(int maxClients) {
		m_maxClients = maxClients;
	}
	
};

