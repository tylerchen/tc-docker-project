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
 * DockerNetworksVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Data
public class DockerNetworksVO {
    private String id;
    private String name;
    private String stack;
    private String scope;
    private String driver;
    private boolean attachable;
    private boolean internal;
    private String ipamDriver;
    private List<String> ipamSubnet;
    private List<String> ipamGateway;
}
