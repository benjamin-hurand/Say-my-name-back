package com.saymyname.core.model.course;

import com.saymyname.core.model.people.Attribute;

public class ResultAttribute {
    private Attribute attribute;
    private String value;
    private boolean isCorrect;
    private boolean isTarget;

    public ResultAttribute() {
    }

    public ResultAttribute(Attribute attribute, String value, boolean isCorrect, boolean isTarget) {
        this.attribute = attribute;
        this.value = value;
        this.isCorrect = isCorrect;
        this.isTarget = isTarget;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + (isCorrect ? 1231 : 1237);
        result = prime * result + (isTarget ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResultAttribute other = (ResultAttribute) obj;
        if (attribute == null) {
            if (other.attribute != null)
                return false;
        } else if (!attribute.equals(other.attribute))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (isCorrect != other.isCorrect)
            return false;
        if (isTarget != other.isTarget)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResultAttribute [attribute=" + attribute + ", value=" + value + ", isCorrect=" + isCorrect
                + ", isTarget=" + isTarget + "]";
    }

}
