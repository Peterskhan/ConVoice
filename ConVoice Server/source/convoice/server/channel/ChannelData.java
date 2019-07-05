package convoice.server.channel;


// Java imports
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The ChannelData class contains all information about a channel,
 * that is semi-permanent, and will not change during the session,
 * unless special interactions.
 */
@XmlRootElement(name = "Channel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChannelData {
	/** The name of the channel. */
	private String m_name;			
	
	/** The topic of the channel. */
	private String m_topic;			
	
	/** The description of the channel. */
	private String m_description;	
	
	/** The flag of a password being used on the channel. */
	private boolean m_hasPassword;	
	
	/** The password of the channel. */
	private String m_password;		
	
	/** The maximum number of clients on the channel. */
	private int m_maxClients;		
	
	/**
	 * Constructs an empty ChannelData object.
	 * This constructor is used for XML serialization only, 
	 * the private fields are set trough the public setter
	 * methods called by the XML Unmarshaller.
	 */
	public ChannelData() {
		// Initializing members
		m_name = null;
		m_topic = null;
		m_description = null;
		m_hasPassword = false;
		m_password = null;
		m_maxClients = 0;
	}
	
	/**
	 * Constructs a ChannelData object.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword The channel has a password?
	 * @param password The password of the channel.
	 * @param maxClients The maximum number of clients.
	 */
	public ChannelData(String name, String topic, String description, boolean hasPassword, String password, int maxClients) {
		// Initializing members
		m_name = name;
		m_topic = topic;
		m_description = description;
		m_hasPassword = hasPassword;
		m_password = password;
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
	 * Gets the password of the channel.
	 * @return The password of the channel.
	 */
	public String getPassword() {
		return m_password;
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
	 * Sets the password of the channel.
	 * @param password The new password to set for the channel.
	 */
	public void setPassword(String password) {
		m_password = password;
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

