package com.lg.awhp.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SingleCycle {

	private String id_site;
	private String id_gw;
	private String id_odu;
	private String id_idu;
	private Timestamp gw_time;
	private Timestamp server_time;
	private String type;
	private String mode;
	private String oper;
	private int error;
	private String lock;
	private Double settemp;
	private String waterouttempflag;
	private Double settemp_hotwater;
	private Double roomtemp;
	private Double pipein_temp;
	private Double pipeout_temp;
	private Double tank_temp;
	private Double sun_temp;
	private String hotwater;
	private String droomtempflag;
	private String droomtempflag_2;
	private String controlType;
	private String lownoisemode_oper;
	private String quickDHWheat_oper;
	private Double pipe_inlet_temp;
	private Double pipe_outlet_temp;
	private Double heat_water_temp;
	private Double flow_rate;
	private Double air_temp_mix_circuit;
	private Double water_temp_mix_circuit;
	private String DHW_oper_thermostat;
	private String heat_oper_thermostat;
	private String cool_oper_thermostat;
	private String control_air_water;
	private String threeway_DHW;
	private String conn_thermostat;
	private String heavy_trouble;
	private String avail_emer_oper_DHW;
	private String avail_emer_oper_heating;
	private String forced_defrost;
	private String slight_trouble;
	private String critical_trouble;
	private String conn_3rd_party;
	private String smartgrid2;
	private String smartgrid1;
	private String flow_switch;
	private String screed_drying_oper;
	private String external_digital;
	private String int_water_pump_oper;
	private String ext_water_pump_oper;
	private String solar_water_pump_oper;
	private String solar_3way_valve_direction;
	private String twoway_valve_direction;
	private String backup_heater_A;
	private String backup_heater_B;
	private String DHW_boost_heater;
	private String ext_boiler_oper;
	private String mix_valve_open;
	private String mix_valve_close;
	private String mixed_water_pump_oper;
	private Double water_pump_capa;
	private Double indoor_unit_capa;
}
