package com.streamapp.network.mdns;

import java.util.Objects;

/**
 * Класс для хранения информации об устройстве в сети.
 */
public class DeviceInfo {
    private final String name;
    private final String hostAddress;
    private final int port;
    private final String username;
    private final String status;
    private final String version;

    /**
     * Создает новый экземпляр информации об устройстве.
     *
     * @param name имя устройства
     * @param hostAddress IP-адрес устройства
     * @param port порт устройства
     * @param username имя пользователя
     * @param status статус устройства
     * @param version версия приложения
     */
    public DeviceInfo(String name, String hostAddress, int port, String username, String status, String version) {
        this.name = name;
        this.hostAddress = hostAddress;
        this.port = port;
        this.username = username;
        this.status = status;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return port == that.port &&
                Objects.equals(name, that.name) &&
                Objects.equals(hostAddress, that.hostAddress) &&
                Objects.equals(username, that.username) &&
                Objects.equals(status, that.status) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hostAddress, port, username, status, version);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", hostAddress='" + hostAddress + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
} 