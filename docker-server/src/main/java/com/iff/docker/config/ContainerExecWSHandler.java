package com.iff.docker.config;

import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接容器执行命令.
 */
@Component
public class ContainerExecWSHandler extends TextWebSocketHandler {
    private final Map<String, JSONObject> execSessionMap = new ConcurrentHashMap();
    @Autowired
    DockerClientConfig config;

    DockerClient client() {
        return DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(new NettyDockerCmdExecFactory()).build();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //获得传参
        String containerId = session.getAttributes().get("containerId").toString();
        //String width = session.getAttributes().get("width").toString();
        //String height = session.getAttributes().get("height").toString();
        //创建bash
        String cmd = StringUtils.defaultString((String) session.getAttributes().get("cmd"), "sh");
        String execId = createExec(containerId, cmd);
        //连接bash
        Socket socket = null;
        if ("unix".equalsIgnoreCase(config.getDockerHost().getScheme())) {
            socket = connectExecByUnixSocket(execId);
        } else {
            socket = connectExec(config.getDockerHost().getHost(), config.getDockerHost().getPort(), execId);
        }
        //获得输出
        getExecMessage(session, containerId, socket);
        //修改tty大小
        //resizeTty(ip, width, height, execId);
    }

    /**
     * 创建bash.
     *
     * @param containerId 容器id
     * @return 命令id
     * @throws Exception
     */
    private String createExec(String containerId, String cmd) throws Exception {
        try (DockerClient client = client()) {
            return client.execCreateCmd(containerId).withAttachStdout(true).withAttachStderr(true).withAttachStdin(true).withTty(true).withCmd(cmd).exec().getId();
        }
    }

    /**
     * 连接bash.
     *
     * @param execId 命令id
     * @return 连接的socket
     * @throws IOException
     */
    private Socket connectExec(String host, int port, String execId) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setKeepAlive(true);
        OutputStream out = socket.getOutputStream();
        StringBuffer pw = new StringBuffer();
        {
            pw.append("POST /exec/" + execId + "/start HTTP/1.1\r\n");
            pw.append("Host: " + host + ":" + port + "\r\n");
            pw.append("User-Agent: Docker-Client\r\n");
            pw.append("Content-Type: application/json\r\n");
            pw.append("Connection: Upgrade\r\n");
            JSONObject obj = new JSONObject();
            {
                obj.put("Detach", false);
                obj.put("Tty", true);
            }
            String json = obj.toJSONString();
            pw.append("Content-Length: " + json.length() + "\r\n");
            pw.append("Upgrade: tcp\r\n");
            pw.append("\r\n");
            pw.append(json);
        }
        out.write(pw.toString().getBytes("UTF-8"));
        out.flush();
        return socket;
    }

    private Socket connectExecByUnixSocket(String execId) throws IOException {
        UnixSocketAddress address = new UnixSocketAddress("/var/run/docker.sock");
        UnixSocketChannel channel = UnixSocketChannel.open(address);
        Socket socket = new UnixSocket(channel);
        socket.setKeepAlive(true);
        OutputStream out = socket.getOutputStream();
        StringBuffer pw = new StringBuffer();
        {
            pw.append("POST /exec/" + execId + "/start HTTP/1.1\r\n");
            pw.append("Host: http\r\n");
            pw.append("User-Agent: Docker-Client\r\n");
            pw.append("Content-Type: application/json\r\n");
            pw.append("Connection: Upgrade\r\n");
            JSONObject obj = new JSONObject();
            {
                obj.put("Detach", false);
                obj.put("Tty", true);
            }
            String json = obj.toJSONString();
            pw.append("Content-Length: " + json.length() + "\r\n");
            pw.append("Upgrade: tcp\r\n");
            pw.append("\r\n");
            pw.append(json);
        }
        out.write(pw.toString().getBytes("UTF-8"));
        out.flush();
        return socket;
    }

    /**
     * 获得输出.
     *
     * @param session     websocket session
     * @param containerId 容器id
     * @param socket      命令连接socket
     * @throws IOException
     */
    private void getExecMessage(WebSocketSession session, String containerId, Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        StringBuffer returnMsg = new StringBuffer();
        while (true) {
            int n = inputStream.read(bytes);
            if (n < 0) {
                break;
            }
            String msg = new String(bytes, 0, n);
            returnMsg.append(msg);
            bytes = new byte[10240];
            if (returnMsg.indexOf("\r\n\r\n") != -1) {
                session.sendMessage(new TextMessage(returnMsg.substring(returnMsg.indexOf("\r\n\r\n") + 4, returnMsg.length())));
                break;
            }
        }
        OutPutThread outPutThread = new OutPutThread(inputStream, session);
        outPutThread.start();
        execSessionMap.put(containerId, new JSONObject().fluentPut("containerId", containerId).fluentPut("socket", socket).fluentPut("outPutThread", outPutThread));
    }

    /**
     * 修改tty大小.
     *
     * @param ip
     * @param width
     * @param height
     * @param execId
     * @throws Exception
     */
    private void resizeTty(String ip, String width, String height, String execId) throws Exception {
//        DockerHelper.execute(ip, docker -> {
//            docker.execResizeTty(execId, Integer.parseInt(height), Integer.parseInt(width));
//        });
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
        String containerId = session.getAttributes().get("containerId").toString();
        JSONObject execSession = execSessionMap.get(containerId);
        if (execSession != null) {
            execSession.getObject("outPutThread", OutPutThread.class).interrupt();
            execSession.getObject("socket", Socket.class).close();
        }
        execSessionMap.remove(containerId);
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
        String containerId = session.getAttributes().get("containerId").toString();
        JSONObject execSession = execSessionMap.get(containerId);
        OutputStream out = execSession.getObject("socket", Socket.class).getOutputStream();
        out.write(message.asBytes());
        out.flush();
    }
}