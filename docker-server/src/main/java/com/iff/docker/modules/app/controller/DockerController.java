/*******************************************************************************
 * Copyright (c) 2020-05-26 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.util.FiltersBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.vo.form.ContainerFormVO;
import com.iff.docker.modules.app.vo.form.ExecFormVO;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.ResultBean;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.CheckForNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * DockerController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-26
 */
@Slf4j
@RestController
@RequestMapping(path = "/docker", produces = Constant.JSON_UTF8)
public class DockerController extends BaseController {
    @Autowired
    DockerClientConfig config;

    DockerClient client() {
        return DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(new NettyDockerCmdExecFactory()).build();
    }

    Object filters1(String filters, DockerCmd cmd) {
        if (StringUtils.isEmpty(filters)) {
            return cmd;
        }
        try {
            Map<String, List<String>> map = JSON.parseObject(filters, new TypeReference<Map<String, List<String>>>() {
            }.getType());
            Field field = FieldUtils.getField(cmd.getClass(), "filters", true);
            FiltersBuilder builder = (FiltersBuilder) field.get(cmd);
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                builder.withFilter(entry.getKey(), entry.getValue());
            }
            field.set(cmd, builder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cmd;
    }

    <T extends SyncDockerCmd> T filters(String filters, SyncDockerCmd<?> cmd) {
        return (T) filters1(filters, cmd);
    }

    <T extends AsyncDockerCmd> T filters(String filters, AsyncDockerCmd<?, ?> cmd) {
        return (T) filters1(filters, cmd);
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
    @GetMapping(path = "/containers/json")
    public List<Container> containersJson(@RequestParam(name = "all", defaultValue = "false") boolean all,
                                          @RequestParam(name = "limit", required = false) Integer limit,
                                          @RequestParam(name = "size", defaultValue = "false") boolean size,
                                          @RequestParam(name = "filters", required = false) String filters,
                                          @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            ListContainersCmd cmd = client.listContainersCmd().withShowAll(all).withShowSize(size);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    @ApiOperation(value = "Create a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerCreate")
    @PostMapping(path = "/containers/create")
    public CreateContainerResponse containersCreate(@RequestBody ContainerFormVO form,
                                                    @RequestParam(name = "autoStart", defaultValue = "false") boolean autoStart,
                                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            CreateContainerCmd cmd = client.createContainerCmd(form.getImage()).withName(form.getName());

            HostConfig hostConfig = HostConfig.newHostConfig();

            if (form.getPortBindings() != null) {
                List<ExposedPort> exposedPorts = new ArrayList<>();
                List<PortBinding> portBindings = new ArrayList<>();
                for (String binding : form.getPortBindings()) {
                    PortBinding parse = PortBinding.parse(binding);
                    portBindings.add(parse);
                    exposedPorts.add(parse.getExposedPort());
                }
                hostConfig.withPortBindings(portBindings);
                cmd.withExposedPorts(exposedPorts);
            }
            {
                hostConfig.withAutoRemove(form.isAutoRemove());
            }

            {//command & logging
                if (form.getCmd() != null && form.getCmd().length > 0) {
                    cmd.withCmd(form.getCmd());
                }
                if (form.getEntrypoint() != null && form.getEntrypoint().length > 0) {
                    cmd.withEntrypoint(form.getEntrypoint());
                }
                if (StringUtils.isNotEmpty(form.getWorkingDir())) {
                    cmd.withWorkingDir(form.getWorkingDir());
                }
                if (StringUtils.isNotEmpty(form.getUser())) {
                    cmd.withUser(form.getUser());
                }
                if (StringUtils.isNotEmpty(form.getLoggingType())) {
                    hostConfig.withLogConfig(new LogConfig(LogConfig.LoggingType.valueOf(form.getLoggingType()), form.getLoggingConfig()));
                }
            }
            {// Volumes
                if (form.getVolumeBinds() != null) {
                    List<Bind> list = new ArrayList<>();
                    for (String bind : form.getVolumeBinds()) {
                        list.add(Bind.parse(bind));
                    }
                    hostConfig.withBinds(list);
                }
            }
            {// Network
                if (StringUtils.isNotEmpty(form.getNetworkMode())) {
                    hostConfig.withNetworkMode(form.getNetworkMode());
                }
                if (StringUtils.isNotEmpty(form.getHostname())) {
                    cmd.withHostName(form.getHostname());
                }
                if (StringUtils.isNotEmpty(form.getDomainname())) {
                    cmd.withDomainName(form.getDomainname());
                }
                if (StringUtils.isNotEmpty(form.getMacAddress())) {
                    cmd.withMacAddress(form.getMacAddress());
                }
                if (StringUtils.isNotEmpty(form.getIpv4Address())) {
                    cmd.withIpv4Address(form.getIpv4Address());
                }
                if (StringUtils.isNotEmpty(form.getIpv6Address())) {
                    cmd.withIpv6Address(form.getIpv6Address());
                }
                if (form.getDns() != null && form.getDns().length > 0) {
                    hostConfig.withDns(form.getDns());
                }
                if (form.getDnsSearch() != null && form.getDnsSearch().length > 0) {
                    hostConfig.withDnsSearch(form.getDnsSearch());
                }
                if (form.getExtraHosts() != null && form.getExtraHosts().length > 0) {
                    hostConfig.withExtraHosts(form.getExtraHosts());
                }
            }
            {// Env
                if (form.getEnv() != null && form.getEnv().length > 0) {
                    cmd.withEnv(form.getEnv());
                }
            }
            {// Labels
                if (form.getLabels() != null && form.getLabels().size() > 0) {
                    cmd.withLabels(form.getLabels());
                }
            }
            {// Restart policy
                if (StringUtils.isNotEmpty(form.getRestartPolicy())) {
                    hostConfig.withRestartPolicy(RestartPolicy.parse(form.getRestartPolicy() + ":" + form.getRestartPolicyFailureRetry()));
                }
            }
            {// Runtime & Resources
                if (form.isPrivileged()) {
                    hostConfig.withPrivileged(form.isPrivileged());
                }
                if (StringUtils.isNotEmpty(form.getRuntime())) {
                    hostConfig.withRuntime(form.getRuntime());
                }
                if (form.getDevice() != null && form.getDevice().length > 0) {
                    List<Device> list = new ArrayList<>();
                    for (String device : form.getDevice()) {
                        list.add(Device.parse(device));
                    }
                    hostConfig.withDevices(list);
                }
                if (StringUtils.isNotEmpty(form.getMemoryReservation())) {
                    String memory = form.getMemoryReservation().toUpperCase();
                    String[] strings = StringUtils.splitByCharacterTypeCamelCase(memory);
                    long value = Long.valueOf(strings[0]);
                    if (strings.length > 1) {
                        if ("K".equals(strings[1]) || "KB".equals(strings[1])) {
                            value = value * 1024;
                        } else if ("M".equals(strings[1]) || "MB".equals(strings[1])) {
                            value = value * 1024 * 1024;
                        } else if ("G".equals(strings[1]) || "GB".equals(strings[1])) {
                            value = value * 1024 * 1024 * 1024;
                        }
                    }
                    hostConfig.withMemoryReservation(value);
                }
                if (StringUtils.isNotEmpty(form.getMemory())) {
                    String memory = form.getMemory().toUpperCase();
                    String[] strings = StringUtils.splitByCharacterTypeCamelCase(memory);
                    long value = Long.valueOf(strings[0]);
                    if (strings.length > 1) {
                        if ("K".equals(strings[1]) || "KB".equals(strings[1])) {
                            value = value * 1024;
                        } else if ("M".equals(strings[1]) || "MB".equals(strings[1])) {
                            value = value * 1024 * 1024;
                        } else if ("G".equals(strings[1]) || "GB".equals(strings[1])) {
                            value = value * 1024 * 1024 * 1024;
                        }
                    }
                    hostConfig.withMemory(value);
                }
                if (form.getCpuPercent() != null && form.getCpuPercent() > 0) {
                    hostConfig.withNanoCPUs(Double.valueOf(form.getCpuPercent() * Math.pow(10, 9)).longValue());
                }
            }

            {// Capabilities
                if (form.getCapabilities() != null && form.getCapabilities().length > 0) {
                    String[] drop = ArrayUtils.removeElements(ContainerFormVO.CAP_DEFAULT, form.getCapabilities());
                    String[] add = ArrayUtils.removeElements(form.getCapabilities(), ContainerFormVO.CAP_DEFAULT);
                    if (drop.length > 0) {
                        List<Capability> list = new ArrayList<>();
                        for (String cap : drop) {
                            list.add(Capability.valueOf(cap));
                        }
                        hostConfig.withCapDrop(list.toArray(new Capability[list.size()]));
                    }
                    if (add.length > 0) {
                        List<Capability> list = new ArrayList<>();
                        for (String cap : add) {
                            list.add(Capability.valueOf(cap));
                        }
                        hostConfig.withCapAdd(list.toArray(new Capability[list.size()]));
                    }
                }
            }

            {// Health Check
                if (form.getHealthcheckTest() != null) {
                    HealthCheck healthCheck = new HealthCheck();
                    healthCheck.withTest(Arrays.asList(form.getHealthcheckTest()));
                    healthCheck.withInterval(form.getHealthcheckInterval());
                    healthCheck.withRetries(form.getHealthcheckRetries());
                    healthCheck.withStartPeriod(form.getHealthcheckStartPeriod());
                    healthCheck.withTimeout(form.getHealthcheckTimeout());
                    cmd.withHealthcheck(healthCheck);
                }
            }

            cmd.withHostConfig(hostConfig);
            CreateContainerResponse exec = cmd.exec();
            if (autoStart) {
                client.startContainerCmd(exec.getId()).exec();
            }
            return exec;
        }
    }

    @ApiOperation(value = "Inspect a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerInspect")
    @GetMapping(path = "/containers/{id}/json")
    public Object containersInspect(@PathVariable("id") String id,
                                    @RequestParam(name = "size", defaultValue = "false") boolean size,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return JSON.toJSON(client.inspectContainerCmd(id).withSize(size).exec());
        }
    }

    @ApiOperation(value = "List processes running inside a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerTop")
    @GetMapping(path = "/containers/{id}/top")
    public TopContainerResponse containersTop(@PathVariable("id") String id,
                                              @RequestParam(name = "ps_args", defaultValue = "-ef") String psArgs,
                                              @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.topContainerCmd(id).withPsArgs(psArgs).exec();
        }
    }

