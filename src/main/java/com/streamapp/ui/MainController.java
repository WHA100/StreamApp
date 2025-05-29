package com.streamapp.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер главного окна приложения
 */
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private ListView<String> usersList;

    @FXML
    private Button startStreamButton;

    @FXML
    private Button stopStreamButton;

    @FXML
    public void initialize() {
        logger.info("Инициализация главного окна");
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        startStreamButton.setOnAction(event -> {
            logger.info("Нажата кнопка начала трансляции");
            // TODO: Реализовать логику начала трансляции
        });

        stopStreamButton.setOnAction(event -> {
            logger.info("Нажата кнопка остановки трансляции");
            // TODO: Реализовать логику остановки трансляции
        });
    }
} 