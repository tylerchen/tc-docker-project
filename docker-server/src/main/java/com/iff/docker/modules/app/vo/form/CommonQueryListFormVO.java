/*******************************************************************************
 * Copyright (c) 2020-05-18 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.form;

import lombok.Data;

/**
 * 能用查询 list 列表的表单
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-18
 */
@Data
public class CommonQueryListFormVO {
    private String namespace;
    private String pretty;
    private Boolean allowWatchBookmarks;
    private String _continue;
    private String fieldSelector;
    private String labelSelector;
    private Integer limit;
    private String resourceVersion;
    private Integer timeoutSeconds;
    private Boolean watch;
}
