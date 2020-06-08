/*******************************************************************************
 * Copyright (c) 2020-06-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.iff.docker.config.RsaConfig;
import com.iff.docker.modules.app.vo.form.ContainerFormVO;
import com.iff.docker.modules.app.vo.form.ExecFormVO;
import com.iff.docker.modules.common.ResultBean;
import com.iff.docker.modules.util.RSAHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * DockerProxyController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Slf4j
@Service
public class DockerProxyService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RsaConfig rsaConfig;

    public static String urlDecode(String url) {
        if (url != null && url.length() > 0) {
            try {
                return URLDecoder.decode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String urlEncode(String url) {
        if (url != null && url.length() > 0) {
            try {
                return URLEncoder.encode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    HttpEntity<String> httpEntity() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("sign", RSAHelper.encryptToHex(String.valueOf(System.currentTimeMillis()), rsaConfig.getPubKey()));
        return new HttpEntity<String>(null, requestHeaders);
    }

    <T> HttpEntity<T> httpEntity(T body) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("sign", RSAHelper.encryptToHex(String.valueOf(System.currentTimeMillis()), rsaConfig.getPubKey()));
        return new HttpEntity<T>(body, requestHeaders);
    }

    String queryString(Object... params) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            Object name = params[i];
            Object value = params[++i];
            if (value != null) {
                list.add(name + "=" + urlEncode(String.valueOf(value)));
            }
        }
        return StringUtils.join(list, "&");
    }

    //Filters to process on the container list, encoded as JSON (a map[string][]string). For example, {"status": ["paused"]} will only return paused containers. Available filters:
    //ancestor=(<image-name>[:<tag>], <image id>, or <image@digest>)
    //before=(<container id> or <container name>)
    //expose=(<port>[/<proto>]|<startport-endport>/[<proto>])
    //exited=<int> containers with exit code of <int>
    //health=(starting|healthy|unhealthy|none)
    //id=<ID> a container's ID
    //isolation=(default|process|hyperv) (Windows daemon only)
    //is-task=(true|false)
    //label=key or label="key=value" of a container label
    //name=<name> a container's name
    //network=(<network id> or <network name>)
    //publish=(<port>[/<proto>]|<startport-endport>/[<proto>])
    //since=(<container id> or <container name>)
    //status=(created|restarting|running|removing|paused|exited|dead)
    //volume=(<volume name> or <mount point destination>)
    //========================================================Containers==========================================================================
    public JSONArray containersJson(String ip,
                                    int port,
                                    boolean all,
                                    Integer limit,
                                    boolean size,
                                    String filters) throws Exception {
        String queryParam = queryString("all", all, "limit", limit, "size", size, "filter", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/json?" + queryParam;
        log.debug("DockerProxy: " + url);
        ResponseEntity<JSONArray> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONArray.class);
        return exchange.getBody();
    }

    public JSONObject containersCreate(String ip,
                                       int port,
                                       ContainerFormVO form,
                                       boolean autoStart) throws Exception {
        String queryParam = queryString("autoStart", autoStart);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/create?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(form), JSONObject.class);
        return exchange.getBody();
    }

    public JSONObject containersInspect(String ip,
                                        int port,
                                        String id,
                                        boolean size) throws Exception {
        String queryParam = queryString("size", size);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/json?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    public JSONObject containersTop(String ip,
                                    int port,
                                    String id,
                                    String psArgs) throws Exception {
        String queryParam = queryString("ps_args", psArgs);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/top?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    public String containersLogs(String ip,
                                 int port,
                                 String id,
                                 boolean follow,
                                 boolean stdout,
                                 boolean stderr,
                                 int since,
                                 int until,/*NOT Support*/
                                 boolean timestamps,
                                 String tail/*NOT Support*/) throws Exception {
        String queryParam = queryString("follow", follow, "stdout", stdout, "stderr", stderr, "since", since, "until", until, "timestamps", timestamps, "tail", tail);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/logs?" + queryParam;
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), String.class);
        return exchange.getBody();
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerChanges
    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerExport


    public JSONObject containersStats(String ip,
                                      int port,
                                      String id,
                                      boolean stream/*NOT Support*/) throws Exception {
        String queryParam = queryString("stream", stream);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/stats?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }


    public ResultBean containersStart(String ip,
                                      int port,
                                      String id,
                                      String detachKeys/*NOT Support*/) throws Exception {
        String queryParam = queryString("detachKeys", detachKeys);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/start?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }


    public ResultBean containersStop(String ip,
                                     int port,
                                     String id,
                                     int timeout/*NOT Support*/) throws Exception {
        String queryParam = queryString("timeout", timeout);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/stop?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }


    public ResultBean containersRestart(String ip,
                                        int port,
                                        String id,
                                        int timeout/*NOT Support*/) throws Exception {
        String queryParam = queryString("timeout", timeout);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/restart?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }


    public JSONObject containersUpdate(String ip,
                                       int port,
                                       String id,
                                       ContainerFormVO form) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/update";
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(form), JSONObject.class);
        return exchange.getBody();
    }


    public ResultBean containersRename(String ip,
                                       int port,
                                       String id,
                                       String name) throws Exception {
        String queryParam = queryString("name", name);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/rename?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }


    public ResultBean containersPause(String ip,
                                      int port,
                                      String id) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/pause";
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }


    public ResultBean containersUnpause(String ip,
                                        int port,
                                        String id) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/unpause";
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    //TODO
    public String containersAttach(String ip,
                                   int port,
                                   String id,
                                   String detachKeys,//NOT Support
                                   boolean logs,
                                   boolean stream,
                                   boolean stdin,
                                   boolean stdout,
                                   boolean stderr) throws Exception {
        String queryParam = queryString("detachKeys", detachKeys, "logs", logs, "stream", stream, "stdin", stdin, "stdout", stdout, "stderr", stderr);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/attach?" + queryParam;
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), String.class);
        return exchange.getBody();
    }

    public JSONObject containersWait(String ip,
                                     int port,
                                     String id,
                                     String condition) throws Exception {
        String queryParam = queryString("condition", condition);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "/wait?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }


    public ResultBean containersRemove(String ip,
                                       int port,
                                       String id,
                                       boolean volumes,
                                       boolean force,
                                       boolean link/*NOT Support*/) throws Exception {
        String queryParam = queryString("volumes", volumes, "force", force, "link", link);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/" + id + "?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerArchiveInfo
    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/PutContainerArchive


    public JSONObject containersPrune(String ip,
                                      int port,
                                      String filters) throws Exception {
        String queryParam = queryString("filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/containers/prune?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    //========================================================Images==========================================================================


    public JSONArray imagesList(String ip,
                                int port,
                                boolean all,
                                String filters,
                                boolean digests/*NOT Support*/) throws Exception {
        String queryParam = queryString("all", all, "filters", filters, "digests", digests);
        String url = "http://" + ip + ":" + port + "/agent/docker/images/json?" + queryParam;
        log.debug("DockerProxy: " + url);
        ResponseEntity<JSONArray> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONArray.class);
        return exchange.getBody();
    }

    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageBuild
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/BuildPrune
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageCreate
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageInspect
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageHistory
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImagePush
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageTag
    public ResultBean imagesDelete(String ip,
                                 int port,
                                 String name,
                                 boolean force,
                                 boolean noprune) throws Exception {
        String queryParam = queryString("force", force, "noprune", noprune);
        String url = "http://" + ip + ":" + port + "/agent/docker/images/" + name + "?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    public JSONArray imagesSearch(String ip,
                                  int port,
                                  String term,
                                  int limit,
                                  String filters) throws Exception {
        String queryParam = queryString("term", term, "filters", filters, "limit", limit);
        String url = "http://" + ip + ":" + port + "/agent/docker/images/search?" + queryParam;
        log.debug("DockerProxy: " + url);
        ResponseEntity<JSONArray> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONArray.class);
        return exchange.getBody();
    }

    public JSONObject imagesPrune(String ip,
                                  int port,
                                  String filters) throws Exception {
        String queryParam = queryString("filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/images/prune?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageCommit
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageGet
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageGetAll
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageLoad

    //========================================================Networks==========================================================================
    public JSONArray networksList(String ip,
                                  int port,
                                  String filters) throws Exception {
        String queryParam = queryString("filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/networks?" + queryParam;
        log.debug("DockerProxy: " + url);
        ResponseEntity<JSONArray> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONArray.class);
        return exchange.getBody();
    }

    public JSONObject networksInspect(String ip,
                                      int port,
                                      String id,
                                      boolean verbose,/*Not Support*/
                                      String scope/*Not Support*/) throws Exception {
        String queryParam = queryString("verbose", verbose, "scope", scope);
        String url = "http://" + ip + ":" + port + "/agent/docker/networks/" + id + "?" + queryParam;
        log.debug("DockerProxy: " + url);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    public ResultBean networksDelete(String ip,
                                     int port,
                                     String id) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/networks/" + id;
        log.debug("DockerProxy: " + url);
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    //TODO
    public JSONObject networksCreate(String ip,
                                     int port,
                                     Network network) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/networks/create";
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(network), JSONObject.class);
        return exchange.getBody();
    }

    public ResultBean networksConnect(String ip,
                                      int port,
                                      String id,
                                      String containerId,
                                      ContainerNetwork endpointConfig) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/networks/" + id + "/connect/" + containerId;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    public ResultBean networksDisconnect(String ip,
                                         int port,
                                         String id,
                                         String containerId,
                                         boolean force) throws Exception {
        String queryParam = queryString("force", force);
        String url = "http://" + ip + ":" + port + "/agent/docker/networks/" + id + "/disconnect/" + containerId + "?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    public JSONObject networksPrune(String ip,
                                    int port,
                                    String filters) throws Exception {
        String queryParam = queryString("filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/networks/prune?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    //========================================================Volumes==========================================================================
    public JSONObject volumesList(String ip,
                                  int port,
                                  String filters) throws Exception {
        String queryParam = queryString("filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/volumes?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    //TODO
    public JSONObject volumesCreate(String ip,
                                    int port,
                                    Volume volume) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/volumes/create";
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(volume), JSONObject.class);
        return exchange.getBody();
    }

    public JSONObject volumesInspect(String ip,
                                     int port,
                                     String name) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/volumes/" + name;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    public ResultBean volumesDelete(String ip,
                                    int port,
                                    String name,
                                    boolean force/*NOT Support*/) throws Exception {
        String queryParam = queryString("force", force);
        String url = "http://" + ip + ":" + port + "/agent/docker/volumes/" + name + "?" + queryParam;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    public JSONObject volumesPrune(String ip,
                                   int port,
                                   String filters) throws Exception {
        String queryParam = queryString("filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/volumes/prune?" + queryParam;
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }

    //========================================================Exec==========================================================================
    public String execStart(String ip,
                            int port,
                            String id,
                            ExecFormVO form) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/exec/start/" + id;
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(form), String.class);
        return exchange.getBody();
    }
    //    private void checkDiskSpace(DockerClient dockerClient, String id) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try {
//            dockerClient
//                    .execStartCmd(dockerClient.execCreateCmd(id).withAttachStdout(true).withCmd("df", "-P").exec().getId())
//                    .exec(new ExecStartResultCallback(outputStream, null))
//                    .awaitCompletion();
//        } catch (Exception e) {
//            log.debug("Can't exec disk checking command", e);
//        }
//        DiskSpaceUsage df = parseAvailableDiskSpace(outputStream.toString());
//        VisibleAssertions.assertTrue(
//                "Docker environment should have more than 2GB free disk space",
//                df.availableMB.map(it -> it >= 2048).orElse(true)
//        );
//    }
//https://www.codota.com/code/java/methods/com.github.dockerjava.api.DockerClient/execStartCmd


    public JSONObject systemAuth(String ip,
                                 int port,
                                 AuthConfig authConfig) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/system/auth";
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(authConfig), JSONObject.class);
        return exchange.getBody();
    }


    public JSONObject systemInfo(String ip,
                                 int port) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/system/info";
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }


    public JSONObject systemVersion(String ip,
                                    int port) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/system/version";
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), JSONObject.class);
        return exchange.getBody();
    }


    public ResultBean systemPing(String ip,
                                 int port) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/system/ping";
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    //TODO
    public String systemEvents(String ip,
                               int port,
                               String since,
                               String until,
                               String filters) throws Exception {
        String queryParam = queryString("since", since, "until", until, "filters", filters);
        String url = "http://" + ip + ":" + port + "/agent/docker/system/events?" + queryParam;
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), String.class);
        return exchange.getBody();
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/SystemDataUsage
}
