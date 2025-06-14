package com.streamapp.network.model;

import java.util.UUID;

/**
 * Класс, представляющий информацию о пользователе в сети
 */
public class User {
    private final UUID id;
    private final String username;
    private final String hostname;
    private final String ipAddress;
    private UserStatus status;

    public User(String username, String hostname, String ipAddress) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.status = UserStatus.OFFLINE;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
} 