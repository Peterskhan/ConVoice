package convoice.server.gui;


// Java imports
import java.io.File;
import java.util.Collection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Project imports
import convoice.server.channel.Channel;
import convoice.server.channel.ChannelManager;
import convoice.server.connection.ConnectionManager;
import convoice.server.permission.PermissionManager;
import convoice.server.server.Server;
import convoice.server.user.User;
import convoice.server.user.UserData;
import convoice.server.user.UserManager;

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
	public TitledPane configPanel;
	@FXML
	public TitledPane serverPanel;
	@FXML
	public TitledPane connectionsPanel;
	@FXML
	public TitledPane channelsPanel;
	@FXML
	public TitledPane permissionsPanel;
	@FXML
	public TitledPane membersPanel;
	@FXML
	public TitledPane logsPanel;
	
	// Elements of the configuration menu
	@FXML
	public TextField configPathTextField;
	@FXML
	public ComboBox<String> configSelectorComboBox;
	@FXML
	public TextArea configInfoTextArea;
	@FXML
	public Button configLoadButton;
	@FXML
	public Button configSaveButton;
	@FXML
	public Label configCurrentLabel;
	
	// Elements of the server menu
	@FXML 
	public Button serverStartButton;
	@FXML
	public Button serverStopButton;
	@FXML
	public TextField serverNameTextField;
	@FXML
	public TextField serverDefaultChannelNameTextField;
	@FXML
	public TextField serverDefaultChannelTopicTextField;
	@FXML
	public TextArea serverDefaultChannelDescriptionTextArea;
	@FXML
	public TextArea serverWelcomeMessageTextArea;
	@FXML 
	public TextField serverVersionTextField;
	@FXML 
	public Button serverEditApplyButton;
	
	// Elements of the connections menu
	@FXML
	public Label connectionsServerStatusLabel;
	@FXML
	public Label connectionsListenerStatusLabel;
	@FXML
	public Label connectionsClientsLabel;
	@FXML
	public Label connectionsHandlersLabel;
	@FXML
	public TextArea connectionsOutputTextArea;
	@FXML
	public TextField connectionsPortTextField;
	@FXML 
	public TextField connectionsHandlersTextField;
	@FXML
	public TextField connectionsUserPerHandlerTextField;
	@FXML
	public Button connectionsEditApplyButton;
	
	// Elements of the channels menu
	@FXML
	public ComboBox<Channel> channelsSelectorComboBox;
	@FXML
	public TextField channelsNameTextField;
	@FXML
	public TextField channelsTopicTextField;
	@FXML
	public TextArea channelsDescriptionTextArea;
	@FXML
	public PasswordField channelsPasswordField;
	@FXML
	public TextField channelsMaxClientsTextField;
	@FXML
	public TextField channelsIdTextField;
	@FXML
	public CheckBox channelsPasswordCheckBox;
	@FXML
	public CheckBox channelsPermanentCheckBox;
	@FXML
	public Button channelsEditApplyButton;
	@FXML
	public Button channelsDeleteButton;
	@FXML
	public Button channelsCreateSaveButton;
	
	// Elements of the permissions menu
	@FXML
	public CheckBox permissionsMemberCreateChannelCheckBox;
	@FXML
	public CheckBox permissionsMemberDeleteChannelCheckBox;
	@FXML
	public CheckBox permissionsMemberModifyChannelCheckBox;
	@FXML
	public CheckBox permissionsGuestCreateChannelCheckBox;
	@FXML
	public CheckBox permissionsGuestDeleteChannelCheckBox;
	@FXML
	public CheckBox permissionsGuestModifyChannelCheckBox;
	@FXML
	public Button permissionsEditApplyButton;
	
	// Elements of the members menu
	@FXML
	public ComboBox<UserData> membersSelectorComboBox;
	@FXML
	public TextField membersUsernameTextField;
	@FXML
	public PasswordField membersPasswordField;
	@FXML
	public TextArea membersOutputTextArea;
	@FXML
	public Button membersEditApplyButton;
	@FXML
	public Button membersDeleteButton;
	@FXML
	public Button membersCreateSaveButton;
	
	// Elements of the logs menu
	@FXML
	public TextArea logsOutputTextArea;
	@FXML
	public Button logsClearButton;
	
	// The application status label
	@FXML
	public Label mainStatusLabel;
	
	// The main tree view
	@FXML
	public TreeView<Object> mainChannelsTreeView;
	
	// The main tree view context menu
	@FXML
	public ContextMenu mainChannelsContextMenu;
	
	// The inspector text area
	@FXML
	public TextArea mainInspectorTextArea;
	
	// Application graphics resources
	private Image serverIcon;
	private Image channelIcon;
	private Image userIcon;
	private Image editIcon;
	private Image deleteIcon;
	private Image createIcon;
	private Image startIcon;
	private Image stopIcon;
	
	// Configuration state flag
	private boolean configChanged = false;
	
	// Static instance
	public static GUIController staticInstance = null;
	
	// Initializer methods
	
	/**
	 * Initializes the user interface.
	 */
	public void initialize() {
		// Loading graphics resources
		loadGraphicsResources();
		
		// Making the configuration menu visible
		mainMenu.setExpandedPane(configPanel);
		configPanel.setCollapsible(false);

		// Disabling menus until configuration is loaded
		serverPanel.setDisable(true);
		connectionsPanel.setDisable(true);
		channelsPanel.setDisable(true);
		permissionsPanel.setDisable(true);
		membersPanel.setDisable(true);
		logsPanel.setDisable(true);
		
		// Initializing configuration menu elements
		configPathTextField.setText(System.getProperty("user.dir") + "\\configs");
		configSaveButton.setDisable(true);
		
		// Initializing server menu elements
		serverStopButton.setDisable(true);
		serverNameTextField.setDisable(true);
		serverDefaultChannelNameTextField.setDisable(true);
		serverDefaultChannelTopicTextField.setDisable(true);
		serverDefaultChannelDescriptionTextArea.setDisable(true);
		serverWelcomeMessageTextArea.setDisable(true);
		
		// Initializing connections menu elements
		connectionsPortTextField.setDisable(true);
		connectionsHandlersTextField.setDisable(true);
		connectionsUserPerHandlerTextField.setDisable(true);
		
		// Initializing channels menu elements
		channelsNameTextField.setDisable(true);
		channelsTopicTextField.setDisable(true);
		channelsDescriptionTextArea.setDisable(true);
		channelsPasswordField.setDisable(true);
		channelsMaxClientsTextField.setDisable(true);
		channelsPasswordCheckBox.setDisable(true);
		channelsPermanentCheckBox.setDisable(true);
		channelsEditApplyButton.setDisable(true);
		channelsDeleteButton.setDisable(true);
		
		// Initializing permissions menu elements
		permissionsMemberCreateChannelCheckBox.setDisable(true);
		permissionsMemberDeleteChannelCheckBox.setDisable(true);
		permissionsMemberModifyChannelCheckBox.setDisable(true);
		permissionsGuestCreateChannelCheckBox.setDisable(true);
		permissionsGuestDeleteChannelCheckBox.setDisable(true);
		permissionsGuestModifyChannelCheckBox.setDisable(true);
		
		// Initializing members menu elements
		membersUsernameTextField.setDisable(true);
		membersPasswordField.setDisable(true);
		membersEditApplyButton.setDisable(true);
		membersDeleteButton.setDisable(true);
		
		// Initializing main channels tree view
		mainChannelsTreeView.setContextMenu(mainChannelsContextMenu);
		mainChannelsTreeView.setDisable(true);
		
		// Binding user interface element to controllers
		bindControllers();
	}
	
	/**
	 * Binds event handlers to user interface elements.
	 */
	private void bindControllers() {
		// Binding configuration menu elements
		configPathTextField.textProperty().addListener(e -> {
			onConfigPathTextFieldChanged();
		});
		configLoadButton.setOnAction(e -> {
			onConfigLoadButtonClicked();
		});
		configSaveButton.setOnAction(e -> {
			onConfigSaveButtonClicked();
		});
		
		// Binding server menu elements
		serverStartButton.setOnAction(e -> {
			onServerStartButtonClicked();
		});
		serverStopButton.setOnAction(e -> {
			onServerStopButtonClicked();
		});
		serverEditApplyButton.setOnAction(e -> {
			onServerEditApplyButtonClicked();
		});
		
		// Binding connections menu elements
		connectionsEditApplyButton.setOnAction(e -> {
			onConnectionsEditApplyButtonClicked();
		});
		
		// Binding channels menu elements
		channelsSelectorComboBox.valueProperty().addListener(e -> {
			onChannelsSelectorComboBoxItemSelected();
		});
		channelsEditApplyButton.setOnAction(e -> {
			onChannelsEditApplyButtonClicked();
		});
		channelsDeleteButton.setOnAction(e -> {
			onChannelsDeleteButtonClicked();
		});
		channelsCreateSaveButton.setOnAction(e -> {
			onChannelsCreateSaveButtonClicked();
		});
		channelsSelectorComboBox.valueProperty().addListener(e -> {
			onChannelsSelectorComboBoxItemSelected();
		});
		
		// Binding permissions menu elements
		permissionsEditApplyButton.setOnAction(e -> {
			onPermissionsEditApplyButtonClicked();
		});
		
		// Binding members menu elements
		membersSelectorComboBox.valueProperty().addListener(e -> {
			onMembersSelectorComboBoxItemSelected();
		});
		membersEditApplyButton.setOnAction(e -> {
			onMembersEditApplyButtonClicked();
		});
		membersDeleteButton.setOnAction(e -> {
			onMembersDeleteButtonClicked();
		});
		membersCreateSaveButton.setOnAction(e -> {
			onMembersCreateSaveButtonClicked();
		});
		
		// Binding logs menu elements
		logsClearButton.setOnAction(e -> {
			onLogsClearButtonClicked();
		});
		
		// Binding main channels tree view
		mainChannelsTreeView.getSelectionModel().selectedItemProperty().addListener(e -> {
			onMainChannelsTreeViewItemSelected();
		});
		mainChannelsTreeView.setOnContextMenuRequested(e -> {
			onMainChannelsTreeViewContextMenuRequested();
		});	
	}
	
	/**
	 * Updates and enables menus dependent on a configuration
	 * being loaded.
	 */
	private void configurationLoaded() {
		// Enabling menus depending on configuration
		serverPanel.setDisable(false);
		connectionsPanel.setDisable(false);
		channelsPanel.setDisable(false);
		permissionsPanel.setDisable(false);
		membersPanel.setDisable(false);
		logsPanel.setDisable(false);
		
		// Enabling the main channels tree view
		mainChannelsTreeView.setDisable(false);
		
		// Making the configuration menu collapsible
		configPanel.setCollapsible(true);
		
		// Enabling saving current configuration
		configSaveButton.setDisable(false);
		
		// Updating server menu
		serverNameTextField.setText(Server.getProperties().getProperty("name"));
		serverDefaultChannelNameTextField.setText(Server.getProperties().getProperty("defaultChannelName"));
		serverDefaultChannelTopicTextField.setText(Server.getProperties().getProperty("defaultChannelTopic"));
		serverDefaultChannelDescriptionTextArea.setText(Server.getProperties().getProperty("defaultChannelDescription"));
		serverWelcomeMessageTextArea.setText(Server.getProperties().getProperty("welcomeMessage"));
		serverVersionTextField.setText(Server.getVersion());
	
		// Updating connections menu
		connectionsPortTextField.setText(Server.getProperties().getProperty("port"));
		connectionsHandlersTextField.setText(Server.getProperties().getProperty("maxHandlers"));
		connectionsUserPerHandlerTextField.setText(Server.getProperties().getProperty("maxUserPerHandler"));
		
		// Updating permissions menu
		permissionsMemberCreateChannelCheckBox.setSelected(
				Boolean.parseBoolean(Server.getProperties().getProperty("memberCanCreateChannel")));
		permissionsMemberDeleteChannelCheckBox.setSelected(
				Boolean.parseBoolean(Server.getProperties().getProperty("memberCanDeleteChannel")));
		permissionsMemberModifyChannelCheckBox.setSelected(
				Boolean.parseBoolean(Server.getProperties().getProperty("memberCanModifyChannel")));
		permissionsGuestCreateChannelCheckBox.setSelected(
				Boolean.parseBoolean(Server.getProperties().getProperty("guestCanCreateChannel")));
		permissionsGuestDeleteChannelCheckBox.setSelected(
				Boolean.parseBoolean(Server.getProperties().getProperty("guestCanDeleteChannel")));
		permissionsGuestModifyChannelCheckBox.setSelected(
				Boolean.parseBoolean(Server.getProperties().getProperty("guestCanModifyChannel")));
		
		// Updating main channels tree view
		TreeItem<Object> root = new TreeItem<Object>(Server.getName(), new ImageView(serverIcon));
		mainChannelsTreeView.setRoot(root);
		mainChannelsTreeView.getRoot().setExpanded(true);
		
		// Setting configuration changed state flag
		configChanged = false;
	}

	/**
	 * Loads graphics resources to the application.
	 */
	private void loadGraphicsResources() {
		try {
			serverIcon = new Image("convoice/server/gui/images/server.png");
			channelIcon = new Image("convoice/server/gui/images/channel.png");
			userIcon = new Image("convoice/server/gui/images/user.png");
			editIcon = new Image("convoice/server/gui/images/edit.png");
			deleteIcon = new Image("convoice/server/gui/images/delete.png");
			createIcon = new Image("convoice/server/gui/images/create.png");
			startIcon = new Image("convoice/server/gui/images/start.png");
			stopIcon = new Image("convoice/server/gui/images/stop.png");
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	// Configuration menu controllers
	
	/**
	 * Handles when the path text field changes on the
	 * Configuration panel.
	 */
	private void onConfigPathTextFieldChanged() {
		// Clearing the list of selectible configuration folders
		configSelectorComboBox.getItems().clear();
		
		// Getting the new list of configuration folders
		File[] folders = new File(configPathTextField.getText()).listFiles(File::isDirectory);
		
		// Checking if the path was valid
		if(folders != null) {
			// Populating the combo box
			for(File folder : folders) {
				configSelectorComboBox.getItems().add(folder.getName());
			}
		} else {
			// Invalid path, clear the options
			configSelectorComboBox.getItems().clear();
		}
	}
	
	/**
	 * Handles when the Load button is clicked on the
	 * Configuration panel.
	 */
	private void onConfigLoadButtonClicked() {
		// Constructing configuration path
		String path = configPathTextField.getText() + "\\" + configSelectorComboBox.getValue();
		
		// Writing information text
		configInfoTextArea.clear();
		configInfoTextArea.appendText("Checking configuration files...\n\n");
		
		// Checking configuration files
		if(new File(path + "/server.prop").exists()) {
			configInfoTextArea.appendText("server.prop: OK\n");
		} else {
			configInfoTextArea.appendText("server.prop: MISSING\n\n");
			configInfoTextArea.appendText("Loading aborted.\n");
			return;
		}
		if(new File(path + "/channels.xml").exists()) {
			configInfoTextArea.appendText("channels.xml: OK\n");
		} else {
			configInfoTextArea.appendText("channels.xml: MISSING\n\n");
			configInfoTextArea.appendText("Loading aborted.\n");
			return;
		}
		if(new File(path + "/members.xml").exists()) {
			configInfoTextArea.appendText("members.xml: OK\n\n");
		} else {
			configInfoTextArea.appendText("members.xml: MISSING\n\n");
			configInfoTextArea.appendText("Loading aborted.\n");
			return;
		}
		
		// Writing information text
		configInfoTextArea.appendText("Loading configuration...\n");

		// Loading configuration
		Server.loadConfiguration(path);
		
		// Writing information text
		configInfoTextArea.appendText("Configuration loaded.\n");
		
		// Updating current configuration label
		configCurrentLabel.setText(path);
		
		// Updating status label
		mainStatusLabel.setText("Configuration loaded.");
		
		// Updating the rest of the user interface
		configurationLoaded();
	}
	
	/**
	 * Handles when the Save button is clicked on the
	 * Configuration panel.
	 */
	private void onConfigSaveButtonClicked() {
		// Constructing configuration path
		String path = configPathTextField.getText() + "\\" + configSelectorComboBox.getValue();

		// Writing information text
		configInfoTextArea.clear();
		configInfoTextArea.appendText("Saving configuration...");

		// Saving configuration
		Server.saveConfiguration(path);

		// Writing information text
		configInfoTextArea.clear();
		configInfoTextArea.appendText("Configuration saved.");

		// Setting configuration changed state flag
		configChanged = false;
		
		// Updating status label
		mainStatusLabel.setText("Configuration saved.");
	}
	
	// Server menu controllers
	
	/**
	 * Handles when the Start button is clicked on the
	 * Server panel.
	 */
	private void onServerStartButtonClicked() {
		// Starting server
		ConnectionManager.start();
		
		// Updating the user interface 
		serverStartButton.setDisable(true);
		serverStopButton.setDisable(false);
		configLoadButton.setDisable(true);

		// Updating status label
		mainStatusLabel.setText("Server started.");
	}

	/**
	 * Handles when the Stop button is clicked on the
	 * Server panel.
	 */
	private void onServerStopButtonClicked() {
		// Stopping server
		ConnectionManager.stop();

		// Updating the user interface 
		serverStartButton.setDisable(false);
		serverStopButton.setDisable(true);
		configLoadButton.setDisable(false);

		// Updating status label
		mainStatusLabel.setText("Server stopped.");
	}
	
	/**
	 * Handles when the Edit/Apply button is clicked on the
	 * Server panel.
	 */
	private void onServerEditApplyButtonClicked() {
		// Checking if editing or applying changes
		if(serverEditApplyButton.getText().equals("Edit")) { // Edit
			// Enabling edition of the fields
			serverNameTextField.setDisable(false);
			serverDefaultChannelNameTextField.setDisable(false);
			serverDefaultChannelTopicTextField.setDisable(false);
			serverDefaultChannelDescriptionTextArea.setDisable(false);
			serverWelcomeMessageTextArea.setDisable(false);
			
			// Changing button text
			serverEditApplyButton.setText("Apply");
		} else { // Apply
			// Disabling edition of the fields
			serverNameTextField.setDisable(true);
			serverDefaultChannelNameTextField.setDisable(true);
			serverDefaultChannelTopicTextField.setDisable(true);
			serverDefaultChannelDescriptionTextArea.setDisable(true);
			serverWelcomeMessageTextArea.setDisable(true);
			
			// Applying changes
			Server.getProperties().setProperty("name", serverNameTextField.getText());
			Server.getProperties().setProperty("defaultChannelName", serverDefaultChannelNameTextField.getText());
			Server.getProperties().setProperty("defaultChannelTopic", serverDefaultChannelTopicTextField.getText());
			Server.getProperties().setProperty("defaultChannelDescription", serverDefaultChannelDescriptionTextArea.getText());
			Server.getProperties().setProperty("welcomeMessage", serverWelcomeMessageTextArea.getText());
			
			// Changing button text
			serverEditApplyButton.setText("Edit");
			
			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating status label
			mainStatusLabel.setText("Server property changes applied.");
		}
	}
	
	// Connections menu controllers
	
	/**
	 * Handles when the Edit/Apply button is clicked on the
	 * Connections panel.
	 */
	private void onConnectionsEditApplyButtonClicked() {
		// Checking if editing or applying changes
		if(connectionsEditApplyButton.getText().equals("Edit")) { // Edit
			// Enabling edition of the fields
			connectionsPortTextField.setDisable(false);
			connectionsHandlersTextField.setDisable(false);
			connectionsUserPerHandlerTextField.setDisable(false);
			
			// Changing button text
			connectionsEditApplyButton.setText("Apply");
		} else { // Apply
			
			try { // Validating port
				Integer.parseInt(connectionsPortTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				connectionsPortTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			try { // Validating number of handlers
				Integer.parseInt(connectionsHandlersTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				connectionsHandlersTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			try { // Validating number of users per handler
				Integer.parseInt(connectionsUserPerHandlerTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				connectionsUserPerHandlerTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			
			// Disabling edition of the fields
			connectionsPortTextField.setDisable(true);
			connectionsHandlersTextField.setDisable(true);
			connectionsUserPerHandlerTextField.setDisable(true);
			
			// Applying changes
			Server.getProperties().setProperty("port", connectionsPortTextField.getText());
			Server.getProperties().setProperty("maxHandlers", connectionsHandlersTextField.getText());
			Server.getProperties().setProperty("maxUserPerHandler", connectionsUserPerHandlerTextField.getText());
			
			// Reloading the Connection manager
			ConnectionManager.reloadConfiguration();
			
			// Changing button text
			connectionsEditApplyButton.setText("Edit");
			
			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating status label
			mainStatusLabel.setText("Connection property changes applied.");
		}
	}
	
	// Channels menu controllers
	
	/**
	 * Handles when the selection of the channel selector
	 * combo box changes on the Channels panel.
	 */
	private void onChannelsSelectorComboBoxItemSelected() {
		// Getting selected channel
		Channel channel = channelsSelectorComboBox.getSelectionModel().getSelectedItem();
		
		// Returning if no channel is selected
		if(channel == null) return;
		
		// Updating fields according to selected channel
		channelsNameTextField.setText(channel.getChannelData().getName());
		channelsTopicTextField.setText(channel.getChannelData().getTopic());
		channelsDescriptionTextArea.setText(channel.getChannelData().getDescription());
		channelsPasswordField.setText(channel.getChannelData().getPassword());
		channelsMaxClientsTextField.setText(String.valueOf(channel.getChannelData().getMaxClients()));
		channelsIdTextField.setText(String.valueOf(channel.getID()));
		channelsPasswordCheckBox.setSelected(channel.getChannelData().hasPassword());
		channelsPermanentCheckBox.setSelected(channel.isPermanent());
		
		// Enabling Edit/Apply button
		channelsEditApplyButton.setDisable(false);
		
		// If default channel is selected disable deletion
		if(channel.getID() == ChannelManager.DEFAULT_CHANNEL_ID) {
			// Disabling delete button
			channelsDeleteButton.setDisable(true);
		} else {
			// Enabling delete button
			channelsDeleteButton.setDisable(false);
		}
	}
	
	/**
	 * Handles when the Edit/Apply button is clicked on the
	 * Channels panel.
	 */
	private void onChannelsEditApplyButtonClicked() {
		// Checking if editing or applying changes
		if(channelsEditApplyButton.getText().equals("Edit")) { // Edit
			// Enabling edition of the fields
			channelsNameTextField.setDisable(false);
			channelsTopicTextField.setDisable(false);
			channelsDescriptionTextArea.setDisable(false);
			channelsPasswordField.setDisable(false);
			channelsMaxClientsTextField.setDisable(false);
			channelsPasswordCheckBox.setDisable(false);
			channelsPermanentCheckBox.setDisable(false);
			
			// Disabling selector combo box
			channelsSelectorComboBox.setDisable(true);
			
			// Disabling delete button
			channelsDeleteButton.setDisable(true);
			
			// Disabling Create/Save button
			channelsCreateSaveButton.setDisable(true);
			
			// Changing button text
			channelsEditApplyButton.setText("Apply");
		} else { // Apply
			
			try { // Validating max clients field
				Integer.parseInt(channelsMaxClientsTextField.getText());
			} catch(NumberFormatException e) {
				// Focusing the malformed input field
				channelsMaxClientsTextField.requestFocus();
				
				// Updating status label
				mainStatusLabel.setText("Invalid parameter, must only use numbers.");
				
				// Validation failed, do not change anything
				return;
			}
			
			// Disabling edition of the fields
			channelsNameTextField.setDisable(true);
			channelsTopicTextField.setDisable(true);
			channelsDescriptionTextArea.setDisable(true);
			channelsPasswordField.setDisable(true);
			channelsMaxClientsTextField.setDisable(true);
			channelsPasswordCheckBox.setDisable(true);
			channelsPermanentCheckBox.setDisable(true);
			
			// Getting selected channel
			Channel channel = channelsSelectorComboBox.getSelectionModel().getSelectedItem();
			
			// Applying changes
			ChannelManager.modifyChannel(channelsSelectorComboBox.getSelectionModel().getSelectedItem().getID(), 
					channelsNameTextField.getText(), 
					channelsTopicTextField.getText(), 
					channelsDescriptionTextArea.getText(), 
					channelsPasswordCheckBox.isSelected(), 
					channelsPasswordField.getText(), 
					Integer.parseInt(channelsMaxClientsTextField.getText()), 
					channelsPermanentCheckBox.isSelected());
			
			// Changing button text
			channelsEditApplyButton.setText("Edit");
			
			// Enabling delete button if not the default channel is selected
			if(channelsSelectorComboBox.getSelectionModel().getSelectedItem().getID() != ChannelManager.DEFAULT_CHANNEL_ID) {
				// Enabling delete button
				channelsDeleteButton.setDisable(false);
			}
			
			// Enabling selector combo box
			channelsSelectorComboBox.setDisable(false);
			
			// Enabling Create/Save button
			channelsCreateSaveButton.setDisable(false);
			
			// Updating inspector text area
			onMainChannelsTreeViewItemSelected();
			
			// Reselecting the edited channel
			channelsSelectorComboBox.getSelectionModel().select(channel);
			
			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating status label
			mainStatusLabel.setText("Channel changes applied.");
		}
	}
	
	/**
	 * Handles when the Delete button is clicked on the
	 * Channels panel.
	 */
	private void onChannelsDeleteButtonClicked() {
		// Deleting selected channel
		ChannelManager.deleteChannel(channelsSelectorComboBox.getSelectionModel().getSelectedItem().getID());
		
		// Disabling Edit/Apply button
		channelsEditApplyButton.setDisable(true);
		
		// Clearing channel data fields
		channelsNameTextField.clear();
		channelsTopicTextField.clear();
		channelsDescriptionTextArea.clear();
		channelsPasswordField.clear();
		channelsMaxClientsTextField.clear();
		channelsIdTextField.clear();
		channelsPasswordCheckBox.setSelected(false);
		channelsPermanentCheckBox.setSelected(false);

		// Expanding main channel tree view root
		mainChannelsTreeView.getRoot().setExpanded(true);
		
		// Disabling delete button
		channelsDeleteButton.setDisable(true);
		
		// Setting configuration changed state flag
		configChanged = true;
	}
	
	/**
	 * Handles when the Create/Save button is clicked on the
	 * Channels panel.
	 */
	private void onChannelsCreateSaveButtonClicked() {
		// Checking if creating or saving channel
		if(channelsCreateSaveButton.getText().equals("Create")) { // Create
			// Clearing channel selector combo box selection
			channelsSelectorComboBox.getSelectionModel().clearSelection();
			
			// Disabling channel selector combo box
			channelsSelectorComboBox.setDisable(true);
			
			// Disabling Edit/Apply button
			channelsEditApplyButton.setDisable(true);
			
			// Disabling delete button
			channelsDeleteButton.setDisable(true);
			
			// Clearing channel data fields
			channelsNameTextField.clear();
			channelsTopicTextField.clear();
			channelsDescriptionTextArea.clear();
			channelsPasswordField.clear();
			channelsMaxClientsTextField.clear();
			channelsIdTextField.clear();
			channelsPasswordCheckBox.setSelected(false);
			channelsPermanentCheckBox.setSelected(false);
			
			// Enabling channel data fields
			channelsNameTextField.setDisable(false);
			channelsTopicTextField.setDisable(false);
			channelsDescriptionTextArea.setDisable(false);
			channelsPasswordField.setDisable(false);
			channelsMaxClientsTextField.setDisable(false);
			channelsIdTextField.setDisable(false);
			channelsPasswordCheckBox.setDisable(false);
			channelsPermanentCheckBox.setDisable(false);

			// Changing button text
			channelsCreateSaveButton.setText("Save");
		} else { // Save
			// Validating channel name
			if(channelsNameTextField.getText().isEmpty()) {
				// Requesting focus for the name field
				channelsNameTextField.requestFocus();
				
				// Updating main status label
				mainStatusLabel.setText("Channel creation unsuccessful, missing parameter.");
				
				// Validation unsuccessful, do not change anything
				return;
			}
			// Validating channel max clients (value)
			if(channelsMaxClientsTextField.getText().isEmpty()) {
				// Requesting focus for the max clients field
				channelsMaxClientsTextField.requestFocus();
				
				// Updating main status label
				mainStatusLabel.setText("Channel creation unsuccessful, missing parameter.");
				
				// Validation unsuccessful, do not change anything
				return;
			}
			// Validating channel max clients (format)
			try {
				Integer.parseInt(channelsMaxClientsTextField.getText());
			} catch(NumberFormatException e) {
				// Requesting focus for the max clients field
				channelsMaxClientsTextField.requestFocus();
				
				// Updating main status label
				mainStatusLabel.setText("Wrong format, must use numbers.");
				
				// Validation unsuccessful, do not change anything
				return;
			}
			
			// Creating channel
			ChannelManager.createChannel(channelsNameTextField.getText(), 
					channelsTopicTextField.getText(), 
					channelsDescriptionTextArea.getText(), 
					channelsPasswordCheckBox.isSelected(), 
					channelsPasswordField.getText(),
					Integer.parseInt(channelsMaxClientsTextField.getText()), 
					channelsPermanentCheckBox.isSelected());
			
			// Disabling channel data fields
			channelsNameTextField.setDisable(true);
			channelsTopicTextField.setDisable(true);
			channelsDescriptionTextArea.setDisable(true);
			channelsPasswordField.setDisable(true);
			channelsMaxClientsTextField.setDisable(true);
			channelsIdTextField.setDisable(true);
			channelsPasswordCheckBox.setDisable(true);
			channelsPermanentCheckBox.setDisable(true);
			
			// Selecting newly created channel
			channelsSelectorComboBox.getSelectionModel().selectLast();
			
			// Expanding main channels tree view 
			mainChannelsTreeView.getRoot().setExpanded(true);
			
			// Enabling channel selector combo box
			channelsSelectorComboBox.setDisable(false);
			
			// Enabling Edit/Apply button
			channelsEditApplyButton.setDisable(false);
			
			// Enabling delete button
			channelsDeleteButton.setDisable(false);
			
			// Changing button text
			channelsCreateSaveButton.setText("Create");
			
			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating main status label
			mainStatusLabel.setText("Channel created.");
		}
	}
	
	// Permissions menu controllers
	
	/**
	 * Handles when the Edit/Apply button is clicked on the
	 * Permissions panel.
	 */
	private void onPermissionsEditApplyButtonClicked() {
		// Checking if editing or applying changes
		if(permissionsEditApplyButton.getText().equals("Edit")) { // Edit
			// Enabling edition of the fields
			permissionsMemberCreateChannelCheckBox.setDisable(false);
			permissionsMemberDeleteChannelCheckBox.setDisable(false);
			permissionsMemberModifyChannelCheckBox.setDisable(false);
			permissionsGuestCreateChannelCheckBox.setDisable(false);
			permissionsGuestDeleteChannelCheckBox.setDisable(false);
			permissionsGuestModifyChannelCheckBox.setDisable(false);
			
			// Changing button text
			permissionsEditApplyButton.setText("Apply");
		} else { // Apply
			// Disabling edition of the fields
			permissionsMemberCreateChannelCheckBox.setDisable(true);
			permissionsMemberDeleteChannelCheckBox.setDisable(true);
			permissionsMemberModifyChannelCheckBox.setDisable(true);
			permissionsGuestCreateChannelCheckBox.setDisable(true);
			permissionsGuestDeleteChannelCheckBox.setDisable(true);
			permissionsGuestModifyChannelCheckBox.setDisable(true);
			
			// Applying changes
			Server.getProperties().setProperty("memberCanCreateChannel", 
					String.valueOf(permissionsMemberCreateChannelCheckBox.isSelected()));
			Server.getProperties().setProperty("memberCanDeleteChannel", 
					String.valueOf(permissionsMemberDeleteChannelCheckBox.isSelected()));
			Server.getProperties().setProperty("memberCanModifyChannel", 
					String.valueOf(permissionsMemberModifyChannelCheckBox.isSelected()));
			Server.getProperties().setProperty("guestCanCreateChannel", 
					String.valueOf(permissionsGuestCreateChannelCheckBox.isSelected()));
			Server.getProperties().setProperty("guestCanDeleteChannel", 
					String.valueOf(permissionsGuestDeleteChannelCheckBox.isSelected()));
			Server.getProperties().setProperty("guestCanModifyChannel", 
					String.valueOf(permissionsGuestModifyChannelCheckBox.isSelected()));
			
			// Reloading the Permission manager
			PermissionManager.reloadConfiguration();
			
			// Changing button text
			permissionsEditApplyButton.setText("Edit");

			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating status label
			mainStatusLabel.setText("Permission changes applied.");
			
		}
	}
	
	// Members menu controllers
	
	/**
	 * Handles when the selection of the member selector
	 * combo box changes on the Members panel.
	 */
	private void onMembersSelectorComboBoxItemSelected() {
		// Getting selected member
		UserData member = membersSelectorComboBox.getSelectionModel().getSelectedItem();
		
		// Returning if no member is selected
		if(member == null) return;
		
		// Updating fields according to selected member
		membersUsernameTextField.setText(member.getUsername());
		membersPasswordField.setText(member.getPassword());
		
		// Enabling Edit/Apply button
		membersEditApplyButton.setDisable(false);
		
		// Enabling delete button
		membersDeleteButton.setDisable(false);
	}
	
	/**
	 * Handles when the Edit/Apply button is clicked on the
	 * Members panel.
	 */
	private void onMembersEditApplyButtonClicked() {
		// Checking if editing or applying changes
		if(membersEditApplyButton.getText().equals("Edit")) { // Edit
			// Enabling edition of the fields
			membersUsernameTextField.setDisable(false);
			membersPasswordField.setDisable(false);
			
			// Disabling selector combo box
			membersSelectorComboBox.setDisable(true);
			
			// Disabling delete button
			membersDeleteButton.setDisable(true);
			
			// Disabling Create/Save button
			membersCreateSaveButton.setDisable(true);
			
			// Changing button text
			membersEditApplyButton.setText("Apply");
		} else { // Apply
			// Validating username field
			if(membersUsernameTextField.getText().isEmpty()) {
				// Requesting focus for the empty field
				membersUsernameTextField.requestFocus();
				
				// Validation failed, do not change anything
				return;
			}
			// Validating password field
			if(membersPasswordField.getText().isEmpty()) {
				// Requesting focus for the empty field
				membersPasswordField.requestFocus();
				
				// Validation failed, do not change anything
				return;
			}
			
			// Disabling edition of fields
			membersUsernameTextField.setDisable(true);
			membersPasswordField.setDisable(true);
			
			// Getting the selected member
			UserData member = membersSelectorComboBox.getSelectionModel().getSelectedItem();
			
			// Applying changes
			UserManager.modifyMember(membersSelectorComboBox.getSelectionModel().getSelectedItem().getUsername(), 
					membersUsernameTextField.getText(), 
					membersPasswordField.getText());
			
			// Changing button text
			membersEditApplyButton.setText("Edit");
			
			// Enabling selector combo box
			membersSelectorComboBox.setDisable(false);
			
			// Enabling delete button
			membersDeleteButton.setDisable(false);
			
			// Enabling Create/Save button
			membersCreateSaveButton.setDisable(false);
			
			// Reselecting the edited member
			membersSelectorComboBox.getSelectionModel().select(member);
			
			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating status label
			mainStatusLabel.setText("Member changes applied.");
		}
	}
	
	/**
	 * Handles when the Delete button is clicked on the
	 * Members panel.
	 */
	private void onMembersDeleteButtonClicked() {
		// Deleting selected member
		UserManager.deleteMember(membersSelectorComboBox.getSelectionModel().getSelectedItem().getUsername());
		
		// Disabling Edit/Apply button
		membersEditApplyButton.setDisable(true);

		// Clearing member data fields
		membersUsernameTextField.clear();
		membersPasswordField.clear();

		// Disabling delete button
		membersDeleteButton.setDisable(true);
		
		// Setting configuration changed state flag
		configChanged = true;
	}
	
	/**
	 * Handles when the Create/Save button is clicked on the
	 * Members panel.
	 */
	private void onMembersCreateSaveButtonClicked() {
		// Checking if creating or saving member
		if(membersCreateSaveButton.getText().equals("Create")) { // Create
			// Clearing member selector combo box selection
			membersSelectorComboBox.getSelectionModel().clearSelection();
			
			// Disabling member selector combo box
			membersSelectorComboBox.setDisable(true);
			
			// Disabling Edit/Apply button
			membersEditApplyButton.setDisable(true);
			
			// Disabling delete button
			membersDeleteButton.setDisable(true);
			
			// Clearing member data fields
			membersUsernameTextField.clear();
			membersPasswordField.clear();
			
			// Enabling member data fields
			membersUsernameTextField.setDisable(false);
			membersPasswordField.setDisable(false);

			// Changing button text
			membersCreateSaveButton.setText("Save");
		} else { // Save
			// Validating username
			if(membersUsernameTextField.getText().isEmpty()) {
				// Requesting focus for the name field
				membersUsernameTextField.requestFocus();
				
				// Updating main status label
				mainStatusLabel.setText("Member creation unsuccessful, missing parameter.");
				
				// Validation unsuccessful, do not change anything
				return;
			}
			// Validating password
			if(membersPasswordField.getText().isEmpty()) {
				// Requesting focus for the max clients field
				membersPasswordField.requestFocus();
				
				// Updating main status label
				mainStatusLabel.setText("Member creation unsuccessful, missing parameter.");
				
				// Validation unsuccessful, do not change anything
				return;
			}
			
			// Creating member
			UserManager.addMember(membersUsernameTextField.getText(), membersPasswordField.getText());
			
			// Disabling member data fields
			membersUsernameTextField.setDisable(true);
			membersPasswordField.setDisable(true);
			
			// Selecting newly created member
			membersSelectorComboBox.getSelectionModel().selectLast();
			
			// Enabling member selector combo box
			membersSelectorComboBox.setDisable(false);
			
			// Enabling Edit/Apply button
			membersEditApplyButton.setDisable(false);
			
			// Enabling delete button
			membersDeleteButton.setDisable(false);
			
			// Changing button text
			membersCreateSaveButton.setText("Create");
			
			// Setting configuration changed state flag
			configChanged = true;
			
			// Updating main status label
			mainStatusLabel.setText("Member created.");
		}
	}
	
	// Logs menu controllers
	
	/**
	 * Handles when the Clear button is clicked on the
	 * Logs panel.
	 */
	private void onLogsClearButtonClicked() {
		logsOutputTextArea.clear();
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
			mainInspectorTextArea.appendText("Name: " + Server.getProperties().getProperty("name") + "\n");
			mainInspectorTextArea.appendText("Version: " + Server.getVersion() + "\n");
			break;
		case 1: 
			// The selected item is a node item (channel)
			Channel channel = (Channel) selected.getValue();
			mainInspectorTextArea.appendText("----- Channel -----\n");
			mainInspectorTextArea.appendText("ID: " + channel.getID() + "\n");
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
			mainInspectorTextArea.appendText("ID: " + user.getID() + "\n");
			mainInspectorTextArea.appendText("Username: " + user.getUserData().getUsername() + "\n");
			mainInspectorTextArea.appendText("Nickname: " + user.getUserData().getNickname() + "\n");
			break;
		}
	}
	
	/**
	 * Handles when a context menu is requested for the
	 * main channels tree view.
	 */
	private void onMainChannelsTreeViewContextMenuRequested() {		
		// Getting the selected tree item
		TreeItem<Object> selected = mainChannelsTreeView.getSelectionModel().getSelectedItem();

		// Checking the level of the selected item
		int itemLevel = mainChannelsTreeView.getTreeItemLevel(selected);

		// Clearing menu options
		mainChannelsContextMenu.getItems().clear();
		
		// Handling the selection according to level
		switch(itemLevel) {
		case 0:
			// The selected item is the root item (server)
			MenuItem editServerOption = new MenuItem("Edit server...", new ImageView(editIcon));
			MenuItem startServerOption = new MenuItem("Start server...", new ImageView(startIcon));
			MenuItem stopServerOption = new MenuItem("Stop server...", new ImageView(stopIcon));
			
			// Binding handlers to menu options
			editServerOption.setOnAction(e -> {
				// Making the Server panel visible
				serverPanel.setExpanded(true);
				
				// Emulating click on the Edit button
				if(serverEditApplyButton.getText().equals("Edit")) {
					onServerEditApplyButtonClicked();
				}
			});
			startServerOption.setOnAction(e -> {
				// Emulating click on the Start button
				onServerStartButtonClicked();
			});
			stopServerOption.setOnAction(e -> {
				// Emulating click on the Stop button
				onServerStopButtonClicked();
			});
			
			// Adding the menu options to the context menu
			mainChannelsContextMenu.getItems().addAll(editServerOption);
			if(!serverStartButton.isDisabled()) {
				mainChannelsContextMenu.getItems().add(startServerOption);
			}
			if(!serverStopButton.isDisabled()) {
				mainChannelsContextMenu.getItems().add(stopServerOption);
			}
			break;
		case 1:
			// The selected item is a node item (channel)
			MenuItem editChannelOption = new MenuItem("Edit channel...", new ImageView(editIcon));
			MenuItem deleteChannelOption = new MenuItem("Delete channel...", new ImageView(deleteIcon));
			MenuItem createChannelOption = new MenuItem("Create channel...", new ImageView(createIcon));
			
			// Binding handlers to menu options
			editChannelOption.setOnAction(e -> {
				// Making the Channels panel visible
				channelsPanel.setExpanded(true);
				
				// Emulating selection of the channel
				// Emulating selection of the channel
				channelsSelectorComboBox.getSelectionModel().select(
						(Channel) mainChannelsTreeView.getSelectionModel().getSelectedItem().getValue());
				
				// Emulating click on the Edit button
				if(channelsEditApplyButton.getText().equals("Edit")) {
					onChannelsEditApplyButtonClicked();
				}
			});
			deleteChannelOption.setOnAction(e -> {
				// Emulating selection of the channel
				channelsSelectorComboBox.getSelectionModel().select(
						(Channel) mainChannelsTreeView.getSelectionModel().getSelectedItem().getValue());
				
				// Emulating click on the Delete button
				onChannelsDeleteButtonClicked();
			});
			createChannelOption.setOnAction(e -> {
				// Making the Channels panel visible
				channelsPanel.setExpanded(true);
				
				// Emulating click on the Create button
				if(channelsCreateSaveButton.getText().equals("Create")) {
					onChannelsCreateSaveButtonClicked();
				}
			});
			
			// Adding the menu options to the context menu
			mainChannelsContextMenu.getItems().addAll(editChannelOption, 
					deleteChannelOption, 
					createChannelOption);
			break;
		case 2:
			// The selected item is a node item (user)
			
			break;
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
		
		// Getting the list of users on the channel
		Collection<Integer> userIDs = channel.getUsers();
		
		// Adding user nodes to the channel node
		for(Integer userID : userIDs) {
			// Creating user tree node
			TreeItem<Object> userNode = new TreeItem<Object>(UserManager.getUser(userID), new ImageView(userIcon));
			
			// Adding the user node
			channelNode.getChildren().add(userNode);
		}
		
		// Adding channel node to root
		mainChannelsTreeView.getRoot().getChildren().add(channelNode);
		
		// Updating channel selector combo box
		channelsSelectorComboBox.getItems().add(channel);
	}
	
	/**
	 * Notifies the GUI about the modified channel.
	 * @param channel The channel object.
	 */
	private void channelModifiedUpdate_m(Channel channel) {
		// Updating main tree view
		mainChannelsTreeView.refresh();
		
		// Updating channel selector combo box
		for(Channel actual : channelsSelectorComboBox.getItems()) {
			if(actual.getID() == channel.getID()) {
				channelsSelectorComboBox.getItems().remove(actual);
				channelsSelectorComboBox.getItems().add(channel);
				return;
			}
		}
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
				break;
			}
		}
		
		// Updating channel selector combo box
		for(Channel actual : channelsSelectorComboBox.getItems()) {
			if(actual.getID() == channelID) {
				channelsSelectorComboBox.getItems().remove(actual);
				return;
			}
		}
	}
	
	/**
	 * Notifies the GUI about the created member.
	 * @param member The created member object.
	 */
	private void memberCreatedUpdate_m(UserData member) {
		// Updating member selector combo box
		membersSelectorComboBox.getItems().add(member);
	}
	
	/**
	 * Notifies the GUI about the modified member.
	 * @param oldUsername The old username of the member.
	 * @param member The new member object.
	 */
	private void memberModifiedUpdate_m(String oldUsername, UserData member) {
		// Updating member selector combo box
		for(UserData actual : membersSelectorComboBox.getItems()) {
			if(actual.getUsername().equals(oldUsername)) {
				membersSelectorComboBox.getItems().remove(actual);
				membersSelectorComboBox.getItems().add(member);
				return;
			}
		}
	}
	
	/**
	 * Notifies the GUI about the deleted member.
	 * @param username The username of the member.
	 */
	private void memberDeletedUpdate_m(String username) {
		// Updating member selector combo box
		for(UserData actual : membersSelectorComboBox.getItems()) {
			if(actual.getUsername().equals(username)) {
				membersSelectorComboBox.getItems().remove(actual);
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
				
				// Refreshing the tree view
				// mainChannelsTreeView.refresh();
				
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
	
	// Output & Status methods
	
	/**
	 * Sets the text of the server status label
	 * on the Connections panel.
	 * @param status The status to set on the label.
	 */
	private void setConnectionsServerStatus_m(String status) {
		connectionsServerStatusLabel.setText(status);
	}
	
	/**
	 * Sets the text of the listener status label
	 * on the Connections panel.
	 * @param status The status to set on the label.
	 */
	private void setConnectionsListenerStatus_m(String status) {
		connectionsListenerStatusLabel.setText(status);
	}
	
	/**
	 * Sets the text of the client count status label
	 * on the Connections panel.
	 * @param count The count to set on the label.
	 */
	private void setConnectionsClientsCount_m(int count) {
		connectionsClientsLabel.setText(String.valueOf(count));
	}
	
	/**
	 * Sets the text of the handler count status label
	 * on the Connections panel.
	 * @param count The count to set on the label.
	 */
	private void setConnectionsHandlersCount_m(int count) {
		connectionsHandlersLabel.setText(String.valueOf(count));
	}
	
	/**
	 * Adds a message to the output text area 
	 * on the Connections panel.
	 * @param message The message to display.
	 */
	private void addConnectionsOutputMessage_m(String message) {
		connectionsOutputTextArea.appendText(message + "\n");
	}
	
	/**
	 * Adds a message to the output text area 
	 * on the Members panel.
	 * @param message The message to display.
	 */
	private void addMembersOutputMessage_m(String message) {
		membersOutputTextArea.appendText(message + "\n");
	}
	
	/**
	 * Adds a message to the output text area 
	 * on the Logs panel.
	 * @param message The message to display.
	 */
	private void addLogsOutputMessage_m(String message) {
		logsOutputTextArea.appendText(message + "\n");
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
	 * @param member The created member object.
	 */
	public static void memberCreatedUpdate(UserData member) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.memberCreatedUpdate_m(member);	
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param oldUsername The old username of the member.
	 * @param member The new member object.
	 */
	public static void memberModifiedUpdate(String oldUsername, UserData member) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.memberModifiedUpdate_m(oldUsername, member);	
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param username The username of the member.
	 */
	public static void memberDeletedUpdate(String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.memberDeletedUpdate_m(username);
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
	 * @param status The status to set on the label.
	 */
	public static void setConnectionsServerStatus(String status) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.setConnectionsServerStatus_m(status);	
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param status The status to set on the label.
	 */
	public static void setConnectionsListenerStatus(String status) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.setConnectionsListenerStatus_m(status);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param count The count to set on the label.
	 */
	public static void setConnectionsClientsCount(int count) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.setConnectionsClientsCount_m(count);	
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param count The count to set on the label.
	 */
	public static void setConnectionsHandlersCount(int count) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.setConnectionsHandlersCount_m(count);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param message The message to display.
	 */
	public static void addConnectionsOutputMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.addConnectionsOutputMessage_m(message);
			}
		});
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param message The message to display.
	 */
	public static void addMembersOutputMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.addMembersOutputMessage_m(message);
			}
		});	
	}
	
	/**
	 * Static proxy for the similar method on the static instance.
	 * @param message The message to display.
	 */
	public static void addLogsOutputMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				staticInstance.addLogsOutputMessage_m(message);
			}
		});	
	}
	
	// Close request handler
	
	/**
	 * Handles application close requests.
	 */
	public void onCloseRequest() {
		// Checking if configuration changed or server is still running
		if(configChanged || ConnectionManager.isRunning()) {
			// Creating alert dialog
			Alert alert = new Alert(Alert.AlertType.WARNING);
			
			// Setting alert dialog text and buttons
			if(configChanged && !ConnectionManager.isRunning()) {
				// Configuration changed
				alert.setHeaderText("Configuration has changed during runtime.");
				alert.setContentText("Do you want to save your changes?");
				
				// Adding buttons
				ButtonType yesButton = new ButtonType("Yes", ButtonData.YES);
				ButtonType noButton = new ButtonType("No", ButtonData.NO);
				ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
				alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
				
				// Showing alert dialog
				alert.showAndWait().ifPresent(type -> {
					if(type.getButtonData() == ButtonData.YES) {
						// Getting configuration path
						String path = configCurrentLabel.getText();

						// Saving configuration
						Server.saveConfiguration(path);
						
						// Closing the application
						Platform.exit();
					} else if(type.getButtonData() == ButtonData.NO) {
						// Closing the application without saving
						Platform.exit();
					}
				});
				
			} else if(!configChanged && ConnectionManager.isRunning()) {
				// The server is still running
				alert.setHeaderText("The server is still running.");
				alert.setContentText("Do you want to stop and quit?");
				
				// Adding buttons
				ButtonType yesButton = new ButtonType("Yes", ButtonData.YES);
				ButtonType noButton = new ButtonType("No", ButtonData.NO);
				ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
				alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
				
				// Showing alert dialog
				alert.showAndWait().ifPresent(type -> {
					if(type.getButtonData() == ButtonData.YES) {
						// Stopping the server
						ConnectionManager.stop();
						
						// Closing the application
						Platform.exit();
					}
				});
				
			} else if(configChanged && ConnectionManager.isRunning()) {
				// Configuration changed and the server is still running
				alert.setHeaderText("Configuration has changed and the server is still running.");
				alert.setContentText("What do you want to do?");
				
				// Adding buttons
				ButtonType yesButton = new ButtonType("Save & Quit", ButtonData.YES);
				ButtonType noButton = new ButtonType("Quit", ButtonData.NO);
				ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
				alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
				
				// Showing alert dialog
				alert.showAndWait().ifPresent(type -> {
					if(type.getButtonData() == ButtonData.YES) {
						// Getting configuration path
						String path = configCurrentLabel.getText();

						// Stopping the server
						ConnectionManager.stop();
						
						// Saving configuration
						Server.saveConfiguration(path);
						
						// Closing the application
						Platform.exit();
					} else if(type.getButtonData() == ButtonData.NO) {
						// Stopping the server
						ConnectionManager.stop();
						
						// Closing the application without saving
						Platform.exit();
					}
				});
			}
		}
		else {
			// Closing the application
			Platform.exit();
		}
	}
	
};
