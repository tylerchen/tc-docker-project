/*******************************************************************************
 * Copyright (c) 2020-06-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * ExecFormVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-03
 */
@Data
public class ExecFormVO {
    @JsonProperty("AttachStdin")
    private boolean attachStdin;
    @JsonProperty("AttachStdout")
    private boolean attachStdout;
    @JsonProperty("AttachStderr")
    private boolean attachStderr;
    @JsonProperty("DetachKeys")
    private String detachKeys;
    @JsonProperty("tty")
    private boolean tty;
    @JsonProperty("env")
    private String[] env;
    @JsonProperty("Cmd")
    private String[] cmd;
    @JsonProperty("Privileged")
    private boolean privileged;
    @JsonProperty("User")
    private String user;
    @JsonProperty("WorkingDir")
    private String workingDir;

    @JsonProperty("Detach")
    private boolean detach;
}
