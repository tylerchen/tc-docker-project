/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package com.iff.docker.modules.app.dao;

import com.iff.docker.modules.app.entity.DockerGroupEndpoint;
import com.iff.docker.modules.common.BaseDao;

import java.util.List;

/**
 * DockerGroupEndpointDao
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
public interface DockerGroupEndpointDao extends BaseDao<DockerGroupEndpoint> {
    List<DockerGroupEndpoint> findByGroupId(Long groupId);

    List<DockerGroupEndpoint> findByEndpointId(Long endpointId);

    void deleteByGroupId(Long groupId);

    void deleteByEndpointId(Long endpointId);
}
