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
    
    // Logic for 2.1.3: Tracks if we are editing an existing rule
    private int currentRuleId = 0; 

    @FXML
    public void initialize() {
        // Populate dropdowns
        comboAction.setItems(FXCollections.observableArrayList("PERMIT", "DENY"));
        comboProtocol.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "ANY"));
        
        // Set default selections
        comboAction.getSelectionModel().selectFirst(); // PERMIT
        comboProtocol.getSelectionModel().selectLast();  // ANY

        // NEW FOR 2.1.5: Dynamic UI based on Protocol selection
        comboProtocol.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("ICMP".equals(newVal)) {
                txtPort.setPromptText("ICMP Type (e.g., 8 for Ping)");
            } else {
                txtPort.setPromptText("Port or Range (e.g., 80-443)");
            }
        });
    }

    /**
     * Logic for 2.1.3: Fills the dialog fields with data from an existing rule.
     */
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
        String errorMessage = "";

        // 1. Validate IP / Subnet / Wildcard (2.1.1 & 2.1.4)
        if(!NetworkUtils.isValidIP(txtSourceIp.getText().trim())){
            errorMessage += "Source IP is invalid. Use: 1.1.1.1, 1.1.1.0/24, or 1.1.1.0 0.0.0.255!\n";
        }
        if(!NetworkUtils.isValidIP(txtDestIp.getText().trim())){
            errorMessage += "Destination IP is invalid. Use: 1.1.1.1, 1.1.1.0/24, or 1.1.1.0 0.0.0.255!\n";
        }

        // 2. Validate Priority
        try {
            Integer.parseInt(txtPriority.getText().trim());
        } catch (NumberFormatException e) {
            errorMessage += "Priority must be a whole number!\n";
        }

        // 3. Validate Port/Range or ICMP Type (Updated for 2.1.5)
        String portInput = txtPort.getText().trim();
        String selectedProto = comboProtocol.getValue();

        if ("ICMP".equals(selectedProto)) {
            // ICMP only accepts a single type number or ANY
            if (!portInput.matches("^\\d+$") && !portInput.equalsIgnoreCase("ANY") && !portInput.equals("-1")) {
                errorMessage += "For ICMP, please enter a single Type number (e.g., 8) or ANY.\n";
            }
        } else {
            // TCP/UDP/ANY accepts single ports or ranges (2.1.2)
            if (!portInput.matches("^(\\d+)(-\\d+)?$") && !portInput.equalsIgnoreCase("ANY") && !portInput.equals("-1")) {
                errorMessage += "Port must be a number, a range (e.g., 80-443), or ANY!\n";
            }
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