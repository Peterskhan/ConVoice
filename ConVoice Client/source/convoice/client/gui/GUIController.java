package convoice.client.gui;


// Java imports 
import java.util.Optional;
import java.util.NoSuchElementException;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;

// Project imports
import convoice.client.channel.ChannelManager;
import convoice.client.channel.Channel;
import convoice.client.connection.ConnectionManager;
import convoice.client.user.User;
import convoice.client.user.UserManager;

/**
 * The GUIController class is responsible for managing
 * the user interface of the application. 
 * It initializes GUI elements, binds them to the appropriate 
 * event handlers, and provides an interface for the rest of 
 * the application to interact with the GUI.
 */
public class GUIController {
	// The main menu
	@FXML
	public Accordion mainMenu;
	
	// Panels of the main menu
	@FXML
	public TitledPane connectionPanel;
	@FXML
	public TitledPane channelsPanel;
	
	// Elements of the connection menu
	@FXML
	public TextField connectionAddressTextField;
	@FXML
	public TextField connectionPortTextField;
	@FXML
	public TextField connectionNicknameTextField;
	@FXML
	public TextField connectionUsernameTextField;
	@FXML
	public PasswordField connectionPasswordField;
	@FXML
	public CheckBox connectionMemberCheckBox;
	@FXML
	public Button connectionConnectDisconnectButton;
	
	// Elements of the channel menu
	@FXML 
	public TextField channelNameTextField;
	@FXML
	public TextField channelTopicTextField;
	@FXML
	public TextArea channelDescriptionTextArea;
	@FXML
	public PasswordField channelPasswordField;
	@FXML
	public TextField channelMaxClientsTextField;
	@FXML
	public CheckBox channelPasswordCheckBox;
	@FXML
	public CheckBox channelPermanentCheckBox;
	@FXML
	public Button channelEditApplyButton;
	@FXML
	public Button channelDeleteButton;
	@FXML
	public Button channelCreateSaveButton;
	
	// The application status label
	@FXML
	public Label mainStatusLabel;
	
	// The main tree view
	@FXML
	public TreeView<Object> mainChannelsTreeView;
	
	// The inspector text area
	@FXML
	public TextArea mainInspectorTextArea;
	
	// The main chat text area
	@FXML
	public TextArea mainChatTextArea;
	
	// The chat input field
	@FXML
	public TextField mainChatInputTextField;
	
	// The send button
	@FXML
	public Button mainSendButton;
	
	// Application graphics resources
	private Image serverIcon;
	private Image channelIcon;
	private Image userIcon;
	
	// Static instance
	public static GUIController staticInstance = null;
	
	// Initializer methods
	
	/**
	 * Initializes the user interface.
	 */
	public void initialize() {
		// Loading graphics resources
		loadGraphicsResources();
		
		// Making the connection menu visible
		mainMenu.setExpandedPane(connectionPanel);
		connectionPanel.setCollapsible(false);
		
		// Disabling menus until configuration is loaded
		channelsPanel.setDisable(true);
				
		// Initializing channel menu elements
		channelNameTextField.setDisable(true);
		channelTopicTextField.setDisable(true);
		channelDescriptionTextArea.setDisable(true);
		channelPasswordCheckBox.setDisable(true);
		channelPasswordField.setDisable(true);
		channelMaxClientsTextField.setDisable(true);
		channelPermanentCheckBox.setDisable(true);
		
		// Initializing main channels tree view
		mainChannelsTreeView.setDisable(true);
		
		// Initializing send button
		mainSendButton.setDisable(true);
		
		// Binding user interface elements to controllers
		bindControllers();
	}
	
