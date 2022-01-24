package com.lg.awhp.job;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.lg.awhp.config.DownloadConfig;
import com.lg.awhp.config.SftpConfig;
import com.lg.awhp.model.SingleCycle;
import com.lg.awhp.model.SingleCycleAvg;
import com.lg.awhp.utils.GZIPUtil;
import com.lg.awhp.utils.SftpUtil;

import ch.qos.logback.classic.Logger;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SingleCycleJobConfiguration {

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	private static final String JOB_NAME = "singleCycleJob";
	private static final int fileToDbChunkSize = 1000;
	private static final int dbToDbChunkSize = 100;
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final SqlSessionFactory sqlSessionFactory;
	
	@Autowired
	SftpConfig sftpConfig;
	
	@Autowired
	DownloadConfig downloadConfig;
	
	
	@Bean
	public Job singleCycleJob() {
	    return jobBuilderFactory.get(JOB_NAME)
	        /*.start(downloadFileStep())
	        .next(decompressFileStep())
	        .next(fileToDbStep())
	        .next(dbToDbStep())*/
	        .start(testStep())
	        .build();
	}
	
	@Bean
	@JobScope
	public Step testStep() {
		return stepBuilderFactory.get("testStep")
                .tasklet((contribution, chunkContext) -> {
                	
                	LocalDate date = LocalDate.now();
                	String parsedLocalDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                			
                	logger.info("Start Test Step");
                	logger.info("SFTP host : {}", sftpConfig.getHost());
                	logger.info("SFTP port : {}", sftpConfig.getPort());
                	logger.info("SFTP user : {}", sftpConfig.getUser());
                	logger.info("SFTP password : {}", sftpConfig.getPassword());
                	logger.info("SFTP directory : {}", sftpConfig.getDirectory());
                	
                	logger.info("Download directory : {}", downloadConfig.getDirectory());
                	
                	logger.info("ParsedLocalDate : {}", parsedLocalDate);
                	
                    return RepeatStatus.FINISHED;
                }).build();
	}
	
	@Bean
	@JobScope
	public Step downloadFileStep() {
		return stepBuilderFactory.get("downloadFileStep")
                .tasklet((contribution, chunkContext) -> {
                	
                	logger.info("Start First Step");
                	
                	String host = sftpConfig.getHost();
                	int port = sftpConfig.getPort();
                    String username = sftpConfig.getUser();
                    String password = sftpConfig.getPassword();
                    String root = sftpConfig.getDirectory();
                    
                    String targetPath ;
                    String destinationPath = downloadConfig.getDirectory();
                    
                	SftpUtil sftpUtil = new SftpUtil(host, port, username, password, root);
                	sftpUtil.downloadFileList("20211217", "C:/awhp_cycle_files/20211217/");
                	
                    return RepeatStatus.FINISHED;
                }).build();
	}
	
	@Bean
	@JobScope
	public Step decompressFileStep() {
		return stepBuilderFactory.get("decompressFileStep")
                .tasklet((contribution, chunkContext) -> {
                	
                	logger.info("Start Second Step");
                	File dirFile = new File("C:/awhp_cycle_files/20211217/");
                    File[] fileList = dirFile.listFiles();
                    Arrays.sort(fileList, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                    
                    for(File file: fileList) {
                            logger.debug("file name : {}", file.getName());
                            Path source = Paths.get("C:/awhp_cycle_files/20211217/" + file.getName());
                            Path target = Paths.get("C:/awhp_cycle_files/20211217/" + FilenameUtils.removeExtension(file.getName()));
                            GZIPUtil.decompressGzip(source , target);
                    }
                	
                    return RepeatStatus.FINISHED;
                }).build();
	}
	
	@Bean
	@JobScope
	public Step fileToDbStep() {
		logger.info("Start Third Step");
		
	    return stepBuilderFactory.get("fileToDbStep")
	        .<SingleCycle, SingleCycle>chunk(fileToDbChunkSize)
	        .reader(multiResourceItemReader())
	        .writer(singleCycleWriter())
	        .build();
	}
	
	@Bean
	@StepScope
	public MultiResourceItemReader<SingleCycle> multiResourceItemReader(){
		
		logger.info("Start MultiResourceItemReader");
		Resource[] inputResources = null;
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		
		try {
			inputResources = resourcePatternResolver.getResources("file:C:/awhp_cycle_files/20211217/*.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    MultiResourceItemReader<SingleCycle> resourceItemReader = new MultiResourceItemReader<SingleCycle>();
	    resourceItemReader.setResources(inputResources);
	    resourceItemReader.setDelegate(singleCycleFlatFileItemReader());
	    resourceItemReader.setStrict(true);
	    return resourceItemReader;
	}
	
	@Bean
	@StepScope
	public FlatFileItemReader<SingleCycle> singleCycleFlatFileItemReader() {
		
	    FlatFileItemReader<SingleCycle> itemReader = new FlatFileItemReader<>();
	    DefaultLineMapper<SingleCycle> lineMapper = new DefaultLineMapper<>();
	    
	    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
	    lineTokenizer.setDelimiter(",");
	    lineTokenizer.setNames(new String[] {"id_site","id_gw","id_odu","id_idu","gw_time","server_time","type","mode","oper"
	    		,"error","lock","settemp","waterouttempflag","settemp_hotwater","roomtemp","pipein_temp","pipeout_temp","tank_temp","sun_temp"
	    		,"hotwater","droomtempflag","droomtempflag_2","controlType","lownoisemode_oper","quickDHWheat_oper","pipe_inlet_temp","pipe_outlet_temp"
	    		,"heat_water_temp","flow_rate","air_temp_mix_circuit","water_temp_mix_circuit","DHW_oper_thermostat","heat_oper_thermostat"
	    		,"cool_oper_thermostat","control_air_water","threeway_DHW","conn_thermostat","heavy_trouble","avail_emer_oper_DHW","avail_emer_oper_heating"
	    		,"forced_defrost","slight_trouble","critical_trouble","conn_3rd_party","smartgrid2","smartgrid1","flow_switch","screed_drying_oper"
	    		,"external_digital","int_water_pump_oper","ext_water_pump_oper","solar_water_pump_oper","solar_3way_valve_direction","twoway_valve_direction"
	    		,"backup_heater_A","backup_heater_B","DHW_boost_heater","ext_boiler_oper","mix_valve_open","mix_valve_close","mixed_water_pump_oper"
	    		,"water_pump_capa","indoor_unit_capa"});
	    lineMapper.setLineTokenizer(lineTokenizer);
	    SingleCycleFieldSetMapper fieldSetMapper = new SingleCycleFieldSetMapper();
	    lineMapper.setFieldSetMapper(fieldSetMapper);
	    itemReader.setLineMapper(lineMapper);
	    return itemReader;
	}
	
	@Bean
	@StepScope
	public MyBatisBatchItemWriter<SingleCycle> singleCycleWriter() {
		
	    return new MyBatisBatchItemWriterBuilder<SingleCycle>()
	    .sqlSessionFactory(sqlSessionFactory)
	    .statementId("singleCycle.saveSingleCycleOriginInfo")
	    .build();
	}
	
	@Bean
	@JobScope
	public Step dbToDbStep() {
		logger.info("Start Fourth Step");
		
	    return stepBuilderFactory.get("dbToDbStep")
	        .<SingleCycleAvg, SingleCycleAvg>chunk(dbToDbChunkSize)
	        .reader(singleCycleAvgPagingItemReader())
	        .writer(singleCycleAvgWriter())
	        .build();
	}
	
	@Bean
    @StepScope
    public MyBatisPagingItemReader<SingleCycleAvg> singleCycleAvgPagingItemReader() {

        /*Map<String,Object> parameterValues = new HashMap<>();
        parameterValues.put("amount", "10000");*/

        return new MyBatisPagingItemReaderBuilder<SingleCycleAvg>()
                .pageSize(dbToDbChunkSize)
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("singleCycle.selectSingleCycleAvg")
                //.parameterValues(parameterValues)
                .build();
    }
	
	@Bean
	@StepScope
	public MyBatisBatchItemWriter<SingleCycleAvg> singleCycleAvgWriter() {
		
	    return new MyBatisBatchItemWriterBuilder<SingleCycleAvg>()
	    .sqlSessionFactory(sqlSessionFactory)
	    .statementId("singleCycle.saveSingleCycleAvgInfo")
	    .build();
	}
	
	protected static class SingleCycleFieldSetMapper implements FieldSetMapper<SingleCycle> {
	    public SingleCycle mapFieldSet(FieldSet fieldSet) {
	        SingleCycle singleCycle = new SingleCycle();
	        
	        singleCycle.setId_site(fieldSet.readString("id_site"));
	        singleCycle.setId_gw(fieldSet.readString("id_gw"));
	        singleCycle.setId_odu(fieldSet.readString("id_odu"));
	        singleCycle.setId_idu(fieldSet.readString("id_idu"));
	        singleCycle.setGw_time( Timestamp.valueOf(fieldSet.readString("gw_time")) );
	        singleCycle.setServer_time( Timestamp.valueOf(fieldSet.readString("server_time")) );
	        singleCycle.setType(fieldSet.readString("type"));
	        singleCycle.setMode(fieldSet.readString("mode"));
	        singleCycle.setOper(fieldSet.readString("oper"));
	        singleCycle.setError(fieldSet.readInt("error"));
	        singleCycle.setLock(fieldSet.readString("lock"));
	        singleCycle.setSettemp(fieldSet.readDouble("settemp"));
	        singleCycle.setWaterouttempflag(fieldSet.readString("waterouttempflag"));
	        singleCycle.setSettemp_hotwater(fieldSet.readDouble("settemp_hotwater"));
	        singleCycle.setRoomtemp(fieldSet.readDouble("roomtemp"));
	        singleCycle.setPipein_temp(fieldSet.readDouble("pipein_temp"));
	        singleCycle.setPipeout_temp(fieldSet.readDouble("pipeout_temp"));
	        singleCycle.setTank_temp(fieldSet.readDouble("tank_temp"));
	        singleCycle.setSun_temp(fieldSet.readDouble("sun_temp"));
	        singleCycle.setHotwater(fieldSet.readString("hotwater"));
	        singleCycle.setDroomtempflag(fieldSet.readString("droomtempflag"));
	        singleCycle.setDroomtempflag_2(fieldSet.readString("droomtempflag_2"));
	        singleCycle.setControlType(fieldSet.readString("controlType"));
	        singleCycle.setLownoisemode_oper(fieldSet.readString("lownoisemode_oper"));
	        singleCycle.setQuickDHWheat_oper(fieldSet.readString("quickDHWheat_oper"));
	        singleCycle.setPipe_inlet_temp(fieldSet.readDouble("pipe_inlet_temp"));
	        singleCycle.setPipe_outlet_temp(fieldSet.readDouble("pipe_outlet_temp"));
	        singleCycle.setHeat_water_temp(fieldSet.readDouble("heat_water_temp"));
	        singleCycle.setFlow_rate(fieldSet.readDouble("flow_rate"));
	        singleCycle.setAir_temp_mix_circuit(fieldSet.readDouble("air_temp_mix_circuit"));
	        singleCycle.setWater_temp_mix_circuit(fieldSet.readDouble("water_temp_mix_circuit"));
	        singleCycle.setDHW_oper_thermostat(fieldSet.readString("DHW_oper_thermostat"));
	        singleCycle.setHeat_oper_thermostat(fieldSet.readString("heat_oper_thermostat"));
	        singleCycle.setCool_oper_thermostat(fieldSet.readString("cool_oper_thermostat"));
	        singleCycle.setControl_air_water(fieldSet.readString("control_air_water"));
	        singleCycle.setThreeway_DHW(fieldSet.readString("threeway_DHW"));
	        singleCycle.setConn_thermostat(fieldSet.readString("conn_thermostat"));
	        singleCycle.setHeavy_trouble(fieldSet.readString("heavy_trouble"));
	        singleCycle.setAvail_emer_oper_DHW(fieldSet.readString("avail_emer_oper_DHW"));
	        singleCycle.setAvail_emer_oper_heating(fieldSet.readString("avail_emer_oper_heating"));
	        singleCycle.setForced_defrost(fieldSet.readString("forced_defrost"));
	        singleCycle.setSlight_trouble(fieldSet.readString("slight_trouble"));
	        singleCycle.setCritical_trouble(fieldSet.readString("critical_trouble"));
	        singleCycle.setConn_3rd_party(fieldSet.readString("conn_3rd_party"));
	        singleCycle.setSmartgrid2(fieldSet.readString("smartgrid2"));
	        singleCycle.setSmartgrid1(fieldSet.readString("smartgrid1"));
	        singleCycle.setFlow_switch(fieldSet.readString("flow_switch"));
	        singleCycle.setScreed_drying_oper(fieldSet.readString("screed_drying_oper"));
	        singleCycle.setExternal_digital(fieldSet.readString("external_digital"));
	        singleCycle.setInt_water_pump_oper(fieldSet.readString("int_water_pump_oper"));
	        singleCycle.setExt_water_pump_oper(fieldSet.readString("ext_water_pump_oper"));
	        singleCycle.setSolar_water_pump_oper(fieldSet.readString("solar_water_pump_oper"));
	        singleCycle.setSolar_3way_valve_direction(fieldSet.readString("solar_3way_valve_direction"));
	        singleCycle.setTwoway_valve_direction(fieldSet.readString("twoway_valve_direction"));
	        singleCycle.setBackup_heater_A(fieldSet.readString("backup_heater_A"));
	        singleCycle.setBackup_heater_B(fieldSet.readString("backup_heater_B"));
	        singleCycle.setDHW_boost_heater(fieldSet.readString("DHW_boost_heater"));
	        singleCycle.setExt_boiler_oper(fieldSet.readString("ext_boiler_oper"));
	        singleCycle.setMix_valve_open(fieldSet.readString("mix_valve_open"));
	        singleCycle.setMix_valve_close(fieldSet.readString("mix_valve_close"));
	        singleCycle.setMixed_water_pump_oper(fieldSet.readString("mixed_water_pump_oper"));
	        singleCycle.setWater_pump_capa(fieldSet.readDouble("water_pump_capa"));
	        singleCycle.setIndoor_unit_capa(fieldSet.readDouble("indoor_unit_capa"));
	        
	        return singleCycle;
	    }
	}
	
	
	
}
