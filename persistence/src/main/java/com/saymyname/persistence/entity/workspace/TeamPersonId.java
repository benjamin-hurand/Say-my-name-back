package com.saymyname.persistence.entity.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Embeddable
public class TeamPersonId implements Serializable {

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "person_id", nullable = false)
    private Long personId;
}
