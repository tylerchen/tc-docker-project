/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.modules.app.entity.*;
import com.iff.docker.modules.app.service.*;
import com.iff.docker.modules.app.vo.form.*;
import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.PageModel;
import com.iff.docker.modules.common.ResultBean;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * DockerManagementController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
@Slf4j
@RestController
@RequestMapping(path = "/docker/management", produces = Constant.JSON_UTF8)
public class DockerManagementController extends BaseController {

    @Autowired
    DockerEndpointService endpointService;
    @Autowired
    DockerGroupService groupService;
    @Autowired
    DockerGroupEndpointService groupEndpointService;
    @Autowired
    DockerTeamService teamService;
    @Autowired
    DockerTeamUserService teamUserService;
    @Autowired
    DockerUserService userService;

    @PostMapping("/endpoint")
    public ResultBean endpointCreateAndUpdate(@RequestBody DockerEndpointFormVO form,
                                              @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerEndpoint entity = null;
        if (form.getId() == null) {
            entity = DockerEndpoint.builder()
                    .name(form.getName())
                    .type(StringUtils.defaultIfBlank(form.getType(), "Agent"))
                    .url(form.getUrl())
                    .publicIp(form.getPublicIp())
                    .build();
        } else {
            entity = endpointService.findById(form.getId());
            entity.setName(StringUtils.defaultIfBlank(form.getName(), entity.getName()));
            entity.setType(StringUtils.defaultIfBlank(form.getType(), entity.getType()));
            entity.setUrl(StringUtils.defaultIfBlank(form.getUrl(), entity.getUrl()));
            entity.setPublicIp(StringUtils.defaultIfBlank(form.getPublicIp(), entity.getPublicIp()));
        }
        return success(endpointService.save(entity));
    }

    @GetMapping("/endpoint/{name}")
    public ResultBean endpointInfo(@PathVariable("name") String name,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        return success(endpointService.findByName(name));
    }

