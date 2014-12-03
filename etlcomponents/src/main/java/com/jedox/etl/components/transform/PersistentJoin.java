package com.jedox.etl.components.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jedox.etl.components.config.transform.TableJoinConfigurator.Conditions;
import com.jedox.etl.components.config.transform.TableJoinConfigurator.JoinDefinition;
import com.jedox.etl.components.config.transform.TableJoinConfigurator.Match;
import com.jedox.etl.core.component.IComponent;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.connection.IRelationalConnection;
import com.jedox.etl.core.node.IColumn;
import com.jedox.etl.core.node.Row;
import com.jedox.etl.core.persistence.mem.MemPersistor;
import com.jedox.etl.core.source.ISource;
import com.jedox.etl.core.source.ISource.CacheTypes;
import com.jedox.etl.core.source.SourceManager;
import com.jedox.etl.core.source.processor.IProcessor;
import com.jedox.etl.core.source.processor.Processor;
import com.jedox.etl.core.util.SQLUtil;

public class PersistentJoin extends Processor {
	
	private JoinDefinition joinDefinition; 
	private SourceManager sourceManager;
	private String escapeCharacter;
	private IComponent owner;
	private IProcessor persistenceProcessor;
	private int size;
	private IRelationalConnection internalConnection;
	private Row leftRow;
	private IProcessor rightProcessor;
	private long rightProcessingTime = 0;
	private CacheTypes cacheType;
	private static final Log log = LogFactory.getLog(PersistentJoin.class);
	
	public PersistentJoin(IComponent owner, JoinDefinition joinDefinition, SourceManager sourceManager, IRelationalConnection internalConnection, CacheTypes cacheType, int size) throws RuntimeException {
		this.owner = owner;
		this.joinDefinition = joinDefinition;
		this.sourceManager = sourceManager;
		this.escapeCharacter = internalConnection.getIdentifierQuote();
		this.internalConnection = internalConnection;
		this.cacheType = cacheType;
		this.size = size;
		ISource left = sourceManager.get(joinDefinition.getLeftSource());
		IProcessor sourceProcessor = getSourceProcessor(left);
		setSourceProcessor(sourceProcessor);
		leftRow = setupSourceProcessor(sourceProcessor);
	}
	
	private IProcessor getSourceProcessor(ISource datasource) throws RuntimeException {
		datasource.setCaching(cacheType);
		return owner.initProcessor(datasource.getProcessor(0),Facets.INPUT);
	}

	private Row setupSourceProcessor(IProcessor processor) throws RuntimeException {
		Row current = processor.current();
		return current;
	}

	private String resolveKey(String sourcename, String keyname, boolean constant, JoinDefinition joinDefinition) {
		if (!constant) {
			return getAlias(sourcename,joinDefinition)+"."+SQLUtil.quoteName(keyname,escapeCharacter);
		} else {
			return "'"+keyname+"'";
		}
	}
	
	private String getCast(String name, boolean constant) {
		return (!constant) ? "TRIM(CAST(CAST("+name+ " AS CHAR(100)) AS VARCHAR(100))) " : name;
	}
	
	private String getJoinConditionExpression(JoinDefinition def) {
		StringBuffer result = new StringBuffer();
		int keys = 0;
		for (Match m: def.getMatches()) {
			if (keys > 0) result.append(" AND ");
			if (m.getCondition().equals(Conditions.EQ) || m.getCondition().equals(Conditions.NE)) {
				result.append(getCast(resolveKey(m.getLeftSource(), m.getLeftKey().getName(), m.getLeftKey().isConstant(),def),m.getLeftKey().isConstant()) +m.getConditionOperator());
				result.append(getCast(resolveKey(m.getRightSource(), m.getRightKey().getName(), m.getRightKey().isConstant(),def),m.getRightKey().isConstant()));
			} else {
				result.append(resolveKey(m.getLeftSource(), m.getLeftKey().getName(), m.getLeftKey().isConstant(),def) +" "+m.getConditionOperator());
				result.append(resolveKey(m.getRightSource(), m.getRightKey().getName(), m.getRightKey().isConstant(),def));
			}
			keys++;
		}
		return result.toString();
	}

	private String getAlias(String name, JoinDefinition joinDefinition) {
		String side;
		if (name.equals(joinDefinition.getLeftSource()))
			side = "l";
		else
			side = "r";
		return "s"+side;
	}

