/**
*   @brief <Description of Class>
*
*   @file
*
*   Copyright (C) 2008-2014 Jedox AG
*
*   This program is free software; you can redistribute it and/or modify it
*   under the terms of the GNU General Public License (Version 2) as published
*   by the Free Software Foundation at http://www.gnu.org/copyleft/gpl.html.
*
*   This program is distributed in the hope that it will be useful, but WITHOUT
*   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
*   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
*   more details.
*
*   You should have received a copy of the GNU General Public License along with
*   this program; if not, write to the Free Software Foundation, Inc., 59 Temple
*   Place, Suite 330, Boston, MA 02111-1307 USA
*
*   If you are developing and distributing open source applications under the
*   GPL License, then you are free to use Palo under the GPL License.  For OEMs,
*   ISVs, and VARs who distribute Palo with their products, and do not license
*   and distribute their source code under the GPL, Jedox provides a flexible
*   OEM Commercial License.
*
*   Developed by proclos OG, Wien on behalf of Jedox AG. Intellectual property
*   rights has proclos OG, Wien. Exclusive worldwide exploitation right
*   (commercial copyright) has Jedox AG, Freiburg.
*
*   @author Christian Schwarzinger, proclos OG, Wien, Austria
*   @author Gerhard Weis, proclos OG, Wien, Austria
*   @author Andreas Fröhlich, Jedox AG, Freiburg, Germany
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/

package com.jedox.etl.core.config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import com.jedox.etl.core.component.ConfigurationException;
import com.jedox.etl.core.util.FileUtil;
import com.jedox.palojlib.interfaces.IConnectionConfiguration;
import com.jedox.palojlib.main.ConnectionConfiguration;
import com.jedox.palojlib.main.ClientInfo;

/**
 * Accsessor class for global settings defined in the file "config.xml" in the config directory.
 * @author Christian Schwarzinger. Mail: christian.schwarzinger@proclos.com
 *
 */
public class Settings {

	private static Log log = LogFactory.getLog(Settings.class);
	private static Settings instance;
	private HashMap<String,Properties> contextLookup = new HashMap<String,Properties>();
	private Document document;
	private String filename;

//  ETL Server Version
	public static final Double versionetl = 6.01;
	public static final String version = "5.1.$BIT$.$WCREV$";

	public static final String DirectoriesCtx = "directories";
	public static final String ProjectsCtx = "projects";
	public static final String EncryptionCtx = "encryption";
	public static final String ExecutionsCtx = "executions";
	public static final String PersistenceCtx = "persistence";

	// TODO: maybe introduce set-once semantic on this property.
	//       after singleton is instanziated it should not be possible to change this property
	private static String configDir = null;
	private static String rootDir = null;
	private static String globalDataDir = null;
	private static String globalLogDir = null;
	private boolean autoSave = true;	
	private String cfgSecret = null;
	
	private Settings(String fname) {
		readConfigXML(fname);
	}

	/**
	 * Sets the root directory of the project
	 * @param path
	 * @return true if path is set to new value otherwise false
	 * @throws IOException
	 */
	public static boolean setRootDir(String path) throws IOException {
		if (instance == null) {
			File f = new File(path);
			rootDir = f.getCanonicalPath();
			return true;
		}
		return false;
	}
	
	/**
	 * Sets a global data directory of the project
	 * If this directory is set, it stores the projects, persistency and files, 
	 * if not they are stored under the local data directory underneath the root directory 
	 * @param path
	 * @return true if path is set to new value otherwise false
	 * @throws IOException
	 */
	public static boolean setGlobalDataDir(String path) throws IOException {
		if (instance == null) {
			File f = new File(path);
			globalDataDir = f.getCanonicalPath();
			return true;
		}
		return false;
	}
	
	/**
	 * Sets a global log directory of the project
	 * if not they are stored under the local log directory underneath the root directory 
	 * @param path
	 * @return true if path is set to new value otherwise false
	 * @throws IOException
	 */
	public static boolean setGlobalLogDir(String path) throws IOException {
		if (instance == null) {
			File f = new File(path);
			globalLogDir = f.getCanonicalPath();
			return true;
		}
		return false;
	}
	
