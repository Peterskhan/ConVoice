package convoice.server.connection;


// Java imports
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Project imports
import convoice.server.channel.Channel;
import convoice.server.channel.ChannelManager;
import convoice.server.gui.GUIController;
import convoice.server.logger.LogManager;
import convoice.server.permission.PermissionManager;
import convoice.server.user.User;
import convoice.server.user.UserManager;

/**
 * The ConnectionHandler class is responsible for handling 
 * existing connections. 
 * The handler stores the ID of users associated with it, 
 * and will handle their requests by iterating the container 
 * and reading / writing messages.
 */
public class ConnectionHandler implements Runnable {
	/** The set of users assigned to the handler. */
	private Set<Integer> m_users;			
	
	/** The running state-flag. */
	private volatile boolean m_running;
	
	/** The should run state-flag. */
	private volatile boolean m_shouldRun;	
	
	/**
	 * Constructs a ConnectionHandler object.
	 */
	public ConnectionHandler() {
		// Initializing members
		m_running = false;
		m_shouldRun = false;
		m_users = new HashSet<Integer>();
	}
	
	/**
	 * Runs the main loop which handles existing connections.
	 * The handler will actively loop the container of associated
	 * users, and will try to read messages from their network
	 * socket. Then the message will be passed to the corresponding
	 * handler method. The the loop finishes all connections will be
	 * terminated.
	 */
	public void run() {
		// Setting state flags
		m_shouldRun = true;
		m_running = true;
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "Connection handler started.");
		
		// Main loop
		while(m_shouldRun) {
			try {
				synchronized(m_users) {
					// Iterating users
					Iterator<Integer> it = m_users.iterator();
					while(it.hasNext()) {
						int userID = it.next();
						Socket socket = UserManager.getUser(userID).getSocket();
						synchronized(socket) {
							// Reading message type
							DataInputStream dis = new DataInputStream(socket.getInputStream());
							MessageType type = MessageType.UNDEFINED;
							if(dis.available() > 0) {
								type = MessageType.fromInteger(dis.readInt());
							}

							// Handling the message
							switch(type) {
							case DISCONNECTION_REQUEST: onDisconnectionRequest(userID, socket, it); break;
							case CHANNEL_LIST_REQUEST: onChannelListRequest(userID, socket); break;
							case CHANNEL_CREATE_REQUEST: onChannelCreateRequest(userID, socket); break;
							case CHANNEL_MODIFY_REQUEST: onChannelModifyRequest(userID, socket); break;
							case CHANNEL_DELETE_REQUEST: onChannelDeleteRequest(userID, socket); break;
							case USER_LIST_REQUEST: onUserListRequest(userID, socket); break;
							case USER_MOVE_REQUEST: onUserMoveRequest(userID, socket); break;
							case MESSAGE_REQUEST: onMessageRequest(userID, socket); break;
							default: /* Unsupported message type */ break;
							}
						}
					}
				}

				// Sleeping to reduce CPU usage
				Thread.sleep(10);
				
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Terminating all connections
		terminateConnections();
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "Connection handler stopped.");
		
		// Setting running state-flag
		m_running = false;
	}
	
	/**
	 * Returns whether the ConnectionHandler is running or not.
	 * @return True if the handler is running.
	 */
	public boolean isRunning() {
		return m_running;
	}
	
	/**
	 * Signals the ConnectionHandler to stop.
	 * The listener will shut down after the current iteration
	 * of run() finishes.
	 */
	public void stop() {
		m_shouldRun = false;
	}
	
	/**
	 * Adds a User to the handler.
	 * After the user has been added, all of it's messages will
	 * be handled by the handler.
	 * @param userID The user to add.
	 */
	public void addUser(int userID) {
		synchronized(m_users) {
			m_users.add(userID);
		}
	}
	
	/**
	 * Returns the number of users associated with the handler.
	 * @return The number of users.
	 */
	public int getUserCount() {
		return m_users.size();
	}
	
