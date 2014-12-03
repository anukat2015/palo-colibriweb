package com.proclos.colibriweb.session.modules.user;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.primefaces.model.DualListModel;

import com.proclos.colibriweb.entity.user.ColibriRole;
import com.proclos.colibriweb.entity.user.ColibriUser;

@Name("accountModule")
@Scope(ScopeType.SESSION)
public class AccountModule extends AbstractUserModule
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7407518169375347417L;

	@In("#{userroles}")
	private List<ColibriRole> userroles;
	
	private DualListModel<ColibriRole> listModel; 

	@Factory(value = "users", scope = ScopeType.APPLICATION)
	public List<ColibriUser> getUsers()
	{
		return getAllResults();
	}

	@Override
	public String persist()
	{
		try
		{
			getInstance().setRoles(listModel.getTarget());
			super.persist();
			return "";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}

	}
	
	public String persistCurrentUser() {
		ColibriUser user = (ColibriUser)Component.getInstance("user");
		if (!getEntityManager().contains(user)) {
			user = getEntityManager().merge(user);
		}
		setInstance(user);
		return super.persist();
	}

	public void loadUser(Long id) {
		setInstance(getEntityManager().find(ColibriUser.class, id));
		//instanceSelected();
	}
	
	@Override
	protected void handleEvent(String event)
	{
		Contexts.getApplicationContext().remove("users");
		Contexts.getApplicationContext().remove("roles");
		super.handleEvent(event);
	}

	@Override
	protected void instanceCreated()
	{
		super.instanceCreated();
		List<ColibriRole> available = new ArrayList<ColibriRole>();
		List<ColibriRole> selected = new ArrayList<ColibriRole>();
		available.addAll(userroles);
		listModel = new DualListModel<ColibriRole>(available, selected);  
		try
		{
			ColibriRole role = (ColibriRole)getEntityManager().createQuery("from OpenSMCRole where name='user'").getSingleResult();
			getInstance().getRoles().add(role);
		}
		catch(Exception e)
		{}
	}
	
	@Override
	protected void instanceSelected()
	{
		super.instanceSelected();
		List<ColibriRole> available = new ArrayList<ColibriRole>();
		List<ColibriRole> selected = new ArrayList<ColibriRole>();
		selected.addAll(getInstance().getRoles());
		available.addAll(userroles);
		available.removeAll(selected);
		listModel = new DualListModel<ColibriRole>(available, selected);  
	}
	
	public DualListModel<ColibriRole> getListModel() {
		return listModel;
	}
	
	public void setListModel(DualListModel<ColibriRole> listModel) {
		this.listModel = listModel;
	}
}
