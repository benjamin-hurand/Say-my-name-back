// src/main/java/com/saymyname/core/model/quiz/QuizLayoutHints.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

/**
 * Layout hints for frontend rendering.
 * Tells the client HOW to display choices/items, not WHAT to display.
 *
 * Examples:
 * - MCQ with 4 text choices: layout="list_vertical", itemStyle="text_only"
 * - MCQ with 4 photo choices: layout="grid_2x2", itemStyle="photo_only"
 * - MCQ with text+photo combos: layout="list_vertical", itemStyle="text_with_thumbnail"
 * - BINARY_SWIPE: layout="swipe_cards", itemStyle="full_card"
 * - ASSOCIATION: layout="two_column_matching", itemStyle="photo_with_caption"
 * - ORDERING: layout="drag_reorder", itemStyle="text_with_thumbnail"
 *
 * All fields are strings for frontend flexibility (no strict enum coupling).
 * Allows frontend experimentation without backend changes.
 */
public class QuizLayoutHints {

    /**
     * Primary layout strategy.
     * Common values: "list_vertical", "list_horizontal", "grid_2x2", "grid_3x3",
     *                "swipe_cards", "two_column_matching", "drag_reorder"
     */
    private String layout;

    /**
     * Item presentation style.
     * Common values: "text_only", "photo_only", "text_with_thumbnail",
     *                "photo_with_caption", "full_card"
     */
    private String itemStyle;

    /**
     * Photo aspect ratio hint (if applicable).
     * Common values: "square", "portrait", "landscape", "circle"
     */
    private String photoAspect;

    /**
     * Photo size hint (if applicable).
     * Common values: "thumbnail", "small", "medium", "large", "full"
     */
    private String photoSize;

    public QuizLayoutHints() {
    }

    private QuizLayoutHints(Builder b) {
        this.layout = b.layout;
        this.itemStyle = b.itemStyle;
        this.photoAspect = b.photoAspect;
        this.photoSize = b.photoSize;
    }

    public String getLayout() {
        return layout;
    }

    public String getItemStyle() {
        return itemStyle;
    }

    public String getPhotoAspect() {
        return photoAspect;
    }

    public String getPhotoSize() {
        return photoSize;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setItemStyle(String itemStyle) {
        this.itemStyle = itemStyle;
    }

    public void setPhotoAspect(String photoAspect) {
        this.photoAspect = photoAspect;
    }

    public void setPhotoSize(String photoSize) {
        this.photoSize = photoSize;
    }

    public static class Builder {
        private String layout;
        private String itemStyle;
        private String photoAspect;
        private String photoSize;

        public Builder withLayout(String v) {
            this.layout = v;
            return this;
        }

        public Builder withItemStyle(String v) {
            this.itemStyle = v;
            return this;
        }

        public Builder withPhotoAspect(String v) {
            this.photoAspect = v;
            return this;
        }

        public Builder withPhotoSize(String v) {
            this.photoSize = v;
            return this;
        }

        public QuizLayoutHints build() {
            return new QuizLayoutHints(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizLayoutHints))
            return false;
        QuizLayoutHints that = (QuizLayoutHints) o;
        return Objects.equals(layout, that.layout)
                && Objects.equals(itemStyle, that.itemStyle)
                && Objects.equals(photoAspect, that.photoAspect)
                && Objects.equals(photoSize, that.photoSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layout, itemStyle, photoAspect, photoSize);
    }

    @Override
    public String toString() {
        return "QuizLayoutHints{" +
                "layout='" + layout + '\'' +
                ", itemStyle='" + itemStyle + '\'' +
                ", photoAspect='" + photoAspect + '\'' +
                ", photoSize='" + photoSize + '\'' +
                '}';
    }
}
