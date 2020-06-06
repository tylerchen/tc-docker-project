/*******************************************************************************
 * Copyright (c) 2020-06-05 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.common;

import java.util.concurrent.atomic.AtomicLong;

/**
 * LastRequestHolder
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-05
 */
public class LastRequestHolder {
    private static final AtomicLong lastRequest = new AtomicLong(0);

    public static void setLastRequest(long value) {
        lastRequest.set(value);
    }

    public static boolean isValidRequest(long value) {
        if (lastRequest.get() < value) {
            lastRequest.set(value);
            return true;
        }
        return false;
    }
}
