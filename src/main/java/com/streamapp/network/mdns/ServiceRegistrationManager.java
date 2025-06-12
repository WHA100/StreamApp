package com.streamapp.network.mdns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jmdns.ServiceInfo;
import javax.jmdns.JmDNS;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Менеджер регистрации сервисов mDNS
 */
public class ServiceRegistrationManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistrationManager.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int MONITOR_INTERVAL_MS = 5000;
    private static final int INITIAL_DELAY_MS = 2000;

    private final JmDNS jmdns;
    private final AtomicBoolean isRegistered = new AtomicBoolean(false);
    private final AtomicBoolean isRegistering = new AtomicBoolean(false);
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ServiceInfo currentService;

    public ServiceRegistrationManager(JmDNS jmdns) {
        this.jmdns = jmdns;
    }

    /**
     * Проверяет доступность порта
     */
    public boolean isPortAvailable(int port) {
        try {
            return jmdns.getInetAddress().isReachable(port);
        } catch (IOException e) {
            logger.error("Ошибка при проверке порта: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Регистрирует сервис с обработкой ошибок и повторными попытками
     */
    public boolean registerService(ServiceInfo serviceInfo) {
        if (isRegistering.get()) {
            logger.warn("Регистрация уже выполняется");
            return false;
        }

        isRegistering.set(true);
        retryCount.set(0);
        currentService = serviceInfo;

        try {
            // Даем время на инициализацию
            Thread.sleep(INITIAL_DELAY_MS);
            
            boolean success = doRegisterService(serviceInfo);
            if (success) {
                startMonitoring();
            }
            return success;
        } catch (InterruptedException e) {
            logger.error("Регистрация прервана: {}", e.getMessage());
            return false;
        } finally {
            isRegistering.set(false);
        }
    }

    private boolean doRegisterService(ServiceInfo serviceInfo) {
        while (retryCount.get() < MAX_RETRIES) {
            try {
                jmdns.registerService(serviceInfo);
                isRegistered.set(true);
                logger.info("Сервис успешно зарегистрирован с именем: {}", serviceInfo.getName());
                return true;
            } catch (IOException e) {
                retryCount.incrementAndGet();
                if (retryCount.get() < MAX_RETRIES) {
                    logger.error("Ошибка при регистрации сервиса (попытка {}/{}): {}", 
                        retryCount.get(), MAX_RETRIES, e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!isRegistered.get() || currentService == null) {
                return;
            }

            try {
                // Проверяем, что сервис все еще зарегистрирован
                ServiceInfo[] services = jmdns.list(currentService.getType());
                boolean found = false;
                for (ServiceInfo service : services) {
                    if (service.getName().equals(currentService.getName())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    logger.warn("Обнаружена потеря регистрации сервиса");
                    if (!isRegistering.get()) {
                        isRegistered.set(false);
                        doRegisterService(currentService);
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при мониторинге сервиса: {}", e.getMessage());
            }
        }, MONITOR_INTERVAL_MS, MONITOR_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Отменяет регистрацию сервиса
     */
    public void unregisterService() {
        if (currentService != null) {
            try {
                jmdns.unregisterService(currentService);
                isRegistered.set(false);
                currentService = null;
                logger.info("Регистрация сервиса отменена");
            } catch (Exception e) {
                logger.error("Ошибка при отмене регистрации: {}", e.getMessage());
            }
        }
    }

    /**
     * Проверяет, зарегистрирован ли сервис
     */
    public boolean isRegistered() {
        return isRegistered.get();
    }

    /**
     * Закрывает менеджер и освобождает ресурсы
     */
    public void close() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 