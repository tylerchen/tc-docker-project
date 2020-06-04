/*******************************************************************************
 * Copyright (c) 2019-11-20 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.common;

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
public enum BooleanEnum {
    Y,
    N;

    @JsonValue
    public String getName() {
        return name();
    }

    public boolean isTrue() {
        return "Y".equals(name());
    }

    public boolean isFalse() {
        return "N".equals(name());
    }
}
