package com.lg.awhp.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SingleCycleAvg {
	
	private String id_site;
	private String id_gw;
	private String id_odu;
	private String id_idu;
	private Timestamp last_date;
	private String oper;
	private Double total_settemp;
	private int count_settemp;
	private Double avg_settemp;
	private Double total_roomtemp;
	private int count_roomtemp;
	private Double avg_roomtemp;
	private Double total_pipein_temp;
	private int count_pipein_temp;
	private Double avg_pipein_temp;
	private Double total_pipeout_temp;
	private int count_pipeout_temp;
	private Double avg_pipeout_temp;
	private Double total_capa;
	private int count_capa;
	private Double avg_capa;
	private Double total_fan;
	private int count_fan;
	private Double avg_fan;
	private Double total_lev;
	private int count_lev;
	private Double avg_lev;
	
}
