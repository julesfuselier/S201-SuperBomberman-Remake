open
module com.superbomberman {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;


    exports com.superbomberman;


    exports com.superbomberman.model;


    exports com.superbomberman.controller;


    exports com.superbomberman.service;

    exports com.superbomberman.model.powerup;


}