package com.lg.awhp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class GZIPUtil {
	
	public static void decompressGzip(Path source, Path target) throws IOException {

		File sourceFile = source.toFile();
		String sourceExt = FilenameUtils.getExtension(sourceFile.getName());
		
		if( sourceExt.equals("gz")) {
			
	        try (GZIPInputStream gis = new GZIPInputStream(
                    new FileInputStream(source.toFile()));
						FileOutputStream fos = new FileOutputStream(target.toFile())) {
						
				byte[] buffer = new byte[1024];
				int len;
				while ((len = gis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
			}
	        
	        sourceFile.delete();
		}
    }
	
}
