package com.aclsim;

import com.aclsim.database.LogDAO;
import com.aclsim.database.RuleDAO;
import com.aclsim.model.Packet;
import com.aclsim.model.Rule;
import com.aclsim.service.BatchSimulatorService;
import com.aclsim.service.CiscoGeneratorService;
import com.aclsim.service.FileService;
import com.aclsim.service.SimulatorService;
import com.aclsim.service.LabDatabase;
import com.aclsim.service.GradingService;
import com.aclsim.model.LabScenario;
import com.aclsim.model.GradingResult;

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
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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

    //FXML Academy components (3.3)
    @FXML private ToggleButton btnToggleAcademy;
    @FXML private VBox academyPane;
    @FXML private ComboBox<String> comboActiveLab;
    @FXML private TextArea txtLabDescription;



    private ObservableList<Rule> ruleList = FXCollections.observableArrayList();
    private SimulatorService simulatorService = new SimulatorService();
    private RuleDAO ruleDAO = new RuleDAO();
    private LogDAO logDAO = new LogDAO();
    private FileService fileService = new FileService();
    private CiscoGeneratorService ciscoService = new CiscoGeneratorService();
    private BatchSimulatorService batchService = new BatchSimulatorService();

    //Academy Services(3.2 & 3.3)
    private GradingService gradingService = new com.aclsim.service.GradingService();
    private LabScenario activateScenario = null;

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

        // Responsive Topology listeners (Bug fix for window resize)
        topoPane.widthProperty().addListener((obs, oldVal, newVal) -> drawTopology());
        topoPane.heightProperty().addListener((obs, oldVal, newVal) -> drawTopology());

        // 2.4.1: Setup Drag and Drop logic
        setupDragAndDrop();

        //3.3.1: Setup the Labs ComboBox
        setupAcademyLabs();

        Platform.runLater(this::drawTopology);
        loadRulesFromDatabase();
    }


    // --- ACADEMY METHODS (3.3) ---
    private void setupAcademyLabs(){
        ObservableList<String> labTitles = FXCollections.observableArrayList();
        for (com.aclsim.model.LabScenario scenario : com.aclsim.service.LabDatabase.getAllScenarios()) {
            labTitles.add(scenario.getTitle());
        }
        comboActiveLab.setItems(labTitles);
        comboActiveLab.getSelectionModel().selectFirst();
        handleLabSelection(); //Load level 1 instructions automatically
    }

    /**
     * 3.3.1: Toggles the visibility and spacing allocation of the Academy sidebar.
     */
    @FXML
    private void handleToggleAcademy(){
        boolean active = btnToggleAcademy.isSelected();
        academyPane.setVisible(active);
        academyPane.setManaged(active); // Tells layout engine to adjust main window spacing
    }

    /**
     * 3.3.1: Triggered when a student selects a different level in the dropdown.
     */
    @FXML
    private void handleLabSelection(){
        int selectedIndex = comboActiveLab.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            activateScenario = com.aclsim.service.LabDatabase.getAllScenarios().get(selectedIndex);
            txtLabDescription.setText(activateScenario.getDescription());
        }
    }

    /**
     * 3.3.2: Evaluates the student's table rules against the scenario constraints.
     */
    @FXML
    private void handleSubmitLab(){
        if (activateScenario == null) return;

        // Run the Auto-Grader!
        com.aclsim.model.GradingResult result = gradingService.gradeLab(activateScenario, ruleList);

        //Display result in a sleek modal
        Alert alert = new Alert(result.isPassed() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle("Grade Report");
        alert.setHeaderText(result.isPassed() ? "MISSION ACCOMPLISHED! 🎉" : "MISSION FAILED. ❌");

        TextArea reportArea = new TextArea(result.getFeedbackDetails());
        reportArea.setEditable(false);
        reportArea.setPrefSize(500, 300);
        reportArea.setStyle("-fx-font-family: 'Monospaced';");

        if(!result.isPassed()) {
            //Display the intelligent hint if they failed(3.4.1)
            reportArea.appendText("\n" + "-".repeat(50) + "\n" + result.getHint());
        }

        alert.getDialogPane().setContent(reportArea);
        alert.showAndWait();
    }

    /**
     * 3.4.3: Automatically populates the Packet Simulator field with a sample packet from the active Lab Scenario for manual debugging.
     */

    @FXML
    private void handleAutoFillPacket() {
        if (activateScenario == null || activateScenario.getTestPackets().isEmpty()){
            showAlert("No Active Lab", "Please select a valid lab level first.", Alert.AlertType.WARNING);
            return;
        }
        Packet samplePacket = activateScenario.getTestPackets().get(0);

        txtSimSource.setText(samplePacket.getSourceIp());
        txtSimDest.setText(samplePacket.getDestIp());
        comboSimProtocol.setValue(samplePacket.getProtocol().toUpperCase());

        if (samplePacket.getProtocol().equalsIgnoreCase("ICMP")){
            txtSimPort.setText(String.valueOf(samplePacket.getIcmpType()));
        } else {
            txtSimPort.setText(samplePacket.getDestPort() == -1 ? "ANY" : String.valueOf(samplePacket.getDestPort()));
        }

        System.out.println("LOG: Populated simulator fields with sample packet from " + activateScenario.getTitle());
    }




    /**
     * 2.4.1: Row factory implementation for Drag-and-Drop support.
     */
    private void setupDragAndDrop() {
        ruleTable.setRowFactory(tv -> {
            TableRow<Rule> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(DataFormat.PLAIN_TEXT, index.toString());
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(DataFormat.PLAIN_TEXT)) {
                    if (row.getIndex() != Integer.parseInt((String) db.getContent(DataFormat.PLAIN_TEXT))) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(DataFormat.PLAIN_TEXT)) {
                    int draggedIndex = Integer.parseInt((String) db.getContent(DataFormat.PLAIN_TEXT));
                    Rule draggedRule = ruleTable.getItems().remove(draggedIndex);

                    int dropIndex = row.isEmpty() ? ruleTable.getItems().size() : row.getIndex();
                    ruleTable.getItems().add(dropIndex, draggedRule);

                    event.setDropCompleted(true);
                    
                    // Trigger optimization suggested for DB performance
                    recalculatePriorities();
                    event.consume();
                }
            });
            return row;
        });
    }

    /**
     * 2.4.1 Optimized: Updates rule priority objects and performs a 
     * single batch update to the database.
     */
    private void recalculatePriorities() {
        int priorityCounter = 10;
        for (Rule rule : ruleList) {
            rule.setPriority(priorityCounter);
            priorityCounter += 10;
        }
        
        // Use optimized syncPriorities method from RuleDAO
        ruleDAO.syncPriorities(ruleList); 
        
        ruleTable.refresh(); 
        System.out.println("LOG: Rules reordered and database synchronized via batch update.");
    }

    private void loadRulesFromDatabase() {
        ruleList.clear();
        ruleList.addAll(ruleDAO.getAllRules());
    }

    // --- CRUD HANDLERS ---

    @FXML 
    private void handleAddRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/aclsim/add_rule_dialog.fxml"));
            Parent root = loader.load();
            AddRuleController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Rule");
            stage.initOwner(ruleTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait(); 

            Rule newRule = controller.getCreatedRule();
            if (newRule != null) {
                ruleDAO.addRule(newRule);
                loadRulesFromDatabase();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML 
    private void handleEditRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a rule to edit.", Alert.AlertType.WARNING);
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

    @FXML private void handleDeleteRule() {
        Rule selected = ruleTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ruleDAO.deleteRule(selected.getId());
            loadRulesFromDatabase();
        }
    }

    @FXML private void handleClearAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setContentText("Delete all rules permanently?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            for (Rule r : ruleList) ruleDAO.deleteRule(r.getId());
            loadRulesFromDatabase();
        }
    }

    // --- SIMULATION & VISUALS ---
    
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
            String resultText = (match != null) ? "RESULT: " + match.getAction() + " (Rule #" + match.getPriority() + ")" : "RESULT: DENY (Implicit Deny)";
            
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

    private void drawTopology() {
        if (topoPane.getWidth() == 0) return;
        topoPane.getChildren().clear();
        double width = topoPane.getWidth();
        double centerY = topoPane.getHeight() / 2 - 10;

        Line cable = new Line(100, centerY, width - 100, centerY);
        cable.setStroke(Color.LIGHTGRAY); cable.setStrokeWidth(4);
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
        text.setStyle("-fx-font-weight: bold; -fx-fill: #2c3e50;");
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

    // --- UTILITY TOOLS ---

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

    @FXML private void handleBatchTest() {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(ruleTable.getScene().getWindow());
        if (file != null) {
            try {
                List<Packet> packets = fileService.importPackets(file);
                String report = batchService.runBatch(packets, ruleList, simulatorService);
                TextArea area = new TextArea(report);
                area.setEditable(false); area.setPrefSize(600, 400); area.setStyle("-fx-font-family: 'Monospaced';");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.getDialogPane().setContent(area);
                alert.showAndWait();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML private void handleGenerateCisco() {
        if (ruleList.isEmpty()) return;
        String config = ciscoService.generateConfig(ruleList);
        TextArea area = new TextArea(config);
        area.setEditable(false); area.setStyle("-fx-font-family: 'Monospaced';");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    @FXML private void handleViewHistory() {
        String data = logDAO.getFormattedLogs(25);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        TextArea area = new TextArea(data);
        area.setEditable(false); area.setPrefSize(700, 400); area.setStyle("-fx-font-family: 'Monospaced';");
        alert.getDialogPane().setContent(area);
        alert.show();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setContentText(content);
        alert.show();
    }
}