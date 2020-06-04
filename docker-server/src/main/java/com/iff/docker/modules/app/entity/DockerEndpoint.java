/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
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
import java.util.Date;

/**
 * DockerEndpoint
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2020-05-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class DockerEndpoint {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false)
    private String publicIp;

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
