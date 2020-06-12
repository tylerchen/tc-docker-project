/*******************************************************************************
 * Copyright (c) 2020-06-05 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * WsProxyHandler
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-05
 */
@Slf4j
@Component
public class WsProxyHandler extends AbstractWebSocketHandler {
    private final Map<String, JSONObject> proxyClients = new ConcurrentHashMap<>();

    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        getProxyClient(webSocketSession).getObject("session", WebSocketSession.class).sendMessage(webSocketMessage);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        getProxyClient(session);
        log.info("WebSocket Proxy create: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        JSONObject client = proxyClients.get(session.getId());
        if (client != null) {
            WebSocketSession proxy = client.getObject("session", WebSocketSession.class);
            proxy.close();
            proxyClients.remove(session.getId());
            log.info("WebSocket Proxy remove: " + session.getId());
        }
    }

    private JSONObject getProxyClient(WebSocketSession webSocketSession) {
        JSONObject client = proxyClients.get(webSocketSession.getId());
        if (client == null) {
            client = new JSONObject().fluentPut("session", createWebSocketClientSession(webSocketSession));
            proxyClients.put(webSocketSession.getId(), client);
        }
        return client;
    }

    private WebSocketSession createWebSocketClientSession(WebSocketSession webSocketServerSession) {
        try {
            String webSocketQueryString = getWebSocketQueryString(webSocketServerSession);
            String ip = (String) webSocketServerSession.getAttributes().get("ip");
            String port = (String) webSocketServerSession.getAttributes().getOrDefault("port", "12300");
            String uriString = "ws://" + ip + ":" + port + "/agent/ws/container/exec?" + webSocketQueryString;
            log.info("Create WebSocket Proxy to: " + uriString);
            return new StandardWebSocketClient()
                    .doHandshake(new WebSocketProxyClientHandler(webSocketServerSession), new WebSocketHttpHeaders(), URI.create(uriString))
                    .get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getWebSocketQueryString(WebSocketSession webSocketServerSession) {
        Map<String, Object> attributes = webSocketServerSession.getAttributes();
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            try {
                list.add(entry.getKey() + "=" + URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
            } catch (Exception e) {
            }
        }
        return StringUtils.join(list, "&");
    }

    public class WebSocketProxyClientHandler extends AbstractWebSocketHandler {
        private final WebSocketSession webSocketServerSession;

        public WebSocketProxyClientHandler(WebSocketSession webSocketServerSession) {
            this.webSocketServerSession = webSocketServerSession;
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
            webSocketServerSession.sendMessage(webSocketMessage);
        }
    }
}
