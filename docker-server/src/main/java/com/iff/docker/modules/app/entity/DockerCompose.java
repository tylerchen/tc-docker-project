/*******************************************************************************
 * Copyright (c) 2020-05-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.app.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

/**
 * DockerCompose
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
public class DockerCompose {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "只允许字母数字及\"_-\"")
    @NotNull(message = "DockerCompose存放目录名称不能为空")
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "file_content_id", nullable = false, updatable = false)
    private FileContent composeFile;

    @OneToMany(mappedBy = "compose", targetEntity = DockerComposeConfigFile.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DockerComposeConfigFile> configFiles;

    private String description;

    @Column(nullable = false)
    private Date updateTime;

    @Column(nullable = false, updatable = false)
    private Date createTime;

    @PrePersist
    void prePersist() {
        setCreateTime(new Date());
        setUpdateTime(new Date());
    }

    @PreUpdate
    void preUpdate() {
        setUpdateTime(new Date());
    }
}
