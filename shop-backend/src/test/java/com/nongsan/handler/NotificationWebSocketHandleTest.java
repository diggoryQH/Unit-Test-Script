package com.nongsan.handler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

class NotificationWebSocketHandleTest {

	private NotificationWebSocketHandle handler;
	private WebSocketSession sessionOne;
	private WebSocketSession sessionTwo;

	@BeforeEach
	void setUp() {
		handler = new NotificationWebSocketHandle();
		sessionOne = mock(WebSocketSession.class);
		sessionTwo = mock(WebSocketSession.class);
	}

	@Test
	void handleTextMessage_shouldBroadcastToAllConnectedSessions() throws Exception {
		handler.afterConnectionEstablished(sessionOne);
		handler.afterConnectionEstablished(sessionTwo);

		TextMessage message = new TextMessage("{\"user\":\"Alice\",\"message\":\"hello\"}");
		handler.handleTextMessage(sessionOne, message);

		verify(sessionOne).sendMessage(message);
		verify(sessionTwo).sendMessage(message);
	}

	@Test
	void afterConnectionClosed_shouldStopBroadcastingToClosedSession() throws Exception {
		handler.afterConnectionEstablished(sessionOne);
		handler.afterConnectionEstablished(sessionTwo);
		handler.afterConnectionClosed(sessionOne, CloseStatus.NORMAL);

		TextMessage message = new TextMessage("{\"user\":\"Alice\",\"message\":\"hello\"}");
		handler.handleTextMessage(sessionTwo, message);

		verify(sessionOne, never()).sendMessage(any());
		verify(sessionTwo).sendMessage(message);
	}
}
