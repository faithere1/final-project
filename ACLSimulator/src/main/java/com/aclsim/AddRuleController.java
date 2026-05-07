package com.aclsim;

import com.aclsim.model.Rule;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddRuleController {
    @FXML private TextField txtPriority, txtSourceIp, txtDestIp, txtPort;
    @FXML private ComboBox<String> comboAction, comboProtocol;

    private Rule createdRule = null;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        // Populate dropdowns
        comboAction.setItems(FXCollections.observableArrayList("PERMIT", "DENY"));
        comboProtocol.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "ANY"));
        
        // Set default selections
        comboAction.getSelectionModel().selectFirst(); // PERMIT
        comboProtocol.getSelectionModel().selectLast();  // ANY
    }

    /**
     * This method is called by MainController to get the result of the dialog.
     */
    public Rule getCreatedRule() {
        return createdRule;
    }

    @FXML 
    private void handleSave() {
        if (isInputValid()) {
            // Create the rule object from the validated input
            createdRule = new Rule(
                0, // ID is assigned by the Database
                Integer.parseInt(txtPriority.getText().trim()),
                comboAction.getValue(),
                comboProtocol.getValue(),
                txtSourceIp.getText().trim(),
                txtDestIp.getText().trim(),
                Integer.parseInt(txtPort.getText().trim())
            );
            
            saveClicked = true;
            closeWindow();
        }
    }

    @FXML 
    private void handleCancel() {
        saveClicked = false;
        createdRule = null;
        closeWindow();
    }

    /**
     * Validates the user input. Shows an alert if something is wrong.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        // Check if numeric fields are actually numbers
        try {
            Integer.parseInt(txtPriority.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Priority must be a whole number!\n";
        }

        try {
            Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Port must be a number (use -1 for ANY)!\n";
        }

        // Check if IPs are empty
        if (txtSourceIp.getText() == null || txtSourceIp.getText().trim().isEmpty()) {
            errorMessage += "Source IP cannot be empty!\n";
        }
        if (txtDestIp.getText() == null || txtDestIp.getText().trim().isEmpty()) {
            errorMessage += "Destination IP cannot be empty!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show the error message to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(txtPriority.getScene().getWindow());
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please correct the following fields:");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) txtPriority.getScene().getWindow();
        stage.close();
    }
}