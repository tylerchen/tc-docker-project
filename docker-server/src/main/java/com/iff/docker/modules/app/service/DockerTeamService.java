/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.dao.DockerTeamDao;
import com.iff.docker.modules.app.entity.DockerTeam;
import com.iff.docker.modules.common.BaseService;
import org.springframework.stereotype.Service;

/**
 * DockerTeamService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
@Service
public class DockerTeamService extends BaseService<DockerTeam, DockerTeamDao> {
    public DockerTeam findByName(String name) {
        return dao.findByName(name);
    }
}
