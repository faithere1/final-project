# ACL Simulator - Troubleshooting Guide

This guide records the important issues found during development and the
standard fixes for building, running, and maintaining the project.

---

## JavaFX App Does Not Start

**Symptom:** Maven starts, but the application window never appears or JavaFX
classes cannot be found.

**Likely cause:** The app is being launched without JavaFX dependencies on the
runtime path.

**Fix:**

```bash
mvn clean javafx:run
```

For the packaged release, build first and launch the generated JAR:

```bash
mvn clean package
java -jar target/ACLSimulator.jar
```

---

## SQLite `prepareCall` Error

**Symptom:** SQLite throws an error when trying to execute DAO operations.

**Cause:** SQLite JDBC does not support stored-procedure style `prepareCall`
usage.

**Fix:** Use `Connection.prepareStatement(...)` for normal SQL operations.
The DAO classes now use prepared statements for CRUD, history, and progress
queries.

---

## Database File Appears In The Wrong Folder

**Symptom:** `acl_simulator.db` appears in the terminal's current directory
instead of inside `src` or `resources`.

**Cause:** The database URL is relative:

```java
jdbc:sqlite:acl_simulator.db
```

SQLite creates the file relative to the process working directory.

**Fix:** Launch the app from the `ACLSimulator/` project folder when developing.
For release builds, keep the launcher script beside the JAR so the database is
created beside the runnable app.

---

## macOS Dock Icon Does Not Update

**Symptom:** The window icon is correct, but the macOS Dock icon remains the
default Java icon.

**Cause:** macOS requires the AWT Taskbar API for Dock icon replacement.

**Fix:** `MainApp.java` detects macOS and calls:

```java
java.awt.Taskbar.getTaskbar().setIconImage(...)
```

If macOS blocks the operation, the app continues running and prints a bypass
message.

---

## JavaFX CSS Parser Rejects Styles

**Symptom:** The UI loads without the expected theme, or JavaFX reports CSS
parser errors such as `RBRACE` or unsupported variable syntax.

**Cause:** JavaFX CSS is not browser CSS. It does not support every modern CSS
feature, including normal `--variable` and `var(...)` syntax.

**Fix:** Use JavaFX-compatible `-fx-*` properties and load the stylesheet from
the JavaFX scene in `MainApp.java`.

---

## Academy Progress Count Is Wrong

**Symptom:** Lab attempts or completion state does not match the UI.

**Likely cause:** Progress rows are inserted and updated separately, so the code
must ensure a lab row exists before incrementing or marking completion.

**Fix:** `ProgressDAO` uses explicit `SELECT`, `INSERT`, and `UPDATE` flows for
attempts, completion count, reset, and lab-completed checks.

---

## Typo In Progress Column Name

**Symptom:** SQL queries fail for attempt tracking.

**Cause:** A misspelled column name such as `attemps` was used during
development.

**Fix:** The canonical column is:

```sql
attempts INTEGER DEFAULT 0
```

Use `attempts` consistently in schema and queries.

---

## JAR Builds But Does Not Launch

**Symptom:** `mvn package` succeeds, but `java -jar target/ACLSimulator.jar`
does not open the app.

**Likely causes:**

- The JAR manifest points directly to a class extending `Application`.
- JavaFX dependencies were not shaded into the JAR.
- Platform-specific JavaFX native libraries are missing.

**Fix:** The v5.0 build uses a small `Launcher` class as the JAR entry point and
the Maven Shade Plugin to build `target/ACLSimulator.jar`.

---

## Tests Do Not Run

**Symptom:** Maven reports no tests or fails to discover JUnit 5 tests.

**Cause:** JUnit 5 requires the Surefire plugin version used by the project.

**Fix:**

```bash
mvn test
```

The project uses `maven-surefire-plugin` 3.1.2 and JUnit Jupiter 5.10.0.
