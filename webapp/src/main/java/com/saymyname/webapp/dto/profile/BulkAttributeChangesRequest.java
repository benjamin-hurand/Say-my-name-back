// src/main/java/com/saymyname/webapp/dto/profile/BulkAttributeChangesRequest.java
package com.saymyname.webapp.dto.profile;

import java.util.List;

public record BulkAttributeChangesRequest(
        List<AddedItem> added,
        List<UpdatedItem> updated,
        List<DeletedItem> deleted) {
    public record AddedItem(String value) {
    }

    public record UpdatedItem(Long id, String value) {
    }

    public record DeletedItem(Long id) {
    }
}
