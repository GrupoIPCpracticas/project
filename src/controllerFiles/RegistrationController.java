import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import upv.ipc.sportlib.User;
import upv.ipc.sportlib.SportActivityApp;

public class RegistrationController implements Initializable {
    @FXML private TextField nickField;
    @FXML private TextField emailField;
    @FXML private PasswordField passField;
    @FXML private DatePicker dobPicker;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    private boolean isNicknameUnique(String nick) {
        return !"jgarcia".equalsIgnoreCase(nick);
    }
    @FXML
    private void handleRegister() {
        String nick = nickField.getText();
        String email = emailField.getText();
        String pass = passField.getText();
        LocalDate dob = dobPicker.getValue();

        if (!User.checkNickName(nick)) {
            errorLabel.setText("Nickname must be 6-15 characters (letters, digits, - or _).");
        } else if (!isNicknameUnique(nick)) {
            errorLabel.setText("Nickname '" + nick + "' is already in use.");
        } else if (!User.checkEmail(email)) {
            errorLabel.setText("Please enter a valid email address.");
        } else if (!User.checkPassword(pass)) {
            errorLabel.setText("Password must be 8-20 chars with Upper, Lower, Digit, and Symbol.");
        } else if (!User.isOlderThan(dob, 12)) {
            errorLabel.setText("You must be at least 12 years old.");
        } else {
            errorLabel.setStyle("-fx-text-fill: green;");
            errorLabel.setText("Registration successful for " + nick + "!");
            regi
        }
    }
}