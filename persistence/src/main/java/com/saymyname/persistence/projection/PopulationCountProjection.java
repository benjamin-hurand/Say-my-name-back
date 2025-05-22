package com.saymyname.persistence.projection;

import com.saymyname.persistence.entity.course.PopulationEntity;

public interface PopulationCountProjection {
    PopulationEntity getPopulation();

    Long getCount();
}
