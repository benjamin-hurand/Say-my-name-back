// webapp/src/main/java/com/saymyname/webapp/config/AsyncConfig.java
package com.saymyname.webapp.config;

import com.saymyname.core.multitenancy.OrgContext;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public TaskDecorator contextPropagatingTaskDecorator() {
        return runnable -> {
            final Long org = OrgContext.get();
            final Map<String, String> mdc = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (org != null)
                        OrgContext.set(org);
                    if (mdc != null)
                        MDC.setContextMap(mdc);
                    runnable.run();
                } finally {
                    OrgContext.clear();
                    MDC.clear();
                }
            };
        };
    }

    @Bean(name = "appAsyncExecutor")
    public Executor appAsyncExecutor(TaskDecorator contextPropagatingTaskDecorator) {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("async-");
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(1000);
        ex.setTaskDecorator(contextPropagatingTaskDecorator);
        ex.initialize();
        return ex;
    }
}
