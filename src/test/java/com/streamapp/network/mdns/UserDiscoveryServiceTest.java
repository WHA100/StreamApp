package com.streamapp.network.mdns;

import com.streamapp.network.model.User;
import com.streamapp.network.model.UserStatus;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;

class UserDiscoveryServiceTest {
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_SERVICE_TYPE = "_streamapp._tcp.local.";
    private static final int TEST_PORT = 8080;

    @Mock
    private JmDNS mockJmDNS;

    private UserDiscoveryService userDiscoveryService;
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws IOException {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        mockJmDNS = mock(JmDNS.class);
        
        // Создаем реальный сервис с моком JmDNS
        userDiscoveryService = new UserDiscoveryService(mockJmDNS, TEST_USERNAME);
    }

    @AfterEach
    void tearDown() throws Exception {
        userDiscoveryService.shutdown();
        mockitoCloseable.close();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testServiceRegistration() throws IOException {
        // Проверяем, что сервис был зарегистрирован
        verify(mockJmDNS, times(1)).registerService(any(ServiceInfo.class));
        userDiscoveryService.startDiscovery();
        verify(mockJmDNS, times(2)).addServiceListener(eq("_streamapp._tcp.local."), any());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testUserDiscovery() throws IOException {
        // Создаем тестовый ServiceInfo
        ServiceInfo testServiceInfo = mock(ServiceInfo.class);
        when(testServiceInfo.getName()).thenReturn("discoveredUser");
        when(testServiceInfo.getType()).thenReturn(TEST_SERVICE_TYPE);
        when(testServiceInfo.getInetAddresses()).thenReturn(new java.net.InetAddress[]{java.net.InetAddress.getLocalHost()});

        // Симулируем обнаружение нового пользователя
        ServiceEvent serviceEvent = mock(ServiceEvent.class);
        when(serviceEvent.getInfo()).thenReturn(testServiceInfo);
        when(serviceEvent.getType()).thenReturn(TEST_SERVICE_TYPE);
        when(serviceEvent.getName()).thenReturn("discoveredUser");

        // Получаем список пользователей
        ObservableList<User> users = userDiscoveryService.getDiscoveredUsers();
        assertTrue(users.isEmpty(), "Список пользователей должен быть пустым в начале");

        // Симулируем события обнаружения сервиса
        userDiscoveryService.startDiscovery();
        verify(mockJmDNS, times(2)).addServiceListener(eq("_streamapp._tcp.local."), any());

        // Получаем ServiceListener и вызываем его методы
        ServiceListener listener = getServiceListener();
        listener.serviceAdded(serviceEvent);
        listener.serviceResolved(serviceEvent);

        // Проверяем, что пользователь был добавлен в список
        assertEquals(1, users.size(), "Должен быть обнаружен один пользователь");
        User discoveredUser = users.get(0);
        assertEquals("discoveredUser", discoveredUser.getUsername());
        assertEquals(UserStatus.ONLINE, discoveredUser.getStatus());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testUserDisconnection() throws IOException {
        // Создаем тестовый ServiceInfo
        ServiceInfo testServiceInfo = mock(ServiceInfo.class);
        when(testServiceInfo.getName()).thenReturn("discoveredUser");
        when(testServiceInfo.getType()).thenReturn(TEST_SERVICE_TYPE);

        // Симулируем обнаружение пользователя
        ServiceEvent addEvent = mock(ServiceEvent.class);
        when(addEvent.getInfo()).thenReturn(testServiceInfo);
        when(addEvent.getType()).thenReturn(TEST_SERVICE_TYPE);
        when(addEvent.getName()).thenReturn("discoveredUser");

        // Симулируем отключение пользователя
        ServiceEvent removeEvent = mock(ServiceEvent.class);
        when(removeEvent.getType()).thenReturn(TEST_SERVICE_TYPE);
        when(removeEvent.getName()).thenReturn("discoveredUser");

        // Получаем список пользователей
        ObservableList<User> users = userDiscoveryService.getDiscoveredUsers();

        // Симулируем события
        userDiscoveryService.startDiscovery();
        verify(mockJmDNS, times(2)).addServiceListener(eq("_streamapp._tcp.local."), any());

        // Проверяем, что пользователь был удален из списка
        assertEquals(0, users.size(), "Список пользователей должен быть пустым после отключения");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testShutdown() throws IOException {
        // Вызываем shutdown
        userDiscoveryService.shutdown();
        // Проверяем, что unregisterService был вызван
        verify(mockJmDNS, atLeastOnce()).unregisterService(any(ServiceInfo.class));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleUsers() throws IOException {
        // Создаем несколько тестовых ServiceInfo
        String[] usernames = {"user1", "user2", "user3"};
        ServiceInfo[] testServices = new ServiceInfo[usernames.length];
        ServiceEvent[] serviceEvents = new ServiceEvent[usernames.length];

        for (int i = 0; i < usernames.length; i++) {
            ServiceInfo serviceInfo = mock(ServiceInfo.class);
            when(serviceInfo.getName()).thenReturn(usernames[i]);
            when(serviceInfo.getType()).thenReturn(TEST_SERVICE_TYPE);
            when(serviceInfo.getInetAddresses()).thenReturn(new java.net.InetAddress[]{java.net.InetAddress.getLocalHost()});
            testServices[i] = serviceInfo;

            ServiceEvent serviceEvent = mock(ServiceEvent.class);
            when(serviceEvent.getInfo()).thenReturn(serviceInfo);
            when(serviceEvent.getType()).thenReturn(TEST_SERVICE_TYPE);
            when(serviceEvent.getName()).thenReturn(usernames[i]);
            serviceEvents[i] = serviceEvent;
        }

        // Получаем список пользователей
        ObservableList<User> users = userDiscoveryService.getDiscoveredUsers();

        // Симулируем события
        userDiscoveryService.startDiscovery();
        verify(mockJmDNS, times(2)).addServiceListener(eq("_streamapp._tcp.local."), any());

        // Получаем ServiceListener и вызываем его методы для каждого пользователя
        ServiceListener listener = getServiceListener();
        for (ServiceEvent event : serviceEvents) {
            listener.serviceAdded(event);
            listener.serviceResolved(event);
        }

        // Проверяем, что все пользователи были добавлены
        assertEquals(3, users.size(), "Должны быть обнаружены три пользователя");
        
        // Проверяем, что все пользователи имеют правильный статус
        for (User user : users) {
            assertEquals(UserStatus.ONLINE, user.getStatus());
        }
    }

    private ServiceListener getServiceListener() {
        return (ServiceListener) getLastArgument(mockJmDNS, "addServiceListener");
    }

    private Object getLastArgument(Object mock, String methodName) {
        return getLastInvocation(mock, methodName).getArguments()[1];
    }

    private org.mockito.invocation.Invocation getLastInvocation(Object mock, String methodName) {
        return org.mockito.Mockito.mockingDetails(mock).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals(methodName))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No invocation found for method: " + methodName));
    }
} 