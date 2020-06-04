/*******************************************************************************
 * Copyright (c) 2019-11-11 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.entity.UserTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * DataInitService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-11
 */
@Slf4j
@Component
public class DataInitService implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    UserService userService;


    public void onApplicationEvent(ContextRefreshedEvent event) {
        addUser();
    }

    void addUser() {
        User root = userService.findByUserName("admin");
        if (root == null) {
            userService.save(User.builder().userName("admin").password("admin").email("admin@admin.com").name("admin").type(UserTypeEnum.ADMIN).build());
            for (int i = 1; i <= 9; i++) {
                userService.save(User.builder().userName("test" + i).password("test" + i).email("test@test.com").name("test" + i).build());
            }
        }
    }
}
