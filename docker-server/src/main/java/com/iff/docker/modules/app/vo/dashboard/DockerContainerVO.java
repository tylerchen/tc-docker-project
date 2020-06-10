/*******************************************************************************
 * Copyright (c) 2020-06-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.dashboard;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * DockerContainerVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Data
public class DockerContainerVO {
    private String publicIp;
    private String id;
    private String name;
    private String state;
    private String stack;
    private String image;
    private String imageId;
    private Date created;
    private List<String> ip;
    private List<String> publishPort;
    private boolean healthy;
    private boolean unhealthy;
    private boolean running;
    private boolean stop;
}
