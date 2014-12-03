package com.proclos.colibriweb.session.modules.component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.primefaces.event.FileUploadEvent;

import com.jedox.etl.core.config.Settings;
import com.jedox.etl.core.util.CustomClassLoader;
import com.proclos.colibriweb.entity.component.Library;
import com.proclos.colibriweb.session.modules.MasterDataModule;

@Name("libraryModule")
@Scope(ScopeType.SESSION)
public class LibraryModule extends MasterDataModule<Library> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069681038559704453L;

	@Override
	protected String getContextParamName() {
		return "customlibs";
	}

	@Override
	public Class<Library> getEntityClass() {
		return Library.class;
	}
	
	@Factory(value = "customlibs", scope = ScopeType.APPLICATION)
	public List<Library> getCustomLibs()
	{
		return getAllResults();
	}
	
	public void handleFileUpload(FileUploadEvent event) {  
       getInstance().setData(event.getFile().getContents());
       getInstance().setFileName(event.getFile().getFileName());
       Settings.getInstance().getCustomlibDir();
       try {
    	   String path = Settings.getInstance().getCustomlibDir()+"/"+getInstance().getFileName();
    	   if (!new File(Settings.getInstance().getCustomlibDir()).exists()) {
    		   new File(Settings.getInstance().getCustomlibDir()).mkdirs();
    	   }
    	   FileOutputStream stream = new FileOutputStream(path);
    	   stream.write(getInstance().getData());
    	   stream.flush();
    	   stream.close();
    	   info("New Library was created at "+path);
    	   CustomClassLoader.refresh();
       }
       catch (Exception e) {
    	   addErrorMessage("File cannot be written: "+e.getMessage());
       }
    }  
	
	public void delete(Object object) {
		if (object instanceof Long) {
			Library instance = getEntityManager().find(Library.class, (Long)object);
	    	String path = Settings.getInstance().getCustomlibDir()+"/"+instance.getFileName();
	    	new File(path).delete();
			super.delete(object);
		}
	}

}
