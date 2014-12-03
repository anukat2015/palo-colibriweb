package com.jedox.etl.core.connection;

import javax.persistence.EntityManager;

import com.jedox.etl.core.component.RuntimeException;

public interface IEntityManagerConnection extends IConnection {
	
	public EntityManager open() throws RuntimeException;

}
