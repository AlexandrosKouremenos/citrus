package com.rue.aurantium.data;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledExecutorService;

@EnableScheduling
@Configuration
public class Scheduler {

    public static final String TASK_SCHEDULER = "task-scheduler";

    public Scheduler() { }

    @Bean(destroyMethod="shutdown", name = TASK_SCHEDULER)
    public ScheduledExecutorService taskScheduler() {

        ThreadPoolTaskScheduler poolTaskScheduler = new ThreadPoolTaskScheduler();
        poolTaskScheduler.setPoolSize(1);
        poolTaskScheduler.setThreadNamePrefix(TASK_SCHEDULER);
        poolTaskScheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        poolTaskScheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        poolTaskScheduler.setWaitForTasksToCompleteOnShutdown(false);
        poolTaskScheduler.initialize();
        return poolTaskScheduler.getScheduledExecutor();

    }

}