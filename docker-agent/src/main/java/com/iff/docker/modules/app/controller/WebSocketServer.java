/*******************************************************************************
 * Copyright (c) 2020-06-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocketServer
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-03
 */
@Slf4j
//@ServerEndpoint("/ws/{sessionId}")
//@Component
public class WebSocketServer {
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收sessionId
     */
    private String sessionId;

    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, @PathParam("sessionId") String sessionId) throws IOException {
        log.info("发送消息到:" + sessionId + "，报文:" + message);
        if (StringUtils.isNotBlank(sessionId) && webSocketMap.containsKey(sessionId)) {
            webSocketMap.get(sessionId).sendMessage(message);
        } else {
            log.error("用户" + sessionId + ",不在线！");
        }
    }

    public static int getOnlineCount() {
        return onlineCount.get();
    }

    public static int addOnlineCount() {
        return onlineCount.incrementAndGet();
    }

    public static int subOnlineCount() {
        return onlineCount.decrementAndGet();
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        this.session = session;
        this.sessionId = sessionId;
        if (webSocketMap.containsKey(sessionId)) {
            webSocketMap.remove(sessionId);
            webSocketMap.put(sessionId, this);
            //加入set中
        } else {
            webSocketMap.put(sessionId, this);
            //加入set中
            addOnlineCount();
            //在线数加1
        }

        log.info("用户连接:" + sessionId + ",当前在线人数为:" + getOnlineCount());

        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            log.error("用户:" + sessionId + ",网络异常!!!!!!");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(sessionId)) {
            webSocketMap.remove(sessionId);
            //从set中删除
            subOnlineCount();
        }
        log.info("用户退出:" + sessionId + ",当前在线人数为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息:" + sessionId + ",报文:" + message);
        //可以群发消息
        //消息保存到数据库、redis
        if (StringUtils.isNotBlank(message)) {
            try {
                //解析发送的报文
                JSONObject jsonObject = JSON.parseObject(message);
                //追加发送人(防止串改)
                jsonObject.put("fromSessionId", this.sessionId);
                String toSessionId = jsonObject.getString("toSessionId");
                //传送给对应toSessionId用户的websocket
                if (StringUtils.isNotBlank(toSessionId) && webSocketMap.containsKey(toSessionId)) {
                    webSocketMap.get(toSessionId).sendMessage(jsonObject.toJSONString());
                } else {
                    log.error("请求的sessionId:" + toSessionId + "不在该服务器上");
                    //否则不在这个服务器上，发送到mysql或者redis
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.sessionId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
}
