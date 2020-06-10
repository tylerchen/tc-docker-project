/*******************************************************************************
 * Copyright (c) 2020-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.iff.docker.config.DockerConfig;
import com.iff.docker.config.RestTemplateConfig;
import com.iff.docker.modules.app.entity.DockerCompose;
import com.iff.docker.modules.app.entity.DockerComposeConfigFile;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import springfox.documentation.annotations.ApiIgnore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * DockerComposeController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-29
 */
@Slf4j
@RestController
@RequestMapping(path = "/docker/compose", produces = Constant.JSON_UTF8)
public class DockerComposeController extends CustomBaseController {

    @Autowired
    RestTemplateConfig restTemplateConfig;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    DockerConfig dockerConfig;

    private String restPath(String path) {
        return (restTemplateConfig.isSslEnable() ? "https://" : "http://") + getServerHost() + ":" + getServerPort() + "/docker/" + path;
    }

    private DockerCompose composeInfo(String name) {
        String url = restPath("/docker/compose/info/{name}");
        String value = restTemplate.getForObject(url, String.class);
        ResultBean<DockerCompose> resultBean = JSON.parseObject(value, new TypeReference<ResultBean<DockerCompose>>() {
        }.getType());
        return resultBean.getData();
    }

    @DeleteMapping("/{name}")
    public ResultBean composeDelete(@PathVariable("name") String name,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        deleteComposeFile(name);
        return success();
    }

    @PostMapping("/kill/{name}")
    public ResultBean composeKill(@PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        if (!existsComposeFile(name)) {
            return error("Docker Compose file is not exists.");
        }
        String[] cmd = new String[]{"docker-compose", "kill"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)
                    .redirectError(baos)
                    .environment(environment)
                    .directory(new File(composeBaseDir() + name))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/stop/{name}")
    public ResultBean composeStop(@PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (!existsComposeFile(name)) {
            return error("Docker Compose file is not exists.");
        }
        String[] cmd = new String[]{"docker-compose", "stop"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)
                    .redirectError(baos)
                    .environment(environment)
                    .directory(new File(composeBaseDir() + name))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/start/{name}")
    public ResultBean composeStart(@PathVariable("name") String name,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (!existsComposeFile(name)) {
            return error("Docker Compose file is not exists.");
        }
        String[] cmd = new String[]{"docker-compose", "start"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)
                    .redirectError(baos)
                    .environment(environment)
                    .directory(new File(composeBaseDir() + name))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/restart/{name}")
    public ResultBean composeRestart(@PathVariable("name") String name,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (!existsComposeFile(name)) {
            return error("Docker Compose file is not exists.");
        }
        String[] cmd = new String[]{"docker-compose", "restart"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)
                    .redirectError(baos)
                    .environment(environment)
                    .directory(new File(composeBaseDir() + name))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/up")
    public ResultBean composeUp(@RequestBody DockerCompose compose,
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (existsComposeFile(compose.getName())) {//down first
            String[] cmd = new String[]{"docker-compose", "down"};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                Map<String, String> environment = new HashMap<>();
                new ProcessExecutor().command(new String[]{})
                        .redirectOutput(baos)
                        .redirectError(baos)
                        .environment(environment)
                        .directory(new File(composeBaseDir() + compose.getName()))
                        .exitValueNormal()
                        .executeNoTimeout();
            } catch (InvalidExitValueException e) {
                return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
            } catch (Exception e) {
                return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
            }
        }
        {// up
            String[] cmd = new String[]{"docker-compose", "up", "-d"};
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                createComposeFile(compose);
                Map<String, String> environment = new HashMap<>();
                new ProcessExecutor().command(new String[]{})
                        .redirectOutput(baos)
                        .redirectError(baos)
                        .environment(environment)
                        .directory(new File(composeBaseDir() + compose.getName()))
                        .exitValueNormal()
                        .executeNoTimeout();
                return success(baos.toString());
            } catch (InvalidExitValueException e) {
                return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
            } catch (Exception e) {
                return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
            }
        }
    }

    @PostMapping("/down/{name}")
    public ResultBean composeDown(@PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (!existsComposeFile(name)) {
            return error("Docker Compose file is not exists.");
        }
        String[] cmd = new String[]{"docker-compose", "down"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)
                    .redirectError(baos)
                    .environment(environment)
                    .directory(new File(composeBaseDir() + name))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    private String composeBaseDir() {
        String dir = dockerConfig.getComposeDir();
        return StringUtils.removeEnd(dir, "/");
    }

    private String dataDir() {
        String dir = dockerConfig.getDataDir();
        return StringUtils.removeEnd(dir, "/");
    }

    private void createComposeFile(DockerCompose compose) throws Exception {
        File dir = new File(composeBaseDir(), compose.getName());
        String lastUpdateTime = String.valueOf(compose.getUpdateTime().getTime());
        File updateFile = new File(dir, "___last_update.txt");
        if (updateFile.exists() && updateFile.isFile() && lastUpdateTime.equals(FileUtils.readFileToString(updateFile))) {
            log.info("Compose files is up to date.");
            return;
        }
        FileUtils.forceMkdir(dir);
        {// make data dir
            String[] split = StringUtils.split(compose.getDataDirs(), ";");
            if (split != null) {
                String baseDataDir = dataDir();
                for (String dataDir : split) {
                    FileUtils.forceMkdir(new File(baseDataDir, dataDir.trim()));
                }
            }
        }
        FileUtils.writeStringToFile(new File(dir, "docker-compose.yml"), compose.getComposeFile().getContent(), "UTF-8", false);
        FileUtils.writeStringToFile(updateFile, lastUpdateTime, "UTF-8", false);
        for (DockerComposeConfigFile config : compose.getConfigFiles()) {
            FileUtils.writeStringToFile(new File(dir, config.getName()), config.getFileContent().getContent(), "UTF-8", false);
        }
    }

    private void deleteComposeFile(String name) throws Exception {
        File dir = new File(composeBaseDir() + name);
        FileUtils.forceDelete(dir);
    }

    private boolean existsComposeFile(String name) {
        File dir = new File(composeBaseDir() + name);
        return dir.exists() && new File(dir, "docker-compose.yml").exists();
    }
}
