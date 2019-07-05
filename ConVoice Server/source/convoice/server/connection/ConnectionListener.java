package convoice.server.connection;


// Java imports
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Project imports
import convoice.server.gui.GUIController;
import convoice.server.logger.LogManager;
import convoice.server.server.Server;
import convoice.server.user.UserManager;

/**
 * The ConnectionListener class is responsible for listening
 * on a specified port and accepting or rejecting incoming
 * connections. 
 * When an incoming connection is detected the
 * listener tries to assign resources to it by assigning it
 * to a ConnectionHandler and creating a User for it trough the
 * UserManager. If all resources are available the connection is 
 * accepted, otherwise it's rejected. In both cases the client
 * received an acknowledgement message about success or failure.
 */
public class ConnectionListener implements Runnable {
	/** The TCP server socket listening for connections. */
	private ServerSocket m_serverSocket;	
	
	/** The running state-flag. */
	private volatile boolean m_running;
	
	/** The should run state-flag. */
	private volatile boolean m_shouldRun;	
	
	/**
	 * Constructs a ConnectionListener object.
	 * @param port The port to listen on.
	 */
	public ConnectionListener(int port) {		
		try {
			// Initializing members
			m_serverSocket = new ServerSocket(port);
			m_running = false;
			m_shouldRun = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs the main loop which handles incoming connections.
	 * The listener will actively block until a connection request
	 * is received. On incoming connections, a ConnectionHandler is
	 * attempted to be assigned to the new connection. If the assignment
	 * was successful the connection is accepted and the listener
	 * performs the initial data-exchange. Otherwise the connection is
	 * rejected by sending the client a message about the reason of the
	 * rejection and closing the created socket.
	 */
	public void run() {
		// Setting state-flags
		m_shouldRun = true;
		m_running = true;
		
		// Updating the user interface
		GUIController.setConnectionsListenerStatus("Active");
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "Connection listener started.");

		// Main loop
		while(m_shouldRun) {
			try {
				// Accepting incoming connection
				Socket socket = m_serverSocket.accept();
				
				// Validating connection
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				MessageType type = MessageType.fromInteger(dis.readInt());
				if(!type.equals(MessageType.CONNECTION_REQUEST)) {
					rejectConnection(socket, "Bad protocol.");
					continue;
				}
				
				// Requesting handler for the connection
				ConnectionHandler handler = ConnectionManager.getHandler();
				
				// Checking the received handler
				if(handler != null) {
					// Accepting connection
					int userID = acceptConnection(socket);
					
					// Assigning to handler
					if(userID != -1) {
						handler.addUser(userID);
					}
				} else {
					// Rejecting connection
					rejectConnection(socket, "Server is full.");
				}	
			// The socket was closed by calling stop()
			} catch(SocketException e) {
				break;
			// Failure at accepting connection
			} catch(IOException e) {
				e.printStackTrace();
				break;
			} 
		}
		
		// Updating the user interface
		GUIController.setConnectionsListenerStatus("Inactive");
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "Connection listener stopped.");
		
		// Setting the running state-flag
		m_running = false;
	}
	
	/**
	 * Returns whether the ConnectionListener is running or not.
	 * @return True if the listener is running.
	 */
	public boolean isRunning() {
		return m_running;
	}
	
	/**
	 * Signals the ConnectionListener to stop.
	 * The listener will shut down after the current iteration
	 * of run() finishes. The server socket is also closed, which
	 * will cause an exception to be thrown if the listener is 
	 * currently blocked in accept().
	 */
	public void stop() {
		try {
			m_serverSocket.close();
			m_shouldRun = false;	
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Accepts the connection incoming on the specified socket.
	 * Reads connection data from the socket, creates a new user via
	 * the UserManager and sends the client a greeting message along
	 * with the server data. The ID of the user created from the connection
	 * is returned. In case of member login attempts, validation also takes
	 * place. If validation fails the connection is rejected and -1 is returned.
	 * @param socket The socket of the connection.
	 * @return The ID of the created user, or -1 if validation failed.
	 */
	private int acceptConnection(Socket socket) {
		int userID = 0;
		try {
			// Reading connection data
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			boolean isMember = dis.readBoolean();
			String username = dis.readUTF();
			String nickname = dis.readUTF();
			String password = dis.readUTF();
						
			if(isMember) {
				// Validating membership in case of member login
				if(UserManager.validateMember(username, password)) {
					// Validation successful, creating user
					userID = UserManager.createUser(username, nickname, password, socket);
					
					// Logging in user with membership rights
					UserManager.loginMember(userID);
					
					// Updating user interface
					GUIController.addMembersOutputMessage(username + " (Member) connected.");
				} else {
					// Validation failed, rejecting connection
					rejectConnection(socket, "Incorrect username / password.");
					return -1;
				}
			} else {
				// Guest login, creating user
				userID = UserManager.createUser(username, nickname, password, socket);
				
				// Logging in user with guest rights
				UserManager.loginGuest(userID);
			}
			
			// Sending server response
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(MessageType.CONNECTION_ACCEPTED.toInteger());
			dos.writeUTF(Server.getName());
			dos.writeUTF(Server.getVersion());
			dos.writeUTF(Server.getWelcomeMessage());
			dos.writeInt(userID);
			dos.flush();
			
			// Updating user interface
			GUIController.setConnectionsClientsCount(ConnectionManager.getClientCount());
		} catch(IOException e) {
			e.printStackTrace();
		} 
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "New connection accepted.");
		
		return userID;
	}
	
	/**
	 * Rejects the connection, and sends a message to the client with
	 * the reason of the rejection.
	 * @param socket The socket of the connection.
	 * @param reason The reason of rejection.
	 */
	private void rejectConnection(Socket socket, String reason) {
		try {
			// Sending rejection data to the client
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(MessageType.CONNECTION_REJECTED.toInteger());
			dos.writeUTF(reason);
			dos.flush();
			
			// Closing the socket
			socket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Logging
		LogManager.addConnectionsLog(Level.INFO, "New connection rejected: " + reason);
	}
		
};
