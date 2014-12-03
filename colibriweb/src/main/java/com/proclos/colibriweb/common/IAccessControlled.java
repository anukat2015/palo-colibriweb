package com.proclos.colibriweb.common;

import com.proclos.colibriweb.entity.user.ColibriUser;

public interface IAccessControlled {
	
	public ColibriUser getCreator();
	public Integer getPublicationState();

}
