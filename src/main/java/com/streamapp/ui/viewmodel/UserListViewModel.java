package com.streamapp.ui.viewmodel;

import com.streamapp.core.model.User;
import com.streamapp.network.mdns.UserDiscoveryService;
import com.streamapp.network.model.UserStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javax.jmdns.JmDNS;
import java.io.IOException;
import java.util.UUID;

/**
 * ViewModel для управления списком пользователей
 */
public class UserListViewModel {
    private final ObservableList<User> users;
    private UserDiscoveryService discoveryService;

    public UserListViewModel() {
        this.users = FXCollections.observableArrayList();
        // Добавляем тестовых пользователей для демонстрации
        addTestUsers();
        // Инициализируем сервис обнаружения пользователей
        initDiscovery();
    }

    private void addTestUsers() {
        // В реальном приложении эти данные будут приходить с сервера
        users.add(new User(
            UUID.randomUUID().toString(),
            "Иван Петров",
            "Онлайн",
            new Image("/images/default_avatar.png")
        ));
        users.add(new User(
            UUID.randomUUID().toString(),
            "Мария Сидорова",
            "Не беспокоить",
            new Image("/images/default_avatar.png")
        ));
    }

    private void initDiscovery() {
        try {
            // Здесь можно получить имя пользователя из настроек приложения
            String username = System.getProperty("user.name", "StreamUser");
            JmDNS jmdns = JmDNS.create();
            discoveryService = new UserDiscoveryService(jmdns, username);
            discoveryService.startDiscovery();
            // Слушаем изменения в списке обнаруженных пользователей
            discoveryService.getDiscoveredUsers().addListener((ListChangeListener<com.streamapp.network.model.User>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (com.streamapp.network.model.User netUser : change.getAddedSubList()) {
                            Platform.runLater(() -> addOrUpdateUserFromNetwork(netUser));
                        }
                    }
                    if (change.wasRemoved()) {
                        for (com.streamapp.network.model.User netUser : change.getRemoved()) {
                            Platform.runLater(() -> removeUserByNetwork(netUser));
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addOrUpdateUserFromNetwork(com.streamapp.network.model.User netUser) {
        // Проверяем, есть ли уже такой пользователь по username и ip
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equals(netUser.getUsername()) && u.getUserId().equals(netUser.getIpAddress()));
        if (!exists) {
            users.add(new User(
                netUser.getIpAddress(), // userId
                netUser.getUsername(),
                netUser.getStatus() == UserStatus.ONLINE ? "Онлайн" : "Оффлайн",
                new Image("/images/default_avatar.png")
            ));
        }
    }

    private void removeUserByNetwork(com.streamapp.network.model.User netUser) {
        users.removeIf(u -> u.getUsername().equals(netUser.getUsername()) && u.getUserId().equals(netUser.getIpAddress()));
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(String userId) {
        users.removeIf(user -> user.getUserId().equals(userId));
    }

    public void updateUserStatus(String userId, String newStatus) {
        users.stream()
            .filter(user -> user.getUserId().equals(userId))
            .findFirst()
            .ifPresent(user -> user.setStatus(newStatus));
    }
} 