	/**
	 * Set configuration base directory.
	 * This is the directory where config.xml, component.xml and server.properties are stored.
	 * After this class is instantiated this function has no effect.
	 * @param path
	 * @return true if path is set to new value otherwise false
	 * @throws IOException
	 */
	public static boolean setConfigDir(String path) throws IOException {
		if (instance == null) {
			File f = new File(path);
			configDir = f.getCanonicalPath();
			return true;
		}
		return false;
	}

	/**
	 * gets the base config directory, where config files are stored on disk.
	 * @return the config directory
	 */
	public static String getConfigDir() {
		if (configDir != null)
			return configDir;
		return getRootDir() + File.separator + "config";
	}

	/**
	 * gets the root directory of the application. default is "."
	 * @return the root directory of the application config tree
	 */
	public static String getRootDir() {
		if (rootDir != null)
			return FileUtil.convertPath(rootDir);
		return ".";
	}
	
	private String getLocalDataDir() {
		if (globalDataDir != null)
			return FileUtil.convertPath(globalDataDir);
		else
			return getRootDir() + File.separator + FileUtil.convertPath(getContext(DirectoriesCtx).getProperty("localdata", ""));				
	}
	
	
	/**
	 * gets the directory for the internal persistency
	 * @return the perdistency directory
	 */
	public String getPersistenceDir() {
		return getLocalDataDir() +  File.separator + FileUtil.convertPath(getContext(DirectoriesCtx).getProperty("persistence", "db"));
	}

	/**
	 * gets the project directory of the application.
	 * @return the project directory
	 */
	public String getProjectDir() {
		return getLocalDataDir() +  File.separator + FileUtil.convertPath(getContext(DirectoriesCtx).getProperty("projects","projects"));
	}

	/**
	 * gets the data directory relative to the projects directory, where external file data (e.g. *.csv files) for projects is stored.
	 * @return the data directory
	 */
	public String getDataDir() {
		return getLocalDataDir() + File.separator + FileUtil.convertPath(getContext(DirectoriesCtx).getProperty("files","data"));
	}
		
	/**
	 * gets the directory for all external libraries to be injected to this application with respect to the root directory.
	 * @return the custom library directory
	 */
	public String getCustomlibDir() {
		return getRootDir() + File.separator + FileUtil.convertPath(getContext(DirectoriesCtx).getProperty("customlib", "customlib"));
	}

	/**
	 * sets the projects directory
	 * @param projects the projects directory relative to the root directory.
	 */
	public void setProjectDir(String projects) {
		if (projects != null)
			getContext(DirectoriesCtx).setProperty("projects", projects);
	}


	public String getLogDir() {
		if (globalLogDir != null)
			return FileUtil.convertPath(globalLogDir);
		else
			return getRootDir() + File.separator + FileUtil.convertPath(getContext(DirectoriesCtx).getProperty("logs", "logs"));				
	}
/*
	public String getLogDir() {
		return getRootDir() + File.separator + convertPath(getContext(DirectoriesCtx).getProperty("logs","logs"));
	}
*/	

