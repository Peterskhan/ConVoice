package convoice.server.user;


// Java imports
import java.net.Socket;

// Project imports
import convoice.server.user.UserData;

/**
 * The User class represents a user for the time of a session.
 * It contains permanent as well as dynamic runtime data, such as the
 * assigned ID, current channel's ID, and TCP socket for the user.
 */
public class User {
	/** The UserData structure of the user. */
	private UserData m_userData;	
	
	/** The unique ID of the user. */
	private int m_id;				
	
	/** The ID of the user's current channel. */
	private int m_channel;			
	
	/** The user's TCP socket for network communication. */
	private Socket m_socket;
	
	/**
	 * Constructs a User object.
	 * @param userData The user's permanent data.
	 * @param id The user's unique ID.
	 * @param channel The ID of the user's starting channel.
	 * @param socket Socket for communication.
	 */
	public User(UserData userData, int id, int channel, Socket socket) {
		// Initializing members
		m_userData = userData;
		m_id = id;
		m_channel = channel;
		m_socket = socket;
	}
	
	/**
	 * Gets the UserData object associated with the user.
	 * @return A UserData object.
	 */
	public UserData getUserData() {
		return m_userData;
	}
	
	/**
	 * Gets the user's unique ID.
	 * @return The user's unique ID.
	 */
	public int getID() {
		return m_id;
	}
	
	/**
	 * Gets the ID of the user's current channel.
	 * @return The ID of the user's current channel.
	 */
	public int getChannel() {
		return m_channel;
	}
	
	/**
	 * Gets the user's TCP socket.
	 * @return The user's TCP socket.
	 */
	public Socket getSocket() {
		return m_socket;
	}
	
	/**
	 * Sets the user's UserData object. Alternatively the retrieved
	 * UserData object from getUserData() can be modified.
	 * @param userData An UserData object containing the data.
	 */
	public void setUserData(UserData userData) {
		m_userData = userData;
	}
	
	/**
	 * Sets the user's current channel ID.
	 * This is for administration purposes only, the channel
	 * has to be modified as well.
	 * @param channelID The new channel ID.
	 */
	public void setChannel(int channelID) {
		m_channel = channelID;
	}
	
	/**
	 * Returns the String representation of the user. 
	 * The returned String can be used to represent the
	 * user on the graphical user interface
	 * @return The nickname of the user.
	 */
	public String toString() {
		return m_userData.getNickname();
	}
		
};
