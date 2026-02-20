// src/main/java/com/saymyname/core/model/course/CourseQuestionItem.java
package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.people.Person;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseQuestionItem {

    Long id;
    int position;
    QuizQuestionItemRole role;

    /**
     * TARGET => knowledge != null
     * DISTRACTOR => knowledge == null && person != null
     */
    Knowledge knowledge;
    Person person;

    boolean answered;
    Boolean correct; // null tant que non répondu
    String normalizedAnswer;

    public void validateInvariants() {
        if (role == null) {
            throw new IllegalStateException("CourseQuestionItem.role is required");
        }
        if (position < 0) {
            throw new IllegalStateException("CourseQuestionItem.position must be >= 0");
        }

        if (role == QuizQuestionItemRole.TARGET) {
            if (knowledge == null) {
                throw new IllegalStateException("TARGET item must reference a Knowledge");
            }
        } else {
            if (knowledge != null) {
                throw new IllegalStateException("DISTRACTOR item must not reference a Knowledge");
            }
            if (person == null) {
                throw new IllegalStateException("DISTRACTOR item must reference a Person");
            }
        }
    }
}
