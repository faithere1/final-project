package com.aclsim;

import com.aclsim.database.LogDAO;
import com.aclsim.database.RuleDAO;
import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
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

        // --- BUG FIX: RESPONSIVE TOPOLOGY ---
        // Listen for width and height changes to redraw the topology automatically
        topoPane.widthProperty().addListener((obs, oldVal, newVal) -> drawTopology());
        topoPane.heightProperty().addListener((obs, oldVal, newVal) -> drawTopology());

        Platform.runLater(this::drawTopology);
        loadRulesFromDatabase();
    }

    /**
     * Updated to support dynamic resizing. 
     * Recalculates device positions based on current pane width/height.
     */
    private void drawTopology() {
        // Prevent drawing if the pane is not yet visible/sized
        if (topoPane.getWidth() == 0) return;

        topoPane.getChildren().clear();
        double width = topoPane.getWidth();
        double height = topoPane.getHeight();
        double centerY = height / 2 - 10;

        // Draw Cables (Relative to current width)
        Line cable = new Line(100, centerY, width - 100, centerY);
        cable.setStroke(Color.LIGHTGRAY);
        cable.setStrokeWidth(4);
        cable.getStrokeDashArray().addAll(10d, 10d);
        topoPane.getChildren().add(cable);

        // Draw Devices (Relative to current width)
        drawDevice(100, centerY, "SOURCE PC", Color.web("#3498db"));
        drawDevice(width / 2, centerY, "ROUTER", Color.web("#34495e"));
        drawDevice(width - 100, centerY, "DEST SERVER", Color.web("#2ecc71"));

        // Re-initialize packetNode so it exists on the new canvas
        packetNode = new Circle(10, Color.YELLOW);
        packetNode.setStroke(Color.BLACK);
        packetNode.setVisible(false);
        topoPane.getChildren().add(packetNode);
    }

    private void drawDevice(double x, double y, String name, Color color) {
        Circle circle = new Circle(x, y, 25, color);
        circle.setStroke(Color.DARKSLATEGRAY);
        circle.setStrokeWidth(2);
        Text text = new Text(name);
        text.setX(x - 35); text.setY(y + 45);
        text.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-fill: #2c3e50;");
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
        packetNode.setOpacity(1.0);
        packetNode.setVisible(true);

        TranslateTransition toRouter = new TranslateTransition(Duration.seconds(1.2), packetNode);
        toRouter.setToX(routerX - startX);

        if (isPermitted) {
            TranslateTransition toDest = new TranslateTransition(Duration.seconds(1.2), packetNode);
            toDest.setToX(endX - startX);
            new SequentialTransition(toRouter, toDest).play();
        } else {
            toRouter.setOnFinished(e -> {
                TranslateTransition shake = new TranslateTransition(Duration.seconds(0.1), packetNode);
                shake.setByX(5); shake.setAutoReverse(true); shake.setCycleCount(4);
                FadeTransition fade = new FadeTransition(Duration.seconds(0.5), packetNode);
                fade.setToValue(0);
                new SequentialTransition(shake, fade).play();
            });
            toRouter.play();
        }
    }

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
            lblResult.setText("ERROR: Port/Type must be a number");
            lblResult.setTextFill(Color.valueOf("#fdcb6e"));
        }
    }

    // --- CSV & DIALOG HANDLERS ---

    @FXML
    private void handleExportCSV() {
        if (ruleList.isEmpty()) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Export CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog(ruleTable.getScene().getWindow());
        if (file != null) {
            try {
                fileService.exportToCSV(ruleList, file);
                showAlert("Success", "Export complete.", Alert.AlertType.INFORMATION);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleImportCSV() {
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

    @FXML
    private void handleViewHistory() {
        String data = logDAO.getFormattedLogs(20);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("History");
        TextArea area = new TextArea(data);
        area.setEditable(false);
        area.setStyle("-fx-font-family: 'Monospaced';");
        alert.getDialogPane().setContent(area);
        alert.show();
    }

    private void loadRulesFromDatabase() {
        ruleList.clear();
        ruleList.addAll(ruleDAO.getAllRules());
    }

    @FXML private void handleAddRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadRulesFromDatabase();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleEditRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            AddRuleController controller = loader.getController();
            controller.setRuleForEdit(selected);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
            if (controller.getCreatedRule() != null) {
                ruleDAO.updateRule(controller.getCreatedRule());
                loadRulesFromDatabase();
            }
        } catch (IOException e) { e.printStackTrace(); }
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
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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