/*******************************************************************************
 * Copyright (c) 2019-12-27 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.common;

import org.springframework.http.MediaType;

/**
 * Constant
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-12-27
 */
public class Constant {
    public static final String LOGIN_USER = "LOGIN_USER";
    public static final String LOGIN_TOKEN = "LOGIN_TOKEN";
    public static final String JSON_UTF8 = MediaType.APPLICATION_JSON_UTF8_VALUE;
    public static final String FORM_URLENCODED = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
    public static final String MEDIA_ALL = MediaType.ALL_VALUE;

    public static final String CACHE_USER = "User";
    public static final String LOGIN_VALID = "LOGIN_VALID";
}