    @ApiOperation(value = "Get container logs", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerLogs")
    @GetMapping(path = "/containers/{id}/logs")
    public String containersLogs(@PathVariable("id") String id,
                                 @RequestParam(name = "follow", defaultValue = "false") boolean follow,
                                 @RequestParam(name = "stdout", defaultValue = "true") boolean stdout,
                                 @RequestParam(name = "stderr", defaultValue = "true") boolean stderr,
                                 @RequestParam(name = "since", defaultValue = "0") int since,
                                 @RequestParam(name = "until", defaultValue = "0") int until,//NOT Support
                                 @RequestParam(name = "timestamps", defaultValue = "false") boolean timestamps,
                                 @RequestParam(name = "tail", defaultValue = "all") String tail,//NOT Support
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            LogContainerResultCallback res = new LogContainerResultCallback();
            client.logContainerCmd(id).withFollowStream(follow).withStdOut(stdout).withStdErr(stderr)
                    .withSince(since).withTimestamps(timestamps).withTailAll().exec(res);
            res.awaitCompletion();
            return res.sb.toString();
        }
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerChanges
    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerExport

    @ApiOperation(value = "Get container stats based on resource usage", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerStats")
    @GetMapping(path = "/containers/{id}/stats")
    public Statistics containersStats(@PathVariable("id") String id,
                                      @RequestParam(name = "stream", defaultValue = "true") boolean stream,//NOT Support
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            StatsContainerResultCallback res = new StatsContainerResultCallback();
            ResultCallback<Statistics> exec = client.statsCmd(id).withNoStream(stream).exec(res);
            res.awaitCompletion();
            return res.stats;
        }
    }

    @ApiOperation(value = "Start a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerStart")
    @PostMapping(path = "/containers/{id}/start")
    public ResultBean containersStart(@PathVariable("id") String id,
                                      @RequestParam(name = "detachKeys", required = false) String detachKeys,//NOT Support
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.startContainerCmd(id).exec());
        }
    }

    @ApiOperation(value = "Stop a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerStop")
    @PostMapping(path = "/containers/{id}/stop")
    public ResultBean containersStop(@PathVariable("id") String id,
                                     @RequestParam(name = "timeout", defaultValue = "0") int timeout,//NOT Support
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.stopContainerCmd(id).withTimeout(timeout).exec());
        }
    }

    @ApiOperation(value = "Restart a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerRestart")
    @PostMapping(path = "/containers/{id}/restart")
    public ResultBean containersRestart(@PathVariable("id") String id,
                                        @RequestParam(name = "timeout", defaultValue = "0") int timeout,//NOT Support
                                        @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.restartContainerCmd(id).withtTimeout(timeout).exec());
        }
    }

    @ApiOperation(value = "Update a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerUpdate")
    @PostMapping(path = "/containers/{id}/update")
    public UpdateContainerResponse containersUpdate(@PathVariable("id") String id,
                                                    @RequestBody Container container,//TODO
                                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.updateContainerCmd(id).exec();
        }
    }

    @ApiOperation(value = "Rename a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerRename")
    @PostMapping(path = "/containers/{id}/rename")
    public ResultBean containersRename(@PathVariable("id") String id,
                                       @RequestParam(name = "name") String name,
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.renameContainerCmd(id).withName(name).exec());
        }
    }

    @ApiOperation(value = "Pause a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerPause")
    @PostMapping(path = "/containers/{id}/pause")
    public ResultBean containersPause(@PathVariable("id") String id,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.pauseContainerCmd(id).exec());
        }
    }

    @ApiOperation(value = "Unpause a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerUnpause")
    @PostMapping(path = "/containers/{id}/unpause")
    public ResultBean containersUnpause(@PathVariable("id") String id,
                                        @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.unpauseContainerCmd(id).exec());
        }
    }

    //TODO
    @ApiOperation(value = "Attach to a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerAttach")
    @PostMapping(path = "/containers/{id}/attach")
    public String containersAttach(@PathVariable("id") String id,
                                   @RequestParam(name = "detachKeys", defaultValue = "ctrl-c") String detachKeys,//NOT Support
                                   @RequestParam(name = "logs", defaultValue = "false") boolean logs,
                                   @RequestParam(name = "stream", defaultValue = "false") boolean stream,
                                   @RequestParam(name = "stdin", defaultValue = "false") boolean stdin,
                                   @RequestParam(name = "stdout", defaultValue = "false") boolean stdout,
                                   @RequestParam(name = "stderr", defaultValue = "false") boolean stderr,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            AttachContainerResultCallback res = new AttachContainerResultCallback();
            client.attachContainerCmd(id).withLogs(logs).withFollowStream(stream).withStdIn(new ByteArrayInputStream("".getBytes()))
                    .withStdOut(stdout).withStdErr(stderr).exec(res);
            res.awaitCompletion();
            return res.sb.toString();
        }
    }

    @ApiOperation(value = "Wait for a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerWait")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "condition", dataType = "String", paramType = "query",
                    allowableValues = "not-running,next-exit,removed", allowMultiple = false)
    })
    @PostMapping(path = "/containers/{id}/wait")
    public WaitResponse containersWait(@PathVariable("id") String id,
                                       @RequestParam(name = "condition", defaultValue = "not-running") String condition,
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            WaitContainerResultCallback res = new WaitContainerResultCallback();
            client.waitContainerCmd(id).exec(res);
            res.awaitCompletion();
            return res.waitResponse;
        }
    }

    @ApiOperation(value = "Remove a container", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerDelete")
    @DeleteMapping(path = "/containers/{id}")
    public ResultBean containersRemove(@PathVariable("id") String id,
                                       @RequestParam(name = "volumes", defaultValue = "false") boolean volumes,
                                       @RequestParam(name = "force", defaultValue = "false") boolean force,
                                       @RequestParam(name = "link", defaultValue = "false") boolean link,//NOT Support
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.removeContainerCmd(id).withRemoveVolumes(volumes).withForce(force).exec());
        }
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ContainerArchiveInfo
    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/PutContainerArchive


    @ApiOperation(value = "Delete stopped containers", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerPrune")
    @DeleteMapping(path = "/containers/prune")
    public PruneResponse containersPrune(@RequestParam(name = "filters", required = false) String filters,
                                         @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            PruneCmd cmd = client.pruneCmd(PruneType.CONTAINERS);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    //========================================================Images==========================================================================
    @ApiOperation(value = "List Images", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImageList")
    @GetMapping(path = "/images/json")
    public List<Image> imagesList(@RequestParam(name = "all", defaultValue = "false") boolean all,
                                  @RequestParam(name = "filters", required = false) String filters,
                                  @RequestParam(name = "digests", defaultValue = "false") boolean digests,//NOT Support
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            ListImagesCmd cmd = client.listImagesCmd().withShowAll(all);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageBuild
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/BuildPrune
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageCreate
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageInspect
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageHistory
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImagePush
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageTag

    @ApiOperation(value = "Remove an image", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImageDelete")
    @DeleteMapping(path = "/images/{name}")
    public ResultBean imagesList(@PathVariable("name") String name,
                                 @RequestParam(name = "force", defaultValue = "false") boolean force,
                                 @RequestParam(name = "noprune", defaultValue = "false") boolean noprune,
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.removeImageCmd(name).withForce(force).withNoPrune(noprune).exec());
        }
    }

    @ApiOperation(value = "Search images", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImageSearch")
    @GetMapping(path = "/images/search")
    public List<SearchItem> imagesSearch(@RequestParam(name = "term") String term,
                                         @RequestParam(name = "limit", defaultValue = "100") int limit,
                                         @RequestParam(name = "filters", required = false) String filters,
                                         @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            SearchImagesCmd cmd = client.searchImagesCmd(term).withLimit(limit);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    @ApiOperation(value = "Delete unused images", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ImagePrune")
    @DeleteMapping(path = "/images/prune")
    public PruneResponse imagesPrune(@RequestParam(name = "filters", required = false) String filters,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            PruneCmd cmd = client.pruneCmd(PruneType.IMAGES);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageCommit
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageGet
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageGetAll
    //NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/ImageLoad

    //========================================================Networks==========================================================================
    @ApiOperation(value = "List networks", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkList")
    @GetMapping(path = "/networks")
    public List<Network> networksList(@RequestParam(name = "filters", required = false) String filters,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            ListNetworksCmd cmd = client.listNetworksCmd();
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "scope", value = "scope", dataType = "String", paramType = "query",
                    allowableValues = "swarm,global,local", allowMultiple = false)
    })
    @ApiOperation(value = "Inspect a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkInspect")
    @GetMapping(path = "/networks/{id}")
    public Network networksInspect(@PathVariable("id") String id,
                                   @RequestParam(name = "verbose", defaultValue = "false") boolean verbose,//Not Support
                                   @RequestParam(name = "scope", defaultValue = "local") String scope,//Not Support
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.inspectNetworkCmd().withNetworkId(id).exec();
        }
    }

    @ApiOperation(value = "Remove a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkDelete")
    @DeleteMapping(path = "/networks/{id}")
    public ResultBean networksDelete(@PathVariable("id") String id,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.removeNetworkCmd(id).exec());
        }
    }

    @ApiOperation(value = "Create a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkCreate")
    @PostMapping(path = "/networks/create")
    public CreateNetworkResponse networksCreate(@RequestBody Network network,//TODO
                                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.createNetworkCmd().withName(network.getName()).withDriver(StringUtils.defaultString(network.getDriver(), "bridge")).exec();
        }
    }

    @ApiOperation(value = "Connect a container to a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkConnect")
    @PostMapping(path = "/networks/{id}/connect/{containerId}")
    public ResultBean networksConnect(@PathVariable("id") String id,
                                      @PathVariable("containerId") String containerId,
                                      @RequestBody ContainerNetwork endpointConfig,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.connectToNetworkCmd().withNetworkId(containerId).withContainerId(containerId).withContainerNetwork(endpointConfig).exec());
        }
    }

    @ApiOperation(value = "Disconnect a container from a network", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkDisconnect")
    @PostMapping(path = "/networks/{id}/disconnect/{containerId}")
    public ResultBean networksDisconnect(@PathVariable("id") String id,
                                         @PathVariable("containerId") String containerId,
                                         @RequestParam(name = "force", defaultValue = "false") boolean force,
                                         @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.disconnectFromNetworkCmd().withNetworkId(id).withContainerId(containerId).withForce(false).exec());
        }
    }

    @ApiOperation(value = "Delete unused networks", notes = "https://docs.docker.com/engine/api/v1.40/#operation/NetworkPrune")
    @PostMapping(path = "/networks/prune")
    public PruneResponse networksPrune(@RequestParam(name = "filters", required = false) String filters,
                                       @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            PruneCmd cmd = client.pruneCmd(PruneType.NETWORKS);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    //========================================================Volumes==========================================================================
    @ApiOperation(value = "List volumes", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeList")
    @GetMapping(path = "/volumes")
    public ListVolumesResponse volumesList(@RequestParam(name = "filters", required = false) String filters,
                                           @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            ListVolumesCmd cmd = client.listVolumesCmd();
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    @ApiOperation(value = "Create a volume", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeCreate")
    @PostMapping(path = "/volumes/create")
    public CreateVolumeResponse volumesCreate(@RequestBody Volume volume,//TODO
                                              @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.createVolumeCmd().exec();
        }
    }

    @ApiOperation(value = "Inspect a volume", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeInspect")
    @GetMapping(path = "/volumes/{name}")
    public InspectVolumeResponse volumesInspect(@PathVariable("name") String name,
                                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.inspectVolumeCmd(name).exec();
        }
    }

    @ApiOperation(value = "Remove a volume", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumeDelete")
    @DeleteMapping(path = "/volumes/{name}")
    public ResultBean volumesDelete(@PathVariable("name") String name,
                                    @RequestParam(name = "force", defaultValue = "false") boolean force,//NOT Support
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.removeVolumeCmd(name).exec());
        }
    }

    @ApiOperation(value = "Delete unused volumes", notes = "https://docs.docker.com/engine/api/v1.40/#operation/VolumePrune")
    @PostMapping(path = "/volumes/prune")
    public PruneResponse volumesPrune(@RequestParam(name = "filters", required = false) String filters,
                                      @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            PruneCmd cmd = client.pruneCmd(PruneType.VOLUMES);
            filters(filters, cmd);
            return cmd.exec();
        }
    }

    //========================================================Exec==========================================================================
    @ApiOperation(value = "Start an exec instance", notes = "https://docs.docker.com/engine/api/v1.40/#operation/ContainerExec")
    @PostMapping(path = "/exec/start/{id}")
    public String execStart(@PathVariable("id") String id,
                            @RequestBody ExecFormVO form,
                            @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayInputStream bais = new ByteArrayInputStream("ls -al /\ndf -P\nexit\n".getBytes());
            ExecStartResultCallback bash = client.execStartCmd(client.execCreateCmd(id).withAttachStdout(true).withAttachStderr(true).withAttachStdin(true).withTty(false).withCmd("bash").exec().getId())
                    .withStdIn(bais)
                    .withDetach(false)
                    .withTty(false)
                    .exec(new ExecStartResultCallback(baos, baos));
            bash.awaitCompletion(10, TimeUnit.SECONDS);
            baos.close();
            return baos.toString();
        }
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
    @PostMapping(path = "/system/auth")
    public AuthResponse systemAuth(@RequestBody AuthConfig authConfig,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.authCmd().withAuthConfig(authConfig).exec();
        }
    }

    @ApiOperation(value = "Get system information", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemInfo")
    @GetMapping(path = "/system/info")
    public Info systemInfo(@ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.infoCmd().exec();
        }
    }

    @ApiOperation(value = "Get version", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemVersion")
    @GetMapping(path = "/system/version")
    public Version systemVersion(@ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return client.versionCmd().exec();
        }
    }

    @ApiOperation(value = "Ping", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemPing")
    @GetMapping(path = "/system/ping")
    public ResultBean systemPing(@ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            return success(client.pingCmd().exec());
        }
    }

    //TODO
    @ApiOperation(value = "Monitor events", notes = "https://docs.docker.com/engine/api/v1.40/#operation/SystemEvents")
    @GetMapping(path = "/system/events")
    public String systemEvents(@RequestParam(name = "since", required = false) String since,
                               @RequestParam(name = "until", required = false) String until,
                               @RequestParam(name = "filters", required = false) String filters,
                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        try (DockerClient client = client()) {
            EventResultCallback res = new EventResultCallback();
            filters(filters, client.eventsCmd().withSince(since).withUntil(until)).exec(res);
            res.awaitCompletion().close();
            return res.sb.toString();
        }
    }

    // NOT Support: https://docs.docker.com/engine/api/v1.40/#operation/SystemDataUsage

    public class LogContainerResultCallback extends ResultCallbackTemplate<LogContainerResultCallback, Frame> {
        StringBuilder sb = new StringBuilder(1024);

        public void onNext(Frame item) {
            sb.append(new String(item.getPayload()));
        }
    }

    public class StatsContainerResultCallback extends ResultCallbackTemplate<StatsContainerResultCallback, Statistics> {
        Statistics stats;

        public void onNext(Statistics statistics) {
            stats = statistics;
        }
    }

    public class AttachContainerResultCallback extends ResultCallbackTemplate<LogContainerResultCallback, Frame> {
        StringBuilder sb = new StringBuilder(1024);

        public void onNext(Frame item) {
            sb.append(new String(item.getPayload()));
        }
    }

    public class WaitContainerResultCallback extends ResultCallbackTemplate<WaitContainerResultCallback, WaitResponse> {
        @CheckForNull
        private WaitResponse waitResponse = null;

        public void onNext(WaitResponse waitResponse) {
            this.waitResponse = waitResponse;
        }

        /**
         * Awaits the status code from the container.
         *
         * @throws DockerClientException if the wait operation fails.
         */
        public Integer awaitStatusCode() {
            try {
                awaitCompletion();
            } catch (InterruptedException e) {
                throw new DockerClientException("", e);
            }
            return getStatusCode();
        }

        /**
         * Awaits the status code from the container.
         *
         * @throws DockerClientException if the wait operation fails.
         */
        public Integer awaitStatusCode(long timeout, TimeUnit timeUnit) {
            try {
                if (!awaitCompletion(timeout, timeUnit)) {
                    throw new DockerClientException("Awaiting status code timeout.");
                }
            } catch (InterruptedException e) {
                throw new DockerClientException("Awaiting status code interrupted: ", e);
            }
            return getStatusCode();
        }

        private Integer getStatusCode() {
            if (waitResponse == null) {
                throw new DockerClientException("Error while wait container");
            } else {
                return waitResponse.getStatusCode();
            }
        }
    }

    public class EventResultCallback extends ResultCallbackTemplate<EventResultCallback, Event> {
        StringBuilder sb = new StringBuilder(1024);

        public void onNext(Event item) {
            log.info("Docker Event: " + item.toString());
            sb.append(new String(item.toString()));
        }
    }

    public class ExecStartResultCallback extends ResultCallbackTemplate<ExecStartResultCallback, Frame> {
        private OutputStream stdout, stderr;

        public ExecStartResultCallback(OutputStream stdout, OutputStream stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public ExecStartResultCallback() {
            this(null, null);
        }

        public void onNext(Frame frame) {
            if (frame != null) {
                try {
                    switch (frame.getStreamType()) {
                        case STDOUT:
                        case RAW:
                            if (stdout != null) {
                                stdout.write(frame.getPayload());
                                System.out.println(new String(frame.getPayload()));
                                stdout.flush();
                            }
                            break;
                        case STDERR:
                            if (stderr != null) {
                                stderr.write(frame.getPayload());
                                System.out.println(new String(frame.getPayload()));
                                stderr.flush();
                            }
                            break;
                        default:
                            log.error("unknown stream type:" + frame.getStreamType());
                    }
                } catch (IOException e) {
                    onError(e);
                }

                log.debug(frame.toString());
            }
        }
    }
}
