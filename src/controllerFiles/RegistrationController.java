package controllerFiles;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import upv.ipc.sportlib.User;
import upv.ipc.sportlib.SportActivityApp;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class RegistrationController implements Initializable {

    // Input Fields
    @FXML private TextField nickField;
    @FXML private TextField emailField;
    @FXML private PasswordField passField;
    @FXML private DatePicker dobPicker;

    // Individual Error Labels
    @FXML private Label nickErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passErrorLabel;
    @FXML private Label dobErrorLabel;

    @FXML private Button registerButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nickField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.isBlank()) nickErrorLabel.setText("");
            else if (!User.checkNickName(newV)) nickErrorLabel.setText("6-15 chars, letters/digits only.");
            else if (!isNicknameUnique(newV)) nickErrorLabel.setText("Nickname taken.");
            else nickErrorLabel.setText("");
        });

        emailField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.isBlank()) emailErrorLabel.setText("");
            else if (!User.checkEmail(newV)) emailErrorLabel.setText("Invalid email format.");
            else emailErrorLabel.setText("");
        });

        passField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.isBlank()) passErrorLabel.setText("");
            else if (!User.checkPassword(newV)) passErrorLabel.setText("8-20 chars: Upper, Lower, Digit, Special.");
            else passErrorLabel.setText("");
        });

        dobPicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) dobErrorLabel.setText("");
            else if (!User.isOlderThan(newV, 12)) dobErrorLabel.setText("Must be 12+ years old.");
            else dobErrorLabel.setText("");
        });

        BooleanBinding isInvalid = Bindings.createBooleanBinding(
                () -> !User.checkNickName(nickField.getText()) ||
                        !isNicknameUnique(nickField.getText()) ||
                        !User.checkEmail(emailField.getText()) ||
                        !User.checkPassword(passField.getText()) ||
                        dobPicker.getValue() == null ||
                        !User.isOlderThan(dobPicker.getValue(), 12),
                nickField.textProperty(),
                emailField.textProperty(),
                passField.textProperty(),
                dobPicker.valueProperty()
        );

        registerButton.disableProperty().bind(isInvalid);
    }

    private boolean isNicknameUnique(String nick) {
        // In a real scenario, you'd check SportActivityApp.getInstance().existsUser(nick)
        return !"jgarcia".equalsIgnoreCase(nick);
    }

    @FXML
    private void handleRegister(ActionEvent event) throws IOException{
        String nick = nickField.getText();
        String email = emailField.getText();
        String pass = passField.getText();
        LocalDate dob = dobPicker.getValue();

        SportActivityApp app = SportActivityApp.getInstance();

        boolean registered = app.registerUser(nick, email, pass, dob, (Image) null);
        if(registered) {
            Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/Welcome.fxml"));
            switchScene(event, root, "Welcome - Running la Safor");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmlFiles/Welcome.fxml"));
        switchScene(event, root, "Welcome - Running la Safor");
    }

    private void switchScene(ActionEvent event, Parent root, String title) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }
}