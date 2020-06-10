/*******************************************************************************
 * Copyright (c) 2020-06-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.config.RsaConfig;
import com.iff.docker.modules.app.entity.DockerCompose;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.service.DockerComposeService;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.ResultBean;
import com.iff.docker.modules.util.RSAHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

/**
 * DockerComposeProxyController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Slf4j
@RestController
@RequestMapping(path = "/docker/compose/proxy", produces = Constant.JSON_UTF8)
public class DockerComposeProxyController extends BaseController {
    @Autowired
    DockerComposeService composeService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    RsaConfig rsaConfig;

    HttpEntity<String> httpEntity() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("sign", RSAHelper.encryptToHex(String.valueOf(System.currentTimeMillis()), rsaConfig.getPubKey()));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new HttpEntity<String>(null, requestHeaders);
    }

    <T> HttpEntity<T> httpEntity(T body) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("sign", RSAHelper.encryptToHex(String.valueOf(System.currentTimeMillis()), rsaConfig.getPubKey()));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new HttpEntity<T>(body, requestHeaders);
    }

    @DeleteMapping("/{ip}/{port}/{name}")
    public ResultBean composeDelete(@PathVariable("ip") String ip,
                                    @PathVariable("port") int port,
                                    @PathVariable("name") String name,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/" + name;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    @PostMapping("/kill/{ip}/{port}/{name}")
    public ResultBean composeKill(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/kill/" + name;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    @PostMapping("/stop/{ip}/{port}/{name}")
    public ResultBean composeStop(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/stop/" + name;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    @PostMapping("/start/{ip}/{port}/{name}")
    public ResultBean composeStart(@PathVariable("ip") String ip,
                                   @PathVariable("port") int port,
                                   @PathVariable("name") String name,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/start/" + name;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    @PostMapping("/restart/{ip}/{port}/{name}")
    public ResultBean composeRestart(@PathVariable("ip") String ip,
                                     @PathVariable("port") int port,
                                     @PathVariable("name") String name,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/restart/" + name;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }

    @PostMapping("/up/{ip}/{port}/{name}")
    public ResultBean composeUp(@PathVariable("ip") String ip,
                                @PathVariable("port") int port,
                                @PathVariable("name") String name,
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerCompose compose = composeService.findByName(name);
        compose.getConfigFiles().stream().forEach(configFile -> {
        });
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/up/";
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(compose), ResultBean.class);
        return exchange.getBody();
    }

    @PostMapping("/down/{ip}/{port}/{name}")
    public ResultBean composeDown(@PathVariable("ip") String ip,
                                  @PathVariable("port") int port,
                                  @PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String url = "http://" + ip + ":" + port + "/agent/docker/compose/down/" + name;
        ResponseEntity<ResultBean> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity(), ResultBean.class);
        return exchange.getBody();
    }
}
