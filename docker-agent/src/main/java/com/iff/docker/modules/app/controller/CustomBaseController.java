/*******************************************************************************
 * Copyright (c) 2020-06-05 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.iff.docker.modules.common.BaseController;

/**
 * CustomBaseController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-05
 */
public class CustomBaseController extends BaseController {
    private static final JSONObject server = new JSONObject();

    public String getServerHost() {
        return server.getString("host");
    }

    public void setServerHost(String host) {
        server.put("host", host);
    }

    public int getServerPort() {
        return server.getInteger("port");
    }

    public void setServerPort(int port) {
        server.put("port", port);
    }
}
