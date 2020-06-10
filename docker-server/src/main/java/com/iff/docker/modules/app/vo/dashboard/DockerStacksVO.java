/*******************************************************************************
 * Copyright (c) 2020-06-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.dashboard;

import lombok.Data;

import java.util.List;

/**
 * DockerStacksVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Data
public class DockerStacksVO {
    private String name;
    private String type;
    private List<DockerContainerVO> containers;
}
