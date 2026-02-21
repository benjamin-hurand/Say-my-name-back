package com.saymyname.webapp.dto.profile;

import java.util.List;

public record UpdateFactsRequest(
                List<FactPatch> attributes) {
}
