/*******************************************************************************
 * Copyright (c) 2019-11-11 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * PageModel
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageModel {
    private int page = 0;
    private int size = 10;
    private List<Sort.Direction> directions;
    private List<String> properties;

    public static PageRequest toPage(PageModel page) {
        page = page != null ? page : PageModel.builder().page(0).size(10).build();
        return page.toPage();
    }

    public PageRequest toPage() {
        if (properties == null || properties.isEmpty()) {
            return PageRequest.of(page, size, Sort.unsorted());
        }
        if (directions == null || directions.isEmpty()) {
            return PageRequest.of(page, size, Sort.by(properties.toArray(new String[properties.size()])));
        }
        if (directions.size() != properties.size()) {
            throw new IllegalArgumentException("directions size not match properties size");
        }
        Sort sort = null;
        for (int i = 0; i < directions.size(); i++) {
            if (sort == null) {
                sort = Sort.by(directions.get(i), properties.get(i));
            } else {
                sort = sort.and(Sort.by(directions.get(i), properties.get(i)));
            }
        }
        return PageRequest.of(page, size, sort);
    }
}