	private String getJoinExpression(JoinDefinition joinDefinition) {
		StringBuffer result = new StringBuffer();
		//append initial source
		ISource baseSource = sourceManager.get(joinDefinition.getLeftSource());
		result.append(baseSource.getPersistentView()+" "+getAlias(joinDefinition.getLeftSource(),joinDefinition));
		result.append(" "+joinDefinition.getType()+" join ");
		result.append(sourceManager.get(joinDefinition.getRightSource()).getLocator().getPersistentName()+" "+getAlias(joinDefinition.getRightSource(),joinDefinition));
		if (joinDefinition.getMatches().size() > 0) {
			result.append(" on ");
			result.append(getJoinConditionExpression(joinDefinition));
		}
		return result.toString();
	}

	private String getAliasForOutput(HashMap<String, Row> lookup, String name, JoinDefinition joinDefinition) {
		if (joinDefinition.getType().contains("inner") || joinDefinition.getType().contains("left outer")) {
			if (lookup.get(joinDefinition.getLeftSource()).containsColumn(name))
				return getAlias(joinDefinition.getLeftSource(),joinDefinition);
			else 
				return getAlias(joinDefinition.getRightSource(),joinDefinition);
		}
		else {//right outer join
			if (lookup.get(joinDefinition.getRightSource()).containsColumn(name))
				return getAlias(joinDefinition.getRightSource(),joinDefinition);
			else 
				return getAlias(joinDefinition.getLeftSource(),joinDefinition);
		}
	}
	
	private boolean isIdenticalMatch(String name, JoinDefinition joinDefinition) {
		for (Match m : joinDefinition.getMatches()) {
			if (name.equalsIgnoreCase(m.getLeftKey().getName()) && name.equalsIgnoreCase(m.getRightKey().getName()) && !m.getLeftKey().isConstant() && !m.getRightKey().isConstant()) {
				return true;
			}
		}
		return false;
	}
	

	@Override
	protected boolean fillRow(Row row) throws Exception {
		return (persistenceProcessor.next() != null);
	}

	@Override
	protected Row getRow() throws RuntimeException {
		return persistenceProcessor.current();
	}
	
	protected String getJoinQuery(JoinDefinition joinDefinition) throws RuntimeException {
		HashMap<String, Row> lookup = new HashMap<String, Row>();
		Row columns = new Row();
		ISource left = sourceManager.get(joinDefinition.getLeftSource());
		lookup.put(left.getName(),leftRow);
		columns.addColumns(leftRow);
		ISource right = sourceManager.get(joinDefinition.getRightSource());
		if (rightProcessor == null) { //avoid multiple intialization on full join
			rightProcessor = getSourceProcessor(right);
		}
		Row rightRow = setupSourceProcessor(rightProcessor);
		lookup.put(right.getName(),rightRow);
		for (IColumn c : rightRow.getColumns()) {
			//removal is implicit, because duplicate columns are not added below
			if (columns.containsColumn(c.getName()) && !isIdenticalMatch(c.getName(),joinDefinition)) 
				log.warn("Removing column "+c.getName()+" of source "+right.getName()+" from joint result, because source "+left.getName()+" already contains column with identical name.");
		}
		columns.addColumns(rightRow);
		List<String> fields = new ArrayList<String>();
		for (IColumn c : columns.getColumns()) {
			String prefix = getAliasForOutput(lookup,c.getName(),joinDefinition);
			fields.add(SQLUtil.prefixName(SQLUtil.quoteName(c.getName(),escapeCharacter),prefix));
		}
		String query = SQLUtil.buildQuery(getJoinExpression(joinDefinition),SQLUtil.enumNames(fields),"","","");
		log.debug("join query is: "+query);
		return query;
	}
	
	protected IProcessor getProcessor(String query) throws RuntimeException {
		log.debug("join query is: "+query);
		IProcessor result = (cacheType.equals(CacheTypes.disk) ? internalConnection.getProcessor(query, false, size) : owner.initProcessor(MemPersistor.getInstance().getQueryResult(null, query, size),Facets.CONNECTION));
		rightProcessingTime = rightProcessor.getOverallProcessingTime();
		return result;
	}

	@Override
	protected void init() throws RuntimeException {
		if (!joinDefinition.getType().equalsIgnoreCase("full")) {
			persistenceProcessor = getProcessor(getJoinQuery(joinDefinition));
		} else {
			JoinDefinition leftOuter = joinDefinition.clone(); 
			leftOuter.setType("left outer");
			JoinDefinition rightOuter = joinDefinition.clone(); 
			rightOuter.setType("right outer");
			StringBuffer query = new StringBuffer();
			query.append("select * from (");
			query.append(getJoinQuery(leftOuter));
			query.append(" UNION ");
			query.append(getJoinQuery(rightOuter));
			query.append(")");
			persistenceProcessor = getProcessor(query.toString());
		}
	}
	
	public long getOwnProcessingTime() {
		return super.getOwnProcessingTime() - rightProcessingTime;
	}
	
}
