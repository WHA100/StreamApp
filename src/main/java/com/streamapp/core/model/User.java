package com.streamapp.core.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * Модель пользователя для отображения в списке
 */
public class User {
    private final StringProperty username;
    private final StringProperty status;
    private final ObjectProperty<Image> avatar;
    private final StringProperty userId;

    public User(String userId, String username, String status, Image avatar) {
        this.userId = new SimpleStringProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.status = new SimpleStringProperty(status);
        this.avatar = new SimpleObjectProperty<>(avatar);
    }

    // Геттеры и сеттеры
    public String getUserId() {
        return userId.get();
    }

    public StringProperty userIdProperty() {
        return userId;
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public Image getAvatar() {
        return avatar.get();
    }

    public ObjectProperty<Image> avatarProperty() {
        return avatar;
    }

    public void setAvatar(Image avatar) {
        this.avatar.set(avatar);
    }
} 