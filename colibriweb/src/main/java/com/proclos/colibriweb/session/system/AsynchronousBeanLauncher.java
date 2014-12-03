package com.proclos.colibriweb.session.system;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

@Name("asynchronousBeanLauncher")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.APPLICATION)
public class AsynchronousBeanLauncher
{
	private static final String CRON = "0,30 * * * * ?";

	//@In(create = true)
	//private HeroldUpdateManager heroldUpdateManager;

	@Observer("org.jboss.seam.postInitialization")
	public void launchAsynchronousBeans()
	{
		//heroldUpdateManager.update(CRON_HEROLD_UPDATE);
	}
}
