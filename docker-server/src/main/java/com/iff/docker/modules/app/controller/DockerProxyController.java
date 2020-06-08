/*******************************************************************************
 * Copyright (c) 2020-06-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.Volume;
import com.iff.docker.config.RsaConfig;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.service.DockerProxyService;
import com.iff.docker.modules.app.vo.form.ContainerFormVO;
import com.iff.docker.modules.app.vo.form.ExecFormVO;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.ResultBean;
import com.iff.docker.modules.util.RSAHelper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * DockerProxyController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Slf4j
@RestController
@RequestMapping(path = "/docker/proxy", produces = Constant.JSON_UTF8)
public class DockerProxyController extends BaseController {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RsaConfig rsaConfig;
    @Autowired
    DockerProxyService proxyService;

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
    @ApiOperation(value = "List containers", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerList")
    @GetMapping(path = "/containers/json/{ip}/{port}")
    public JSONArray containersJson(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @RequestParam(name = "all", defaultValue = "false") boolean all,
                                    @RequestParam(name = "limit", required = false) Integer limit,
                                    @RequestParam(name = "size", defaultValue = "false") boolean size,
                                    @RequestParam(name = "filters", required = false) String filters,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersJson(ip, port, all, limit, size, filters);
    }

    @ApiOperation(value = "Create a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerCreate")
    @PostMapping(path = "/containers/create/{ip}/{port}")
    public JSONObject containersCreate(@PathVariable("ip") String ip,
                                       @PathVariable("port") int port,
                                       @RequestBody ContainerFormVO form,
                                       @RequestParam(name = "autoStart", defaultValue = "false") boolean autoStart,
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersCreate(ip, port, form, autoStart);
    }

    @ApiOperation(value = "Inspect a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerInspect")
    @GetMapping(path = "/containers/{id}/json/{ip}/{port}")
    public JSONObject containersInspect(@PathVariable("ip") String ip,
                                        @PathVariable("port") int port,
                                        @PathVariable("id") String id,
                                        @RequestParam(name = "size", defaultValue = "false") boolean size,
                                        @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersInspect(ip, port, id, size);
    }

    @ApiOperation(value = "List processes running inside a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerTop")
    @GetMapping(path = "/containers/{id}/top/{ip}/{port}")
    public JSONObject containersTop(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @PathVariable("id") String id,
                                    @RequestParam(name = "ps_args", defaultValue = "-ef") String psArgs,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersTop(ip, port, id, psArgs);
    }

    @ApiOperation(value = "Get container logs", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerLogs")
    @GetMapping(path = "/containers/{id}/logs/{ip}/{port}")
    public String containersLogs(@PathVariable("ip") String ip,
                                 @PathVariable("port") int port,
                                 @PathVariable("id") String id,
                                 @RequestParam(name = "follow", defaultValue = "false") boolean follow,
                                 @RequestParam(name = "stdout", defaultValue = "true") boolean stdout,
                                 @RequestParam(name = "stderr", defaultValue = "true") boolean stderr,
                                 @RequestParam(name = "since", defaultValue = "0") int since,
                                 @RequestParam(name = "until", defaultValue = "0") int until,//NOT Support
                                 @RequestParam(name = "timestamps", defaultValue = "false") boolean timestamps,
                                 @RequestParam(name = "tail", defaultValue = "all") String tail,//NOT Support
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersLogs(ip, port, id, follow, stdout, stderr, since, until, timestamps, tail);
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerChanges
    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerExport

    @ApiOperation(value = "Get container stats based on resource usage", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerStats")
    @GetMapping(path = "/containers/{id}/stats/{ip}/{port}")
    public JSONObject containersStats(@PathVariable("ip") String ip,
                                      @PathVariable("port") int port,
                                      @PathVariable("id") String id,
                                      @RequestParam(name = "stream", defaultValue = "true") boolean stream,//NOT Support
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersStats(ip, port, id, stream);
    }

    @ApiOperation(value = "Start a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerStart")
    @PostMapping(path = "/containers/{id}/start/{ip}/{port}")
    public ResultBean containersStart(@PathVariable("ip") String ip,
                                      @PathVariable("port") int port,
                                      @PathVariable("id") String id,
                                      @RequestParam(name = "detachKeys", required = false) String detachKeys,//NOT Support
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersStart(ip, port, id, detachKeys);
    }

    @ApiOperation(value = "Stop a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerStop")
    @PostMapping(path = "/containers/{id}/stop/{ip}/{port}")
    public ResultBean containersStop(@PathVariable("ip") String ip,
                                     @PathVariable("port") int port,
                                     @PathVariable("id") String id,
                                     @RequestParam(name = "timeout", defaultValue = "0") int timeout,//NOT Support
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersStop(ip, port, id, timeout);
    }

    @ApiOperation(value = "Restart a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerRestart")
    @PostMapping(path = "/containers/{id}/restart/{ip}/{port}")
    public ResultBean containersRestart(@PathVariable("ip") String ip,
                                        @PathVariable("port") int port,
                                        @PathVariable("id") String id,
                                        @RequestParam(name = "timeout", defaultValue = "0") int timeout,//NOT Support
                                        @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersRestart(ip, port, id, timeout);
    }

    @ApiOperation(value = "Update a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerUpdate")
    @PostMapping(path = "/containers/{id}/update/{ip}/{port}")
    public JSONObject containersUpdate(@PathVariable("ip") String ip,
                                       @PathVariable("port") int port,
                                       @PathVariable("id") String id,
                                       @RequestBody ContainerFormVO form,
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersUpdate(ip, port, id, form);
    }

    @ApiOperation(value = "Rename a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerRename")
    @PostMapping(path = "/containers/{id}/rename/{ip}/{port}")
    public ResultBean containersRename(@PathVariable("ip") String ip,
                                       @PathVariable("port") int port,
                                       @PathVariable("id") String id,
                                       @RequestParam(name = "name") String name,
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersRename(ip, port, id, name);
    }

    @ApiOperation(value = "Pause a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerPause")
    @PostMapping(path = "/containers/{id}/pause/{ip}/{port}")
    public ResultBean containersPause(@PathVariable("ip") String ip,
                                      @PathVariable("port") int port,
                                      @PathVariable("id") String id,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersPause(ip, port, id);
    }

    @ApiOperation(value = "Unpause a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerUnpause")
    @PostMapping(path = "/containers/{id}/unpause/{ip}/{port}")
    public ResultBean containersUnpause(@PathVariable("ip") String ip,
                                        @PathVariable("port") int port,
                                        @PathVariable("id") String id,
                                        @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersUnpause(ip, port, id);
    }

    //TODO
    @ApiOperation(value = "Attach to a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerAttach")
    @PostMapping(path = "/containers/{id}/attach/{ip}/{port}")
    public String containersAttach(@PathVariable("ip") String ip,
                                   @PathVariable("port") int port,
                                   @PathVariable("id") String id,
                                   @RequestParam(name = "detachKeys", defaultValue = "ctrl-c") String detachKeys,//NOT Support
                                   @RequestParam(name = "logs", defaultValue = "false") boolean logs,
                                   @RequestParam(name = "stream", defaultValue = "false") boolean stream,
                                   @RequestParam(name = "stdin", defaultValue = "false") boolean stdin,
                                   @RequestParam(name = "stdout", defaultValue = "false") boolean stdout,
                                   @RequestParam(name = "stderr", defaultValue = "false") boolean stderr,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersAttach(ip, port, id, detachKeys, logs, stream, stdin, stdout, stderr);
    }

    @ApiOperation(value = "Wait for a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerWait")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "condition", dataType = "String", paramType = "query",
                    allowableValues = "not-running,next-exit,removed", allowMultiple = false)
    })
    @PostMapping(path = "/containers/{id}/wait/{ip}/{port}")
    public JSONObject containersWait(@PathVariable("ip") String ip,
                                     @PathVariable("port") int port,
                                     @PathVariable("id") String id,
                                     @RequestParam(name = "condition", defaultValue = "not-running") String condition,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersWait(ip, port, id, condition);
    }

    @ApiOperation(value = "Remove a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerDelete")
    @DeleteMapping(path = "/containers/{id}/{ip}/{port}")
    public ResultBean containersRemove(@PathVariable("ip") String ip,
                                       @PathVariable("port") int port,
                                       @PathVariable("id") String id,
                                       @RequestParam(name = "volumes", defaultValue = "false") boolean volumes,
                                       @RequestParam(name = "force", defaultValue = "false") boolean force,
                                       @RequestParam(name = "link", defaultValue = "false") boolean link,//NOT Support
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersRemove(ip, port, id, volumes, force, link);
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerArchiveInfo
    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/PutContainerArchive


    @ApiOperation(value = "Delete stopped containers", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerPrune")
    @DeleteMapping(path = "/containers/prune/{ip}/{port}")
    public JSONObject containersPrune(@PathVariable("ip") String ip,
                                      @PathVariable("port") int port,
                                      @RequestParam(name = "filters", required = false) String filters,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.containersPrune(ip, port, filters);
    }

    //========================================================Images==========================================================================
    @ApiOperation(value = "List Images", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImageList")
    @GetMapping(path = "/images/json/{ip}/{port}")
    public JSONArray imagesList(@PathVariable("ip") String ip,
                                @PathVariable("port") int port,
                                @RequestParam(name = "all", defaultValue = "false") boolean all,
                                @RequestParam(name = "filters", required = false) String filters,
                                @RequestParam(name = "digests", defaultValue = "false") boolean digests,//NOT Support
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.imagesList(ip, port, all, filters, digests);
    }

    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageBuild
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/BuildPrune
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageCreate
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageInspect
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageHistory
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImagePush
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageTag

    @ApiOperation(value = "Remove an image", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImageDelete")
    @DeleteMapping(path = "/images/{name}/{ip}/{port}")
    public ResultBean imagesDelete(@PathVariable("ip") String ip,
                                   @PathVariable("port") int port,
                                   @PathVariable("name") String name,
                                   @RequestParam(name = "force", defaultValue = "false") boolean force,
                                   @RequestParam(name = "noprune", defaultValue = "false") boolean noprune,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.imagesDelete(ip, port, name, force, noprune);
    }

    @ApiOperation(value = "Search images", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImageSearch")
    @GetMapping(path = "/images/search/{ip}/{port}")
    public JSONArray imagesSearch(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @RequestParam(name = "term") String term,
                                  @RequestParam(name = "limit", defaultValue = "100") int limit,
                                  @RequestParam(name = "filters", required = false) String filters,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.imagesSearch(ip, port, term, limit, filters);
    }

    @ApiOperation(value = "Delete unused images", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImagePrune")
    @DeleteMapping(path = "/images/prune/{ip}/{port}")
    public JSONObject imagesPrune(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @RequestParam(name = "filters", required = false) String filters,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.imagesPrune(ip, port, filters);
    }

    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageCommit
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageGet
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageGetAll
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageLoad

    //========================================================Networks==========================================================================
    @ApiOperation(value = "List networks", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkList")
    @GetMapping(path = "/networks/{ip}/{port}")
    public JSONArray networksList(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @RequestParam(name = "filters", required = false) String filters,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksList(ip, port, filters);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "scope", value = "scope", dataType = "String", paramType = "query",
                    allowableValues = "swarm,global,local", allowMultiple = false)
    })
    @ApiOperation(value = "Inspect a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkInspect")
    @GetMapping(path = "/networks/{id}/{ip}/{port}")
    public JSONObject networksInspect(@PathVariable("ip") String ip,
                                      @PathVariable("port") int port,
                                      @PathVariable("id") String id,
                                      @RequestParam(name = "verbose", defaultValue = "false") boolean verbose,//Not Support
                                      @RequestParam(name = "scope", defaultValue = "local") String scope,//Not Support
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksInspect(ip, port, id, verbose, scope);
    }

    @ApiOperation(value = "Remove a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkDelete")
    @DeleteMapping(path = "/networks/{id}/{ip}/{port}")
    public ResultBean networksDelete(@PathVariable("ip") String ip,
                                     @PathVariable("port") int port,
                                     @PathVariable("id") String id,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksDelete(ip, port, id);
    }

    @ApiOperation(value = "Create a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkCreate")
    @PostMapping(path = "/networks/create/{ip}/{port}")
    public JSONObject networksCreate(@PathVariable("ip") String ip,
                                     @PathVariable("port") int port,
                                     @RequestBody Network network,//TODO
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksCreate(ip, port, network);
    }

    @ApiOperation(value = "Connect a container to a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkConnect")
    @PostMapping(path = "/networks/{id}/connect/{containerId}/{ip}/{port}")
    public ResultBean networksConnect(@PathVariable("ip") String ip,
                                      @PathVariable("port") int port,
                                      @PathVariable("id") String id,
                                      @PathVariable("containerId") String containerId,
                                      @RequestBody ContainerNetwork endpointConfig,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksConnect(ip, port, id, containerId, endpointConfig);
    }

    @ApiOperation(value = "Disconnect a container from a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkDisconnect")
    @PostMapping(path = "/networks/{id}/disconnect/{containerId}/{ip}/{port}")
    public ResultBean networksDisconnect(@PathVariable("ip") String ip,
                                         @PathVariable("port") int port,
                                         @PathVariable("id") String id,
                                         @PathVariable("containerId") String containerId,
                                         @RequestParam(name = "force", defaultValue = "false") boolean force,
                                         @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksDisconnect(ip, port, id, containerId, force);
    }

    @ApiOperation(value = "Delete unused networks", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkPrune")
    @PostMapping(path = "/networks/prune/{ip}/{port}")
    public JSONObject networksPrune(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @RequestParam(name = "filters", required = false) String filters,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.networksPrune(ip, port, filters);
    }

    //========================================================Volumes==========================================================================
    @ApiOperation(value = "List volumes", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeList")
    @GetMapping(path = "/volumes/{ip}/{port}")
    public JSONObject volumesList(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @RequestParam(name = "filters", required = false) String filters,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.volumesList(ip, port, filters);
    }

    @ApiOperation(value = "Create a volume", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeCreate")
    @PostMapping(path = "/volumes/create/{ip}/{port}")
    public JSONObject volumesCreate(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @RequestBody Volume volume,//TODO
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.volumesCreate(ip, port, volume);
    }

    @ApiOperation(value = "Inspect a volume", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeInspect")
    @GetMapping(path = "/volumes/{name}/{ip}/{port}")
    public JSONObject volumesInspect(@PathVariable("ip") String ip,
                                     @PathVariable("port") int port,
                                     @PathVariable("name") String name,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.volumesInspect(ip, port, name);
    }

    @ApiOperation(value = "Remove a volume", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeDelete")
    @DeleteMapping(path = "/volumes/{name}/{ip}/{port}")
    public ResultBean volumesDelete(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @PathVariable("name") String name,
                                    @RequestParam(name = "force", defaultValue = "false") boolean force,//NOT Support
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.volumesDelete(ip, port, name, force);
    }

    @ApiOperation(value = "Delete unused volumes", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumePrune")
    @PostMapping(path = "/volumes/prune/{ip}/{port}")
    public JSONObject volumesPrune(@PathVariable("ip") String ip,
                                   @PathVariable("port") int port,
                                   @RequestParam(name = "filters", required = false) String filters,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.volumesPrune(ip, port, filters);
    }

    //========================================================Exec==========================================================================
    @ApiOperation(value = "Start an exec instance", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerExec")
    @PostMapping(path = "/exec/start/{id}/{ip}/{port}")
    public String execStart(@PathVariable("ip") String ip,
                            @PathVariable("port") int port,
                            @PathVariable("id") String id,
                            @RequestBody ExecFormVO form,
                            @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.execStart(ip, port, id, form);
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

    @ApiOperation(value = "Check auth configuration", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemAuth")
    @PostMapping(path = "/system/auth/{ip}/{port}")
    public JSONObject systemAuth(@PathVariable("ip") String ip,
                                 @PathVariable("port") int port,
                                 @RequestBody AuthConfig authConfig,
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.systemAuth(ip, port, authConfig);
    }

    @ApiOperation(value = "Get system information", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemInfo")
    @GetMapping(path = "/system/info/{ip}/{port}")
    public JSONObject systemInfo(@PathVariable("ip") String ip,
                                 @PathVariable("port") int port,
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.systemInfo(ip, port);
    }

    @ApiOperation(value = "Get version", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemVersion")
    @GetMapping(path = "/system/version/{ip}/{port}")
    public JSONObject systemVersion(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.systemVersion(ip, port);
    }

    @ApiOperation(value = "Ping", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemPing")
    @GetMapping(path = "/system/ping/{ip}/{port}")
    public ResultBean systemPing(@PathVariable("ip") String ip,
                                 @PathVariable("port") int port,
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.systemPing(ip, port);
    }

    //TODO
    @ApiOperation(value = "Monitor events", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemEvents")
    @GetMapping(path = "/system/events/{ip}/{port}")
    public String systemEvents(@PathVariable("ip") String ip,
                               @PathVariable("port") int port,
                               @RequestParam(name = "since", required = false) String since,
                               @RequestParam(name = "until", required = false) String until,
                               @RequestParam(name = "filters", required = false) String filters,
                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        return proxyService.systemEvents(ip, port, since, until, filters);
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/SystemDataUsage
}
