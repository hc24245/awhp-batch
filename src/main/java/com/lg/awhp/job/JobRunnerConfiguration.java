package com.lg.awhp.job;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lg.awhp.model.SingleCycle;
import com.lg.awhp.model.SingleCycleAvg;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JobRunnerConfiguration {

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	private static final String JOB_NAME = "testJob";
	
	private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SqlSessionFactory sqlSessionFactory;
    
    @Bean
    public Job job(){
        return jobBuilderFactory.get(JOB_NAME)
                .start(step1())
                .build();
    }

    @Bean
    @JobScope
    public Step step1(){
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                	logger.info(">>> This is Step1");
                    return RepeatStatus.FINISHED;
                }).build();
    }
    
    @Bean
	@JobScope
	public Step dbToDbStep1() {
		logger.info("Start Fourth Step");
		
	    return stepBuilderFactory.get("dbToDbStep")
	        .<SingleCycleAvg, SingleCycleAvg>chunk(100)
	        .reader(singleCycleAvgPagingItemReader1())
	        .writer(singleCycleAvgWriter1())
	        .build();
	}
    
    @Bean
    @StepScope
    public MyBatisPagingItemReader<SingleCycleAvg> singleCycleAvgPagingItemReader1() {

        /*Map<String,Object> parameterValues = new HashMap<>();
        parameterValues.put("amount", "10000");*/

        return new MyBatisPagingItemReaderBuilder<SingleCycleAvg>()
                .pageSize(100)
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("singleCycle.selectSingleCycleAvg")
                //.parameterValues(parameterValues)
                .build();
    }
	
	@Bean
	@StepScope
	public MyBatisBatchItemWriter<SingleCycleAvg> singleCycleAvgWriter1() {
		
	    return new MyBatisBatchItemWriterBuilder<SingleCycleAvg>()
	    .sqlSessionFactory(sqlSessionFactory)
	    .statementId("singleCycle.savePlayer")
	    .build();
	}
}
