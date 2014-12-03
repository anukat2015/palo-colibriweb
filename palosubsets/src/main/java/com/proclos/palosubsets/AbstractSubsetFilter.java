/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.IDimension;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *
 * @author chris
 */
public abstract class AbstractSubsetFilter implements SubsetFilter {

    private Subset2 subset;
    private Element definition;

    @Override
    public void adapt(SubsetFilter from) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(SubsetFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void bind(Subset2 subset) {
        this.subset = subset;
        subset.add(this);
    }

    @Override
    public SubsetFilter copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IDimension getDimension() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Subset2 getSubset() {
        return subset;
    }

    @Override
    public int getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(SubsetFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unbind() {
        if (subset != null) subset.remove(this);
    }

    @Override
    public void validateSettings() throws PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void setSubset(Subset2 subset) {
        this.subset = subset;
    }

    protected void setDefinition(Element definition) {
        this.definition = definition;
    }

    protected Element getDefinition() {
        return definition;
    }

    protected int bool2int(boolean value) {
        return value ? 1 : 0;
    }

    protected boolean int2bool(int value) {
        return value == 0 ? false : true;
    }

    protected Element createTextElement(String name, Object value, Namespace ns) {
        Element result = new Element(name,ns);
        if (value != null) result.setText(value.toString());
        return result;
    }

}
