package com.saymyname.core.model.people;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SubscriptionBulkResult {
    long matched;
    long acted;
    long skipped;
    double seconds;
}
