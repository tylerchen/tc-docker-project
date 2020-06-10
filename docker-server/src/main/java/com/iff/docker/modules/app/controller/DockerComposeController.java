/*******************************************************************************
 * Copyright (c) 2020-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.modules.app.entity.*;
import com.iff.docker.modules.app.service.DockerComposeConfigFileService;
import com.iff.docker.modules.app.service.DockerComposeService;
import com.iff.docker.modules.app.vo.form.DockerComposeConfigFileFormVO;
import com.iff.docker.modules.app.vo.form.DockerComposeFormVO;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.PageModel;
import com.iff.docker.modules.common.ResultBean;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
import java.util.Objects;

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
                    .dataDirs(form.getDataDirs())
                    .description(form.getDescription())
                    .composeFile(FileContent.builder().content(form.getContent()).name("docker-compose.yml").build())
                    .build();
        } else {
            entity = composeService.findById(form.getId());
            entity.setName(StringUtils.defaultIfBlank(form.getName(), entity.getName()));
            entity.setDataDirs(StringUtils.defaultIfBlank(form.getDataDirs(), entity.getDataDirs()));
            entity.setDescription(StringUtils.defaultIfBlank(form.getDescription(), entity.getDescription()));
            if (form.getContent() != null) {
                entity.getComposeFile().setContent(form.getContent());
            }
        }
        composeService.save(entity);
        return success(entity.getId());
    }

    @PostMapping("/configFile")
    public ResultBean composeAddOrUpdateConfigFile(@RequestBody DockerComposeConfigFileFormVO form,
                                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerCompose entity = composeService.findById(form.getComposeId());
        Objects.requireNonNull(entity, "Compose file not found.");
        DockerComposeConfigFile config = null;
        if (form.getId() != null) {
            config = configFileService.findById(form.getId());
            config.getFileContent().setContent(form.getContent());
        } else {
            config = DockerComposeConfigFile.builder()
                    .composeId(form.getComposeId()).name(form.getName()).description(form.getDescription())
                    .fileContent(FileContent.builder().name(form.getName()).content(form.getContent()).build())
                    .build();
        }
        configFileService.save(config);
        composeService.save(entity);
        return success(entity.getId());
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

    @DeleteMapping("/configFile/{id}")
    public ResultBean composeConfigDelete(@PathVariable("id") Long id,
                                          @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerComposeConfigFile config = configFileService.findById(id);
        DockerCompose compose = config.getCompose();
        compose.getConfigFiles().remove(config);
        composeService.save(compose);
        return success();
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

    @PostMapping("/up")
    public ResultBean composeUp(@PathVariable("id") Long id,
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        String[] cmd = new String[]{"docker-compose", "up", "-d"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DockerCompose compose = composeService.findById(id);
        try {
            createComposeFile(compose);
            Map<String, String> environment = new HashMap<>();
            new ProcessExecutor().command(new String[]{})
                    .redirectOutput(baos)//Slf4jStream.of(log).asInfo()
                    .redirectError(baos)//Slf4jStream.of(log).asInfo() // docker-compose will log pull information to stderr
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

    private String composeBaseDir() {
        return "/opt/compose/";
    }

    private void createComposeFile(DockerCompose compose) throws Exception {
        File dir = new File(composeBaseDir() + compose.getName());
        String lastUpdateTime = String.valueOf(compose.getUpdateTime().getTime());
        File updateFile = new File(dir, "___last_update.txt");
        if (updateFile.exists() && updateFile.isFile() && lastUpdateTime.equals(FileUtils.readFileToString(updateFile))) {
            log.info("Compose files is up to date.");
            return;
        }
        FileUtils.forceMkdir(dir);
        FileUtils.writeStringToFile(new File(dir, "docker-compose.yml"), compose.getComposeFile().getContent(), "UTF-8", false);
        FileUtils.writeStringToFile(updateFile, lastUpdateTime, "UTF-8", false);
        for (DockerComposeConfigFile config : compose.getConfigFiles()) {
            FileUtils.writeStringToFile(new File(dir, config.getName()), config.getFileContent().getContent(), "UTF-8", false);
        }
    }
}
