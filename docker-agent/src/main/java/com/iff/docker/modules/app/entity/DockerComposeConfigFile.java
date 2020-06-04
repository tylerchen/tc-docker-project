/*******************************************************************************
 * Copyright (c) 2020-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * DockerComposeConfigFile
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class DockerComposeConfigFile {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Pattern(regexp = "^[a-zA-Z0-9_\\.\\-/]+$", message = "只允许字母数字及\"_.-\"/")
    @NotNull(message = "配置文件名称")
    @Column(nullable = false)
    private String name;

    @Column(name = "compose_id", nullable = false, updatable = false)
    private Long composeId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compose_id", updatable = false, insertable = false)
    private DockerCompose compose;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_content_id", nullable = false, updatable = false)
    private FileContent fileContent;

    private String description;

    @Column(nullable = false)
    private Date updateTime;

    @Column(nullable = false, updatable = false)
    private Date createTime;

    @PrePersist
    void prePersist() {
        setCreateTime(new Date());
        setUpdateTime(new Date());
        if (getComposeId() == null && getCompose() != null) {
            setComposeId(getCompose().getId());
        }
    }

    @PreUpdate
    void preUpdate() {
        setUpdateTime(new Date());
    }
}
