/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.dao.DockerTeamUserDao;
import com.iff.docker.modules.app.entity.DockerTeamUser;
import com.iff.docker.modules.common.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DockerTeamUserService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
@Service
public class DockerTeamUserService extends BaseService<DockerTeamUser, DockerTeamUserDao> {

    public List<DockerTeamUser> findByTeamId(Long teamId) {
        return dao.findByTeamId(teamId);
    }

    public List<DockerTeamUser> findByUserId(Long userId) {
        return dao.findByUserId(userId);
    }

    public void deleteByUserId(Long userId) {
        dao.deleteByUserId(userId);
    }

    public void deleteByTeamId(Long teamId) {
        dao.deleteByTeamId(teamId);
    }
}
