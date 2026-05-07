package com.aclsim;

import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import com.aclsim.service.SimulatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

public class MainController {

    // These @FXML names MUST match the "fx:id" in the FXML file
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

    @FXML
    public void initialize() {
        // Link Table Columns to Rule class variables (getPriority, getAction, etc.)
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        colSource.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));
        colDest.setCellValueFactory(new PropertyValueFactory<>("destIp"));
        colPort.setCellValueFactory(new PropertyValueFactory<>("destPort"));

        // Setup the Protocol dropdown
        comboSimProtocol.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "ANY"));
        comboSimProtocol.getSelectionModel().select("ANY");

        // Bind the list to the table
        ruleTable.setItems(ruleList);

        // Add dummy data so the table isn't empty when you start
        ruleList.add(new Rule(1, 10, "DENY", "TCP", "192.168.1.50", "10.0.0.1", 80));
        ruleList.add(new Rule(2, 20, "PERMIT", "ANY", "192.168.1.0", "0.0.0.0", -1));
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
            } else {
                lblResult.setText("RESULT: DENY (Implicit Deny)");
                lblResult.setTextFill(Color.DARKRED);
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
            ruleList.remove(selected);
        }
    }
}