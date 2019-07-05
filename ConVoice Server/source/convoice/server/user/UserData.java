package convoice.server.user;


// Java imports
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The UserData class contains all information about a user,
 * that is semi-permanent, and will not change during the
 * session, unless special interactions.
 */
@XmlRootElement(name = "Member")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserData {
	/** The username of the user. */
	private String m_username;	
	
	/** The nickname of the user. */
	@XmlTransient
	private String m_nickname;
	
	/** The password of the user. */
	private String m_password;	
	
	/**
	 * Constructs an empty UserData object.
	 * This constructor is used for XML serialization only, 
	 * the private fields are set trough the public setter
	 * methods called by the XML Unmarshaller.
	 */
	public UserData() {
		m_username = null;
		m_nickname = null;
		m_password = null;
	}
	
	/**
	 * Constructs a UserData object.
	 * @param username The username to use.
	 * @param nickname The nickname to use.
	 * @param password The password to use.
	 */
	public UserData(String username, String nickname, String password) {
		m_username = username;
		m_nickname = nickname;
		m_password = password;
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
	 * Gets the password.
	 * @return The password.
	 */
	public String getPassword() {
		return m_password;
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
	 * Sets the password.
	 * @param password The new password to set for the user.
	 */
	public void setPassword(String password) {
		m_password = password;
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
