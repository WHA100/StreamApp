package com.streamapp.network.mdns;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для MDNS сервиса.
 */
class MDNSServiceTest {
    private MDNSService mdnsService;

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
    void testServiceRegistration() throws IOException {
        // Регистрируем сервис
        mdnsService.registerService("testUser", "online", "1.0.0");
        assertTrue(mdnsService.isRegistered());

        // Отменяем регистрацию
        mdnsService.unregisterService();
        assertFalse(mdnsService.isRegistered());
    }

    @Test
    void testDeviceDiscovery() throws IOException, InterruptedException {
        // Создаем второй экземпляр сервиса для эмуляции другого устройства
        MDNSService secondService = new MDNSService();
        secondService.initialize();

        // Регистрируем второе устройство
        secondService.registerService("secondUser", "online", "1.0.0");

        // Ждем обнаружения устройства
        CountDownLatch latch = new CountDownLatch(1);
        Thread discoveryThread = new Thread(() -> {
            while (mdnsService.getDiscoveredDevices().isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            latch.countDown();
        });
        discoveryThread.start();

        // Проверяем, что устройство было обнаружено в течение 5 секунд
        boolean discovered = latch.await(5, TimeUnit.SECONDS);
        assertTrue(discovered, "Устройство не было обнаружено в течение 5 секунд");

        // Проверяем информацию об обнаруженном устройстве
        assertFalse(mdnsService.getDiscoveredDevices().isEmpty());
        DeviceInfo device = mdnsService.getDiscoveredDevices().get(0);
        assertEquals("secondUser", device.getUsername());
        assertEquals("online", device.getStatus());
        assertEquals("1.0.0", device.getVersion());

        // Закрываем второе устройство
        secondService.close();
    }

    @Test
    void testServiceUnregistration() throws IOException, InterruptedException {
        // Регистрируем сервис
        mdnsService.registerService("testUser", "online", "1.0.0");

        // Создаем второй экземпляр сервиса
        MDNSService secondService = new MDNSService();
        secondService.initialize();

        // Ждем обнаружения устройства
        CountDownLatch discoveryLatch = new CountDownLatch(1);
        Thread discoveryThread = new Thread(() -> {
            while (secondService.getDiscoveredDevices().isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            discoveryLatch.countDown();
        });
        discoveryThread.start();
        discoveryLatch.await(5, TimeUnit.SECONDS);

        // Отменяем регистрацию первого сервиса
        mdnsService.unregisterService();

        // Ждем удаления устройства
        CountDownLatch removalLatch = new CountDownLatch(1);
        Thread removalThread = new Thread(() -> {
            while (!secondService.getDiscoveredDevices().isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            removalLatch.countDown();
        });
        removalThread.start();

        // Проверяем, что устройство было удалено в течение 5 секунд
        boolean removed = removalLatch.await(5, TimeUnit.SECONDS);
        assertTrue(removed, "Устройство не было удалено в течение 5 секунд");

        // Закрываем второй сервис
        secondService.close();
    }
} 