package com.aclsim;

import com.aclsim.model.Rule;
import com.aclsim.service.NetworkUtils;
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

    public Rule getCreatedRule() {
        return createdRule;
    }

    @FXML 
    private void handleSave() {
        if (isInputValid()) {
            // Create the rule object
            // Port is now passed as a String to support 2.1.2 Port Ranges
            createdRule = new Rule(
                0, 
                Integer.parseInt(txtPriority.getText().trim()),
                comboAction.getValue(),
                comboProtocol.getValue(),
                txtSourceIp.getText().trim(),
                txtDestIp.getText().trim(),
                txtPort.getText().trim() 
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

    private boolean isInputValid() {
        String errorMessage = "";

        // 1. Validate IP / Subnet (Handles CIDR for 2.1.1)
        if(!NetworkUtils.isValidIP(txtSourceIp.getText().trim())){
            errorMessage += "Source IP/CIDR is invalid (e.g., 1.1.1.1 or 192.168.1.0/24)!\n";
        }
        if(!NetworkUtils.isValidIP(txtDestIp.getText().trim())){
            errorMessage += "Destination IP/CIDR is invalid!\n";
        }

        // 2. Validate Priority (Must be a number)
        try {
            Integer.parseInt(txtPriority.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Priority must be a whole number!\n";
        }

        // 3. Validate Port/Range (New for 2.1.2)
        String portInput = txtPort.getText().trim();
        // This RegEx allows: digits (80), digits-digits (80-443), or "ANY"
        if (!portInput.matches("^(\\d+)(-\\d+)?$") && !portInput.equalsIgnoreCase("ANY") && !portInput.equals("-1")) {
            errorMessage += "Port must be a number, a range (e.g., 80-443), or ANY!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
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