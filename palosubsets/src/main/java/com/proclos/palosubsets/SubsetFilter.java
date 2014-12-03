/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.IDimension;
import org.jdom2.Element;

/**
 *
 * @author chris
 */
public interface SubsetFilter {

    /* supported types */
	/** type constant for textual filter */
	public static final int TYPE_TEXT = 1<<0;
	/** type constant for hierarchical filter */
	public static final int TYPE_HIERARCHICAL = 1<<1;
	/** type constant for picklist filter */
	public static final int TYPE_PICKLIST = 1<<2;
	/** type constant for data filter */
	public static final int TYPE_DATA = 1<<3;
	/** type constant for sorting filter */
	public static final int TYPE_SORTING = 1<<4;
	/** type constant for attribute filter */
	public static final int TYPE_ATTRIBUTE = 1<<5;
	/** type constant for alias filter */
	public static final int TYPE_ALIAS = 1<<6;


	/**
	 * Returns the filter type which is one of the defined type constants.
	 * @return the filter type
	 */
	public int getType();


	/**
	 * Resets this filter, i.e. its internal setting is switched back to its
	 * default. Clears all internal used caches too.
	 */
	void reset();

	/**
	 * Initializes this filter.
	 */
	public void initialize();

	/**
	 * Convenient method to access the subset hierarchy.
	 * @return the subset hierarchy
	 */
	public IDimension getDimension();

	/**
	 * Adds the given <code>EffectiveFilter</code> to the list of all affective
	 * filters which affect this subset filter
	 * @param filter a filter which affects this subset filter.
	 */
	public void add(SubsetFilter filter);
	/**
	 * Removes the given <code>EffectiveFilter</code> from the list of all
	 * affective filters which affect this subset filter
	 * @param filter the affective filter to remove
	 */
	public void remove(SubsetFilter filter);
	/**
	 * Adapts this subset filter from the given one. Both filter must be of
	 * same type!
	 * @param from the subset filter to adapt from
	 */
	public void adapt(SubsetFilter from);

	/**
	 * Creates a deep copy of this subset filter
	 * @return a copy of this subset filter
	 */
	public SubsetFilter copy();

	/**
	 * Returns the {@link Subset2} to which this filter belongs or
	 * <code>null</code> if this filter isn't bind to a subset yet.
	 * @return the {@link Subset2} to which this filter belongs or
	 * <code>null</code>
	 */
	public Subset2 getSubset();

	/**
	 * <p>Binds this filter instance to the given {@link Subset2}</p>
	 * <b>NOTE: PLEASE DON'T USE! INTERNAL METHOD </b>
	 * @param subset
	 */
	public void bind(Subset2 subset);
	/**
	 * <p>Releases this filter instance from a previously binded {@link Subset2}</p>
	 * <b>NOTE: PLEASE DON'T USE! INTERNAL METHOD </b>
	 */
	public void unbind();

	/**
	 * Checks if the internal subset settings are valid.
	 * @throws PaloIOException if internal subset settings are not valid.
	 */
	public void validateSettings() throws PaloJException;

        public Element serializeAsXML();

      

}
