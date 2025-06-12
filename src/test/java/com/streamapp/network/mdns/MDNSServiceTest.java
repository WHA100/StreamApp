package com.streamapp.network.mdns;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для MDNS сервиса
 */
public class MDNSServiceTest {
    private MDNSService mdnsService;
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_STATUS = "active";
    private static final String TEST_VERSION = "1.0";

    @BeforeEach
    void setUp() throws IOException {
        mdnsService = new MDNSService();
        mdnsService.initialize();
    }

    @AfterEach
    void tearDown() throws IOException {
        mdnsService.close();
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testServiceRegistration() throws IOException, InterruptedException {
        // Регистрируем сервис
        mdnsService.registerService(TEST_USERNAME, TEST_STATUS, TEST_VERSION);
        
        // Даем время на обработку регистрации
        Thread.sleep(2000);
        
        // Проверяем статус регистрации
        assertTrue(mdnsService.isRegistered(), "Сервис должен быть зарегистрирован");
        
        // Проверяем, что сервис остается зарегистрированным
        Thread.sleep(5000);
        assertTrue(mdnsService.isRegistered(), "Сервис должен оставаться зарегистрированным");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testDeviceDiscovery() throws IOException, InterruptedException {
        // Регистрируем первый сервис
        mdnsService.registerService(TEST_USERNAME, TEST_STATUS, TEST_VERSION);
        
        // Даем время на обработку регистрации
        Thread.sleep(2000);
        
        // Создаем второй сервис для обнаружения
        MDNSService secondService = new MDNSService();
        secondService.initialize();
        try {
            // Регистрируем второй сервис
            secondService.registerService("secondUser", "active", "1.0");
            
            // Даем время на обнаружение
            Thread.sleep(5000);
            
            // Проверяем, что устройство обнаружено
            assertTrue(mdnsService.getDiscoveredDevices().stream()
                .anyMatch(device -> device.getUsername().equals("secondUser")), 
                "Устройство должно быть обнаружено в течение 10 секунд");
        } finally {
            secondService.close();
        }
    }

    @Test
    void testUnregisterService() throws IOException, InterruptedException {
        // Регистрируем сервис
        mdnsService.registerService(TEST_USERNAME, TEST_STATUS, TEST_VERSION);
        
        // Даем время на обработку регистрации
        Thread.sleep(2000);
        
        // Отменяем регистрацию
        mdnsService.unregisterService();
        
        // Проверяем, что сервис отрегистрирован
        assertFalse(mdnsService.isRegistered(), "Сервис должен быть отрегистрирован");
    }
} 