package com.lg.awhp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import ch.qos.logback.classic.Logger;

public class SftpUtil {
	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	// Set the prompt when logging in for the first time. Optional value: (ask | yes | no)
    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "no";
    private static final String PATHSEPARATOR = "/";
    
    String host ="";
    String username ="";
    String password = "";
    String root = "";
    
    int port;
    int timeout = 15000;
    
    public SftpUtil(String host , int port , String username , String password, String root)
    {
    	this.host = host;
    	this.port = port;
    	this.username = username;
    	this.password = password;
    	this.root = root;
    }
    
	private ChannelSftp createSftp() throws Exception {
		JSch jsch = new JSch();

		Session session = createSession(jsch, host, username, port);
		session.setPassword(password);
		session.connect(timeout);
		
		Channel channel = session.openChannel("sftp");
		channel.connect(timeout);

		return (ChannelSftp) channel;
	}
	
    private Session createSession(JSch jsch, String host, String username, Integer port) throws Exception {
        Session session = null;
 
        if (port <= 0) {
            session = jsch.getSession(username, host);
        } else {
            session = jsch.getSession(username, host, port);
        }
 
        if (session == null) {
            throw new Exception(host + " session is null");
        }
 
        session.setConfig("StrictHostKeyChecking", SESSION_CONFIG_STRICT_HOST_KEY_CHECKING);
        return session;
    }

    private void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                } else if (sftp.isClosed()) {
                }
                if (null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }
    
    public File downloadFile(String targetPath) throws Exception {
    	
    	ChannelSftp sftp = this.createSftp();
        OutputStream outputStream = null;
        try {
            sftp.cd(root);
            logger.debug("Change path to {}", root);
     
            File file = new File(targetPath.substring(targetPath.lastIndexOf(PATHSEPARATOR) + 1));
     
            outputStream = new FileOutputStream(file);
            sftp.get(targetPath, outputStream);
            logger.debug("Download file success. TargetPath: {}", targetPath);
            return file;
            
        } catch (Exception e) {
        	
        	logger.error("Download file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Download File failure");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            this.disconnect(sftp);
        }

    }
    
    public void downloadFileList(String targetPath, String destinationPath) throws Exception {
    	
    	ChannelSftp sftp = this.createSftp();
        OutputStream outputStream = null;
        try {
            sftp.cd(root);
            logger.debug("Change path to {}", root);
            
            Vector<ChannelSftp.LsEntry> fileAndFolderList = sftp.ls(targetPath);
            
            for (ChannelSftp.LsEntry item : fileAndFolderList) {
            	if (!item.getAttrs().isDir()) {
            		
            		if (!(new File(destinationPath + PATHSEPARATOR + item.getFilename())).exists()
                            || (item.getAttrs().getMTime() > Long
                                    .valueOf(new File(destinationPath + PATHSEPARATOR + item.getFilename()).lastModified()
                                            / (long) 1000)
                                    .intValue())) {
            			
            			File folder = new File(destinationPath);
            			if( !folder.exists() ) {
            				folder.mkdir();
            				
            				logger.debug("Make Folder! | destinationPath: {}", destinationPath);
            			}

            			new File(destinationPath + PATHSEPARATOR + item.getFilename());
            			sftp.get(targetPath + PATHSEPARATOR + item.getFilename(),
                                destinationPath + PATHSEPARATOR + item.getFilename()); // 파일 다운로드 하기
            		}
            		
            	}
            	else if (!(".".equals(item.getFilename()) || "..".equals(item.getFilename()))) {
                    new File(destinationPath + PATHSEPARATOR + item.getFilename()).mkdirs(); // Empty folder copy.
                    sftp.get(targetPath + PATHSEPARATOR + item.getFilename(),
                            destinationPath + PATHSEPARATOR + item.getFilename()); 
                }
            }
                        
        } catch (Exception e) {
        	
        	logger.error("Download file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Download File failure");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            this.disconnect(sftp);
            
        }
    }
    
    public boolean uploadFile(String targetPath, File file) throws Exception {
        return this.uploadFile(targetPath, new FileInputStream(file));
    }
    
    private boolean uploadFile(String targetPath, InputStream inputStream) throws Exception {
        ChannelSftp sftp = this.createSftp();
        try {
            sftp.cd(root);

            int index = targetPath.lastIndexOf(PATHSEPARATOR);
 
            String fileName = targetPath.substring(index + 1);

            sftp.put(inputStream, fileName);
            return true;
        } catch (Exception e) {
        
        
            throw new Exception("Upload File failure");
        } finally {
            this.disconnect(sftp);
        }
    }
    
    public boolean deleteFile(String targetPath) throws Exception {
        ChannelSftp sftp = null;
        try {
            sftp = this.createSftp();
            sftp.cd(root);
            sftp.rm(targetPath);
            return true;
        } catch (Exception e) {
        	logger.error("Delete file failure. TargetPath: {}", targetPath, e);
            throw new Exception("Delete File failure");
        } finally {
            this.disconnect(sftp);
        }
    }
    
}
