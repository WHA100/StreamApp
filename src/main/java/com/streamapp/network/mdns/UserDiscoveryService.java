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
 * Сервис для обнаружения пользователей в локальной сети с использованием JmDNS.
 * Обеспечивает регистрацию и обнаружение пользователей в локальной сети.
 */
public class UserDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(UserDiscoveryService.class);
    
    private final JmDNS jmdns;
    private final ObservableList<User> discoveredUsers;
    private final ConcurrentHashMap<String, User> userMap;
    private final ServiceRegistrationManager registrationManager;
    private boolean isDiscoveryStarted = false;

    /**
     * Создает новый экземпляр сервиса обнаружения пользователей.
     *
     * @param jmdns экземпляр JmDNS
     * @param username имя пользователя для регистрации
     */
    public UserDiscoveryService(JmDNS jmdns, String username) {
        this.jmdns = jmdns;
        this.discoveredUsers = FXCollections.observableArrayList();
        this.userMap = new ConcurrentHashMap<>();
        this.registrationManager = new ServiceRegistrationManager(jmdns);
        
        registerService(username);
    }

    /**
     * Регистрирует сервис в локальной сети.
     *
     * @param username имя пользователя
     */
    private void registerService(String username) {
        try {
            ServiceInfo serviceInfo = ServiceInfo.create(
                MDNSConstants.SERVICE_TYPE,
                username,
                MDNSConstants.DEFAULT_PORT,
                "StreamApp User"
            );
            
            if (!registrationManager.registerService(serviceInfo)) {
                logger.error("Не удалось зарегистрировать сервис для пользователя: {}", username);
            } else {
                logger.info("Сервис успешно зарегистрирован для пользователя: {}", username);
            }
        } catch (Exception e) {
            logger.error("Ошибка при регистрации сервиса: {}", e.getMessage(), e);
        }
    }

    /**
     * Начинает поиск других пользователей в сети.
     * Если поиск уже запущен, метод не выполняет никаких действий.
     */
    public void startDiscovery() {
        if (isDiscoveryStarted) {
            logger.warn("Поиск пользователей уже запущен");
            return;
        }

        try {
            ServiceListener listener = new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent event) {
                    try {
                        logger.debug("Обнаружен новый сервис: {}", event.getName());
                        jmdns.requestServiceInfo(event.getType(), event.getName());
                    } catch (Exception e) {
                        logger.error("Ошибка при запросе информации о сервисе: {}", e.getMessage(), e);
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
                        logger.info("Обнаружен новый пользователь: {} ({})", name, ipAddress);
                    } catch (Exception e) {
                        logger.error("Ошибка при обработке информации о сервисе: {}", e.getMessage(), e);
                    }
                }
            };

            jmdns.addServiceListener(MDNSConstants.SERVICE_TYPE, listener);
            isDiscoveryStarted = true;
            logger.info("Поиск пользователей успешно запущен");
        } catch (Exception e) {
            logger.error("Ошибка при запуске поиска пользователей: {}", e.getMessage(), e);
        }
    }

    /**
     * Возвращает список обнаруженных пользователей.
     *
     * @return список пользователей
     */
    public ObservableList<User> getDiscoveredUsers() {
        return discoveredUsers;
    }

    /**
     * Проверяет, запущен ли поиск пользователей.
     *
     * @return true, если поиск запущен
     */
    public boolean isDiscoveryStarted() {
        return isDiscoveryStarted;
    }

    /**
     * Останавливает сервис и освобождает ресурсы.
     */
    public void shutdown() {
        try {
            if (isDiscoveryStarted) {
                jmdns.removeServiceListener(MDNSConstants.SERVICE_TYPE, null);
                isDiscoveryStarted = false;
            }
            registrationManager.unregisterService();
            registrationManager.close();
            logger.info("Сервис обнаружения успешно остановлен");
        } catch (Exception e) {
            logger.error("Ошибка при остановке сервиса: {}", e.getMessage(), e);
        }
    }
} 