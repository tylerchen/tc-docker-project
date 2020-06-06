/*******************************************************************************
 * Copyright (c) 2020-06-05 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.vo.form.RegisterFormVO;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.common.LastRequestHolder;
import com.iff.docker.modules.common.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * RegisterController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-05
 */
@Slf4j
@RestController
@RequestMapping(path = "/register", produces = Constant.JSON_UTF8)
public class RegisterController extends CustomBaseController {
    @PostMapping(path = "/")
    public ResultBean containersJson(@RequestBody RegisterFormVO form,
                                     @ApiIgnore @RequestAttribute(Constant.LOGIN_USER) User user) throws Exception {
        setServerHost(form.getHost());
        setServerPort(form.getPort());
        LastRequestHolder.setLastRequest(form.getCurrentTime());
        return success();
    }
}
