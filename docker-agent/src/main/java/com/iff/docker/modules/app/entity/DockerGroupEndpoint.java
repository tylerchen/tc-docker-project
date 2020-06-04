/*******************************************************************************
 * Copyright (c) 2020-05-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
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
import java.util.Date;

/**
 * DockerGroupEndpoint
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
public class DockerGroupEndpoint {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "group_id", nullable = false, updatable = false)
    private Long groupId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", updatable = false, insertable = false)
    private DockerGroup group;

    @Column(name = "endpoint_id", nullable = false, updatable = false, unique = true)
    private Long endpointId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", updatable = false, insertable = false)
    private DockerEndpoint endpoint;

    @Column(nullable = false)
    private Date updateTime;

    @Column(nullable = false, updatable = false)
    private Date createTime;

    @PrePersist
    void prePersist() {
        setCreateTime(new Date());
        setUpdateTime(new Date());
        if (getGroupId() == null && getGroup() != null) {
            setGroupId(getGroup().getId());
        }
        if (getEndpointId() == null && getEndpoint() != null) {
            setEndpointId(getEndpoint().getId());
        }
    }

    @PreUpdate
    void preUpdate() {
        setUpdateTime(new Date());
    }
}
