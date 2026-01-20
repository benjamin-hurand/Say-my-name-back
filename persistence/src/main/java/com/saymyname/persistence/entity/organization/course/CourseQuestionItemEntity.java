package com.saymyname.persistence.entity.organization.course;

import java.util.Objects;

import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course_question_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cqi_attempt_position", columnNames = { "attempt_id", "position" })
})
public class CourseQuestionItemEntity extends BaseOrgScoped {

    public enum Role {
        TARGET,
        DISTRACTOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", referencedColumnName = "id", nullable = false)
    private CourseQuestionAttemptEntity attempt;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "knowledge_id", referencedColumnName = "id")
    private KnowledgeEntity knowledge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private PersonEntity person;

    @Column(name = "answered", nullable = false)
    private boolean answered;

    @Column(name = "correct")
    private Boolean correct;

    @Lob
    @Column(name = "normalized_answer")
    private String normalizedAnswer;

    public CourseQuestionItemEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseQuestionAttemptEntity getAttempt() {
        return attempt;
    }

    public void setAttempt(CourseQuestionAttemptEntity attempt) {
        this.attempt = attempt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public KnowledgeEntity getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(KnowledgeEntity knowledge) {
        this.knowledge = knowledge;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public String getNormalizedAnswer() {
        return normalizedAnswer;
    }

    public void setNormalizedAnswer(String normalizedAnswer) {
        this.normalizedAnswer = normalizedAnswer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseQuestionItemEntity))
            return false;
        CourseQuestionItemEntity that = (CourseQuestionItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
