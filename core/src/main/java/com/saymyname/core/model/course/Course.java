package com.saymyname.core.model.course;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Attribute;

import java.util.List;
import java.util.Objects;

public class Course {
    private Long id;
    private User user;
    private GameMode gameMode;
    private Attribute sortingAttribute;
    private String sortingOrder;
    private CourseStatus status;
    private int currentRound;
    private List<Population> populations;

    public Course() {
    }

    private Course(Builder b) {
        this.id = b.id;
        this.user = b.user;
        this.gameMode = b.gameMode;
        this.sortingAttribute = b.sortingAttribute;
        this.sortingOrder = b.sortingOrder;
        this.status = b.status;
        this.currentRound = b.currentRound;
        this.populations = b.populations;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Attribute getSortingAttribute() {
        return sortingAttribute;
    }

    public String getSortingOrder() {
        return sortingOrder;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public List<Population> getPopulations() {
        return populations;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setSortingAttribute(Attribute sortingAttribute) {
        this.sortingAttribute = sortingAttribute;
    }

    public void setSortingOrder(String sortingOrder) {
        this.sortingOrder = sortingOrder;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public void setPopulations(List<Population> populations) {
        this.populations = populations;
    }

    public static class Builder {
        private Long id;
        private User user;
        private GameMode gameMode;
        private Attribute sortingAttribute;
        private String sortingOrder;
        private CourseStatus status;
        private int currentRound;
        private List<Population> populations;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder withSortingAttribute(Attribute a) {
            this.sortingAttribute = a;
            return this;
        }

        public Builder withSortingOrder(String o) {
            this.sortingOrder = o;
            return this;
        }

        public Builder withStatus(CourseStatus s) {
            this.status = s;
            return this;
        }

        public Builder withCurrentRound(int r) {
            this.currentRound = r;
            return this;
        }

        public Builder withPopulations(List<Population> pops) {
            this.populations = pops;
            return this;
        }

        public Course build() {
            return new Course(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Course))
            return false;
        Course c = (Course) o;
        return id == c.id &&
                Objects.equals(user, c.user) &&
                Objects.equals(gameMode, c.gameMode) &&
                Objects.equals(sortingAttribute, c.sortingAttribute) &&
                Objects.equals(sortingOrder, c.sortingOrder) &&
                Objects.equals(status, c.status) &&
                currentRound == c.currentRound &&
                Objects.equals(populations, c.populations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameMode,
                sortingAttribute, sortingOrder,
                status, populations);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", user=" + user +
                ", gameMode=" + gameMode +
                ", sortingAttribute=" + sortingAttribute +
                ", sortingOrder='" + sortingOrder + '\'' +
                ", status='" + status + '\'' +
                ", currentRound=" + currentRound +
                ", populations=" + populations +
                '}';
    }
}
