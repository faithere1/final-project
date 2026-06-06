# ACL Simulator - Development Process Notes

This document chronicles the complete development journey of the ACL Simulator
across all major version milestones.

---

## Version History Overview

| Version | Title | Key Focus |
|---------|-------|-----------|
| v1.0 | Foundation | Maven setup, SQLite, JDBC, Rule model |
| v2.0 | The Feature Update | Advanced logic, GUI, database integration |
| v3.0 | The Sentinel & Academy Update | JUnit testing, LMS engine, Auto-grader |
| v4.0 | The NetOps & Identity Update | Theme, branding, splash screen |
| v5.0 | The Distribution Update | Packaging, README, documentation |

---

## v1.0: Foundation

- Created Maven project with JavaFX 21 and SQLite JDBC.
- Established MVC package structure: `model`, `database`, `service`.
- Built `DatabaseManager.java` for SQLite auto-initialization.
- Implemented `Rule` and `Packet` POJOs.
- Built `SimulatorService.java` with First-Match algorithm and Implicit Deny.

**Key Decisions:**
- JDBC over JPA/Hibernate for transparency.
- SQLite over MySQL for zero-server-install portability.
- MVC pattern enforced from day one.

---

## v2.0: The Feature Update

### 2.1 Advanced Networking Logic
- CIDR Matching: `ipToInt()` + bitwise AND masking.
- Port Ranges: Changed `destPort` from `int` to `String`.
- Full CRUD: Added `updateRule()` and Edit Dialog.
- Wildcard Masking: Bitwise NOT inversion (`~wildcardInt`).
- ICMP Type Filtering: Added `icmpType` field to `Packet`.

### 2.2 Visual Topology
- Used JavaFX `Pane` as a drawing canvas.
- Drew `Circle` and `Line` nodes programmatically.
- Implemented `TranslateTransition` and `FadeTransition`.
- Added resize listeners to fix animation misalignment bug.

### 2.3 Advanced Data Features
- CSV Export/Import via `PrintWriter` and `Scanner`.
- Simulation Logging with Java `LocalDateTime` timestamps.
- Cisco CLI Generator with CIDR-to-wildcard conversion.
- Batch Testing via external `.txt` packet file import.

### 2.4 UX Enhancements
- Drag-and-Drop via JavaFX `DragBoard` RowFactory.
- Real-time validation via `textProperty().addListener()`.
- Batch priority sync via JDBC `executeBatch()` transactions.

---

## v3.0: The Sentinel & Academy Update

### 3.1 Automated Testing
- Added JUnit 5 and Maven Surefire Plugin.
- 9 test cases across `NetworkUtilsTest` and `SimulatorServiceTest`.

### 3.2 Academy Engine
- `LabScenario.java`: immutable lab data container.
- `GradingService.java`: result comparison with context-aware hints.
- `LabDatabase.java`: 8 progressive story-driven scenarios.

### 3.3 Academy UI
- Added `<right>` BorderPane region toggled via `setVisible()` and `setManaged()`.

### 3.4 Mentor System
- `user_progress` SQLite table for attempt and completion tracking.
- `ProgressDAO.java` using explicit SELECT/INSERT/UPDATE logic.
- Academic audit report generator via `FileService`.

---

## v4.0: The NetOps & Identity Update

### 4.1 CSS Theme
- Discovered JavaFX USS parser does not support `--variable` / `var()` syntax.
- Replaced with JavaFX-native `-fx-variable` property lookups.
- Programmatically bound CSS to `Scene` in `MainApp.java` to guarantee loading.

### 4.2 SVG Icons & Typography
- Embedded `<SVGPath>` vector icons inside FXML buttons.
- Applied `CONSTRAINED_RESIZE_POLICY` to eliminate empty table header space.
- Applied JetBrains Mono monospace font stack to table cells and text fields.

### 4.3 Brand Identity
- Used `java.awt.Taskbar.getTaskbar().setIconImage()` for macOS Dock override.
- OS detection via `System.getProperty("os.name")`.
- Built `StageStyle.UNDECORATED` splash screen with `Timeline` typewriter animation.

### 4.4 Micro-Animations
- Added `:hover` and `:pressed` CSS pseudo-class states to all buttons.
- Added `.table-row-cell:hover` for interactive row highlighting.
- Applied context-aware drop-shadow glows to each button class.

---

## v5.0: The Distribution & Documentation Update

### 5.1 Developer Documentation
- Added a GitHub-ready root `README.md`.
- Added `CODEBASE.md` as a package and method responsibility atlas.
- Added `TROUBLESHOOTING.md` for known build, JavaFX, SQLite, and CSS issues.

### 5.2 Release Packaging
- Updated Maven project metadata to release version `5.0.0`.
- Added a dedicated `Launcher` entry point for executable JAR packaging.
- Configured Maven Shade Plugin to produce `target/ACLSimulator.jar`.

### 5.3 Launch Experience
- Added `run.sh` for macOS/Linux.
- Added `run.bat` for Windows.
- Updated visible application version labels from v4.4 to v5.0.
