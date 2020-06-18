/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iff.docker.config.DockerConfig;
import com.iff.docker.modules.app.entity.DockerEndpoint;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.service.DockerEndpointService;
import com.iff.docker.modules.app.service.DockerProxyService;
import com.iff.docker.modules.app.vo.dashboard.*;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * DockerDashboardController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@Slf4j
@RestController
@RequestMapping(path = "/docker/dashboard", produces = Constant.JSON_UTF8)
public class DockerDashboardController extends BaseController {

    @Autowired
    DockerProxyService proxyService;
    @Autowired
    DockerEndpointService endpointService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    DockerConfig dockerConfig;

    DockerDashboardVO systemInfo(DockerDashboardVO vo, DockerEndpoint endpoint) {
        String[] urlSplit = StringUtils.split(endpoint.getUrl(), ":");
        JSONObject json = new JSONObject();
        try {
            json = proxyService.systemInfo(urlSplit[0], Integer.valueOf(urlSplit[1]));
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        vo.setCups(json.getIntValue("NCPU"));
        vo.setMemories(json.getLongValue("MemTotal"));
        return vo;
    }

    DockerDashboardVO containers(DockerDashboardVO vo, DockerEndpoint endpoint) {
        String[] urlSplit = StringUtils.split(endpoint.getUrl(), ":");
        JSONArray array = new JSONArray();
        try {
            array = proxyService.containersJson(urlSplit[0], Integer.valueOf(urlSplit[1]), true, null, false, null);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        Map<String, Boolean> stacks = new HashMap<>();
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            if (StringUtils.equalsIgnoreCase("running", json.getString("State"))) {
                vo.setUpContainers(vo.getUpContainers() + 1);
            }
            if (StringUtils.containsIgnoreCase(json.getString("Status"), "(healthy)")) {
                vo.setHealthContainers(vo.getHealthContainers() + 1);
            } else if (StringUtils.containsIgnoreCase(json.getString("Status"), "(unhealthy)")) {
                vo.setUnHealthContainers(vo.getUnHealthContainers() + 1);
            }
            if (json.get("Labels") != null) {
                String stackName = (String) json.getObject("Labels", Map.class).get("com.docker.compose.project");
                if (stackName != null) {
                    stacks.put(stackName, true);
                }
            }
        }
        {
            vo.setContainers(array.size());
            vo.setStacks(stacks.size());
        }
        return vo;
    }

    DockerDashboardVO volumes(DockerDashboardVO vo, DockerEndpoint endpoint) {
        String[] urlSplit = StringUtils.split(endpoint.getUrl(), ":");
        JSONObject json = new JSONObject();
        try {
            json = proxyService.volumesList(urlSplit[0], Integer.valueOf(urlSplit[1]), null);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        JSONArray array = json.getObject("Volumes", JSONArray.class);
        vo.setVolumes(array == null ? 0 : array.size());
        return vo;
    }

    DockerDashboardVO images(DockerDashboardVO vo, DockerEndpoint endpoint) {
        String[] urlSplit = StringUtils.split(endpoint.getUrl(), ":");
        JSONArray array = new JSONArray();
        try {
            array = proxyService.imagesList(urlSplit[0], Integer.valueOf(urlSplit[1]), true, null, false);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        vo.setVolumes(array == null ? 0 : array.size());
        long totalSize = 0L;
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            totalSize = totalSize + json.getLongValue("Size");
        }
        vo.setImagesSize(totalSize);
        BigDecimal bigDecimal = BigDecimal.valueOf(totalSize).divide(BigDecimal.valueOf(1024 * 1024 * 1024)).setScale(2, RoundingMode.UP);
        vo.setImagesSizeGb(bigDecimal.toPlainString() + "GB");
        return vo;
    }

    DockerContainerVO container(JSONObject json) {
        DockerContainerVO vo = new DockerContainerVO();
        vo.setId(json.getString("Id"));
        vo.setName(StringUtils.join(json.getObject("Names", String[].class), ","));
        vo.setImage(json.getString("Image"));
        vo.setImageId(json.getString("ImageID"));
        vo.setCreated(new Date(json.getLongValue("Created") * 1000));
        vo.setState(json.getString("State"));
        if (StringUtils.equalsIgnoreCase("running", json.getString("State"))) {
            vo.setRunning(true);
        } else if (StringUtils.equalsIgnoreCase("stopped", json.getString("State"))) {
            vo.setStop(true);
        }
        if (StringUtils.containsIgnoreCase(json.getString("Status"), "(healthy)")) {
            vo.setState("healthy");
            vo.setHealthy(true);
        } else if (StringUtils.containsIgnoreCase(json.getString("Status"), "(unhealthy)")) {
            vo.setState("unhealthy");
            vo.setUnhealthy(true);
        }
        if (json.get("Labels") != null) {
            vo.setStack((String) json.getObject("Labels", Map.class).get("com.docker.compose.project"));
        }
        JSONArray ports = json.getJSONArray("Ports");
        if (ports != null) {
            List<String> portList = new ArrayList<>();
            for (JSONObject exportPort : ports.toJavaList(JSONObject.class)) {
                portList.add(exportPort.getInteger("PublicPort") + ":" + exportPort.getInteger("PrivatePort"));
            }
            vo.setPublishPort(portList);
        }
        JSONObject networkSettings = json.getJSONObject("NetworkSettings");
        JSONObject networks = networkSettings.getJSONObject("Networks");
        if (networks != null) {
            List<String> ipList = new ArrayList<>();
            Set<String> networkNames = networks.keySet();
            for (String networkName : networkNames) {
                ipList.add(networks.getJSONObject(networkName).getString("IPAddress"));
            }
            vo.setIp(ipList);
        }
        return vo;
    }

    @ApiOperation(value = "List EndPoints")
    @GetMapping(path = "/endpoints")
    public List<DockerDashboardVO> endpointsList(@RequestParam(name = "name", required = false) String name,
                                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        List<DockerDashboardVO> list = new ArrayList<>();
        List<DockerEndpoint> all = null;
        if (StringUtils.isNotEmpty(name)) {
            DockerEndpoint byName = endpointService.findByName(name);
            all = Arrays.asList(byName);
        } else {
            all = endpointService.findAll();
        }
        for (DockerEndpoint endpoint : all) {
            DockerDashboardVO vo = new DockerDashboardVO();
            vo.setName(endpoint.getName());
            vo.setPublicIp(endpoint.getPublicIp());
            vo.setUrl(endpoint.getUrl());

            vo = systemInfo(vo, endpoint);
            vo = containers(vo, endpoint);
            vo = volumes(vo, endpoint);
            vo = images(vo, endpoint);
            list.add(vo);
        }
        return list;
    }

    @ApiOperation(value = "List Containers")
    @GetMapping(path = "/containers/{ip}/{port}")
    public List<DockerContainerVO> containersList(@PathVariable("ip") String ip,
                                                  @PathVariable("port") int port,
                                                  @RequestParam(name = "filters", required = false) String filters,
                                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        List<DockerContainerVO> list = new ArrayList<>();
        JSONArray array = proxyService.containersJson(ip, port, true, null, false, filters);
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            DockerContainerVO container = container(json);
            container.setPublicIp(ip);
            list.add(container);
        }
        return list;
    }

    @ApiOperation(value = "List Stacks")
    @GetMapping(path = "/stacks/{ip}/{port}")
    public List<DockerStacksVO> stacksList(@PathVariable("ip") String ip,
                                           @PathVariable("port") int port,
                                           @RequestParam(name = "all", defaultValue = "true") boolean all,
                                           @RequestParam(name = "limit", required = false) Integer limit,
                                           @RequestParam(name = "size", defaultValue = "false") boolean size,
                                           @RequestParam(name = "filters", required = false) String filters,
                                           @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        List<DockerStacksVO> list = new ArrayList<>();
        JSONArray array = proxyService.containersJson(ip, port, all, limit, false, filters);
        Map<String, List<DockerContainerVO>> stacks = new HashMap<>();
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            if (json.get("Labels") == null) {
                continue;
            }
            String stackName = (String) json.getObject("Labels", Map.class).get("com.docker.compose.project");
            if (stackName == null) {
                continue;
            }
            List<DockerContainerVO> containers = stacks.get(stackName);
            if (containers == null) {
                containers = new ArrayList<>();
                stacks.put(stackName, containers);
            }
            DockerContainerVO container = container(json);
            container.setPublicIp(ip);
            containers.add(container);
        }
        for (String stack : stacks.keySet()) {
            DockerStacksVO vo = new DockerStacksVO();
            vo.setName(stack);
            vo.setType("Compose");
            vo.setContainers(stacks.get(stack));
            list.add(vo);
        }
        return list;
    }

