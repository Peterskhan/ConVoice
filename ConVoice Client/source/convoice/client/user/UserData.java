package convoice.client.user;


/**
 * The UserData class contains all information about a user,
 * that is semi-permanent, and will not change during the
 * session, unless special interactions.
 */
public class UserData {
	/** The username of the user. */
	private String m_username;	
	
	/** The nickname of the user. */
	private String m_nickname;
		
	/**
	 * Constructs a UserData object.
	 * @param username The username to use.
	 * @param nickname The nickname to use.
	 */
	public UserData(String username, String nickname) {
		m_username = username;
		m_nickname = nickname;
	}
	
	/**
	 * Gets the username.
	 * @return The username.
	 */
	public String getUsername() {
		return m_username;
	}

	/**
	 * Gets the nickname.
	 * @return The nickname.
	 */
	public String getNickname() {
		return m_nickname;
	}
	
	/**
	 * Sets the username.
	 * @param username The new username to set for the user.
	 */
	public void setUsername(String username) {
		m_username = username;
	}
	
	/**
	 * Sets the nickname.
	 * @param nickname The new nickname to set for the user.
	 */
	public void setNickname(String nickname) {
		m_nickname = nickname;
	}
	
	/**
	 * Gets the string representation of the UserData.
	 * The returned username can be used to represent
	 * the user on the graphical user interface. This
	 * version returns the username which is useful 
	 * for displaying members, while the User class's
	 * version returns the nickname which is suitable
	 * for regular users.
	 * @return The stored username.
	 */
	public String toString() {
		return m_username;
	}
	
};
