package com.lg.awhp.schedules;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lg.awhp.job.JobRunnerConfiguration;
import com.lg.awhp.job.SingleCycleJobConfiguration;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobScheduler {
	
	private final JobRunnerConfiguration jobRunnerConfiguration;
	private final JobLauncher jobLauncher;

    @Scheduled(fixedDelay = 5 * 1000L)
    public void testJobSchedule() {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addParameter("currenteDateTime", new JobParameter(LocalDateTime.now().toString()));
        try {
            jobLauncher.run(jobRunnerConfiguration.job(), jobParametersBuilder.toJobParameters());
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
    
}
