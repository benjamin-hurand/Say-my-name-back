package com.saymyname.persistence.entity.course;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.Formula;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.attribute.AttributeEntity;

@Entity
@Table(name = "populations")
public class PopulationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_filter_id")
    private AttributeEntity attributeFilter;

    @Column(name = "min_value")
    private String minValue;

    @Column(name = "max_value")
    private String maxValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Formula("(SELECT COUNT(DISTINCT pa.person_id) " +
            "   FROM persons_attributes pa " +
            "  WHERE pa.attribute_id = attribute_filter_id " +
            "    AND LEFT(pa.value, CHAR_LENGTH(min_value)) >= min_value " +
            "    AND LEFT(pa.value, CHAR_LENGTH(max_value)) <= max_value " +
            "    AND (pa.valid_from IS NULL OR pa.valid_from <= CURRENT_DATE()) " +
            "    AND (pa.valid_to   IS NULL OR pa.valid_to   >= CURRENT_DATE())" +
            ")")
    private int countPersons;

    public PopulationEntity() {
    }

    public PopulationEntity(Long id, String title, String description,
            AttributeEntity attributeFilter, String minValue,
            String maxValue, UserEntity createdBy,
            LocalDateTime createdAt, int countPersons) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.attributeFilter = attributeFilter;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.countPersons = countPersons;
    }

    // getters & setters..
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AttributeEntity getAttributeFilter() {
        return attributeFilter;
    }

    public void setAttributeFilter(AttributeEntity attributeFilter) {
        this.attributeFilter = attributeFilter;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getCountPersons() {
        return countPersons;
    }

    public void setCountPersons(int countPersons) {
        this.countPersons = countPersons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PopulationEntity))
            return false;
        PopulationEntity that = (PopulationEntity) o;
        return id == that.id &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(attributeFilter, that.attributeFilter) &&
                Objects.equals(minValue, that.minValue) &&
                Objects.equals(maxValue, that.maxValue) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(createdAt, that.createdAt) &&
                countPersons == that.countPersons;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, attributeFilter,
                minValue, maxValue, createdBy, createdAt, countPersons);
    }

    @Override
    public String toString() {
        return "PopulationEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", attributeFilter=" + attributeFilter +
                ", minValue='" + minValue + '\'' +
                ", maxValue='" + maxValue + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", countPersons=" + countPersons +
                '}';
    }
}
