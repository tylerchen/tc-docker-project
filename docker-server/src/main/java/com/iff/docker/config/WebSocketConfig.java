/*******************************************************************************
 * Copyright (c) 2020-06-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * WebSocketConfig
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-03
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    ContainerExecWSHandler containerExecWSHandler;
    @Autowired
    SshWSHandler sshWSHandler;
    @Autowired
    WsProxyHandler wsProxyHandler;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(containerExecWSHandler, "/ws/container/exec").addInterceptors(new WsHandshakeInterceptor()).setAllowedOrigins("*");
        registry.addHandler(sshWSHandler, "/ws/ssh").addInterceptors(new WsHandshakeInterceptor()).setAllowedOrigins("*");
        registry.addHandler(wsProxyHandler, "/ws/proxy").addInterceptors(new WsHandshakeInterceptor()).setAllowedOrigins("*");
    }

    public class WsHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            if (request.getHeaders().containsKey("Sec-WebSocket-Extensions")) {
                request.getHeaders().set("Sec-WebSocket-Extensions", "permessage-deflate");
            }
            HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
            Enumeration<String> parameterNames = req.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String name = parameterNames.nextElement();
                String value = req.getParameter(name);
                if (value != null) {
                    attributes.put(name, value);
                }
            }
            return super.beforeHandshake(request, response, wsHandler, attributes);
        }
    }
}
