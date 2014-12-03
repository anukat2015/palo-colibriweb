package at.adaptive.components.session;

import java.util.List;

import org.hibernate.ScrollableResults;

/**
 * Control for results
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 */
public interface IResultController<T>
{
	/**
	 * Returns all results for the handled entity type.<br>
	 * Use this method with caution, as all results from the database for the handled type are returned.<br>
	 * In addition, the dirty-flag of this result controller has no effect - this means that every call to this method hits the database! <br>
	 * To avoid massive querying, you should handle the returned list and its dirty state by yourself!
	 * 
	 * @return a list of all results for the handled entity type
	 */
	List<T> getAllResults();

	/**
	 * Returns the first result (index)
	 * 
	 * @return the first result (index)
	 */
	Integer getFirstResult();

	/**
	 * Returns the maximum number of results to be returned.
	 * <p>
	 * Subclasses may overwrite
	 * 
	 * @return the maximum number of results to be returned
	 */
	Integer getMaxResults();

	/**
	 * Returns the number of results found
	 * 
	 * @return the number of results found
	 */
	Integer getResultCount();

	/**
	 * Returns a list of results
	 * 
	 * @return a list of results
	 */
	List<T> getResults();

	/**
	 * Reloads the results
	 */
	void reloadResults();

	/**
	 * Returns a {@link ScrollableResults} object from the created criteria
	 * 
	 * @return a {@link ScrollableResults} object from the created criteria
	 */
	ScrollableResults scrollResults();

	/**
	 * Sets the dirty state on this result controller
	 * 
	 * @param dirty
	 *            the dirty state
	 */
	void setDirty(boolean dirty);
}