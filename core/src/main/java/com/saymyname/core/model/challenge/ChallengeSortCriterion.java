package com.saymyname.core.model.challenge;

import com.saymyname.core.model.enums.ChallengeSortCriterionType;
import com.saymyname.core.model.enums.OrderDirection;
import java.util.Objects;

public class ChallengeSortCriterion {
    private String id; // Identifiant unique du critère de tri
    private ChallengeSortCriterionType sortType; // Par exemple, CREATION_DATE, POPULARITY, etc.
    private OrderDirection order; // ASC ou DESC

    public ChallengeSortCriterion() {}

    public ChallengeSortCriterion(String id, ChallengeSortCriterionType sortType, OrderDirection order) {
        this.id = id;
        this.sortType = sortType;
        this.order = order;
    }

    public ChallengeSortCriterion(ChallengeSortCriterionType sortType, OrderDirection order) {
        this.sortType = sortType;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChallengeSortCriterionType getSortType() {
        return sortType;
    }

    public void setSortType(ChallengeSortCriterionType sortType) {
        this.sortType = sortType;
    }

    public OrderDirection getOrder() {
        return order;
    }

    public void setOrder(OrderDirection order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeSortCriterion)) return false;
        ChallengeSortCriterion that = (ChallengeSortCriterion) o;
        return Objects.equals(id, that.id) &&
               sortType == that.sortType &&
               order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sortType, order);
    }

    @Override
    public String toString() {
        return "ChallengeSortCriterion{" +
               "id='" + id + '\'' +
               ", sortType=" + sortType +
               ", order=" + order +
               '}';
    }
}
