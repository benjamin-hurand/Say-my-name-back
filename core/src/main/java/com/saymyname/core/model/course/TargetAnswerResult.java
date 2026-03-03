package com.saymyname.core.model.course;

public class TargetAnswerResult {
    private Long attributeId;
    private String value;
    private boolean isCorrect;

    public TargetAnswerResult() {
    }

    public TargetAnswerResult(Long attributeId, String value, boolean isCorrect, boolean isTarget) {
        this.attributeId = attributeId;
        this.value = value;
        this.isCorrect = isCorrect;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public String getValue() {
        return value;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setAttributeId(Long attribute) {
        this.attributeId = attribute;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attributeId == null) ? 0 : attributeId.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + (isCorrect ? 1231 : 1237);
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
        TargetAnswerResult other = (TargetAnswerResult) obj;
        if (attributeId == null) {
            if (other.attributeId != null)
                return false;
        } else if (!attributeId.equals(other.attributeId))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (isCorrect != other.isCorrect)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TargetAnswerResult [attributeId=" + attributeId + ", value=" + value + ", isCorrect=" + isCorrect + "]";
    }

}
