package com.streamapp.network.mdns;

/**
 * Константы для работы с mDNS сервисом.
 */
public final class MDNSConstants {
    private MDNSConstants() {
        // Запрещаем создание экземпляров
    }

    /**
     * Тип сервиса для StreamApp.
     */
    public static final String SERVICE_TYPE = "_streamapp._tcp.local.";

    /**
     * Имя сервиса по умолчанию.
     */
    public static final String DEFAULT_SERVICE_NAME = "StreamApp";

    /**
     * Порт по умолчанию для сервиса.
     */
    public static final int DEFAULT_PORT = 8080;

    /**
     * Ключ для имени пользователя в TXT записи.
     */
    public static final String TXT_USERNAME = "username";

    /**
     * Ключ для статуса пользователя в TXT записи.
     */
    public static final String TXT_STATUS = "status";

    /**
     * Ключ для версии приложения в TXT записи.
     */
    public static final String TXT_VERSION = "version";
} 