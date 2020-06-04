/*******************************************************************************
 * Copyright (c) 2019-11-20 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BooleanEnum
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-20
 */
@AllArgsConstructor
@Getter
public enum UserTypeEnum {
    ADMIN,
    ADMIN_RO,
    USER,
    USER_RO;

    @JsonValue
    public String getName() {
        return name();
    }

    public boolean isAdmin() {
        return ADMIN.name().equals(name());
    }

    public boolean isAdminReadOnly() {
        return ADMIN_RO.name().equals(name());
    }

    public boolean isUser() {
        return USER.name().equals(name());
    }

    public boolean isUserReadOnly() {
        return USER_RO.name().equals(name());
    }
}
