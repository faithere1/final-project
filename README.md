# ACL Simulator

ACL Simulator is a JavaFX desktop application for building, testing, and learning
network Access Control Lists. It combines a rule table, packet simulator,
visual topology animation, Cisco IOS config generation, batch testing, history
logging, and an Academy mode with auto-graded lab scenarios.

![ACL Simulator main screen](image.png)

## Current Release

**v5.0.0 - The Distribution & Documentation Update**

This release turns the capstone prototype into a shareable project package:

- GitHub-ready README and documentation set.
- Codebase atlas for packages, classes, and public responsibilities.
- Troubleshooting notes for known build, JavaFX, SQLite, and UI issues.
- Maven release build configuration for a runnable application JAR.
- Cross-platform launcher scripts for macOS/Linux and Windows.

## Feature Highlights

- **ACL rule engine:** first-match evaluation with implicit deny behavior.
- **Advanced matching:** exact IP, CIDR, wildcard mask, protocol, port range,
  and ICMP type handling.
- **Rule management:** create, edit, delete, reorder, import, and export rules.
- **Visual simulation:** animated source-to-router-to-destination packet flow.
- **Cisco generator:** converts GUI rules into Cisco IOS-style ACL commands.
- **Batch testing:** imports packet lists and runs them through the simulator.
- **Academy mode:** 8 scenario-based labs with grading, hints, attempts, and
  completion tracking.
- **Audit reports:** exports structured student progress and simulation history.
- **SQLite persistence:** local database for rules, logs, and lab progress.

## Screenshots

![Academy mode](image-1.png)

![Lab completion report](image-2.png)

![Splash screen](image-4.png)

![Updated dashboard](image-3.png)

![Brand update](image-5.png)

## Architecture

```mermaid
flowchart LR
    UI["JavaFX UI\nFXML + Controllers"] --> Service["Service Layer\nSimulation, grading, files"]
    UI --> DAO["DAO Layer\nRules, logs, progress"]
    Service --> Model["Model Layer\nRule, Packet, LabScenario"]
    DAO --> DB["SQLite\nacl_simulator.db"]
    Service --> DAO
```

## Project Structure

```text
ACLSimulator/
├── pom.xml
├── DEVELOPMENT.md
├── CODEBASE.md
├── TROUBLESHOOTING.md
├── run.sh
├── run.bat
└── src/
    ├── main/java/com/aclsim/
    │   ├── MainApp.java
    │   ├── MainController.java
    │   ├── AddRuleController.java
    │   ├── Launcher.java
    │   ├── database/
    │   ├── model/
    │   └── service/
    └── test/java/com/aclsim/service/
```

## Requirements

- Java 17 or newer
- Maven 3.8 or newer

JavaFX dependencies are managed by Maven.

## Run From Source

```bash
cd ACLSimulator
mvn clean javafx:run
```

## Run Tests

```bash
cd ACLSimulator
mvn test
```

## Build The Release JAR

```bash
cd ACLSimulator
mvn clean package
```

The runnable JAR is generated at:

```text
ACLSimulator/target/ACLSimulator.jar
```

Launch it with:

```bash
java -jar target/ACLSimulator.jar
```

Or use the launcher scripts:

```bash
./run.sh
```

```bat
run.bat
```

## Documentation

- [Development process](ACLSimulator/DEVELOPMENT.md)
- [Codebase atlas](ACLSimulator/CODEBASE.md)
- [Troubleshooting guide](ACLSimulator/TROUBLESHOOTING.md)
- [v5.0 roadmap](v5.0%20plan.md)

## Release Package

The intended v5.0 distribution folder is:

```text
ACLSimulator_v5.0/
├── ACLSimulator.jar
├── run.sh
├── run.bat
└── README.md
```

The final instructor submission should include the source code, built JAR,
documentation files, screenshots, and the v5.0 release archive.
