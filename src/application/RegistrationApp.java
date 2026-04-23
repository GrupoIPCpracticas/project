import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class RegistrationApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Registration.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("App Registration System");
        primaryStage.setScene(new Scene(root, 400, 450));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}