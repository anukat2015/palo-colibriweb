package at.adaptive.components.session;

/**
 * Control for entities
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 */
interface IEntityController<T>
{
	void create();

	/**
	 * Creates a new instance
	 */
	String createNewInstance();

	/**
	 * Attempts to delete an entity for a given id
	 * 
	 * @param id
	 *            the id
	 */
	void delete(Object id);

	T find();

	Object getId();

	T getInstance();

	boolean isIdDefined();

	boolean isManaged();

	String persist();

	String remove();

	/**
	 * Selects an entity for a specified id
	 * 
	 * @param id
	 *            the id
	 * @return the outcome of the operation
	 */
	String select(Object id);

	/**
	 * Sets the entity id-value for the specified id
	 * 
	 * @param id
	 *            the id to set
	 */
	void setEntityId(String id);

	void setId(Object id);

	void setInstance(T instance);

	String update();
}