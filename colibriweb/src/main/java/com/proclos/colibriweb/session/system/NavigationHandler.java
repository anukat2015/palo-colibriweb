package com.proclos.colibriweb.session.system;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.navigation.Pages;

@Name("navigationHandler")
@Install(precedence = Install.FRAMEWORK)
public class NavigationHandler
{
	public void endAllOtherConversationsAndRedirect(String viewId)
	{
		Redirect.instance().setViewId(viewId);
		Manager.instance().killAllOtherConversations();
		Redirect.instance().execute();
	}

	public void endConversationAndRedirect(String viewId)
	{
		Map<String, Object> parameters = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
		Redirect.instance().setViewId(viewId);
		if(parameters.containsKey("pid"))
		{
			Conversation.instance().endBeforeRedirect();
			Redirect.instance().setParameter("cid", parameters.get("pid"));
		}
		else if(parameters.containsKey("cid"))
		{
			Redirect.instance().setParameter("cid", parameters.get("cid"));
		}
		Redirect.instance().execute();
	}

	public void endCurrentConversation()
	{
		Conversation.instance().endBeforeRedirect();
		Redirect.instance().setViewId(Pages.getCurrentViewId());
		Redirect.instance().execute();
	}
}
