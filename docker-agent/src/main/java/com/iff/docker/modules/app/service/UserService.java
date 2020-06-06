/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.entity.UserTypeEnum;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * UserService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@Service
public class UserService {
    private final User user = User.builder()
            .id(1000L)
            .userName("admin")
            .password("admin")
            .email("admin@admin.com")
            .name("admin")
            .type(UserTypeEnum.ADMIN)
            .createTime(new Date())
            .updateTime(new Date())
            .build();

    public User get(Long id) {
        return user;
    }

    public User findByUserName(String userName) {
        return user;
    }

}
