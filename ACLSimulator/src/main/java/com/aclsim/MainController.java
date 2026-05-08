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

public class MainController {
    
    @FXML private TableView<Rule> ruleTable;
    @FXML private TableColumn<Rule, Integer> colPriority;
    @FXML private TableColumn<Rule, String> colAction;
    @FXML private TableColumn<Rule, String> colProtocol;
    @FXML private TableColumn<Rule, String> colSource;
    @FXML private TableColumn<Rule, String> colDest;
    @FXML private TableColumn<Rule, Integer> colPort;

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

        // 3. LOAD DATA FROM DATABASE (Phase 4)
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
            // Load the Dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            
            // Create a new window (Stage) for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New ACL Rule");
            dialogStage.initOwner(ruleTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Get the controller and show the window
            AddRuleController controller = loader.getController();
            dialogStage.showAndWait(); // The code stops here until the user closes the dialog

            // If a rule was created, save it!
            Rule newRule = controller.getCreatedRule();
            if (newRule != null) {
                ruleDAO.addRule(newRule); // Write to SQLite
                loadRulesFromDatabase();  // Refresh Table
            }
        } catch (IOException e) {
            System.err.println("Error opening Add Rule dialog: " + e.getMessage());
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
                lblResult.setTextFill(match.getAction().equals("PERMIT") ? Color.GREEN : Color.RED);

            // NEW: Highlight the row in the table
                ruleTable.getSelectionModel().select(match);
                ruleTable.scrollTo(match);
            } else {
                lblResult.setText("RESULT: DENY (Implicit Deny)");
                lblResult.setTextFill(Color.DARKRED);
                ruleTable.getSelectionModel().clearSelection();
            }
        } catch (NumberFormatException e) {
            lblResult.setText("ERROR: Port must be a number");
            lblResult.setTextFill(Color.ORANGE);
        }
}

    @FXML
    private void handleDeleteRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Delete from Database
            ruleDAO.deleteRule(selected.getId());
            // Refresh the UI list
            loadRulesFromDatabase();
        }
    }
}