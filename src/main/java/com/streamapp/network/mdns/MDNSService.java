package com.streamapp.network.mdns;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с mDNS протоколом.
 * Обеспечивает обнаружение и регистрацию устройств в локальной сети.
 */
public class MDNSService {
    private static final Logger logger = LoggerFactory.getLogger(MDNSService.class);
    
    private JmDNS jmdns;
    private final ObservableList<DeviceInfo> discoveredDevices;
    private ServiceInfo serviceInfo;
    private boolean isRegistered;

    /**
     * Создает новый экземпляр MDNS сервиса.
     */
    public MDNSService() {
        this.discoveredDevices = FXCollections.observableArrayList();
        this.isRegistered = false;
    }

    /**
     * Инициализирует mDNS сервис.
     *
     * @throws IOException если произошла ошибка при инициализации
     */
    public void initialize() throws IOException {
        logger.info("Инициализация mDNS сервиса");
        InetAddress localHost = InetAddress.getLocalHost();
        jmdns = JmDNS.create(localHost);
        
        // Добавляем слушатель для обнаружения сервисов
        jmdns.addServiceListener(MDNSConstants.SERVICE_TYPE, new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                logger.info("Обнаружен новый сервис: {}", event.getName());
                jmdns.requestServiceInfo(event.getType(), event.getName());
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                logger.info("Сервис удален: {}", event.getName());
                discoveredDevices.removeIf(device -> device.getName().equals(event.getName()));
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                logger.info("Сервис разрешен: {}", event.getName());
                ServiceInfo info = event.getInfo();
                
                Map<String, String> properties = new HashMap<>();
                Enumeration<String> propertyNames = info.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                    String key = propertyNames.nextElement();
                    properties.put(key, info.getPropertyString(key));
                }

                DeviceInfo deviceInfo = new DeviceInfo(
                    event.getName(),
                    info.getHostAddresses()[0],
                    info.getPort(),
                    properties.getOrDefault(MDNSConstants.TXT_USERNAME, "Unknown"),
                    properties.getOrDefault(MDNSConstants.TXT_STATUS, "Unknown"),
                    properties.getOrDefault(MDNSConstants.TXT_VERSION, "Unknown")
                );

                discoveredDevices.removeIf(device -> device.getName().equals(deviceInfo.getName()));
                discoveredDevices.add(deviceInfo);
            }
        });
    }

    /**
     * Регистрирует сервис в сети.
     *
     * @param username имя пользователя
     * @param status статус пользователя
     * @param version версия приложения
     * @throws IOException если произошла ошибка при регистрации
     */
    public void registerService(String username, String status, String version) throws IOException {
        if (isRegistered) {
            logger.warn("Сервис уже зарегистрирован");
            return;
        }

        logger.info("Регистрация mDNS сервиса");
        Map<String, String> properties = new HashMap<>();
        properties.put(MDNSConstants.TXT_USERNAME, username);
        properties.put(MDNSConstants.TXT_STATUS, status);
        properties.put(MDNSConstants.TXT_VERSION, version);

        serviceInfo = ServiceInfo.create(
            MDNSConstants.SERVICE_TYPE,
            MDNSConstants.DEFAULT_SERVICE_NAME,
            MDNSConstants.DEFAULT_PORT,
            0, 0, properties
        );

        jmdns.registerService(serviceInfo);
        isRegistered = true;
        logger.info("mDNS сервис успешно зарегистрирован");
    }

    /**
     * Отменяет регистрацию сервиса.
     */
    public void unregisterService() {
        if (!isRegistered) {
            logger.warn("Сервис не был зарегистрирован");
            return;
        }

        logger.info("Отмена регистрации mDNS сервиса");
        jmdns.unregisterService(serviceInfo);
        isRegistered = false;
        logger.info("Регистрация mDNS сервиса отменена");
    }

    /**
     * Закрывает mDNS сервис.
     *
     * @throws IOException если произошла ошибка при закрытии
     */
    public void close() throws IOException {
        logger.info("Закрытие mDNS сервиса");
        if (isRegistered) {
            unregisterService();
        }
        jmdns.close();
        logger.info("mDNS сервис успешно закрыт");
    }

    /**
     * Возвращает список обнаруженных устройств.
     *
     * @return список устройств
     */
    public ObservableList<DeviceInfo> getDiscoveredDevices() {
        return discoveredDevices;
    }

    /**
     * Проверяет, зарегистрирован ли сервис.
     *
     * @return true, если сервис зарегистрирован
     */
    public boolean isRegistered() {
        return isRegistered;
    }
} 