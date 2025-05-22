package com.saymyname.service.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saymyname.core.model.enums.ReviewAlgorithm;
import com.saymyname.service.course.scheduler.FsrsScheduler;
import com.saymyname.service.course.scheduler.PfaScheduler;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.service.course.scheduler.Sm2Scheduler;

// service/config/SchedulerConfig.java
@Configuration
public class CourseSchedulerConfig {
    @Bean
    public Map<ReviewAlgorithm, SchedulerStrategy> schedulerStrategies(
            Sm2Scheduler sm2,
            PfaScheduler pfa,
            FsrsScheduler fsrs) {
        return Map.of(
                ReviewAlgorithm.SM2, sm2,
                ReviewAlgorithm.PFA, pfa,
                ReviewAlgorithm.FSRS, fsrs);
    }
}