	/**
	 * gets the instance of this settings class.
	 * @return the Settings class
	 */
	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings(getConfigDir() + File.separator + "config.xml");
		}
		return instance;
	}

	private void readConfigXML(String fname)
	{
		if (filename == null || filename.compareTo(fname) != 0)
		{
			filename = fname;
			try {
				Reader r = new BufferedReader(new FileReader(filename));
				readConfigXML(r);
			} catch (FileNotFoundException e)
			{
				log.warn("Configuration file "+new File(fname).getAbsolutePath()+" not found. Using default settings.");
				log.debug(e);
			}
		}
	}

	private void readConfigXML(Reader r)
	{
		Document doc = openXML(r);
		setDocument(doc);
	}

	private void setContext(String context, Properties properties) {
		contextLookup.put(context, properties);
	}

	/**
	 * gets all properties for a specific context. A context applies to a section defined in the "config.xml".
	 * @param context the name of the context.
	 * @return all properties of this context
	 */
	public Properties getContext(String context) {
		Properties properties = contextLookup.get(context);
		if (properties == null) {
			properties = new Properties();
			setContext(context,properties);
		}
		return properties;
	}

	private void initProjectsContext() {
		Element projects = document.getRootElement().getChild(ProjectsCtx);
		if (projects != null)
			setContext(ProjectsCtx, processParameters(projects.getChildren("parameter")));
	}

	private void initPersistenceContext() {
		Element projects = document.getRootElement().getChild(PersistenceCtx);
		if (projects != null)
			setContext(PersistenceCtx, processParameters(projects.getChildren("parameter")));
	}

	private void initEncryptionContext() {
		Element encryption = document.getRootElement().getChild(EncryptionCtx);
		if (encryption != null)
			setContext(EncryptionCtx, processParameters(encryption.getChildren("parameter")));
	}

	private void initExecutionsContext() {
		Element executions = document.getRootElement().getChild(ExecutionsCtx);
		if (executions != null)
			setContext(ExecutionsCtx, processParameters(executions.getChildren("parameter")));
	}

	// TODO: maybe convert directories here to absolute path names. (relativ to configDir?)
	private void initDirectoriesContext() {
		Element dirs = document.getRootElement().getChild("directories");
		if (dirs != null)
			setContext(DirectoriesCtx,processParameters(dirs.getChildren("parameter")));
	}

	/**
	 * adds some extra properties to a context, which is defined by a section in the "config.xml"
	 * @param context the name of the context
	 * @param properties the extra properties to add.
	 */
	public void addToContext(String context, Properties properties) {
		getContext(context).putAll(properties);
	}

	/**
	 * removes some properties from a context, which is defined by a section in the "config.xml"
	 * @param context the name of the context
	 * @param properties the properties to remove
	 */
	public void removeFromContext(String context, Properties properties) {
		Iterator<Object> i = properties.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			getContext(context).remove(key);
		}
	}

	private void setDocument(Document document)
	{
		if (document != null)
		{
			this.document = document;
			initProjectsContext();
			initDirectoriesContext();
			initEncryptionContext();
			initExecutionsContext();
			initPersistenceContext();
		}
		else
		{
			log.error("Document is null");
		}
	}

	/**
	 * helper method to get properties from a list of jdom elements specifying them.
	 * @param parameters the list of jdom Elements
	 * @return the properties defined by these elements
	 */
	private Properties processParameters(List<?> parameters) {
		Properties properties = new Properties();
		for (int j=0; j<parameters.size(); j++) {
			Element parameter = (Element) parameters.get(j);
			properties.put(parameter.getAttributeValue("name"), parameter.getTextTrim());
		}
		return properties;
	}

	private Document openXML(Reader r) {
		Document document = null;
		try {
			document = new SAXBuilder().build(r);
		} catch (IOException e) {
			log.error(e.getMessage());
			log.debug(e);
		} catch (JDOMException e) {
			log.error(e.getMessage());
			log.debug(e);
		}
		return document;
	}
	
	
	/** 
	 * Activates the writing of added projects to disk. In Server-Mode it should be true, in Standalone-Mode false
	 * @param autoSave
	 */
	public void setAutoSave(boolean autoSave) {
		this.autoSave = autoSave;
	}
	
	/**
	 * are added projects written to disk
	 * @return
	 */
	public boolean getAutoSave() {
		return autoSave;
	}
		
		
	public String getVersion() {
		String model = System.getProperty("sun.arch.data.model");
		String bitness="0";
		if (model==null)
			bitness="0";
		else if (model.equals("32"))
			bitness="2";
		else if (model.equals("64"))
			bitness="4";
		return version.replace("$BIT$",bitness);				
	}
	
	/**
	 * gets the type of the persistence for the projects.
	 * @return 
	 */
	public String getProjectsPersistence() {
		return FileUtil.convertPath(getContext(PersistenceCtx).getProperty("projectsBackend","file"));
	}
	
}
