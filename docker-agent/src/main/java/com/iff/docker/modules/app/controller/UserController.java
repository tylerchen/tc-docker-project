/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.modules.common.BaseController;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.PageModel;
import com.iff.docker.modules.common.ResultBean;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.entity.QUser;
import com.iff.docker.modules.app.entity.UserTypeEnum;
import com.iff.docker.modules.app.service.UserService;
import com.iff.docker.modules.util.TokenUtil;
import com.iff.docker.modules.app.vo.form.ResetPasswordFormVO;
import com.iff.docker.modules.app.vo.form.UserFormVO;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * UserController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@Slf4j
@RestController
@RequestMapping(path = "/user", produces = Constant.JSON_UTF8)
public class UserController extends BaseController {
    @Autowired
    UserService userService;
    @Autowired
    RestTemplate restTemplate;

    @PostMapping("/login")
    public ResultBean login(UserFormVO update, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.findByUserName(update.getUserName());
        if (user == null) {
            return error("UserName or Password incorrect.");
        }
        if (!user.getPassword().equals(update.getPassword())) {
            return error("UserName or Password incorrect.");
        }
        userService.detach(user);
        response.setHeader("token", TokenUtil.toToken(user.getUserName(), user.getPassword()));
        user.setPassword("");
        return success(user.getId());
    }

    @PostMapping(path = "/resetPassword", consumes = Constant.JSON_UTF8)
    public ResultBean resetPassword(@RequestBody ResetPasswordFormVO update, @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (!user.getType().isAdmin() && !user.getId().equals(update.getId())) {
            return error("Can not update user");
        }
        User resetUser = userService.findById(update.getId());
        if (user.getType().isAdmin()) {
            resetUser.setPassword(update.getPassword());
        } else if (resetUser.getPassword().equals(update.getOldPassword())) {
            resetUser.setPassword(update.getPassword());
        } else {
            return error("Password incorrect");
        }
        userService.save(resetUser);
        return success();
    }

    @GetMapping("/get/{id}")
    public ResultBean get(@PathVariable("id") Long id) {
        return success(userService.get(id));
    }

    @GetMapping("/getLoginUser")
    public ResultBean getLoginUser(@RequestAttribute(Constant.LOGIN_USER) User user) {
        User byId = userService.findById(user.getId());
        return success(byId);
    }

    @GetMapping("/getByUserName/{userName}")
    public ResultBean get(@PathVariable("userName") String userName) {
        return success(userService.findByUserName(userName));
    }

    @GetMapping("/list")
    public ResultBean list(@RequestParam(name = "userName", required = false) String userName, @RequestAttribute(Constant.LOGIN_USER) User user, PageModel page) {
        BooleanBuilder conditions = new BooleanBuilder();
        if (!user.getType().isAdmin() && !user.getType().isAdminReadOnly()) {
            conditions.and(QUser.user.id.eq(user.getId()));
        } else {
            if (StringUtils.isNotBlank(userName)) {
                conditions.and(QUser.user.userName.like("%" + userName + "%"));
            }
        }
        return success(userService.findAll(conditions, PageModel.toPage(page)));
    }

    @PostMapping(path = "/update", consumes = Constant.JSON_UTF8)
    public ResultBean add(@RequestBody UserFormVO update, @RequestAttribute(Constant.LOGIN_USER) User user) {
        if (!user.getType().isAdmin() && !user.getId().equals(update.getId())) {
            return error("Can not update user");
        }
        User build = null;
        if (update.getId() == null) {
            build = User.builder()
                    .id(update.getId())
                    .userName(update.getUserName())
                    .password(update.getPassword())
                    .email(update.getEmail())
                    .name(update.getName())
                    .build();
        } else {
            build = userService.get(update.getId());
            if (StringUtils.isNotBlank(update.getEmail())) {
                build.setEmail(update.getEmail());
            }
            if (StringUtils.isNotBlank(update.getName())) {
                build.setName(update.getName());
            }
            if (!StringUtils.equals(update.getType(), build.getType().name())) {
                if (user.getType().isAdmin()) {
                    build.setType(UserTypeEnum.valueOf(update.getType()));
                }
            }
        }
        userService.save(build);
        return success(build.getId());
    }

//    @GetMapping("/findAll")
//    public ResultBean findAll() {
//        return success(userService.findAll());
//    }
//
//    @GetMapping("/rest")
//    public ResultBean rest() {
//        String url = "https://localhost:8080/tpl/user/findAll";
//        ResponseEntity<ResultBean> result = restTemplate.getForEntity(url, ResultBean.class);
//        return success(result.getBody());
//    }
}
