module no.ntnu.principes {
  requires javafx.fxml;
  requires javafx.web;

  requires org.controlsfx.controls;
  requires com.dlsc.formsfx;
  requires net.synedra.validatorfx;
  requires org.kordamp.ikonli.core;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.material2;
  requires org.kordamp.bootstrapfx.core;
  requires eu.hansolo.tilesfx;
  requires com.almasb.fxgl.all;
  requires atlantafx.base;
  requires org.slf4j;
  requires com.google.protobuf;
  requires annotations;
  requires javafx.media;
  requires static lombok;
  requires org.reflections;
  requires com.fasterxml.jackson.databind;
  requires org.xerial.sqlitejdbc;
  requires java.desktop;
  requires java.prefs;

  opens no.ntnu.principes to javafx.fxml, jdk.javadoc;
  exports no.ntnu.principes;
  exports no.ntnu.principes.components;
  exports no.ntnu.principes.controller;
  exports no.ntnu.principes.controller.screen;
  exports no.ntnu.principes.domain;
  exports no.ntnu.principes.domain.task;
  exports no.ntnu.principes.config;
  exports no.ntnu.principes.domain.profile;
  exports no.ntnu.principes.repository;
  exports no.ntnu.principes.service;
  exports no.ntnu.principes.util;
  exports no.ntnu.principes.view;
  exports no.ntnu.principes.view.dev;
  exports no.ntnu.principes.event.navigation;
  exports no.ntnu.principes.dto;
//  exports no.ntnu.principes.controller.screen;
  opens no.ntnu.principes.components to javafx.fxml;
  exports no.ntnu.principes.components.validations;
  opens no.ntnu.principes.components.validations to javafx.fxml;
}