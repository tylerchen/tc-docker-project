/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package com.iff.docker.modules.app.dao;

import com.iff.docker.modules.app.entity.DockerUser;
import com.iff.docker.modules.common.BaseDao;

/**
 * DockerUserDao
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
public interface DockerUserDao extends BaseDao<DockerUser> {
    DockerUser findByUserName(String userName);
}
