module com.superbomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.superbomberman to javafx.fxml;
    exports com.superbomberman;

    opens com.superbomberman.model to javafx.fxml;
    exports com.superbomberman.model;

    opens com.superbomberman.controller to javafx.fxml;
    exports com.superbomberman.controller;
}