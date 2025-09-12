package com.saymyname.webapp.dto.profile;

import java.util.List;

/** Requête bulk en 3 listes: create / update / delete */
public record BulkPersonAttributeRequest(
        List<CreateItem> create,
        List<UpdateItem> update,
        List<DeleteItem> delete) {
    public record CreateItem(String value) {
    }

    public record UpdateItem(Long id, String value) {
    }

    public record DeleteItem(Long id) {
    }
}
