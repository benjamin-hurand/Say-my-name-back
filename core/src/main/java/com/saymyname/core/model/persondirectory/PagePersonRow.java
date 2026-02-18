package com.saymyname.core.model.persondirectory;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PagePersonRow {
    Long personId;
    String photoStorageKey;
}
