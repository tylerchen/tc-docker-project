/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.form;

import lombok.Data;

/**
 * DockerUserFormVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
@Data
public class DockerUserFormVO {
    private Long id;
    private String userName;
    private String password;
    private String email;
    /**
     * type，ADMIN: 管理员，ADMIN_RO：管理员-只读，USER：用户，USER_RO: 只读
     */
    private String type;
    private String name;
}
