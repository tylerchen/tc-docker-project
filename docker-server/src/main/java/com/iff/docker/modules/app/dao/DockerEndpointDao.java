/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package com.iff.docker.modules.app.dao;

import com.iff.docker.modules.app.entity.DockerEndpoint;
import com.iff.docker.modules.common.BaseDao;

/**
 * DockerEndpoint
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
public interface DockerEndpointDao extends BaseDao<DockerEndpoint> {
    DockerEndpoint findByName(String name);
}
