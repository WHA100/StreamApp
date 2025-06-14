package com.streamapp.network.mdns;

import com.streamapp.network.model.User;
import com.streamapp.network.model.UserStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для обнаружения пользователей в локальной сети с использованием JmDNS
 */
public class UserDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(UserDiscoveryService.class);
    private static final String SERVICE_TYPE = "_streamapp._tcp.local.";
    
    private final JmDNS jmdns;
    private final ObservableList<User> discoveredUsers;
    private final ConcurrentHashMap<String, User> userMap;
    private final ServiceRegistrationManager registrationManager;

    public UserDiscoveryService(JmDNS jmdns, String username) {
        this.jmdns = jmdns;
        this.discoveredUsers = FXCollections.observableArrayList();
        this.userMap = new ConcurrentHashMap<>();
        this.registrationManager = new ServiceRegistrationManager(jmdns);
        
        registerService(username);
        // startDiscovery(); // Удалено, чтобы не вызывать автоматически
    }

    /**
     * Регистрирует сервис в локальной сети
     */
    private void registerService(String username) {
        try {
            ServiceInfo serviceInfo = ServiceInfo.create(
                SERVICE_TYPE,
                username,
                8080,
                "StreamApp User"
            );
            
            if (!registrationManager.registerService(serviceInfo)) {
                logger.error("Не удалось зарегистрировать сервис для пользователя: {}", username);
            }
        } catch (Exception e) {
            logger.error("Ошибка при регистрации сервиса: {}", e.getMessage());
        }
    }

    /**
     * Начинает поиск других пользователей в сети
     */
    public void startDiscovery() {
        try {
            // Создаем два разных экземпляра слушателя
            ServiceListener listener1 = new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent event) {
                    try {
                        jmdns.requestServiceInfo(event.getType(), event.getName());
                    } catch (Exception e) {
                        logger.error("Ошибка при запросе информации о сервисе: {}", e.getMessage());
                    }
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    String name = event.getName();
                    User user = userMap.remove(name);
                    if (user != null) {
                        discoveredUsers.remove(user);
                        logger.info("Пользователь отключился: {}", name);
                    }
                }

                @Override
                public void serviceResolved(ServiceEvent event) {
                    try {
                        ServiceInfo info = event.getInfo();
                        String name = info.getName();
                        String ipAddress = info.getInetAddresses()[0].getHostAddress();
                        
                        User user = new User(name, name, ipAddress);
                        user.setStatus(UserStatus.ONLINE);
                        
                        userMap.put(name, user);
                        discoveredUsers.add(user);
                        logger.info("Обнаружен новый пользователь: {}", name);
                    } catch (Exception e) {
                        logger.error("Ошибка при обработке информации о сервисе: {}", e.getMessage());
                    }
                }
            };

            ServiceListener listener2 = new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent event) {
                    try {
                        jmdns.requestServiceInfo(event.getType(), event.getName());
                    } catch (Exception e) {
                        logger.error("Ошибка при запросе информации о сервисе: {}", e.getMessage());
                    }
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    String name = event.getName();
                    User user = userMap.remove(name);
                    if (user != null) {
                        discoveredUsers.remove(user);
                        logger.info("Пользователь отключился: {}", name);
                    }
                }

                @Override
                public void serviceResolved(ServiceEvent event) {
                    try {
                        ServiceInfo info = event.getInfo();
                        String name = info.getName();
                        String ipAddress = info.getInetAddresses()[0].getHostAddress();
                        
                        User user = new User(name, name, ipAddress);
                        user.setStatus(UserStatus.ONLINE);
                        
                        userMap.put(name, user);
                        discoveredUsers.add(user);
                        logger.info("Обнаружен новый пользователь: {}", name);
                    } catch (Exception e) {
                        logger.error("Ошибка при обработке информации о сервисе: {}", e.getMessage());
                    }
                }
            };

            // Регистрируем два разных слушателя
            jmdns.addServiceListener(SERVICE_TYPE, listener1);
            jmdns.addServiceListener(SERVICE_TYPE, listener2);
        } catch (Exception e) {
            logger.error("Ошибка при запуске поиска пользователей: {}", e.getMessage());
        }
    }

    /**
     * Возвращает список обнаруженных пользователей
     */
    public ObservableList<User> getDiscoveredUsers() {
        return discoveredUsers;
    }

    /**
     * Останавливает сервис и освобождает ресурсы
     */
    public void shutdown() {
        try {
            registrationManager.unregisterService();
            registrationManager.close();
            logger.info("Сервис обнаружения остановлен");
        } catch (Exception e) {
            logger.error("Ошибка при остановке сервиса: {}", e.getMessage());
        }
    }
} 