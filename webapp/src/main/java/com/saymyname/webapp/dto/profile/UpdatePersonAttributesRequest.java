package com.saymyname.webapp.dto.profile;

import java.util.List;

public record UpdatePersonAttributesRequest(
        List<PersonAttributePatch> attributes) {
}
