package com.saymyname.persistence.entity.organization.subscription;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSubscriptionKey implements Serializable {
    private Long tenantId;
    private Long userId;
    private Long personId;
}