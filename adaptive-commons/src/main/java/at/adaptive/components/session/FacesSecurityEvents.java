package at.adaptive.components.session;

import static org.jboss.seam.ScopeType.APPLICATION;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage.Severity;

@Name("org.jboss.seam.security.facesSecurityEvents")
@Scope(APPLICATION)
@Install(precedence = Install.FRAMEWORK, classDependencies = "javax.faces.context.FacesContext")
@BypassInterceptors
@Startup
public class FacesSecurityEvents extends org.jboss.seam.security.FacesSecurityEvents
{
	@Override
	public Severity getLoginFailedMessageSeverity()
	{
		return Severity.ERROR;
	}
}
