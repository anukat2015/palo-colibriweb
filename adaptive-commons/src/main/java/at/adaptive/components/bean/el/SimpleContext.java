package at.adaptive.components.bean.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.jboss.el.ValueExpressionLiteral;

/**
 * Simple ELContext extension
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class SimpleContext extends ELContext
{
	private final BeanELResolver resolver = new BeanELResolver();
	private final FunctionMapper functionMapper = new ExportFunctionMapper();
	private final VariableMapper variableMapper = new ExportVariableMapper();
	private final Map<String, ValueExpression> variables = new HashMap<String, ValueExpression>();

	@Override
	public ELResolver getELResolver()
	{
		return resolver;
	}

	@Override
	public FunctionMapper getFunctionMapper()
	{
		return functionMapper;
	}

	@Override
	public VariableMapper getVariableMapper()
	{
		return variableMapper;
	}

	public void bind(String variable, Object obj)
	{
		variables.put(variable, new ValueExpressionLiteral(obj, Object.class));
	}

	private class ExportVariableMapper extends VariableMapper
	{
		public ValueExpression resolveVariable(String s)
		{
			return variables.get(s);
		}

		public ValueExpression setVariable(String s, ValueExpression valueExpression)
		{
			return (variables.put(s, valueExpression));
		}
	}

	private class ExportFunctionMapper extends FunctionMapper
	{
		public Method resolveFunction(String s, String s1)
		{
			return null;
		}
	}
}