package com.lg.awhp.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix="sftp")
public class SftpConfig {
	
	private String host;
	private int port;
	private String user;
	private String password;
	private String directory;

}
