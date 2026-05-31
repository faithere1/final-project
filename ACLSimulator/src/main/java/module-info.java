module com.aclsim {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    // SQLite often doesn't have a formal module name, 
    // so we use its automatic name.
    requires org.xerial.sqlitejdbc;
    requires java.desktop; // For Taskbar icon support

    opens com.aclsim to javafx.fxml, javafx.graphics;
    opens com.aclsim.model to javafx.base;

    exports com.aclsim;
    exports com.aclsim.model;
    exports com.aclsim.database;
}