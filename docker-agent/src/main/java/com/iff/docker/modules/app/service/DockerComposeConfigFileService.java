/*******************************************************************************
 * Copyright (c) 2020-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.service;

import com.iff.docker.modules.app.dao.DockerComposeConfigFileDao;
import com.iff.docker.modules.app.entity.DockerComposeConfigFile;
import com.iff.docker.modules.common.BaseService;
import org.springframework.stereotype.Service;

/**
 * DockerComposeConfigFileService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-29
 */
@Service
public class DockerComposeConfigFileService extends BaseService<DockerComposeConfigFile, DockerComposeConfigFileDao> {
}