	/**
	 * Binds event handler to user interface elements.
	 */
	private void bindControllers() {
		// Binding connection menu elements
		connectionConnectDisconnectButton.setOnAction(e -> {
			onConnectionConnectDisconnectButtonClicked();
		});
		
		// Binding channels menu elements
		channelEditApplyButton.setOnAction(e -> {
			onChannelEditApplyButtonClicked();
		});
		channelDeleteButton.setOnAction(e -> {
			onChannelDeleteButtonClicked();
		});
		channelCreateSaveButton.setOnAction(e -> {
			onChannelCreateSaveButtonClicked();
		});
		
		// Binding main channels tree view
		mainChannelsTreeView.setOnMouseClicked(e -> {
			if(e.getClickCount() == 2) {
				onMainChannelsTreeViewChannelSwitchRequested();
			}
		});
		mainChannelsTreeView.getSelectionModel().selectedItemProperty().addListener(e -> {
			onMainChannelsTreeViewItemSelected();
		});
		
		// Binding main chat input text field
		mainChatInputTextField.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER){
				onMainSendButtonClicked();
			}
		});
		
		// Binding main send button
		mainSendButton.setOnAction(e -> {
			onMainSendButtonClicked();
		});
	}
	
	/**
	 * Updates and enables menus dependent on being connected
	 * to a remote server.
	 */
	private void connectedToServer() {
		// Enabling menus dependent on connection
		channelsPanel.setDisable(false);
		
		// Enabling main channels tree view
		mainChannelsTreeView.setDisable(false);
		
		// Enabling send button
		mainSendButton.setDisable(false);
		
		// Clearing chat text area
		mainChatTextArea.clear();
		
		// Making the connection menu collapsible
		connectionPanel.setCollapsible(true);
		
		// Updating channel menu
		channelModifiedUpdate_m(ChannelManager.getChannel(ChannelManager.getOwnChannelID()));
		
		// Updating main channels tree view
		TreeItem<Object> root = new TreeItem<Object>("Server", new ImageView(serverIcon));
		mainChannelsTreeView.setRoot(root);
		mainChannelsTreeView.getRoot().setExpanded(true);
	}
	
	/**
	 * Loads graphics resources to the application.
	 */
	private void loadGraphicsResources() {
		try {
			serverIcon = new Image("convoice/client/gui/images/server.png");
			channelIcon = new Image("convoice/client/gui/images/channel.png");
			userIcon = new Image("convoice/client/gui/images/user.png");
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	// Connection menu controllers
	
	/**
	 * Handles when the Connect/Disconnect button is clicked on the
	 * Connection panel.
	 */
	private void onConnectionConnectDisconnectButtonClicked() {
		// Checking whether connecting or disconnecting
		if(connectionConnectDisconnectButton.getText().equals("Connect")) { // Connect
			// Getting connection data
			String address = connectionAddressTextField.getText();
			int port;
			String nickname = connectionNicknameTextField.getText();
			String username = connectionUsernameTextField.getText();
			String password = connectionPasswordField.getText();
			boolean member = connectionMemberCheckBox.isSelected();

			// Validating connection data
			if(address.isEmpty()) {
				// Focusing the malformed input field
				connectionAddressTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Connection failed: No address.");
				
				// Validation failed, do not change anything
				return;
			}
			try {
				port = Integer.parseInt(connectionPortTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				connectionPortTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			if(nickname.isEmpty()) {
				// Focusing the malformed input field
				connectionNicknameTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Connection failed: No nickname");
				
				// Validation failed, do not change anything
				return;
			}
			if(member) {
				if(username.isEmpty()) {
					// Focusing the malformed input field
					connectionUsernameTextField.requestFocus();
					
					// Updating status label
					mainStatusLabel.setText("Connection failed: No username.");
					
					// Validation failed, do not change anything
					return;
				}
				if(password.isEmpty()) {
					// Focusing the malformed input field
					connectionPasswordField.requestFocus();
					
					// Updating status label
					mainStatusLabel.setText("Connection failed: No password.");
					
					// Validation failed, do not change anything
					return;
				}
			}

			// Connecting 
			if(!ConnectionManager.connect(address, port)) {
				// Updating main status label
				mainStatusLabel.setText("Server is unreachable.");
				return;
			}

			// Logging in			
			if(!ConnectionManager.login(nickname, username, password, member)) {
				// Updating main status label
				mainStatusLabel.setText("Login to the server failed.");
				return;
			}

			// Getting channels from server
			ConnectionManager.requestChannelList();

			// Getting users from server
			ConnectionManager.requestUserList();

			// Connection was successful
			connectedToServer();

			// Starting listening for server messages
			ConnectionManager.start();

			// Updating main status label
			mainStatusLabel.setText("Connected.");

			// Disabling connection data fields
			connectionAddressTextField.setDisable(true);
			connectionPortTextField.setDisable(true);
			connectionNicknameTextField.setDisable(true);
			connectionUsernameTextField.setDisable(true);
			connectionPasswordField.setDisable(true);
			connectionMemberCheckBox.setDisable(true);

			// Switching to channels panel
			channelsPanel.setExpanded(true);

			// Setting button text
			connectionConnectDisconnectButton.setText("Disconnect");
			
		} else { // Disconnect
			// Stopping listening for server messages
			ConnectionManager.stop();

			// Disconnecting
			ConnectionManager.disconnect();

			// Clearing channel tree view
			mainChannelsTreeView.setRoot(null);

			// Disabling Send button
			mainSendButton.setDisable(true);

			// Updating main status label
			mainStatusLabel.setText("Disconnected.");

			// Enabling connection data fields
			connectionAddressTextField.setDisable(false);
			connectionPortTextField.setDisable(false);
			connectionNicknameTextField.setDisable(false);
			connectionUsernameTextField.setDisable(false);
			connectionPasswordField.setDisable(false);
			connectionMemberCheckBox.setDisable(false);

			// Disabling channels panel
			channelsPanel.setDisable(true);

			// Setting button text
			connectionConnectDisconnectButton.setText("Connect");
		}
	}
	
	// Channel menu controllers
	
	/**
	 * Handles when the Edit/Apply button is clicked on the
	 * Channels panel.
	 */
	private void onChannelEditApplyButtonClicked() {
		// Checking if editing or applying changes
		if(channelEditApplyButton.getText().equals("Edit")) { // Edit
			// Enabling edition of fields
			channelNameTextField.setDisable(false);
			channelTopicTextField.setDisable(false);
			channelDescriptionTextArea.setDisable(false);
			channelPasswordCheckBox.setDisable(false);
			channelPasswordField.setDisable(false);
			channelMaxClientsTextField.setDisable(false);
			channelPermanentCheckBox.setDisable(false);
			
			// Disabling buttons
			channelDeleteButton.setDisable(true);
			channelCreateSaveButton.setDisable(true);
			
			// Changing button text
			channelEditApplyButton.setText("Apply");
			
		} else { // Apply
			// Validating fields
			if(channelNameTextField.getText().isEmpty()) {
				// Focusing the malformed input field
				channelNameTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Error: Set channel name.");
				
				// Validation failed, do not change anything
				return;
			}
			if(channelPasswordCheckBox.isSelected()) {
				if(channelPasswordField.getText().isEmpty()) {
					// Focusing the malformed input field
					channelPasswordField.requestFocus();
					
					// Updating status label
					mainStatusLabel.setText("Error: Set channel password.");
					
					// Validation failed, do not change anything
					return;
				}
			}
			try {
				Integer.parseInt(channelMaxClientsTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				channelMaxClientsTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			
			// Sending modification request
			ConnectionManager.requestChannelModify(ChannelManager.getOwnChannelID(), 
					channelNameTextField.getText(), 
					channelTopicTextField.getText(), 
					channelDescriptionTextArea.getText(), 
					channelPasswordCheckBox.isSelected(), 
					channelPasswordField.getText(), 
					Integer.parseInt(channelMaxClientsTextField.getText()), 
					channelPermanentCheckBox.isSelected());
			
			// Updating channel menu
			channelModifiedUpdate_m(ChannelManager.getChannel(ChannelManager.getOwnChannelID()));
			
			// Disabling edition of fields
			channelNameTextField.setDisable(true);
			channelTopicTextField.setDisable(true);
			channelDescriptionTextArea.setDisable(true);
			channelPasswordCheckBox.setDisable(true);
			channelPasswordField.setDisable(true);
			channelMaxClientsTextField.setDisable(true);
			channelPermanentCheckBox.setDisable(true);

			// Enabling buttons
			channelDeleteButton.setDisable(false);
			channelCreateSaveButton.setDisable(false);
			
			// Changing button text
			channelEditApplyButton.setText("Edit");
		}
	}
	
	/**
	 * Handles when the Delete button is clicked on the
	 * Channels panel.
	 */
	private void onChannelDeleteButtonClicked() {
		// Sending deletion request
		if(ChannelManager.getOwnChannelID() != ChannelManager.DEFAULT_CHANNEL_ID) {
			ConnectionManager.requestChannelDelete(ChannelManager.getOwnChannelID());
		}	
	}
	
	/**
	 * Handles when the Create/Save button is clicked on the
	 * Channels panel.
	 */
	private void onChannelCreateSaveButtonClicked() {
		// Checking if creating or saving
		if(channelCreateSaveButton.getText().equals("Create")) { // Create
			// Enabling edition of fields
			channelNameTextField.setDisable(false);
			channelTopicTextField.setDisable(false);
			channelDescriptionTextArea.setDisable(false);
			channelPasswordCheckBox.setDisable(false);
			channelPasswordField.setDisable(false);
			channelMaxClientsTextField.setDisable(false);
			channelPermanentCheckBox.setDisable(false);
			
			// Clearing fields
			channelNameTextField.clear();
			channelTopicTextField.clear();
			channelDescriptionTextArea.clear();
			channelPasswordCheckBox.setSelected(false);
			channelPasswordField.clear();
			channelMaxClientsTextField.clear();
			channelPermanentCheckBox.setSelected(false);
			
			// Disabling buttons
			channelDeleteButton.setDisable(true);
			channelEditApplyButton.setDisable(true);
			
			// Changing button text
			channelCreateSaveButton.setText("Save");
			
		} else { // Save
			// Validating fields
			if(channelNameTextField.getText().isEmpty()) {
				// Focusing the malformed input field
				channelNameTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Error: Set channel name.");
				
				// Validation failed, do not change anything
				return;
			}
			if(channelPasswordCheckBox.isSelected()) {
				if(channelPasswordField.getText().isEmpty()) {
					// Focusing the malformed input field
					channelPasswordField.requestFocus();
					
					// Updating status label
					mainStatusLabel.setText("Error: Set channel password.");
					
					// Validation failed, do not change anything
					return;
				}
			}
			try {
				Integer.parseInt(channelMaxClientsTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				channelMaxClientsTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			
			// Sending creating request
			ConnectionManager.requestChannelCreate(channelNameTextField.getText(), 
					channelTopicTextField.getText(), 
					channelDescriptionTextArea.getText(), 
					channelPasswordCheckBox.isSelected(), 
					channelPasswordField.getText(), 
					Integer.parseInt(channelMaxClientsTextField.getText()), 
					channelPermanentCheckBox.isSelected());
			
			// Disabling edition of fields
			channelNameTextField.setDisable(true);
			channelTopicTextField.setDisable(true);
			channelDescriptionTextArea.setDisable(true);
			channelPasswordCheckBox.setDisable(true);
			channelPasswordField.setDisable(true);
			channelMaxClientsTextField.setDisable(true);
			channelPermanentCheckBox.setDisable(true);

			// Enabling buttons
			channelDeleteButton.setDisable(false);
			channelEditApplyButton.setDisable(false);
			
			// Changing button text
			channelCreateSaveButton.setText("Create");
		}
	}
	
	// Main channels tree view controllers
	
	/**
	 * Handles when the selection of the main
	 * tree view changes.
	 */
	private void onMainChannelsTreeViewItemSelected() {
		// Getting the selected tree item
		TreeItem<Object> selected = mainChannelsTreeView.getSelectionModel().getSelectedItem();
		
		// Checking the level of the selected item
		int itemLevel = mainChannelsTreeView.getTreeItemLevel(selected);
		
		// Clearing the inspector text area
		mainInspectorTextArea.clear();
		
		// Handling the selection according to level
		switch(itemLevel) {
		case 0: 
			// The selected item is the root item (server)
			mainInspectorTextArea.appendText("----- Server -----\n");
			mainInspectorTextArea.appendText("Name: " + ConnectionManager.getServerName()+ "\n");
			mainInspectorTextArea.appendText("Version: " + ConnectionManager.getServerVersion() + "\n"); 
			break;
		case 1: 
			// The selected item is a node item (channel)
			Channel channel = (Channel) selected.getValue();
			mainInspectorTextArea.appendText("----- Channel -----\n");
			mainInspectorTextArea.appendText("Name: " + channel.getChannelData().getName() + "\n");
			mainInspectorTextArea.appendText("Topic: " + channel.getChannelData().getTopic() + "\n");
			mainInspectorTextArea.appendText("Description: " + channel.getChannelData().getDescription() + "\n");
			mainInspectorTextArea.appendText("Password protected: " + channel.getChannelData().hasPassword() + "\n");
			mainInspectorTextArea.appendText("Clients: " + channel.getUsers().size() + " / " + channel.getChannelData().getMaxClients() + "\n");
			mainInspectorTextArea.appendText("Permanent: " + channel.isPermanent() + "\n");
			break;
		case 2: 
			// The selected item is a node item (user)
			User user = (User) selected.getValue();
			mainInspectorTextArea.appendText("----- User -----\n");
			mainInspectorTextArea.appendText("Nickname: " + user.getUserData().getNickname() + "\n");
			break;
		}
	}
	
	/**
	 * Handles when a channel switch is initiated by double
	 * clicking a channel in the channels tree view.
	 */
	private void onMainChannelsTreeViewChannelSwitchRequested() {
		// Getting selected tree item
		TreeItem<Object> selected = mainChannelsTreeView.getSelectionModel().getSelectedItem();
		
		// Checking the selected item is a channel
		if(mainChannelsTreeView.getTreeItemLevel(selected) == 1) {
			// Getting channel object from tree item
			Channel channel = (Channel) selected.getValue();
			
			// Setting up dialog results
			Optional<String> result = null;
			String password = "";
			
			// Showing password dialog if necessary
			if(channel.getChannelData().hasPassword()) {
				TextInputDialog passwordDialog = new TextInputDialog();
				passwordDialog.setTitle("Password validation...");
				passwordDialog.setHeaderText("Please enter the channel password:");
				result = passwordDialog.showAndWait();
				try {
					password = result.get();
				} catch(NoSuchElementException e) {
					return;
				}
			}
			
			// Requesting move
			ConnectionManager.requestUserMove(UserManager.getOwnID(), channel.getID(), password);
		}
	}
	
	// Main send button controller
	
	/**
	 * Handles when the Send button is clicked.
	 */
	private void onMainSendButtonClicked() {
		// Sending message
		if(!mainChatInputTextField.getText().isEmpty()) {
			ConnectionManager.sendMessage(mainChatInputTextField.getText());
			mainChatInputTextField.clear();
		}
	}
	
	// Update methods
	
	/**
	 * Notifies the GUI about the created channel.
	 * @param channel The created channel object.
	 */
	private void channelCreatedUpdate_m(Channel channel) {
		// Updating main tree view

		// Creating channel tree node
		TreeItem<Object> channelNode = new TreeItem<Object>(channel, new ImageView(channelIcon));

		// Adding channel node to root
		mainChannelsTreeView.getRoot().getChildren().add(channelNode);
	}
	
	/**
	 * Notifies the GUI about the modified channel.
	 * @param channel The channel object.
	 */
	private void channelModifiedUpdate_m(Channel channel) {
		// Updating user interface elements
		channelNameTextField.setText(channel.getChannelData().getName());
		channelTopicTextField.setText(channel.getChannelData().getTopic());
		channelDescriptionTextArea.setText(channel.getChannelData().getDescription());
		channelPasswordCheckBox.setSelected(channel.getChannelData().hasPassword());
		channelPasswordField.clear();
		channelMaxClientsTextField.setText(String.valueOf(channel.getChannelData().getMaxClients()));
		channelPermanentCheckBox.setSelected(channel.isPermanent());
		
		// Updating main tree view
		mainChannelsTreeView.refresh();
	}
	
	/**
	 * Notifies the GUI about the deleted channel.
	 * @param channelID The ID of the channel.
	 */
	private void channelDeletedUpdate_m(int channelID) {
		// Updating main tree view
		
		// Searching the channel node to delete
		for(TreeItem<Object> actualNode : mainChannelsTreeView.getRoot().getChildren()) {
			// Getting channel object from the node
			Channel actualChannel = (Channel) actualNode.getValue();
			
			// Comparing channel IDs
			if(actualChannel.getID() == channelID) {
				// Removing node
				actualNode.getParent().getChildren().remove(actualNode);
				return;
			}
		}
	}
	
	/**
	 * Notifies the GUI about the created user.
	 * @param user The created user object.
	 */
	private void userCreatedUpdate_m(User user) {

		// Updating main tree view
		
		// Creating user tree node
		TreeItem<Object> userNode = new TreeItem<Object>(user, new ImageView(userIcon));
		
		// Searching the default channel
		for(TreeItem<Object> actualNode : mainChannelsTreeView.getRoot().getChildren()) {
			// Getting the channel object from the node
			Channel actualChannel = (Channel) actualNode.getValue();
			
			// Comparing channel IDs
			if(actualChannel.getID() == ChannelManager.DEFAULT_CHANNEL_ID) {
				// Adding user node
				actualNode.getChildren().add(userNode);				
				return;
			}
		}
	}

	/**
	 * Notifies the GUI about the moved user.
	 * @param userID The ID of the user.
	 * @param channelID The ID of the channel moved to.
	 */
	private void userMovedUpdate_m(int userID, int channelID) {
		// Updating main tree view

		// Removing user node from previous channel node
		for(TreeItem<Object> actualChannelNode : mainChannelsTreeView.getRoot().getChildren()) {
			// Searching moved user
			for(TreeItem<Object> actualUserNode : actualChannelNode.getChildren()) {
				// Getting user object from the node
				User actualUser = (User) actualUserNode.getValue();

				// Comparing user IDs
				if(actualUser.getID() == userID) {
					actualChannelNode.getChildren().remove(actualUserNode);
					break;
				}
			}
		}

		// Adding user to current channel node
		for(TreeItem<Object> actualChannelNode : mainChannelsTreeView.getRoot().getChildren()) {
			// Getting channel object from the node
			Channel actualChannel = (Channel) actualChannelNode.getValue();

			// Comparing channel IDs
			if(actualChannel.getID() == channelID) {
				// Creating user node
				TreeItem<Object> userNode = new TreeItem<Object>(UserManager.getUser(userID), 
						new ImageView(userIcon));

				// Adding user node to channel node
				actualChannelNode.getChildren().add(userNode);
				return;
			}
		}
	}
	
	/**
	 * Notifies the GUI about the deleted user.
	 * @param userID The ID of the user.
	 */
	private void userDeletedUpdate_m(int userID) {
		// Updating main tree view

		// Removing user node from previous channel node
		for(TreeItem<Object> actualChannelNode : mainChannelsTreeView.getRoot().getChildren()) {
			// Searching moved user
			for(TreeItem<Object> actualUserNode : actualChannelNode.getChildren()) {
				// Getting user object from the node
				User actualUser = (User) actualUserNode.getValue();

				// Comparing user IDs
				if(actualUser.getID() == userID) {
					actualChannelNode.getChildren().remove(actualUserNode);
					return;
				}
			}
		}
	}
	
	/**
	 * Notifies the GUI about the received message.
	 * @param message The received message.
	 */
	private void messageReceivedUpdate_m(String message) {
		// Appending message to chat
		mainChatTextArea.appendText(message + "\n");
	}
	
	/**
	 * Notifies the GUI about the connection being terminated.
	 */
	private void connectionTerminatedUpdate_m() {
		// Changing Connect/Disconnect button text
		connectionConnectDisconnectButton.setText("Connect");
		
		// Clearing channel tree view
		mainChannelsTreeView.setRoot(null);
		
		// Clearing the inspector
		mainInspectorTextArea.clear();
		
		// Collapsing & disabling channel menu
		channelsPanel.setDisable(true);
		channelsPanel.setExpanded(false);
		
		// Bringing up connection menu
		connectionPanel.setExpanded(true);
		
		// Setting status message
		mainStatusLabel.setText("Connection terminated.");
	}
	
	/**
	 * Notifies the GUI about the being switched to another channel.
	 * @param channelID The ID of the channel switched to.
	 */
	private void channelSwitchedUpdate_m(int channelID) {
		// Getting channel object
		Channel channel = ChannelManager.getChannel(channelID);
		
		// Setting fields
		channelNameTextField.setText(channel.getChannelData().getName());
		channelTopicTextField.setText(channel.getChannelData().getTopic());
		channelDescriptionTextArea.setText(channel.getChannelData().getDescription());
		channelPasswordCheckBox.setSelected(channel.getChannelData().hasPassword());
		channelPasswordField.clear();
		channelMaxClientsTextField.setText(String.valueOf(channel.getChannelData().getMaxClients()));
		channelPermanentCheckBox.setSelected(channel.isPermanent());
	}
	
	// Status & Output
	
	/**
	 * Sets the text of the main status label.
	 * @param message The status message to set.
	 */
	private void setStatusMessage_m(String message) {
		mainStatusLabel.setText(message);
	}
	
	/**
	 * Shows an alert dialog with the specified text.
	 * @param message The message to display in the dialog.
	 */
	private void showAlertDialog_m(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setHeaderText(message);
		alert.show();
	}
	
	// Static accessor methods
	
	/**
	 * Gets the static instance of the GUIController.
	 * @return The static GUIController instance.
	 */
	public static GUIController getStaticInstance() {
		return staticInstance;
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param channel The created channel object.
	 */
	public static void channelCreatedUpdate(Channel channel) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.channelCreatedUpdate_m(channel);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param channel The channel object.
	 */
	public static void channelModifiedUpdate(Channel channel) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.channelModifiedUpdate_m(channel);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param channelID The ID of the channel.
	 */
	public static void channelDeletedUpdate(int channelID) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.channelDeletedUpdate_m(channelID);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param user The created user object.
	 */
	public static void userCreatedUpdate(User user) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.userCreatedUpdate_m(user);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param userID The ID of the user.
	 * @param channelID The ID of the channel moved to.
	 */
	public static void userMovedUpdate(int userID, int channelID) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.userMovedUpdate_m(userID, channelID);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param userID The ID of the user.
	 */
	public static void userDeletedUpdate(int userID) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.userDeletedUpdate_m(userID);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param message The received message.
	 */
	public static void messageReceivedUpdate(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.messageReceivedUpdate_m(message);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 */
	public static void connectionTerminatedUpdate() {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.connectionTerminatedUpdate_m();
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param channelID The ID of the channel switched to.
	 */
	public static void channelSwitchedUpdate(int channelID) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.channelSwitchedUpdate_m(channelID);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param message The status message to set.
	 */
	public static void setStatusMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.setStatusMessage_m(message);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param message The message to display in the dialog.
	 */
	public static void showAlertDialog(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.showAlertDialog_m(message);
			}
		});
	}

	// Close request handler

	/**
	 * Handles application close requests.
	 */
	public void onCloseRequest() {
		ConnectionManager.disconnect();
		Platform.exit();
	}
	
};
