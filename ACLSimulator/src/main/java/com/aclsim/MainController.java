package com.aclsim;

import com.aclsim.database.LogDAO;
import com.aclsim.database.RuleDAO;
import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import com.aclsim.service.CiscoGeneratorService;
import com.aclsim.service.FileService;
import com.aclsim.service.SimulatorService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {
    
    @FXML private TableView<Rule> ruleTable;
    @FXML private TableColumn<Rule, Integer> colPriority;
    @FXML private TableColumn<Rule, String> colAction;
    @FXML private TableColumn<Rule, String> colProtocol;
    @FXML private TableColumn<Rule, String> colSource;
    @FXML private TableColumn<Rule, String> colDest;
    @FXML private TableColumn<Rule, String> colPort;

    @FXML private TextField txtSimSource, txtSimDest, txtSimPort;
    @FXML private ComboBox<String> comboSimProtocol;
    @FXML private Label lblResult;

    @FXML private Pane topoPane;
    private Circle packetNode;

    private ObservableList<Rule> ruleList = FXCollections.observableArrayList();
    private SimulatorService simulatorService = new SimulatorService();
    private RuleDAO ruleDAO = new RuleDAO();
    private LogDAO logDAO = new LogDAO();
    private FileService fileService = new FileService();
    private CiscoGeneratorService ciscoService = new CiscoGeneratorService();

    @FXML
    public void initialize() {
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        colSource.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));
        colDest.setCellValueFactory(new PropertyValueFactory<>("destIp"));
        colPort.setCellValueFactory(new PropertyValueFactory<>("destPort"));

        comboSimProtocol.setItems(FXCollections.observableArrayList("TCP", "UDP", "ICMP", "ANY"));
        comboSimProtocol.getSelectionModel().select("ANY");
        ruleTable.setItems(ruleList);

        topoPane.widthProperty().addListener((obs, oldVal, newVal) -> drawTopology());
        topoPane.heightProperty().addListener((obs, oldVal, newVal) -> drawTopology());

        Platform.runLater(this::drawTopology);
        loadRulesFromDatabase();
    }

    // FIXED handleAddRule Logic
    @FXML 
    private void handleAddRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            
            AddRuleController controller = loader.getController(); // Get controller instance
            
            Stage stage = new Stage();
            stage.setTitle("Add New Rule");
            stage.initOwner(ruleTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait(); 

            // GET THE RULE AND SAVE IT
            Rule newRule = controller.getCreatedRule();
            if (newRule != null) {
                ruleDAO.addRule(newRule); // SAVE TO DATABASE
                loadRulesFromDatabase();  // REFRESH TABLE
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    @FXML 
    private void handleEditRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a rule to edit.", Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            AddRuleController controller = loader.getController();
            controller.setRuleForEdit(selected);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Rule");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            Rule updatedRule = controller.getCreatedRule();
            if (updatedRule != null) {
                ruleDAO.updateRule(updatedRule); 
                loadRulesFromDatabase();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleGenerateCisco() {
        if (ruleList.isEmpty()) return;
        String ciscoConfig = ciscoService.generateConfig(ruleList);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cisco Configuration");
        TextArea textArea = new TextArea(ciscoConfig);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Monospaced';");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void loadRulesFromDatabase() {
        ruleList.clear();
        ruleList.addAll(ruleDAO.getAllRules());
    }

    // --- OTHER HANDLERS AND ANIMATIONS ---
    
    @FXML
    private void handleSimulate() {
        try {
            String src = txtSimSource.getText();
            String dest = txtSimDest.getText();
            String proto = comboSimProtocol.getValue();
            int val = txtSimPort.getText().isEmpty() ? -1 : Integer.parseInt(txtSimPort.getText());
            int destPort = proto.equalsIgnoreCase("ICMP") ? -1 : val;
            int icmpType = proto.equalsIgnoreCase("ICMP") ? val : -1;

            Packet packet = new Packet(src, dest, proto, destPort, icmpType);
            Rule match = simulatorService.simulate(packet, ruleList);

            boolean permitted = (match != null && match.getAction().equals("PERMIT"));
            String resultText = match != null ? "RESULT: " + match.getAction() + " (Rule #" + match.getPriority() + ")" : "RESULT: DENY (Implicit Deny)";
            
            lblResult.setText(resultText);
            lblResult.setTextFill(permitted ? Color.valueOf("#2ecc71") : Color.valueOf("#ff7675"));
            
            if(match != null) {
                ruleTable.getSelectionModel().select(match);
                ruleTable.scrollTo(match);
            } else {
                ruleTable.getSelectionModel().clearSelection();
            }

            logDAO.saveLog(packet, resultText, match != null ? match.getId() : null);
            animatePacket(permitted);
        } catch (NumberFormatException e) {
            lblResult.setText("ERROR: Port must be a number");
        }
    }

    private void drawTopology() {
        if (topoPane.getWidth() == 0) return;
        topoPane.getChildren().clear();
        double width = topoPane.getWidth();
        double centerY = topoPane.getHeight() / 2 - 10;
        Line cable = new Line(100, centerY, width - 100, centerY);
        cable.setStroke(Color.LIGHTGRAY);
        cable.setStrokeWidth(4);
        cable.getStrokeDashArray().addAll(10d, 10d);
        topoPane.getChildren().add(cable);
        drawDevice(100, centerY, "SOURCE PC", Color.web("#3498db"));
        drawDevice(width / 2, centerY, "ROUTER", Color.web("#34495e"));
        drawDevice(width - 100, centerY, "DEST SERVER", Color.web("#2ecc71"));
        packetNode = new Circle(10, Color.YELLOW);
        packetNode.setVisible(false);
        topoPane.getChildren().add(packetNode);
    }

    private void drawDevice(double x, double y, String name, Color color) {
        Circle circle = new Circle(x, y, 25, color);
        Text text = new Text(name);
        text.setX(x - 35); text.setY(y + 45);
        topoPane.getChildren().addAll(circle, text);
    }

    private void animatePacket(boolean isPermitted) {
        double startX = 100;
        double routerX = topoPane.getWidth() / 2;
        double endX = topoPane.getWidth() - 100;
        double centerY = topoPane.getHeight() / 2 - 10;
        packetNode.setTranslateX(0);
        packetNode.setCenterX(startX);
        packetNode.setCenterY(centerY);
        packetNode.setFill(isPermitted ? Color.web("#2ecc71") : Color.web("#ff7675"));
        packetNode.setVisible(true);
        TranslateTransition toRouter = new TranslateTransition(Duration.seconds(1.2), packetNode);
        toRouter.setToX(routerX - startX);
        if (isPermitted) {
            TranslateTransition toDest = new TranslateTransition(Duration.seconds(1.2), packetNode);
            toDest.setToX(endX - startX);
            new SequentialTransition(toRouter, toDest).play();
        } else {
            toRouter.play();
        }
    }

    @FXML private void handleExportCSV() {
        FileChooser fc = new FileChooser();
        File file = fc.showSaveDialog(ruleTable.getScene().getWindow());
        if (file != null) {
            try { fileService.exportToCSV(ruleList, file); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML private void handleImportCSV() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(ruleTable.getScene().getWindow());
        if (file != null) {
            try {
                List<Rule> imported = fileService.importFromCSV(file);
                for (Rule r : imported) ruleDAO.addRule(r);
                loadRulesFromDatabase();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML private void handleViewHistory() {
        String data = logDAO.getFormattedLogs(20);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        TextArea area = new TextArea(data);
        area.setEditable(false);
        alert.getDialogPane().setContent(area);
        alert.show();
    }

    @FXML private void handleDeleteRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ruleDAO.deleteRule(selected.getId());
            loadRulesFromDatabase();
        }
    }

    @FXML private void handleClearAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Delete all rules?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            for (Rule r : ruleList) ruleDAO.deleteRule(r.getId());
            loadRulesFromDatabase();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}