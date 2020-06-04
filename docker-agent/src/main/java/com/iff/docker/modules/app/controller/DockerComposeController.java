/*******************************************************************************
 * Copyright (c) 2020-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.modules.app.entity.DockerCompose;
import com.iff.docker.modules.app.entity.FileContent;
import com.iff.docker.modules.app.entity.QDockerCompose;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.service.DockerComposeConfigFileService;
import com.iff.docker.modules.app.service.DockerComposeService;
import com.iff.docker.modules.app.vo.form.DockerComposeFormVO;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.PageModel;
import com.iff.docker.modules.common.ResultBean;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
public class DockerComposeController extends BaseController {

    @Autowired
    DockerComposeService composeService;
    @Autowired
    DockerComposeConfigFileService configFileService;

    @PostMapping("/")
    public ResultBean composeCreateAndUpdate(@RequestBody DockerComposeFormVO form,
                                             @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerCompose entity = null;
        if (form.getId() == null) {
            entity = DockerCompose.builder()
                    .name(form.getName())
                    .description(form.getDescription())
                    .composeFile(FileContent.builder().content(form.getContent()).name("docker-compose.yml").build())
                    .build();
        } else {
            entity = composeService.findById(form.getId());
            entity.setName(StringUtils.defaultIfBlank(form.getName(), entity.getName()));
            entity.setDescription(StringUtils.defaultIfBlank(form.getDescription(), entity.getDescription()));
            if (form.getContent() != null) {
                entity.getComposeFile().setContent(form.getContent());
            }
        }
        return success(composeService.save(entity));
    }

    @GetMapping("/info/{name}")
    public ResultBean composeInfo(@PathVariable("name") String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerCompose compose = composeService.findByName(name);
        compose.getConfigFiles().stream().forEach(configFile -> {
        });
        return success(compose);
    }

    @GetMapping("/list")
    public ResultBean composeList(PageModel page,
                                  @RequestParam(name = "name", required = false) String name,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerCompose.dockerCompose.name.like("%" + name + "%"));
        }
        return success(composeService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/{id}")
    public ResultBean composeDelete(@PathVariable("id") Long id,
                                    @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        composeService.delete(id);
        return success();
    }

    @PostMapping("/kill")
    public ResultBean composeKill(@PathVariable("id") Long id,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "kill"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
                    .environment(environment)
                    .directory(new File("/opt/compose/" + compose.getName()))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/stop")
    public ResultBean composeStop(@PathVariable("id") Long id,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "stop"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
                    .environment(environment)
                    .directory(new File("/opt/compose/" + compose.getName()))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/start")
    public ResultBean composeStart(@PathVariable("id") Long id,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "start"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
                    .environment(environment)
                    .directory(new File("/opt/compose/" + compose.getName()))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/restart")
    public ResultBean composeRestart(@PathVariable("id") Long id,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "restart"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
                    .environment(environment)
                    .directory(new File("/opt/compose/" + compose.getName()))
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
    public ResultBean composeUp(@PathVariable("id") Long id,
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "up", "-d"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
                    .environment(environment)
                    .directory(new File("/opt/compose/" + compose.getName()))
                    .exitValueNormal()
                    .executeNoTimeout();
            return success(baos.toString());
        } catch (InvalidExitValueException e) {
            return error("Local Docker Compose exited abnormally with code " + e.getExitValue() + " whilst running command: " + cmd + "\n" + baos.toString());
        } catch (Exception e) {
            return error("Error running local Docker Compose command: " + cmd + "\n" + baos.toString(), e);
        }
    }

    @PostMapping("/down")
    public ResultBean composeDown(@PathVariable("id") Long id,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "down"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
                    .environment(environment)
                    .directory(new File("/opt/compose/" + compose.getName()))
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
