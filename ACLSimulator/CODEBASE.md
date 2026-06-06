# ACL Simulator - Codebase Reference Atlas

This document provides a comprehensive breakdown of every file, package, 
and public method in the ACL Simulator project.

---

## Package Structure Overview

```
src/main/java/com/aclsim/
├── MainApp.java                  → Application entry point & splash screen
├── MainController.java           → Primary GUI event handler and view coordinator
├── AddRuleController.java        → Dialog controller for rule creation/editing
│
├── model/
│   ├── Rule.java                 → Data model for a single ACL rule entry
│   ├── Packet.java               → Data model for a simulated network packet
│   ├── LabScenario.java          → Data model for a single Academy lab level
│   └── GradingResult.java        → Data model for the auto-grader's evaluation
│
├── database/
│   ├── DatabaseManager.java      → SQLite connection pool & table initialization
│   ├── RuleDAO.java              → CRUD operations for the rules table
│   ├── LogDAO.java               → Read/write for simulation_logs table
│   └── ProgressDAO.java          → Read/write for user_progress table
│
└── service/
    ├── SimulatorService.java      → Core First-Match ACL simulation algorithm
    ├── NetworkUtils.java          → IP validation, CIDR, and Wildcard bitwise logic
    ├── FileService.java           → CSV export/import and progress report generation
    ├── CiscoGeneratorService.java → Translates GUI rules to Cisco IOS CLI format
    ├── BatchSimulatorService.java → Processes multiple packets in a single batch
    ├── GradingService.java        → Auto-grader with intelligent hint generation
    └── LabDatabase.java           → Static repository of all 8 lab scenarios
```

---

## File & Method Responsibilities

### `MainApp.java`
**Package:** `com.aclsim`
**Purpose:** The application's main entry point. Initializes the database,
applies the stylesheet, loads the primary FXML, and displays the animated
startup splash screen before revealing the main dashboard.

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `start(Stage)` | `primaryStage` | `void` | JavaFX lifecycle entry. Loads FXML, binds CSS, shows splash. |
| `showSplashScreen(Stage)` | `mainStage` | `void` | Creates the animated NetOps boot loader splash window. |
| `main(String[])` | `args` | `void` | Standard Java entry point. Calls `launch()`. |

---

### `MainController.java`
**Package:** `com.aclsim`
**Purpose:** The central GUI controller. Handles all button interactions, 
manages the TableView bindings, coordinates between the DAO layer and the 
simulation engine, and renders the visual network topology canvas.

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `initialize()` | - | `void` | Binds table columns, setups topology listeners, initializes Academy labs. |
| `loadRulesFromDatabase()` | - | `void` | Clears and reloads the rule list from SQLite into the TableView. |
| `handleAddRule()` | - | `void` | Opens the Add Rule dialog and persists a new rule. |
| `handleEditRule()` | - | `void` | Opens the Edit Rule dialog pre-filled with the selected rule's data. |
| `handleDeleteRule()` | - | `void` | Deletes the selected rule from the database and refreshes the table. |
| `handleClearAll()` | - | `void` | Shows a confirmation dialog and deletes all rules from the database. |
| `handleSimulate()` | - | `void` | Builds a Packet from the input fields, runs the simulation, updates the result label, logs the event, and triggers the animation. |
| `handleBatchTest()` | - | `void` | Opens a file chooser, imports packets from a .txt file, and displays a batch simulation report. |
| `handleExportCSV()` | - | `void` | Exports the current rule list to a user-specified CSV file. |
| `handleImportCSV()` | - | `void` | Imports rules from a user-selected CSV file into the database. |
| `handleGenerateCisco()` | - | `void` | Generates and displays Cisco IOS CLI access-list commands. |
| `handleViewHistory()` | - | `void` | Fetches and displays the last 25 simulation log entries. |
| `handleToggleAcademy()` | - | `void` | Toggles the visibility of the Academy sidebar panel. |
| `handleLabSelection()` | - | `void` | Loads the selected lab's description into the Academy text area. |
| `handleSubmitLab()` | - | `void` | Grades the student's current ruleset and displays the report card. |
| `handleAutoFillPacket()` | - | `void` | Populates the simulator fields with the active lab's first test packet. |
| `handleSaveProgressReport()` | - | `void` | Generates and saves a full academic audit report to a .txt file. |
| `handleResetProgress()` | - | `void` | Confirms and clears all lab progress from the database. |
| `setupDragAndDrop()` | - | `void` | Attaches drag-and-drop row factory to the TableView for rule reordering. |
| `recalculatePriorities()` | - | `void` | Re-indexes rule priorities (10,20,30...) and batch-syncs to the database. |
| `drawTopology()` | - | `void` | Draws the Source PC, branded Router logo, and Dest Server on the canvas. |
| `drawDevice(double, double, String, Color)` | `x, y, name, color` | `void` | Renders a single labeled circle device on the topology canvas. |
| `drawRouterImage(double, double)` | `x, y` | `void` | Renders the custom 100x100px brand logo PNG as the center Router node. |
| `animatePacket(boolean)` | `isPermitted` | `void` | Animates the packet across the topology (green=pass, red=deny+shake+fade). |

---

### `AddRuleController.java`
**Package:** `com.aclsim`
**Purpose:** Controls the "Add Rule" and "Edit Rule" dialog window. Provides 
real-time validation feedback (red/green borders) as the user types, and 
returns the final constructed Rule object back to the MainController.

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `initialize()` | - | `void` | Populates dropdowns, sets up protocol-aware port context switching and real-time validation listeners. |
| `setRuleForEdit(Rule)` | `rule` | `void` | Pre-fills all dialog fields with data from an existing rule for editing. |
| `getCreatedRule()` | - | `Rule` | Returns the Rule object built from the dialog's fields after saving. |
| `handleSave()` | - | `void` | Validates input, constructs the Rule object, and closes the dialog. |
| `handleCancel()` | - | `void` | Clears the created rule and closes the dialog without saving. |
| `setupRealTimeValidation(TextField, String)` | `field, type` | `void` | Attaches a text change listener that applies CSS success/error border states. |
| `validateField(TextField, String)` | `field, type` | `void` | Evaluates a single field's value and applies the correct CSS style class. |
| `isInputValid()` | - | `boolean` | Final validation check before construct