/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package com.iff.docker.modules.app.dao;

import com.iff.docker.modules.app.entity.DockerTeamUser;
import com.iff.docker.modules.common.BaseDao;

import java.util.List;

/**
 * DockerTeamUserDao
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
public interface DockerTeamUserDao extends BaseDao<DockerTeamUser> {
    List<DockerTeamUser> findByTeamId(Long teamId);

    List<DockerTeamUser> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByTeamId(Long teamId);
}
