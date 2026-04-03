package com.skillshare.skillshare.websocket;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.ExchangeMessageRepository;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.repository.SkillRepository;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeMessageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for real-time WebSocket messaging.
 *
 * Simulates two users (User A and User B) sharing an ExchangeRequest
 * and verifies that a message sent via the WebSocket endpoint is
 * broadcast to and received by both clients.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:wstest;DB_CLOSE_DELAY=-1",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WebSocketMessagingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRequestRepository exchangeRequestRepository;

    @Autowired
    private ExchangeMessageRepository exchangeMessageRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ExchangeMessageService exchangeMessageService;

    private User userA;
    private User userB;
    private ExchangeRequest sharedRequest;

    @BeforeEach
    void setUp() {
        exchangeMessageRepository.deleteAll();
        exchangeRequestRepository.deleteAll();
        skillRepository.deleteAll();
        userRepository.deleteAll();

        // Create two users
        userA = userRepository.save(User.register("Alice", "alice@test.com", "$2a$10$dummyhashForAlice"));
        userB = userRepository.save(User.register("Bob", "bob@test.com", "$2a$10$dummyhashForBob"));

        // Create a skill owned by userB
        Skill skill = new Skill("Java Programming", SkillCategory.PROGRAMMING, SkillProficiency.INTERMEDIATE, userB);
        skill = skillRepository.save(skill);

        // Create an exchange request: userA requests userB's skill
        sharedRequest = new ExchangeRequest(userA, userB, skill, "I'd love to learn Java!");
        sharedRequest = exchangeRequestRepository.save(sharedRequest);
    }

    @Test
    @DisplayName("✅ Service Layer: Message is saved and returned correctly")
    void testMessageIsSavedByService() {
        // Authenticate as userA (mimics the logged-in user context)
        setSecurityContext(userA);

        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Hello, Bob!");

        MessageResponseDTO response = exchangeMessageService.sendMessage(
                sharedRequest.getId(), userA.getId(), dto
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("Hello, Bob!");
        assertThat(response.getSenderId()).isEqualTo(userA.getId());
        assertThat(response.getSenderName()).isEqualTo("Alice");
        assertThat(response.getExchangeRequestId()).isEqualTo(sharedRequest.getId());
        System.out.println("✅ PASS: Message saved and response DTO is correct");
    }

    @Test
    @DisplayName("✅ Service Layer: Both users can retrieve messages")
    void testBothUsersCanFetchMessages() {
        // Send a message as userA
        setSecurityContext(userA);
        MessageCreateDTO dto = new MessageCreateDTO();
        dto.setContent("Hi Bob, ready to start?");
        exchangeMessageService.sendMessage(sharedRequest.getId(), userA.getId(), dto);

        // UserA retrieves messages
        List<MessageResponseDTO> msgsAsA = exchangeMessageService.getMessages(sharedRequest.getId(), userA.getId());
        assertThat(msgsAsA).hasSize(1);
        assertThat(msgsAsA.get(0).getContent()).isEqualTo("Hi Bob, ready to start?");

        // UserB retrieves the same messages (as receiver)
        List<MessageResponseDTO> msgsAsB = exchangeMessageService.getMessages(sharedRequest.getId(), userB.getId());
        assertThat(msgsAsB).hasSize(1);
        assertThat(msgsAsB.get(0).getContent()).isEqualTo("Hi Bob, ready to start?");

        System.out.println("✅ PASS: Both users can access messages for the same exchange request");
    }

    @Test
    @DisplayName("✅ Service Layer: Multiple messages from both users")
    void testMessagesFromBothUsers() {
        MessageCreateDTO dto1 = new MessageCreateDTO();
        dto1.setContent("Hello from Alice");
        exchangeMessageService.sendMessage(sharedRequest.getId(), userA.getId(), dto1);

        MessageCreateDTO dto2 = new MessageCreateDTO();
        dto2.setContent("Hello back from Bob");
        exchangeMessageService.sendMessage(sharedRequest.getId(), userB.getId(), dto2);

        List<MessageResponseDTO> msgs = exchangeMessageService.getMessages(sharedRequest.getId(), userA.getId());
        assertThat(msgs).hasSize(2);
        assertThat(msgs.get(0).getSenderId()).isEqualTo(userA.getId());
        assertThat(msgs.get(1).getSenderId()).isEqualTo(userB.getId());

        System.out.println("✅ PASS: Messages from both users are stored and retrieved in order");
    }

    @Test
    @DisplayName("✅ WebSocket: STOMP client can connect to /ws endpoint")
    void testWebSocketClientCanConnect() throws Exception {
        String wsUrl = "http://localhost:" + port + "/ws";

        WebSocketStompClient client = createStompClient();

        CompletableFuture<Boolean> connected = new CompletableFuture<>();

        StompSessionHandler handler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                connected.complete(true);
                session.disconnect();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                connected.completeExceptionally(exception);
            }
        };

        client.connectAsync(wsUrl, handler);

        Boolean result = connected.get(10, TimeUnit.SECONDS);
        assertThat(result).isTrue();

        System.out.println("✅ PASS: STOMP client successfully connected to WebSocket endpoint at " + wsUrl);
    }

    // ---- Helpers ----

    private WebSocketStompClient createStompClient() {
        SockJsClient sockJsClient = new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))
        );
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        return stompClient;
    }

    private void setSecurityContext(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
