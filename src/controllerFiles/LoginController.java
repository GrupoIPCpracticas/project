package controllerFiles;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

import java.io.IOException;


public class LoginController {
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String nick = userField.getText();
        String pass = passField.getText();

        if (nick.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        SportActivityApp app = SportActivityApp.getInstance();

        // Use the library's authentication (Method names might vary slightly, e.g., login or authenticate)
        boolean authenticated = app.login(nick, pass);

        if (authenticated) {
            System.out.println("Login successful!");
            navigateToMainMap(event);
        } else {
            errorLabel.setText("Invalid nickname or password.");
        }
    }

    public void handleBackNav(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/Welcome.fxml"));
        switchScene(event, root, "Welcome - Running la Safor");
    }

    private void navigateToMainMap(ActionEvent event) {
        try {
            // This loads the Category 3 map interface we fixed earlier
            Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/FXMLDocument.fxml"));
            switchScene(event, root, "Running la Safor - Dashboard");
        } catch (IOException e) {
            errorLabel.setText("Error loading the map interface.");
            e.printStackTrace();
        }
    }

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
}