    @ApiOperation(value = "List Images")
    @GetMapping(path = "/images/{ip}/{port}")
    public List<DockerImagesVO> imagesList(@PathVariable("ip") String ip,
                                           @PathVariable("port") int port,
                                           @RequestParam(name = "all", defaultValue = "true") boolean all,
                                           @RequestParam(name = "filters", required = false) String filters,
                                           @RequestParam(name = "digests", defaultValue = "false") boolean digests,/*NOT Support*/
                                           @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        List<DockerImagesVO> list = new ArrayList<>();
        JSONArray array = proxyService.imagesList(ip, port, all, filters, digests);
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            DockerImagesVO vo = new DockerImagesVO();
            list.add(vo);
            vo.setId(json.getString("Id"));
            vo.setCreated(new Date(json.getLongValue("Created") * 1000));
            vo.setSize(json.getLongValue("Size"));
            long size = json.getLongValue("Size") / 1024 / 1024;
            vo.setSizeMb(size + "MB");
            List<String> tags = new ArrayList<>();
            vo.setTags(tags);
            JSONArray repoTags = json.getJSONArray("RepoTags");
            if (repoTags != null) {
                tags.addAll(repoTags.toJavaList(String.class));
            }
        }
        return list;
    }

    @ApiOperation(value = "List Volumes")
    @GetMapping(path = "/volumes/{ip}/{port}")
    public List<DockerVolumesVO> volumesList(@PathVariable("ip") String ip,
                                             @PathVariable("port") int port,
                                             @RequestParam(name = "filters", required = false) String filters,
                                             @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        List<DockerVolumesVO> list = new ArrayList<>();
        JSONObject object = proxyService.volumesList(ip, port, filters);
        JSONArray array = object.getJSONArray("Volumes");
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            DockerVolumesVO vo = new DockerVolumesVO();
            list.add(vo);
            vo.setName(json.getString("Name"));
            vo.setNameShort(vo.getName().substring(0, 4) + "..." + vo.getName().substring(vo.getName().length() - 4));
            vo.setDriver(json.getString("Driver"));
            vo.setMountPoint(json.getString("Mountpoint"));
            String hash = StringUtils.removeStart(vo.getMountPoint(), "/var/lib/docker/volumes/");
            hash = StringUtils.removeEnd(hash, "/_data");
            if (hash.length() > 60) {
                hash = hash.substring(0, 4) + "..." + hash.substring(hash.length() - 4);
                vo.setMountPointShort("/var/lib/docker/volumes/" + hash + "/_data");
            }
        }
        return list;
    }

    @ApiOperation(value = "List Networks")
    @GetMapping(path = "/networks/{ip}/{port}")
    public List<DockerNetworksVO> networksList(@PathVariable("ip") String ip,
                                               @PathVariable("port") int port,
                                               @RequestParam(name = "filters", required = false) String filters,
                                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        List<DockerNetworksVO> list = new ArrayList<>();
        JSONArray array = proxyService.networksList(ip, port, filters);
        for (JSONObject json : array.toJavaList(JSONObject.class)) {
            DockerNetworksVO vo = new DockerNetworksVO();
            list.add(vo);
            vo.setId(json.getString("Id"));
            vo.setName(json.getString("Name"));
            vo.setScope(json.getString("Scope"));
            vo.setDriver(json.getString("Driver"));
            vo.setAttachable(json.getBooleanValue("Attachable"));
            vo.setInternal(json.getBooleanValue("Internal"));
            JSONObject ipam = json.getJSONObject("IPAM");
            if (ipam == null) {
                continue;
            }
            vo.setIpamDriver(ipam.getString("Driver"));
            JSONArray configs = ipam.getJSONArray("Config");
            if (configs == null) {
                continue;
            }
            List<String> subnets = new ArrayList<>();
            List<String> gateways = new ArrayList<>();
            for (JSONObject config : configs.toJavaList(JSONObject.class)) {
                subnets.add(config.getString("Subnet"));
                gateways.add(config.getString("Gateway"));
            }
            vo.setIpamSubnet(subnets);
            vo.setIpamGateway(gateways);

        }
        return list;
    }

    @ApiOperation(value = "List Images In Repository")
    @GetMapping(path = "/repository/images")
    public List<DockerImagesRepositoryVO.Image> repositoryImagesList(@ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        List<DockerImagesRepositoryVO.Image> list = new ArrayList<>();
        String url = dockerConfig.getNexusRepoUrl();
        if (StringUtils.isEmpty(url)) {
            return list;
        }
        log.debug("DockerProxy: " + url);
        DockerImagesRepositoryVO repo = restTemplate.getForObject(url, DockerImagesRepositoryVO.class);
        if (repo.getItems() != null) {
            list.addAll(repo.getItems());
        }
        return list;
    }
}
