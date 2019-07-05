package convoice.client.connection;


// Java imports
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

// Project imports
import convoice.client.channel.ChannelManager;
import convoice.client.gui.GUIController;
import convoice.client.user.UserManager;

/**
 * The ConnectionManager class provides an interface for all network
 * operations within the application. 
 * It handles all network communication with the remote server, like
 * connecting, disconnecting, processing updates and handling messages.
 * The ConnectionManager is synchronized with all the other managers.
 */
public class ConnectionManager {
	/** The TCP socket for communication */
	private static Socket m_socket;
	
	/** The run state-flag */
	private static volatile boolean m_shouldRun;
	
	/** The name of the server currently connected to. */
	private static String m_serverName;
	
	/** The version of the server currently connected to. */
	private static String m_serverVersion;
	
	/**
	 * Initializes the manager.
	 */
	static {
		// Initializing members
		m_socket = null;
		m_shouldRun = false;
	}
	
	/**
	 * Returns the name of the server connected to.
	 * @return The name of the server.
	 */
	public static String getServerName() {
		return m_serverName;
	}
	
	/**
	 * Returns the version string of the server connected to.
	 * @return The version string of the server.
	 */
	public static String getServerVersion() {
		return m_serverVersion;
	}
	
	/**
	 * Repeatedly checks for incoming server messages and
	 * delegates them to the specific handlers. This method
	 * runs on a separate thread.
	 */
	public static void start() {
		// Creating the new thread
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// Setting should run state-flag
				m_shouldRun = true;
				
				// Reading messages until shutdown
				while(m_shouldRun) {
					try {
						synchronized(m_socket) {
							
							// Reading message type
							DataInputStream dis = new DataInputStream(m_socket.getInputStream());
							MessageType type = MessageType.UNDEFINED;	
							if(dis.available() > 0) {
								type = MessageType.fromInteger(dis.readInt());
							}
							
							// Handling message
							switch(type) {
							case CONNECTION_TERMINATED: onConnectionTerminatedNotification(); break;
							case CHANNEL_CREATED: onChannelCreatedNotification(); break;
							case CHANNEL_MODIFIED: onChannelModifiedNotification(); break;
							case CHANNEL_DELETED: onChannelDeletedNotification(); break;
							case USER_CREATED: onUserCreatedNotification(); break;
							case USER_MOVED: onUserMovedNotification(); break;
							case USER_DELETED: onUserDeletedNotification(); break;
							case MESSAGE: onMessage(); break;
							case INSUFFICIENT_PERMISSION: onInsufficientPermissionNotification(); break;
							default: /* Unsupported message type */ break;
							}
						}
						
						// Sleeping to reduce CPU usage
						Thread.sleep(10);
						
					} catch(IOException e) {
						return;
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		// Starting the thread
		thread.start();
	}
	
	/**
	 * Sets the should run state-flag to false.
	 * If the manager is running, it will finish operation
	 * after processing the current message.
	 */
	public static void stop() {
		m_shouldRun = false;
	}
	
	/**
	 * Attempts to establish TCP connection with the server.
	 * @param address The address of the server.
	 * @param port The port number of the server.
	 * @return Returns true if connection was successful.
	 */
	public static boolean connect(String address, int port) {
		try {
			if(m_socket != null) {
				synchronized(m_socket) {
					// Creating and connecting socket
					m_socket = new Socket(address, port);
				}
			} else {
				m_socket = new Socket(address, port);
			}

			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Sends login data to the server and handles acceptance response.
	 * @param nickname The nickname to use on the server.
	 * @param username The username if login as member.
	 * @param password The password if login as member.
	 * @param member The member login flag.
	 * @return Returns true if login was successful.
	 */
	public static boolean login(String nickname, String username, String password, boolean member) {
		synchronized(m_socket) {
			try {			
				// Sending login data
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.CONNECTION_REQUEST.toInteger());
				dos.writeBoolean(member);
				dos.writeUTF(username);
				dos.writeUTF(nickname);
				dos.writeUTF(password);
				dos.flush();

				// Reading acceptance response
				DataInputStream dis = new DataInputStream(m_socket.getInputStream());
				MessageType type = MessageType.fromInteger(dis.readInt());
				
				// Checking response type
				if(type == MessageType.CONNECTION_ACCEPTED) {
					String name = dis.readUTF();
					String version = dis.readUTF();
					String welcomeMessage = dis.readUTF();
					int clientID = dis.readInt();
					
					// Storing server data
					m_serverName = name;
					m_serverVersion = version;
					
					// Storing own client ID
					UserManager.setOwnID(clientID);
					
					// Storing own channel ID
					ChannelManager.setOwnChannelID(ChannelManager.DEFAULT_CHANNEL_ID);
					
					// Updating user interface
					GUIController.setStatusMessage(welcomeMessage);
				
					return true;
				}
				else if(type == MessageType.CONNECTION_REJECTED) {
					// Reading reason
					String reason = dis.readUTF();
					
					// Closing the socket
					m_socket.close();
					
					// Updating user interface
					GUIController.setStatusMessage(reason);
					
					return false;
				} 
				
				// No answer from the server
				return false;
				
			} catch(IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	/**
	 * Disconnects from the server.
	 */
	public static void disconnect() {
		if(m_socket == null || m_socket.isClosed()) return;
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.DISCONNECTION_REQUEST.toInteger());
				dos.flush();
				
				// Closing socket
				m_socket.close();
				
				// Updating user interface
				GUIController.setStatusMessage("Disconnected");
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a channel list request to the server, and builds the list
	 * of channels from the server response.
	 */
	public static void requestChannelList() {
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.CHANNEL_LIST_REQUEST.toInteger());
				dos.flush();
				
				// Reading response
				DataInputStream dis = new DataInputStream(m_socket.getInputStream());
				MessageType type = MessageType.fromInteger(dis.readInt());

				// Checking response type
				if(type != MessageType.CHANNEL_LIST) {
					return;
				}
				
				// Reading channels
				int numChannels = dis.readInt();
				for(int i = 0; i < numChannels; i++) {
					int channelID = dis.readInt();
					String name = dis.readUTF();
					String topic = dis.readUTF();
					String description = dis.readUTF();
					boolean hasPassword = dis.readBoolean();
					int maxClients = dis.readInt();
					boolean permanent = dis.readBoolean();
					
					// Creating new channel
					ChannelManager.createChannel(name, topic, description, hasPassword, maxClients, permanent, channelID);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a channel creation request to the server.
	 * @param name The name of the channel.
	 * @param topic The topic of the channel.
	 * @param description The description of the channel.
	 * @param hasPassword The password flag of the channel.
	 * @param password The password of the channel.
	 * @param maxClients The maximum number of clients on the channel.
	 * @param permanent The permanence flag of the channel.
	 */
	public static void requestChannelCreate(String name, String topic, String description, boolean hasPassword,
											String password, int maxClients, boolean permanent) {
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.CHANNEL_CREATE_REQUEST.toInteger());
				dos.writeUTF(name);
				dos.writeUTF(topic);
				dos.writeUTF(description);
				dos.writeBoolean(hasPassword);
				dos.writeUTF(password);
				dos.writeInt(maxClients);
				dos.writeBoolean(permanent);
				dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	/**
	 * Sends a channel modification request to the server.
	 * @param channelID The ID of the channel. 
	 * @param name The new name of the channel.
	 * @param topic The new topic of the channel.
	 * @param description The new description of the channel.
	 * @param hasPassword The new password flag of the channel.
	 * @param password The new password of the channel.
	 * @param maxClients The new maximum number of clients on the channel.
	 * @param permanent The new permanence flag of the channel.
	 */
	public static void requestChannelModify(int channelID, String name, String topic, String description, boolean hasPassword,
											String password, int maxClients, boolean permanent) {
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.CHANNEL_MODIFY_REQUEST.toInteger());
				dos.writeInt(channelID);
				dos.writeUTF(name);
				dos.writeUTF(topic);
				dos.writeUTF(description);
				dos.writeBoolean(hasPassword);
				dos.writeUTF(password);
				dos.writeInt(maxClients);
				dos.writeBoolean(permanent);
				dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a channel deletion request to the server.
	 * @param channelID The ID of the channel.
	 */
	public static void requestChannelDelete(int channelID) {
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.CHANNEL_DELETE_REQUEST.toInteger());
				dos.writeInt(channelID);
				dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a user list request to the server, and builds the list
	 * of users from the server response.
	 */
	public static void requestUserList() {
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.USER_LIST_REQUEST.toInteger());
				dos.flush();
				
				// Reading response
				DataInputStream dis = new DataInputStream(m_socket.getInputStream());
				MessageType type = MessageType.fromInteger(dis.readInt());
				
				// Checking response type
				if(type != MessageType.USER_LIST) {
					return;
				}
				
				// Reading users
				int numUsers = dis.readInt();
				for(int i = 0; i < numUsers; i++) {
					int userID = dis.readInt();
					String username = dis.readUTF();
					String nickname = dis.readUTF();
					int channelID = dis.readInt();
					
					// Creating new user
					UserManager.createUser(username, nickname, userID, channelID);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Requests a user move from the server.
	 * @param userID The ID of the user.
	 * @param channelID The ID of the channel moving to.
	 * @param password The password of the channel if any.
	 */
	public static void requestUserMove(int userID, int channelID, String password) {
		synchronized(m_socket) {
			try {
				// Sending request
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.USER_MOVE_REQUEST.toInteger());
				dos.writeInt(userID);
				dos.writeInt(channelID);
				dos.writeUTF(password);
				dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a message to the server to be delivered to other clients.
	 * @param message The message to send.
	 */
	public static void sendMessage(String message) {
		if(m_socket == null || m_socket.isClosed()) return;
		synchronized(m_socket) {
			try {
				// Sending message
				DataOutputStream dos = new DataOutputStream(m_socket.getOutputStream());
				dos.writeInt(MessageType.MESSAGE_REQUEST.toInteger());
				dos.writeUTF(message);
				dos.flush();
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Handles channel created notifications.
	 */
	private static void onChannelCreatedNotification() {
		try {
			// Reading channel data
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int channelID = dis.readInt();
			String name = dis.readUTF();
			String topic = dis.readUTF();
			String description = dis.readUTF();
			boolean hasPassword = dis.readBoolean();
			int maxClients = dis.readInt();
			boolean permanent = dis.readBoolean();
			
			// Creating channel
			ChannelManager.createChannel(name, topic, description, hasPassword, maxClients, permanent, channelID);
		
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles channel modified notifications.
	 */
	private static void onChannelModifiedNotification() {
		try {
			// Reading channel data
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int channelID = dis.readInt();
			String name = dis.readUTF();
			String topic = dis.readUTF();
			String description = dis.readUTF();
			boolean hasPassword = dis.readBoolean();
			int maxClients = dis.readInt();
			boolean permanent = dis.readBoolean();
			
			// Modifying channel
			ChannelManager.modifyChannel(channelID, name, topic, description, hasPassword, maxClients, permanent);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles channel deleted notifications.
	 */
	private static void onChannelDeletedNotification() {
		try {
			// Reading channel data
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int channelID = dis.readInt();
			
			// Deleting channel
			ChannelManager.deleteChannel(channelID);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles user created notifications.
	 */
	private static void onUserCreatedNotification() {
		try {
			// Reading user data
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int userID = dis.readInt();
			String username = dis.readUTF();
			String nickname = dis.readUTF();
			
			// Creating user
			UserManager.createUser(username, nickname, userID, ChannelManager.DEFAULT_CHANNEL_ID);

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles user moved notifications.
	 */
	private static void onUserMovedNotification() {
		try {
			// Reading user data
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int userID = dis.readInt();
			int channelID = dis.readInt();
			
			// Moving user
			UserManager.moveUser(userID, channelID);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles user deleted notifications.
	 */
	private static void onUserDeletedNotification() {
		try {
			// Reading user data
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int userID = dis.readInt();
			
			// Deleting user
			UserManager.deleteUser(userID);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles incoming messages.
	 */
	private static void onMessage() {
		try {
			// Reading message
			DataInputStream dis = new DataInputStream(m_socket.getInputStream());
			int sourceID = dis.readInt();
			String message = dis.readUTF();
			
			// Getting nickname
			String sourceName = UserManager.getUser(sourceID).getUserData().getNickname();
			
			// Updating the user interface
			GUIController.messageReceivedUpdate(sourceName + ": " + message);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles when the server terminates the connection.
	 */
	private static void onConnectionTerminatedNotification() {
		try {
			// Closing the socket
			m_socket.close();
			
			// Updating the user interface
			GUIController.connectionTerminatedUpdate();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles insufficient permission notifications.
	 */
	private static void onInsufficientPermissionNotification() {
		// Updating user interface
		GUIController.showAlertDialog("Insufficient permission.");
	}

};
