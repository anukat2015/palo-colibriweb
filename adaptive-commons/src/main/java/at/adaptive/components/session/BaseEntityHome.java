package at.adaptive.components.session;

import org.jboss.seam.faces.Redirect;

/**
 * Base entity home class. Implements both the handling of a single entity as well as the handling of a collection of entities. <br>
 * This includes pagination, sorting, filtering, message handling, etc. <br>
 * This implementation also covers the export of result lists to various formats
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 *            the type of the handled entity
 * @param <E>
 *            the type of the export item (this is usually the same as the entity type)
 */
public abstract class BaseEntityHome<T, E> extends ResultExportController<T, E> implements IBaseEntityHome<T, E>
{
	private static final long serialVersionUID = -2003072284116539239L;

	@Override
	public void create()
	{
		super.create();
		createRestrictions();
		createRestrictionGroups();
		setDefaultRestrictions();
		try
		{
			initializeColumnDefinitionContainer();
			initializeExportColumns();
		}
		catch(Exception e)
		{
			throw new RuntimeException("Error initializing base entity home", e);
		}
	}

	public void redirect(String viewId)
	{
		Redirect.instance().setViewId(viewId);
		Redirect.instance().execute();
	}
}