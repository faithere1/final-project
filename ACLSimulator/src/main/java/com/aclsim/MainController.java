package com.aclsim;

import com.aclsim.database.RuleDAO;
import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import com.aclsim.service.SimulatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainController {
    
    @FXML private TableView<Rule> ruleTable;
    @FXML private TableColumn<Rule, Integer> colPriority;
    @FXML private TableColumn<Rule, String> colAction;
    @FXML private TableColumn<Rule, String> colProtocol;
    @FXML private TableColumn<Rule, String> colSource;
    @FXML private TableColumn<Rule, String> colDest;
    @FXML private TableColumn<Rule, String> colPort; // Updated to String for 2.1.2

    @FXML private TextField txtSimSource, txtSimDest, txtSimPort;
    @FXML private ComboBox<String> comboSimProtocol;
    @FXML private Label lblResult;

    private ObservableList<Rule> ruleList = FXCollections.observableArrayList();
    private SimulatorService simulatorService = new SimulatorService();
    private RuleDAO ruleDAO = new RuleDAO();

    @FXML
    public void initialize() {
        // 1. Link Table Columns
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        colSource.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));
        colDest.setCellValueFactory(new PropertyValueFactory<>("destIp"));
        colPort.setCellValueFactory(new PropertyValueFactory<>("destPort"));

        // 2. Setup UI Components
        comboSimProtocol.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "ANY"));
        comboSimProtocol.getSelectionModel().select("ANY");
        ruleTable.setItems(ruleList);

        // 3. Load initial data
        loadRulesFromDatabase();
    }

    private void loadRulesFromDatabase() {
        ruleList.clear();
        ruleList.addAll(ruleDAO.getAllRules());
        System.out.println(">>> Rules loaded from database: " + ruleList.size());
    }

    @FXML
    private void handleAddRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New ACL Rule");
            dialogStage.initOwner(ruleTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            AddRuleController controller = loader.getController();
            dialogStage.showAndWait(); 

            Rule newRule = controller.getCreatedRule();
            if (newRule != null) {
                ruleDAO.addRule(newRule); 
                loadRulesFromDatabase(); 
            }
        } catch (IOException e) {
            System.err.println("Error opening Add Rule dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NEW FOR 2.1.3: Handles the logic for editing an existing rule.
     */
    @FXML
    private void handleEditRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Rule Selected");
            alert.setContentText("Please select a rule in the table to edit.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            
            // Pass the selected rule to the dialog controller
            AddRuleController controller = loader.getController();
            controller.setRuleForEdit(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit ACL Rule");
            stage.initOwner(ruleTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // After dialog closes, check if we have an updated rule
            Rule updatedRule = controller.getCreatedRule();
            if (updatedRule != null) {
                ruleDAO.updateRule(updatedRule); // Save changes to DB
                loadRulesFromDatabase();        // Refresh table
            }
        } catch (IOException e) {
            System.err.println("Error opening Edit Rule dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSimulate() {
        try {
            String src = txtSimSource.getText();
            String dest = txtSimDest.getText();
            String proto = comboSimProtocol.getValue();
            int port = txtSimPort.getText().isEmpty() ? -1 : Integer.parseInt(txtSimPort.getText());

            Packet packet = new Packet(src, dest, proto, port);
            Rule match = simulatorService.simulate(packet, ruleList);

            if (match != null) {
                lblResult.setText("RESULT: " + match.getAction() + " (Matched Rule #" + match.getPriority() + ")");
                
                if (match.getAction().equals("PERMIT")) {
                    lblResult.setTextFill(Color.valueOf("#2ecc71")); 
                } else {
                    lblResult.setTextFill(Color.valueOf("#ff7675")); 
                }

                ruleTable.getSelectionModel().select(match);
                ruleTable.scrollTo(match);
            } else {
                lblResult.setText("RESULT: DENY (Implicit Deny)");
                lblResult.setTextFill(Color.valueOf("#fab1a0")); 
                ruleTable.getSelectionModel().clearSelection();
            }
        } catch (NumberFormatException e) {
            lblResult.setText("ERROR: Port must be a number");
            lblResult.setTextFill(Color.valueOf("#fdcb6e")); 
        }
    }

    @FXML
    private void handleDeleteRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ruleDAO.deleteRule(selected.getId());
            loadRulesFromDatabase();
        }
    }

    @FXML
    private void handleClearAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Clear All ACL Rules");
        alert.setContentText("Are you sure? This will delete everything in the database.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (Rule rule : ruleList) {
                ruleDAO.deleteRule(rule.getId());
            }
            loadRulesFromDatabase();
            System.out.println(">>> Database cleared.");
        }
    }
}