/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.dao.DockerGroupEndpointDao;
import com.iff.docker.modules.app.entity.DockerGroupEndpoint;
import com.iff.docker.modules.common.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DockerGroupEndpointService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
@Service
public class DockerGroupEndpointService extends BaseService<DockerGroupEndpoint, DockerGroupEndpointDao> {
    public List<DockerGroupEndpoint> findByGroupId(Long groupId) {
        return dao.findByGroupId(groupId);
    }

    public List<DockerGroupEndpoint> findByEndpointId(Long endpointId) {
        return dao.findByEndpointId(endpointId);
    }

    public void deleteByGroupId(Long groupId) {
        dao.deleteByGroupId(groupId);
    }

    public void deleteByEndpointId(Long endpointId) {
        dao.deleteByEndpointId(endpointId);
    }
}
