/*******************************************************************************
 * Copyright (c) 2020-01-09 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.form;

import lombok.Data;

/**
 * UserFormVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-01-09
 */
@Data
public class UserFormVO {
    private Long id;
    private String userName;
    private String password;
    private String email;
    private String name;
    private String type;
}
