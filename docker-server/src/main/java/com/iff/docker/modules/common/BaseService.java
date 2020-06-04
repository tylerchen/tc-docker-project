/*******************************************************************************
 * Copyright (c) 2019-11-01 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.common;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

/**
 * BaseService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-01
 * auto generate by qdp.
 */
public abstract class BaseService<E, D extends BaseDao> {
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected D dao;

    public E findById(Object id) {
        Optional<E> optional = dao.findById(id);
        return optional.isPresent() ? optional.get() : null;
    }

    public List<E> findAll() {
        return dao.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Object id) {
        dao.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletes(Object[] ids) {
        for (Object id : ids) {
            delete(id);
        }
    }

    public E save(E e) {
        return (E) dao.save(e);
    }

    /**
     * @param predicate 筛选条件
     * @param page      分页对象
     * @return
     */
    @Transactional(readOnly = true)
    public Page<E> findAll(Predicate predicate, Pageable page) {
        return dao.findAll(predicate, page);
    }

    /**
     * @param page 分页对象
     * @return
     */
    @Transactional(readOnly = true)
    public Page<E> findAll(Pageable page) {
        return dao.findAll(page);
    }

    /**
     * @param predicate 筛选条件
     * @return
     */
    @Transactional(readOnly = true)
    public Iterable<E> findAll(Predicate predicate) {
        return dao.findAll(predicate, Sort.unsorted());
    }

    /**
     * 把对象从会话中移除，这样不会出现关联查询。
     *
     * @param entity
     */
    public void detach(Object entity) {
        entityManager.detach(entity);
    }

    protected Pageable of(int page, int size) {
        return of(page, size, Sort.unsorted());
    }

    protected Pageable of(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }

    protected Pageable of(int page, int size, Sort.Direction direction, String... properties) {
        return of(page, size, Sort.by(direction, properties));
    }
}
