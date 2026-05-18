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
    private int currentRuleId = 0; 

    @FXML
    public void initialize() {
        // 1. Populate dropdowns
        comboAction.setItems(FXCollections.observableArrayList("PERMIT", "DENY"));
        comboProtocol.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "ANY"));
        
        comboAction.getSelectionModel().selectFirst();
        comboProtocol.getSelectionModel().selectLast();

        // 2. Protocol Listener for Port/Type context-switching (2.1.5)
        comboProtocol.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("ICMP".equals(newVal)) {
                txtPort.setPromptText("ICMP Type (e.g., 8 for Ping)");
            } else {
                txtPort.setPromptText("Port or Range (e.g., 80-443)");
            }
            // Trigger a re-validation of the port field when protocol changes
            validateField(txtPort, "port");
        });

        // 3. NEW FOR 2.4.2: Setup Real-time Validation Listeners
        setupRealTimeValidation(txtSourceIp, "ip");
        setupRealTimeValidation(txtDestIp, "ip");
        setupRealTimeValidation(txtPriority, "number");
        setupRealTimeValidation(txtPort, "port");
    }

    /**
     * Attaches a listener to a field to provide instant visual feedback.
     */
    private void setupRealTimeValidation(TextField field, String type) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(field, type);
        });
    }

    /**
     * Logic to determine if a field is valid and apply the correct CSS class.
     */
    private void validateField(TextField field, String type) {
        String value = field.getText().trim();
        boolean isValid = false;

        if (value.isEmpty()) {
            field.getStyleClass().removeAll("field-error", "field-success");
            return;
        }

        switch (type) {
            case "ip" -> isValid = NetworkUtils.isValidIP(value);
            case "number" -> isValid = value.matches("^\\d+$");
            case "port" -> {
                String proto = comboProtocol.getValue();
                if ("ICMP".equals(proto)) {
                    isValid = value.matches("^\\d+$") || value.equalsIgnoreCase("ANY");
                } else {
                    // Supports single port or range (2.1.2)
                    isValid = value.matches("^(\\d+)(-\\d+)?$") || value.equalsIgnoreCase("ANY") || value.equals("-1");
                }
            }
        }

        // Apply visual "Vibe"
        field.getStyleClass().removeAll("field-error", "field-success");
        if (isValid) {
            field.getStyleClass().add("field-success");
        } else {
            field.getStyleClass().add("field-error");
        }
    }

    public void setRuleForEdit(Rule rule) {
        this.currentRuleId = rule.getId(); 
        txtPriority.setText(String.valueOf(rule.getPriority()));
        comboAction.setValue(rule.getAction());
        comboProtocol.setValue(rule.getProtocol());
        txtSourceIp.setText(rule.getSourceIp());
        txtDestIp.setText(rule.getDestIp());
        txtPort.setText(rule.getDestPort());
    }

    public Rule getCreatedRule() {
        return createdRule;
    }

    @FXML 
    private void handleSave() {
        if (isInputValid()) {
            createdRule = new Rule(
                currentRuleId, 
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
        // Final sanity check before saving (Keep your existing validation logic)
        String errorMessage = "";

        if(!NetworkUtils.isValidIP(txtSourceIp.getText().trim())) 
            errorMessage += "Invalid Source IP/Wildcard!\n";
        
        if(!NetworkUtils.isValidIP(txtDestIp.getText().trim())) 
            errorMessage += "Invalid Destination IP/Wildcard!\n";

        try {
            Integer.parseInt(txtPriority.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Priority must be a number!\n";
        }

        if (errorMessage.isEmpty()) return true;

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(txtPriority.getScene().getWindow());
        alert.setTitle("Validation Error");
        alert.setContentText(errorMessage);
        alert.showAndWait();
        return false;
    }

    private void closeWindow() {
        Stage stage = (Stage) txtPriority.getScene().getWindow();
        stage.close();
    }
}