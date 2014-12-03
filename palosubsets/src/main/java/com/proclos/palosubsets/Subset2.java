/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;

/**
 *
 * @author chris
 */
public interface Subset2 {

    /**
	 * Subset type local, i.e. the subset access is restricted to certain users
	 */
	public static int TYPE_LOCAL = 0;
	/**
	 * Subset type global, i.e. this subset can be read and modified by each
	 * user who can read and modify all database cubes
	 **/
	public static int TYPE_GLOBAL = 1;

        public static String namespace = "http://www.jedox.com/palo/SubsetXML";


	/**
	 * Returns the <code>Dimension</code> to which this subset applies.
	 * @return the subset dimension
	 */
	public IDimension getDimension();

	/**
	 * Returns the indent of this subset
	 * @return subset indent
	 */
	public int getIndent();
	/**
	 * Sets the subset indent
	 * @param indent the new subset indent
	 */
	public void setIndent(int indent);

	/**
	 * Resets this subset to its defaults, i.e. all filters and aliases are
	 * removed.
	 */
	public void reset();

	/**
	 * Adds the given subset filter to the list of all subset filters which
	 * should be applied to this subset. Note that this will replace a former
	 * added filter of same type.
	 * @param the subset filter to apply to this subset
	 */
	public void add(SubsetFilter filter);
	/**
	 * Removes the given subset filter from the list of all subset filters which
	 * should be applied to this subset.
	 * @param the subset filter to remove
	 */
	public void remove(SubsetFilter filter);

	/**
	 * Returns all subset filters of this subset
	 * @return an array of applied subset filters
	 */
	public SubsetFilter[] getFilters();

	/**
	 * Returns the subset filter which corresponds to the given type
	 * @param type a valid subset filter type
	 * @return the corresponding <code>ISubsetFilter</code> or <code>null</code>
	 */
	public SubsetFilter getFilter(int type);

	/**
	 * Checks if the subset filter which corresponds to the given type is
	 * active, i.e. {@link #getFilter(int)} returns not <code>null</code>
	 * @param filterType the type of filter to check
	 * @return <code>true</code> if corresponding filter is active,
	 * <code>false</code> otherwise
	 */
	public boolean isActive(int filterType);

	/**
	 * Saves this subset
	 */
	public void save();

	/**
	 * Checks if the given <code>Element</code> is inside this subset or not.
	 * @param element the <code>Element</code> to check
	 * @return <code>true</code> if <code>Element</code> is inside this subset,
	 * <code>false</code> if not.
	 */
	public boolean contains(IElement element);

	/**
	 * Returns all <code>Elements</code> of this subset.
	 * @return all <code>Elements</code> of this subset
	 */
	public IElement[] getElements();

	/**
	 * Returns all root nodes of this subset. To retrieve all defined
	 * <code>ElementNodes</code> the root nodes should be traversed.
	 * @return
	 */
	public IElement[] getRootNodes();

//	/**
//	 * Applies the defined filters to this subset. Call this to reflect the
//	 * actual status of the subset.
//	 */
//	public void applyFilters();
	/**
	 * <p>Marks the subset as being modified. The consequence of this method is
	 * that all registered filters are applied the next time the subset elements
	 * are requested.</p>
	 */
	public void modified();

	/**
	 * Renames this subset.
	 * @param newName the new subset name
	 */
	public void rename(String newName);

	/**
	 * Returns the subset type which is one of the predefined type constants.
	 * @return the subset type
	 */
	public int getType();

        public String getName();

	/**
	 * Creates a deep copy of this subset.
	 * @return the subset copy
	 */
	public Subset2 copy();

	public String getDefinition();

	public Subset2 setDefinition(String definition);

	public boolean validate();

        public IDatabase getDatabase();

        public Long getId();

        public void setId(Long id);

        public String getUsername();

}
