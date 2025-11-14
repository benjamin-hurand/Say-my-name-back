// src/main/java/com/saymyname/persistence/util/JpaSortUtil.java
package com.saymyname.persistence.util;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Sort;

public final class JpaSortUtil {
    private JpaSortUtil() {
    }

    public static <T> void applySort(CriteriaBuilder cb,
            CriteriaQuery<T> cq,
            Root<?> root,
            Sort sort,
            Class<?> rootMeta,
            List<String> fallbackProps) {
        if (sort != null && sort.isSorted()) {
            cq.orderBy(sort.stream().map(order -> {
                Path<?> path = root.get(order.getProperty());
                return order.isAscending() ? cb.asc(path) : cb.desc(path);
            }).toList());
        } else if (fallbackProps != null && !fallbackProps.isEmpty()) {
            cq.orderBy(fallbackProps.stream().map(p -> cb.desc(root.get(p))).toList());
        }
    }
}
