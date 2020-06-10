/*******************************************************************************
 * Copyright (c) 2020-06-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.vo.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DockerImagesRepositoryVO
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-06-08
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerImagesRepositoryVO {
    private List<Image> items;
    private String continuationToken;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private String id;
        private String downloadUrl;
        private String path;
        private String repository;
        private String format;
    }
}
