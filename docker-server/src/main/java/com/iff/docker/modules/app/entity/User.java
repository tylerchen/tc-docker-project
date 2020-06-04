/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
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
 * User
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String userName;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    /**
     * 状态，ADMIN: 管理员，ADMIN_RO：管理员-只读，USER：用户，USER_RO: 只读
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTypeEnum type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Date updateTime;

    @Column(nullable = false, updatable = false)
    private Date createTime;

    @PrePersist
    void prePersist() {
        setCreateTime(new Date());
        setUpdateTime(new Date());
        if (getType() == null) {
            setType(UserTypeEnum.USER);
        }
    }

    @PreUpdate
    void preUpdate() {
        setUpdateTime(new Date());
    }
}
