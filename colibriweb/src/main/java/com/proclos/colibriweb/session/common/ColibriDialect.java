package com.proclos.colibriweb.session.common;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.IntegerType;
import org.hibernate.type.TimestampType;

public class ColibriDialect extends PostgreSQLDialect
{

	public ColibriDialect()
	{
		super();
		registerFunction("first", new StandardSQLFunction("first"));
		registerFunction("last", new StandardSQLFunction("last"));
		registerFunction("dateadd", new SQLFunctionTemplate(new TimestampType(), "?1 + interval '?2 ?3'"));
		registerFunction("datesub", new SQLFunctionTemplate(new TimestampType(), "?1 - interval '?2 ?3'"));
		registerFunction("datediff", new SQLFunctionTemplate(new IntegerType(), "date(?1) - date(?2)"));
	}

}
