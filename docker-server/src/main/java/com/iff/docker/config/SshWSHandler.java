package com.iff.docker.config;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接容器执行命令.
 */
@Component
public class SshWSHandler extends TextWebSocketHandler {
    private final Map<String, JSONObject> sshMap = new ConcurrentHashMap();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //获得传参
        String ip = session.getAttributes().get("ip").toString();
        String port = session.getAttributes().get("port").toString();
        String username = session.getAttributes().get("username").toString();
        String password = session.getAttributes().get("password").toString();
        if (StringUtils.isEmpty(port)) {
            port = "22";
        }
        JSch jsch = new JSch();
        //将这个ssh连接信息放入map中
        JSONObject json = new JSONObject().fluentPut("ip", ip).fluentPut("port", port).fluentPut("username", username).fluentPut("password", password);
        JSONObject allData = new JSONObject().fluentPut("jsch", jsch).fluentPut("session", session).fluentPut("auth", json);
        sshMap.put(ip, allData);
        //连接bash
        connectToSSH(jsch, session, allData);
    }

    private void transToSSH(Channel channel, String command) throws IOException {
        if (channel != null) {
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(command.getBytes());
            outputStream.flush();
        }
    }

    private void connectToSSH(JSch jsch, WebSocketSession webSocketSession, JSONObject allData) throws Exception {
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        //获取jsch的会话
        JSONObject auth = allData.getJSONObject("auth");
        Session session = jsch.getSession(auth.getString("username"), auth.getString("ip"), auth.getInteger("port"));
        session.setConfig(config);
        //设置密码
        session.setPassword(auth.getString("password"));
        //连接  超时时间30s
        session.connect(30000);
        //开启shell通道
        Channel channel = session.openChannel("shell");
        //通道连接 超时时间3s
        channel.connect(3000);
        //设置channel
        allData.fluentPut("channel", channel);
        //转发消息
        //transToSSH(channel, "\r");

        //读取终端返回的信息流
        InputStream inputStream = channel.getInputStream();
        OutPutThread thread = new OutPutThread(inputStream, webSocketSession);
        thread.start();
        allData.fluentPut("outPutThread", thread);
    }

    /**
     * websocket关闭后关闭线程.
     *
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String ip = session.getAttributes().get("ip").toString();
        JSONObject allData = sshMap.get(ip);
        if (allData != null && allData.getObject("channel", Channel.class) != null) {
            allData.getObject("outPutThread", Thread.class).interrupt();
            allData.getObject("channel", Channel.class).disconnect();
        }
        sshMap.remove(ip);
    }

    /**
     * 获得先输入.
     *
     * @param session
     * @param message 输入信息
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String ip = session.getAttributes().get("ip").toString();
        JSONObject allData = sshMap.get(ip);
        if (allData != null && allData.getObject("channel", Channel.class) != null) {
            Channel channel = allData.getObject("channel", Channel.class);
            OutputStream outputStream = channel.getOutputStream();
            outputStream.write(message.asBytes());
            outputStream.flush();
        }
    }
}