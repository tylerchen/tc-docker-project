/*******************************************************************************
 * Copyright (c) 2020-06-10 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.form;

import lombok.Data;

/**
 * DockerComposeConfigFileFormVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-10
 */
@Data
public class DockerComposeConfigFileFormVO {
    private Long composeId;
    private Long id;
    private String name;
    private String description;
    private String content;
}
