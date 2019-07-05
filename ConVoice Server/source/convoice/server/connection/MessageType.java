package convoice.server.connection;


/**
 * The MessageType enum represents the types of messages,
 * that the server and clients can send to each other.
 * These values are used to identify the type of the 
 * message before reading it's content to determine the
 * way of reading. All values in the enum are convertible
 * to an Integer and vice versa, the actual network operations
 * use the Integer form.
 */
public enum MessageType {
	// From clients
	CONNECTION_REQUEST(0),
	DISCONNECTION_REQUEST(1),
	CHANNEL_LIST_REQUEST(2),
	CHANNEL_CREATE_REQUEST(3),
	CHANNEL_MODIFY_REQUEST(4),
	CHANNEL_DELETE_REQUEST(5),
	USER_LIST_REQUEST(6),
	USER_MOVE_REQUEST(7),
	MESSAGE_REQUEST(8),
	
	// From server
	CONNECTION_ACCEPTED(9),
	CONNECTION_REJECTED(10),
	CONNECTION_TERMINATED(11),
	CHANNEL_LIST(12),
	CHANNEL_CREATED(13),
	CHANNEL_MODIFIED(14),
	CHANNEL_DELETED(15),
	USER_LIST(16),
	USER_CREATED(17),
	USER_MOVED(18),
	USER_DELETED(19),
	MESSAGE(20),
	
	// Miscellaneous
	UNDEFINED(21),
	INSUFFICIENT_PERMISSION(22);
	
	/** The Integer representation of the enum value. */
	private final int m_value;	
	
	/**
	 * Constructs a MessageType enum value.
	 * @param value The integer representation of the value.
	 */
	MessageType(int value) {
		m_value = value;
	}
	
	/**
	 * Returns the integer representation of the value.
	 * @return The integer representation of the value.
	 */
	public int toInteger() {
		return m_value;
	}
	
	/**
	 * Returns the enum value with the specified integer.
	 * @param value The integer representation of the value.
	 * @return The enum value.
	 */
	public static MessageType fromInteger(int value) {
		return MessageType.values()[value];
	}

};
