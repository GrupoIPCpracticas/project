import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class RegistrationApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Registration.fxml"));
        Parent root = loader.load();

        // Set the title and the scene
        primaryStage.setTitle("App Registration System");
        primaryStage.setScene(new Scene(root, 400, 450));

        // Display the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}