/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *
 * @author chris
 */
public class SortingFilter extends AbstractSubsetFilter {

    public static final int HIERARCHICAL_MODE_DISABLED = 0;
    public static final int HIERARCHICAL_MODE_SHOW_CHILDREN = 1;
    public static final int HIERARCHICAL_MODE_HIDE_CHILDREN = 2;

    public static final int ORDER_MODE_REVERSE_DISABLED = 0;
    public static final int ORDER_MODE_REVERSE_TOTAL = 1;
    public static final int ORDER_MODE_REVERSE_PER_LEVEL = 2;
    public static final int ORDER_MODE_REVERSE_HIERARCHY = 3;

    
    private int whole = -1;
    private int sortingCriteria = -1;
    private int typeLimitation = -1;
    private int reverse = -1;
    private int sortAttribute = -1;

    private int sortLevel = -1;
    private boolean showDuplicates;

    public SortingFilter(Subset2 subset, Element filterDefinition) {
        bind(subset);
        setDefinition(filterDefinition);
        initialize();
    }

    public SortingFilter(Subset2 subset) {
        bind(subset);
    }

   @Override
    public Element serializeAsXML() {
        Namespace ns = Namespace.getNamespace(Subset2.namespace);
        Element definition = new Element("sorting_filter",ns);
        if (whole != -1) {
            Element wholeElem = new Element("whole",ns);
            definition.addContent(wholeElem);
            wholeElem.addContent(createTextElement("value", whole, ns));
        }
        if (sortingCriteria != -1) {
            Element sortingElem = new Element("sorting_criteria",ns);
            definition.addContent(sortingElem);
            sortingElem.addContent(createTextElement("value", sortingCriteria, ns));
        }
        if (typeLimitation != -1) {
            Element typeLimitationElem = new Element("type_limitation",ns);
            definition.addContent(typeLimitationElem);
            typeLimitationElem.addContent(createTextElement("value", typeLimitation, ns));
        }
        if (sortAttribute != -1) {
            Element sortAttributeElem = new Element("attribute",ns);
            definition.addContent(sortAttributeElem);
            sortAttributeElem.addContent(createTextElement("value", sortAttribute, ns));
        }
        if (sortLevel != -1) {
            Element sortLevelElem = new Element("level",ns);
            definition.addContent(sortLevelElem);
            sortLevelElem.addContent(createTextElement("value", sortLevel, ns));
        }
        Element reverseElem = new Element("reverse",ns);
        Element showDupElem = new Element("show_duplicates",ns);
        definition.addContent(reverseElem);
        definition.addContent(showDupElem);
        reverseElem.addContent(createTextElement("value", reverse, ns));
        showDupElem.addContent(createTextElement("value", bool2int(showDuplicates), ns));
        setDefinition(definition);
        return definition;
    }

    @Override
    public int getType() {
        return TYPE_SORTING;
    }

    @Override
    public void initialize() {
        Element definition = getDefinition();
        Namespace ns = Namespace.getNamespace(Subset2.namespace);
        Element wholeElem = definition.getChild("whole",ns);
        Element sortingElem = definition.getChild("sorting_criteria",ns);
        Element typeLimitationElem = definition.getChild("type_limitation",ns);
        Element reverseElem = definition.getChild("reverse",ns);
        Element showDupElem = definition.getChild("show_duplicates",ns);
        Element sortAttributeElem = definition.getChild("attribute",ns);
        Element sortLevelElem= definition.getChild("level",ns);
        if (wholeElem != null)
            whole = Integer.parseInt(wholeElem.getChildTextNormalize("value",ns));
        if (sortingElem != null)
            sortingCriteria = Integer.parseInt(sortingElem.getChildTextNormalize("value",ns));
        if (typeLimitationElem != null)
            typeLimitation = Integer.parseInt(typeLimitationElem.getChildTextNormalize("value",ns));
        if (reverseElem != null)
            reverse = Integer.parseInt(reverseElem.getChildTextNormalize("value",ns));
        if (showDupElem != null)
            showDuplicates = int2bool(Integer.parseInt(showDupElem.getChildTextNormalize("value",ns)));
        if (sortAttributeElem != null)
            sortAttribute = Integer.parseInt(sortAttributeElem.getChildTextNormalize("value",ns));
        if (sortLevelElem != null)
            sortLevel = Integer.parseInt(sortLevelElem.getChildTextNormalize("value", ns));
    }

    public int getReverse() {
        return reverse;
    }

    public void setReverse(int reverse) {
        this.reverse = reverse;
    }

    public boolean isShowDuplicates() {
        return showDuplicates;
    }

    public void setShowDuplicates(boolean showDuplicates) {
        this.showDuplicates = showDuplicates;
    }

    public int getSortingCriteria() {
        return sortingCriteria;
    }

    public void setSortingCriteria(int sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
    }

    public int getTypeLimitation() {
        return typeLimitation;
    }

    public void setTypeLimitation(int typeLimitation) {
        this.typeLimitation = typeLimitation;
    }

    public int getWhole() {
        return whole;
    }

    public void setWhole(int whole) {
        this.whole = whole;
    }

     public int getSortAttribute() {
        return sortAttribute;
    }

    public void setSortAttribute(int sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public int getSortLevel() {
        return sortLevel;
    }

    public void setSortLevel(int sortLevel) {
        this.sortLevel = sortLevel;
    }

}
