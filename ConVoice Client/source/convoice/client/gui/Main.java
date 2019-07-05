package convoice.client.gui;


// Java imports
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.application.Application;

/**
 * The Main class is responsible for providing an entry point
 * for the application, and prepare for showing the user interface.
 */
public class Main extends Application {
	
	/**
	 * The entry point of the application.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) throws InterruptedException {
		// Launching the graphical user interface
		launch();
	}	
	
	/**
	 * Initializes and starts the user interface.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		// Loading FXML-file for the main window
		FXMLLoader loader = new FXMLLoader(getClass().getResource("clientWindow.fxml"));
		Parent root = loader.load();
		
		// Acquiring handle for the main window controller object
		GUIController.staticInstance = (GUIController) loader.getController();
		
		// Initializing the controller
		GUIController.staticInstance.initialize();
		
		// Creating the scene for the application
		Scene scene = new Scene(root, 1100, 700);
		
		// Loading application icon
		Image appIcon = null;
		try {
			appIcon = new Image("convoice/client/gui/images/convoice_icon.png");
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		// Setting user interface properties
		stage.setTitle("ConVoice Client v1.0.0");
		stage.getIcons().add(appIcon);
		stage.setResizable(false);
		stage.setScene(scene);
		
		// Setting close request handler
		stage.setOnCloseRequest(e -> {
			// Consuming the event
			e.consume();
			
			// Calling the close request handler
			GUIController.getStaticInstance().onCloseRequest();
		});
		
		// Showing the main window
		stage.show();
	}
	
};
