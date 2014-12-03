package com.proclos.colibriweb.session.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import com.jedox.etl.core.config.Settings;
import com.jedox.etl.core.logging.LogManager;
import com.jedox.etl.core.util.FileUtil;



public class ColibriListener implements ServletContextListener {
	
	private String getContextInitParameter(ServletContext servletCtx, String rootDir, String param){
		// Try to find global directory
		// It has to contain repository and persistence
		String dir = servletCtx.getInitParameter(param);
		if (dir != null && FileUtil.isRelativ(dir))
			dir = rootDir + File.separator + dir;
		return dir;
	}
	
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                Logger.getRootLogger().info("Deregistering jdbc driver: "+driver);
            } catch (SQLException e) {
            	Logger.getRootLogger().error("Error deregistering jdbc driver "+driver);
            }
        }    
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		
		String rootPath = context.getRealPath("/");
		
		if (rootPath != null) {
			try {
				Settings.setRootDir(rootPath);
			} catch (IOException e) {
				// log.error("Can't get canonical path to root directory" + rootDir);
			}
		} // else: rely on Config that it finds some reasonable default

		String logDir = getContextInitParameter(context,rootPath,"etlserver.logdir");
		if (logDir != null) {
			try {
				Settings.setGlobalLogDir(logDir);
			} catch (IOException e) {
				// log.error("Can't get canonical path to global log directory" + logDir);
			}
		} // else: log directory from config.xml used
		
		String dataDir = getContextInitParameter(context,rootPath,"etlserver.datadir");		
		if (dataDir != null) {
			try {
				Settings.setGlobalDataDir(dataDir);
			} catch (IOException e) {
				// log.error("Can't get canonical path to global data directory" + dataDir);
			}
		} // else: data directory from config.xml used
	
		// Initialise LogManager and override log level with value stored in config.xml
		String level = Settings.getInstance().getContext(Settings.ProjectsCtx).getProperty("loglevel");
		LogManager.createLogFiles(false);
		LogManager.getInstance().setLevel(level);
		
		String filename = Settings.getInstance().getLogDir() + File.separator + "colibri.log";
		try {
			RollingFileAppender appender = new RollingFileAppender(new PatternLayout("%d %p %t %c - %m%n"),filename,true);
			appender.setMaxFileSize("10000KB");
			appender.setMaxBackupIndex(5);
			//appender.setThreshold(Level.WARN);
			Logger.getRootLogger().addAppender(appender);
		}
		catch (IOException e) {
			Logger.getRootLogger().warn("Cannot create logfile "+filename+": "+e.getMessage());
		}
		/*
				
		Logger.getRootLogger().info("StartUp ETL-Service");
		Logger.getRootLogger().info("Palo ETL Server Version: "+Settings.getInstance().getVersion());
		Logger.getRootLogger().info("Setting application root directory : " + rootPath);
		Logger.getRootLogger().info("Setting application log directory : " + logDir);
		Logger.getRootLogger().info("Setting application data directory : " + dataDir);
		*/
		setSSLProperties(rootPath);
		
	}
	
	private void setSSLProperties(String rootDir) {
		// sets the keystore path
		try{
			 Properties props = new Properties();
			 File propFile = new File(rootDir + "config/ssl.properties");

				try {
					props.load(propFile.toURI().toURL().openStream());
				} catch (MalformedURLException e) {
					Logger.getRootLogger().debug("Try to open SSL connecion " + e.getMessage());
				} catch (IOException e) {
					Logger.getRootLogger().debug("Try to open SSL connecion " + e.getMessage());
				}

			 for (Object prop: props.keySet()) {
				 String value = props.getProperty((String)prop);
				 if( (((String)prop).equals("javax.net.ssl.trustStore")) || (((String)prop).equals("javax.net.ssl.keyStore")) ){
					 value = rootDir.concat(value);
				 }
			     System.getProperties().setProperty((String)prop,value);
			 }
		}catch(Exception ioe){
			Logger.getRootLogger().debug("SSL properties file is not found under the expected folder," + ioe.getMessage());
		}		
	}

}