    @GetMapping("/endpoint")
    public ResultBean endpointList(PageModel page,
                                   @RequestParam(name = "name", required = false) String name,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerEndpoint.dockerEndpoint.name.like("%" + name + "%"));
        }
        return success(endpointService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/endpoint/{id}")
    public ResultBean endpointDelete(@PathVariable("id") Long id,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        {// remove Endpoint from Group
            groupEndpointService.deleteByEndpointId(id);
        }
        endpointService.delete(id);
        return success();
    }


    @PostMapping("/group")
    public ResultBean groupCreateAndUpdate(@RequestBody DockerGroupFormVO form,
                                           @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerGroup entity = null;
        if (form.getId() == null) {
            entity = DockerGroup.builder()
                    .name(form.getName())
                    .description(form.getDescription())
                    .build();
        } else {
            entity = groupService.findById(form.getId());
            entity.setName(StringUtils.defaultIfBlank(form.getName(), entity.getName()));
            entity.setDescription(StringUtils.defaultString(form.getDescription(), entity.getDescription()));
        }
        return success(groupService.save(entity));
    }

    @GetMapping("/group/{name}")
    public ResultBean groupInfo(@PathVariable("name") String name,
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        return success(groupService.findByName(name));
    }

    @GetMapping("/group")
    public ResultBean groupList(PageModel page,
                                @RequestParam(name = "name", required = false) String name,
                                @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerGroup.dockerGroup.name.like("%" + name + "%"));
        }
        return success(groupService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/group/{id}")
    public ResultBean groupDelete(@PathVariable("id") Long id,
                                  @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        {// remove Endpoint from group
            groupEndpointService.deleteByGroupId(id);
        }
        groupService.delete(id);
        return success();
    }


    @PostMapping("/groupEndpoint")
    public ResultBean groupEndpointCreateAndUpdate(@RequestBody DockerGroupEndpointFormVO form,
                                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerGroupEndpoint entity = null;
        if (form.getId() == null) {
            entity = DockerGroupEndpoint.builder()
                    .groupId(form.getGroupId())
                    .endpointId(form.getEndpointId())
                    .build();
        } else {
            return error("Update not support.");
        }
        return success(groupEndpointService.save(entity));
    }

    @GetMapping("/groupEndpoint")
    public ResultBean groupEndpointList(PageModel page,
                                        @RequestParam(name = "id", required = false) Long id,
                                        @RequestParam(name = "name", required = false) String name,
                                        @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (id != null) {
            conditions.and(QDockerGroupEndpoint.dockerGroupEndpoint.id.eq(id));
        }
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerGroupEndpoint.dockerGroupEndpoint.group.name.like("%" + name + "%"));
        }
        return success(groupEndpointService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/groupEndpoint/{id}")
    public ResultBean groupEndpointDelete(@PathVariable("id") Long id,
                                          @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        groupEndpointService.delete(id);
        return success();
    }

    @PostMapping("/team")
    public ResultBean teamCreateAndUpdate(@RequestBody DockerTeamFormVO form,
                                          @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerTeam entity = null;
        if (form.getId() == null) {
            entity = DockerTeam.builder()
                    .name(form.getName())
                    .build();
        } else {
            entity = teamService.findById(form.getId());
            entity.setName(StringUtils.defaultIfBlank(form.getName(), entity.getName()));
        }
        return success(teamService.save(entity));
    }

    @GetMapping("/team/{name}")
    public ResultBean teamInfo(@PathVariable("name") String name,
                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        return success(teamService.findByName(name));
    }

    @GetMapping("/team")
    public ResultBean teamList(PageModel page,
                               @RequestParam(name = "name", required = false) String name,
                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerTeam.dockerTeam.name.like("%" + name + "%"));
        }
        return success(teamService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/team/{id}")
    public ResultBean teamDelete(@PathVariable("id") Long id,
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        teamService.delete(id);
        return success();
    }

    @PostMapping("/user")
    public ResultBean userCreateAndUpdate(@RequestBody DockerUserFormVO form,
                                          @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerUser entity = null;
        if (form.getId() == null) {
            entity = DockerUser.builder()
                    .userName(form.getUserName())
                    .password(form.getPassword())
                    .email(form.getEmail())
                    .type(DockerUserTypeEnum.valueOf(form.getType()))
                    .name(form.getName())
                    .build();
        } else {
            entity = userService.findById(form.getId());
            entity.setPassword(StringUtils.defaultIfBlank(form.getPassword(), entity.getPassword()));
            entity.setEmail(StringUtils.defaultIfBlank(form.getEmail(), entity.getEmail()));
            entity.setType(DockerUserTypeEnum.valueOf(StringUtils.defaultIfBlank(form.getType(), entity.getType().getName())));
            entity.setName(StringUtils.defaultIfBlank(form.getName(), entity.getName()));
        }
        return success(userService.save(entity));
    }

    @GetMapping("/user/{userName}")
    public ResultBean userInfo(@PathVariable("userName") String userName,
                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        return success(userService.findByUserName(userName));
    }

    @GetMapping("/user")
    public ResultBean userList(PageModel page,
                               @RequestParam(name = "userName", required = false) String userName,
                               @RequestParam(name = "name", required = false) String name,
                               @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (StringUtils.isNotBlank(userName)) {
            conditions.and(QDockerUser.dockerUser.userName.like("%" + userName + "%"));
        }
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerUser.dockerUser.name.like("%" + name + "%"));
        }
        return success(userService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/user/{id}")
    public ResultBean userDelete(@PathVariable("id") Long id,
                                 @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        {// remove user from team
            teamUserService.deleteByUserId(id);
        }
        userService.delete(id);
        return success();
    }

    @PostMapping("/teamUser")
    public ResultBean teamUserCreateAndUpdate(@RequestBody DockerTeamUserFormVO form,
                                              @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        DockerTeamUser entity = null;
        if (form.getId() == null) {
            entity = DockerTeamUser.builder()
                    .teamId(form.getTeamId())
                    .userId(form.getUserId())
                    .build();
        } else {
            return error("Update not support.");
        }
        return success(teamUserService.save(entity));
    }

    @GetMapping("/teamUser")
    public ResultBean teamUserList(PageModel page,
                                   @RequestParam(name = "id", required = false) Long id,
                                   @RequestParam(name = "name", required = false) String name,
                                   @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (id != null) {
            conditions.and(QDockerTeamUser.dockerTeamUser.id.eq(id));
        }
        if (StringUtils.isNotBlank(name)) {
            conditions.and(QDockerTeamUser.dockerTeamUser.team.name.like("%" + name + "%"));
        }
        return success(teamUserService.findAll(conditions, PageModel.toPage(page)));
    }

    @DeleteMapping("/teamUser/{id}")
    public ResultBean teamUserDelete(@PathVariable("id") Long id,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) {
        {// remove user from team
            teamUserService.deleteByTeamId(id);
        }
        teamUserService.delete(id);
        return success();
    }
}
