package com.streamapp.ui.controller;

import com.streamapp.core.model.User;
import com.streamapp.ui.viewmodel.UserListViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

/**
 * Контроллер для управления списком пользователей
 */
public class UserListController {
    @FXML
    private ListView<User> userListView;
    
    private UserListViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new UserListViewModel();
        userListView.setItems(viewModel.getUsers());
        userListView.setCellFactory(lv -> new UserListCell());
    }

    /**
     * Кастомная ячейка для отображения пользователя в списке
     */
    private class UserListCell extends ListCell<User> {
        private final HBox content;
        private final ImageView avatarView;
        private final Label usernameLabel;
        private final Label statusLabel;
        private final VBox textContainer;

        public UserListCell() {
            content = new HBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            
            avatarView = new ImageView();
            avatarView.setFitHeight(40);
            avatarView.setFitWidth(40);
            avatarView.setPreserveRatio(true);
            
            textContainer = new VBox(5);
            usernameLabel = new Label();
            statusLabel = new Label();
            statusLabel.getStyleClass().add("status-label");
            
            textContainer.getChildren().addAll(usernameLabel, statusLabel);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            
            content.getChildren().addAll(avatarView, textContainer);
            
            // Добавляем анимацию появления
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            setOnMouseEntered(e -> {
                content.setStyle("-fx-background-color: #f0f0f0;");
            });
            
            setOnMouseExited(e -> {
                content.setStyle("-fx-background-color: transparent;");
            });
        }

        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            
            if (empty || user == null) {
                setGraphic(null);
            } else {
                avatarView.setImage(user.getAvatar());
                usernameLabel.setText(user.getUsername());
                statusLabel.setText(user.getStatus());
                setGraphic(content);
            }
        }
    }
} 