	/**
	 * Terminates all connections associated with the handler,
	 * by first sending a termination message to the client,
	 * then closing it's socket and deleted the corresponding
	 * user.
	 */
	private void terminateConnections() {
		try {
			synchronized(m_users) {
				for(int userID : m_users) {
					// Sending connection termination message
					Socket socket = UserManager.getUser(userID).getSocket();
					synchronized(socket) {
						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
						dos.writeInt(MessageType.CONNECTION_TERMINATED.toInteger());
						dos.flush();
						
						// Closing the socket
						socket.close();
					}
					
					// Unregistering user from the PermissionManager
					PermissionManager.unregister(userID);
					
					// Deleting the user
					UserManager.deleteUser(userID);
				}
				
				// Clearing the list of assigned users
				m_users.clear();
			}
			
			// Updating the user interface
			GUIController.setConnectionsClientsCount(ConnectionManager.getClientCount());
			
			// Logging
			LogManager.addConnectionsLog(Level.INFO, "Connection handler connections terminated.");
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Terminates the connection with the specified user after
	 * the handler received a disconnect request in run().
	 * @param source The source of the disconnect request.
	 * @param socket Socket to the requesting user.
	 * @param it The iterator used for deleting the user from the handler.
	 */
	private void onDisconnectionRequest(int source, Socket socket, Iterator<Integer> it) {
		try { 			
			// Closing the socket
			socket.close();
			
			// Updating user interface
			if(PermissionManager.isMember(source)) {
				GUIController.addMembersOutputMessage(
						UserManager.getUser(source).getUserData().getUsername() + " disconnected.");
			}
			
			// Unregistering user from the PermissionManager
			PermissionManager.unregister(source);
			
			// Removing the user from the handler
			it.remove();
			//m_users.remove(source);
			
			// Deleting the user
			UserManager.deleteUser(source);
			
			// Updating the user interface
			GUIController.setConnectionsClientsCount(ConnectionManager.getClientCount());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends the list of channels to the specified user after
	 * the handler received a channel list request in run().
	 * @param source The source of the channel list request.
	 * @param socket Socket to the requesting user.
	 */
	private void onChannelListRequest(int source, Socket socket) {
		try {
			// Getting channel list
			Map<Integer, Channel> channelList = ChannelManager.getAllChannels();
			
			// Writing channel list 
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(MessageType.CHANNEL_LIST.toInteger());
			dos.writeInt(channelList.size());
			
			for(Channel channel : channelList.values()) {
				dos.writeInt(channel.getID());
				dos.writeUTF(channel.getChannelData().getName());
				dos.writeUTF(channel.getChannelData().getTopic());
				dos.writeUTF(channel.getChannelData().getDescription());
				dos.writeBoolean(channel.getChannelData().hasPassword());
				dos.writeInt(channel.getChannelData().getMaxClients());
				dos.writeBoolean(channel.isPermanent());
				dos.flush();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a channel with the provided parameters after
	 * the handler received a channel create request in run().
	 * If channel creating is successful, the requesting user
	 * is automatically moved to the created channel.
	 * @param source The source of the channel create request.
	 * @param socket Socket to the requesting user.
	 */
	private void onChannelCreateRequest(int source, Socket socket) {
		try {
			// Reading channel data
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			String name = dis.readUTF();
			String topic = dis.readUTF();
			String description = dis.readUTF();
			boolean hasPassword = dis.readBoolean();
			String password = dis.readUTF();
			int maxClients = dis.readInt();
			boolean permanent = dis.readBoolean();
			
			// Checking permission
			if(PermissionManager.canCreateChannel(source)) {
				// Creating the channel
				int channelID = ChannelManager.createChannel(name, topic, description, hasPassword, password, maxClients, permanent);
				
				// Moving user to the new channel
				UserManager.moveUser(source, channelID, password);
			} else {
				// Sending insufficient permission notification
				sendInsufficientPermissionNotification(socket);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Modifies a channel with the provided parameters after
	 * the handler received a channel modify request in run().
	 * @param source The source of the channel modify request.
	 * @param socket Socket to the requesting user.
	 */
	private void onChannelModifyRequest(int source, Socket socket) {
		try {
			// Reading channel data 
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			int channelID = dis.readInt();
			String name = dis.readUTF();
			String topic = dis.readUTF();
			String description = dis.readUTF();
			boolean hasPassword = dis.readBoolean();
			String password = dis.readUTF();
			int maxClients = dis.readInt();
			boolean permanent = dis.readBoolean();
			
			// Checking permission
			if(PermissionManager.canModifyChannel(source)) {
				// Modifying the channel
				ChannelManager.modifyChannel(channelID, name, topic, description, hasPassword, password, maxClients, permanent);
			} else {
				// Sending insufficient permission notification
				sendInsufficientPermissionNotification(socket);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes a channel with the provided parameters after
	 * the handler received a channel delete request in run().
	 * @param source The source of the channel delete request.
	 * @param socket Socket to the requesting user.
	 */
	private void onChannelDeleteRequest(int source, Socket socket) {
		try {
			// Reading channel data
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			int channelID = dis.readInt();
			
			// Checking permission
			if(PermissionManager.canDeleteChannel(source)) {
				// Deleting channel
				ChannelManager.deleteChannel(channelID);
			} else {
				// Sending insufficient permission notification
				sendInsufficientPermissionNotification(socket);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends the list of users to the requesting user after
	 * the handler received a user list request in run().
	 * @param source The source of the user list request.
	 * @param socket Socket to the requesting user.
	 */
	private void onUserListRequest(int source, Socket socket) {
		try {
			// Getting user list
			Map<Integer, User> userList = UserManager.getAllUsers();
			
			// Writing user list
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(MessageType.USER_LIST.toInteger());
			dos.writeInt(userList.size());
			
			for(User user : userList.values()) {
				dos.writeInt(user.getID());
				dos.writeUTF(user.getUserData().getUsername());
				dos.writeUTF(user.getUserData().getNickname());
				dos.writeInt(user.getChannel());
				dos.flush();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Moves the specified user to the specified channel after
	 * the handler received a user move request in run().
	 * @param source The source of the user move request.
	 * @param socket Socket to the requesting user.
	 */
	private void onUserMoveRequest(int source, Socket socket) {
		try {
			// Reading move data
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			int userID = dis.readInt();
			int channelID = dis.readInt();
			String password = dis.readUTF();
			
			// Moving user
			UserManager.moveUser(userID, channelID, password);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to all users within the same channel as the
	 * source after the handler received a message request in run().
	 * @param source The source of the message request.
	 * @param socket Socket to the requesting user.
	 */
	private void onMessageRequest(int source, Socket socket) {
		try {
			// Reading message data
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			String message = dis.readUTF();
			
			// Acquiring message targets
			int channelID = UserManager.getUser(source).getChannel();
			Set<Integer> targets = ChannelManager.getChannel(channelID).getUsers();
			
			for(int target : targets) {
				// Getting socket for the target
				Socket targetSocket = UserManager.getUser(target).getSocket();
				
				synchronized(targetSocket) {
					// Sending message data to targets
					DataOutputStream dos = new DataOutputStream(targetSocket.getOutputStream());
					dos.writeInt(MessageType.MESSAGE.toInteger());
					dos.writeInt(source);
					dos.writeUTF(message);
					dos.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends an insufficient permission message to the specified socket.
	 * This message is sent to clients when their requested operation
	 * failed.
	 * @param socket The TCP socket to send the notification message.
	 */
	private void sendInsufficientPermissionNotification(Socket socket) {
		try { 
			// Sending notification
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(MessageType.INSUFFICIENT_PERMISSION.toInteger());
			dos.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
};
