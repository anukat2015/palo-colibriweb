package com.proclos.colibriweb.session.modules.component;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.log.Log;
import org.jdom.Element;

import com.jedox.etl.core.component.Locator;
import com.jedox.etl.core.config.ConfigConverter;
import com.jedox.etl.core.config.ConfigManager;
import com.jedox.etl.core.project.IProject;
import com.jedox.etl.core.util.XMLUtil;

@Name("projectExport")
@Scope(ScopeType.CONVERSATION)
public class ProjectExport {
	
	@In(value="#{facesContext.externalContext}")
	private ExternalContext extCtx;
	
	@In(value="#{facesContext}")
	FacesContext facesContext;

	@RequestParameter
	private String projectName;
	
	@Logger
	private Log log;
	
	
	public String download() {
		String defaultCharSet = "UTF-8";
		String defaultMimeType = "application/xml";
		HttpServletResponse response = (HttpServletResponse)extCtx.getResponse();
		try {
			Element element = ConfigManager.getInstance().get(new Locator().add(projectName));
			//enforce conversion to newest format. Only necessary after a version upgrade when a project has not been saved....
			element.setAttribute("format", IProject.Declaration.lazy.toString());
			ConfigConverter converter = new ConfigConverter();
			element = converter.convert(element);
			String value = XMLUtil.jdomToString(element);
			byte[] bytes = value.getBytes(defaultCharSet);
			defaultMimeType += "; charset=" + defaultCharSet;
			response.setContentType(defaultMimeType);
	        response.addHeader("Content-disposition", "attachment; filename=\"" + projectName +".xml\"");
			ServletOutputStream os = response.getOutputStream();
			os.write(bytes);
			os.flush();
			os.close();
			facesContext.responseComplete();
		} catch(Exception e) {
			log.error("Failed to export project "+projectName+": "+e.getMessage());
		}

		return null;
	}
}