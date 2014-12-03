package at.adaptive.components.session;

import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionGroup;
import at.adaptive.components.restriction.RestrictionType;

public interface IResultSearchController<T>
{
	/**
	 * Cycles the order of the specified restriction
	 * 
	 * @param name
	 *            the name of the restriction
	 */
	void cycleRestrictionOrder(String name);

	/**
	 * Filters the result list by the specified restrictions
	 */
	void filter();

	/**
	 * Returns the order value of the specified restriction
	 * 
	 * @param restriction
	 *            the restriction
	 * @return the order vlaue of the specified restriction
	 */
	String getOrderValue(String restrictionName);

	/**
	 * Returns a {@link Restriction} for a specified name
	 * 
	 * @param name
	 *            the name of the restriction (search field)
	 * @return the restriction for the specified name or <code>null</code> if the restriction could not be found
	 */
	Restriction<?> getRestriction(String name);

	/**
	 * Returns a {@link RestrictionGroup} for a specified name
	 * 
	 * @param name
	 *            the name of the restriction group
	 * @return the restriction group for the specified name or <code>null</code> if the restriction group could not be found
	 */
	RestrictionGroup getRestrictionGroup(String name);

	/**
	 * Returns a list of supported restriction types
	 * 
	 * @return a list of supported restriction types
	 */
	RestrictionType[] getRestrictionTypes();

	/**
	 * Returns whether a restriction is set
	 * 
	 * @return <code>true</code> if a restriction is set; <code>false</code> otherwise
	 */
	boolean isRestrictionSet();

	void resetRestrictionGroups();

	/**
	 * Resets all restrictions (values and ordering)
	 */
	void resetRestrictions();

	/**
	 * Sets an ascending order on the specified restriction
	 * 
	 * @param name
	 *            the name of the restriction
	 */
	void setRestrictionAscOrder(String name);

	/**
	 * Sets a descending order on the specified restriction
	 * 
	 * @param name
	 *            the name of the restriction
	 */
	void setRestrictionDescOrder(String name);
}