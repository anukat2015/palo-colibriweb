package at.adaptive.components.session;

import java.util.List;

/**
 * Control for paginated results
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 */
public interface IPaginationResultController<T>
{
	/**
	 * Returns the current page index
	 * 
	 * @return the current page index
	 */
	int getCurrentIndex();

	/**
	 * Returns the current page
	 * 
	 * @return the current page
	 */
	Integer getCurrentPage();

	/**
	 * Returns a list of pages to display. This method also handles the current page and the page count
	 * 
	 * @return a list of pages to display
	 */
	List<Integer> getDisplayPages();

	/**
	 * Returns a list of pages to display using a defined maximum of pages. This method also handles the current page and the page count
	 * 
	 * @param maxPageListLength
	 *            the max size of the page list
	 * @return a list of pages to display using the defined maximum of pages
	 */
	List<Integer> getCustomDisplayPages(int maxPageListLength);

	/**
	 * Queries for the results on the first page
	 */
	void getFirstPageResult();

	/**
	 * Queries for results on the last page
	 */
	void getLastFirstResult();

	/**
	 * Queries for the results on the next page
	 */
	void getNextFirstResult();

	/**
	 * Returns the number of pages
	 * 
	 * @return the number of pages
	 */
	Integer getPageCount();

	/**
	 * Queries for the results for the specified page
	 * 
	 * @param page
	 *            the page
	 */
	void getPageResults(Integer page);

	/**
	 * Queries for the results on the previous page
	 */
	void getPreviousFirstResult();

	/**
	 * Returns whether a next page exists
	 * 
	 * @return <code>true</code> if a next page exists; <code>false</code> otherwise
	 */
	boolean isNextExists();

	/**
	 * Returns whether a previous page exists
	 * 
	 * @return <code>true</code> if a previous page exists; <code>false</code> otherwise
	 */
	boolean isPreviousExists();

	/**
	 * Returns the size of the managed results for the current page
	 * 
	 * @return the size of the managed results for the current page or <code>null</code> if the results are null
	 */
	Integer getPageResultCount();

}