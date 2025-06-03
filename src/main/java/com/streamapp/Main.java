package com.streamapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Основной класс приложения StreamApp.
 * Отвечает за запуск приложения и инициализацию загрузочного экрана.
 */
public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Запуск приложения StreamApp");
            
            // Загрузка FXML файла загрузочного экрана
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/streamapp/ui/splash/SplashScreen.fxml"));
            Parent root = loader.load();
            
            // Настройка сцены
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/streamapp/ui/splash/splash.css").toExternalForm());
            
            // Настройка окна
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setTitle("StreamApp");
            primaryStage.setScene(scene);
            
            // Добавление возможности перетаскивания окна
            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            
            root.setOnMouseDragged(event -> {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            });
            
            primaryStage.show();
            logger.info("Загрузочный экран успешно отображен");
            
        } catch (Exception e) {
            logger.error("Ошибка при запуске приложения", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 