/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.dao.UserDao;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.common.BaseService;
import com.iff.docker.modules.common.Constant;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * UserService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@Service
public class UserService extends BaseService<User, UserDao> {

    @Cacheable(value = Constant.CACHE_USER, key = "'get_'+#p0", unless = "#result==null")
    public User get(Long id) {
        return dao.findById(id).get();
    }

    @Cacheable(value = Constant.CACHE_USER, key = "'findByUserName_'+#p0", unless = "#result==null")
    public User findByUserName(String userName) {
        return dao.findByUserName(userName);
    }

}
