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

/**
 * DockerDashboardVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Data
public class DockerDashboardVO {
    private String name;
    private String publicIp;
    private String url;

    private Date lastUpTime;
    private int stacks = 0;
    private int containers = 0;
    private int upContainers = 0;
    private int downContainers = 0;
    private int healthContainers = 0;
    private int unHealthContainers = 0;
    private int volumes = 0;
    private int images = 0;
    private int cups = 0;
    private long memories = 0L;
    private long imagesSize = 0L;
    private String imagesSizeGb;
}
