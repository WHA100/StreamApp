package com.streamapp.network.mdns;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceRegistrationManagerTest {
    @Mock
    private JmDNS jmDNS;
    @Mock
    private ServiceInfo serviceInfo;
    private ServiceRegistrationManager registrationManager;
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        registrationManager = new ServiceRegistrationManager(jmDNS);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testSuccessfulRegistration() throws IOException {
        // Act
        boolean result = registrationManager.registerService(serviceInfo);
        // Assert
        verify(jmDNS, times(1)).registerService(serviceInfo);
        assertTrue(result);
        assertTrue(registrationManager.isRegistered());
    }

    @Test
    void testRegistrationWithRetry() throws IOException {
        // Arrange
        doThrow(new IOException("Ошибка сети"))
            .doNothing()
            .when(jmDNS).registerService(serviceInfo);
        // Act
        boolean result = registrationManager.registerService(serviceInfo);
        // Assert
        verify(jmDNS, times(2)).registerService(serviceInfo);
        assertTrue(result);
        assertTrue(registrationManager.isRegistered());
    }

    @Test
    void testRegistrationFailure() throws IOException {
        // Arrange
        doThrow(new IOException("Ошибка сети"))
            .when(jmDNS).registerService(serviceInfo);
        // Act
        boolean result = registrationManager.registerService(serviceInfo);
        // Assert
        verify(jmDNS, times(3)).registerService(serviceInfo);
        assertFalse(result);
        assertFalse(registrationManager.isRegistered());
    }

    @Test
    void testUnregisterService() throws IOException {
        // Arrange
        registrationManager.registerService(serviceInfo);
        // Act
        registrationManager.unregisterService();
        // Assert
        verify(jmDNS, times(1)).unregisterService(serviceInfo);
        assertFalse(registrationManager.isRegistered());
    }

    @Test
    void testPreventMultipleRegistrations() throws IOException, ReflectiveOperationException {
        // Arrange
        registrationManager.registerService(serviceInfo);
        // Принудительно выставляем isRegistering в true
        java.lang.reflect.Field field = ServiceRegistrationManager.class.getDeclaredField("isRegistering");
        field.setAccessible(true);
        java.util.concurrent.atomic.AtomicBoolean isRegistering = (java.util.concurrent.atomic.AtomicBoolean) field.get(registrationManager);
        isRegistering.set(true);
        // Act
        boolean result = registrationManager.registerService(serviceInfo);
        // Assert
        assertFalse(result);
    }
} 