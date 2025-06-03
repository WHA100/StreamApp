package com.streamapp.ui.splash;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер загрузочного экрана приложения.
 * Управляет анимацией и процессом загрузки.
 */
public class SplashScreenController {
    private static final Logger logger = LoggerFactory.getLogger(SplashScreenController.class);

    @FXML
    private ImageView logoImageView;
    
    @FXML
    private Label appNameLabel;
    
    @FXML
    private ProgressIndicator loadingIndicator;
    
    @FXML
    private Label statusLabel;

    /**
     * Инициализация контроллера и запуск анимаций.
     */
    @FXML
    public void initialize() {
        logger.info("Инициализация загрузочного экрана");
        setupAnimations();
        startLoadingProcess();
    }

    /**
     * Настройка анимаций для элементов интерфейса.
     */
    private void setupAnimations() {
        // Анимация логотипа
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.5), logoImageView);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setCycleCount(1);
        scaleTransition.play();

        // Анимация названия приложения
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), appNameLabel);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();

        // Анимация индикатора загрузки
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), loadingIndicator);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.play();
    }

    /**
     * Запуск процесса загрузки приложения.
     */
    private void startLoadingProcess() {
        // Здесь будет логика загрузки приложения
        updateStatus("Инициализация компонентов...");
        
        // Имитация загрузки (в реальном приложении здесь будет реальная логика)
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                updateStatus("Загрузка сетевых компонентов...");
                Thread.sleep(1000);
                updateStatus("Подготовка интерфейса...");
                Thread.sleep(1000);
                updateStatus("Запуск приложения...");
                Thread.sleep(500);
                
                // Здесь будет переход к основному окну приложения
                logger.info("Загрузка завершена");
            } catch (InterruptedException e) {
                logger.error("Ошибка при загрузке", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Обновление статуса загрузки.
     * @param status новый статус
     */
    private void updateStatus(String status) {
        javafx.application.Platform.runLater(() -> {
            statusLabel.setText(status);
            logger.info("Статус загрузки: {}", status);
        });
    }
} 