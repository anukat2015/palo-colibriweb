/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author chris
 */
public class SubsetImpl implements Subset2 {

    private String definition;
    private String name;
    private IDimension dimension;
    private IDatabase database;
    private int type;
    private int indent;
    private Long id;
    private boolean modified = true;
    private String username;
    private Set<IElement> elements = new LinkedHashSet<IElement>();

    private List<SubsetFilter> filters = new ArrayList<SubsetFilter>();

    public SubsetImpl(String name, IDatabase database, String definition, int type, String username) {
        this.name = name;
        this.database = database;
        this.type = type;
        this.username = username;
        setDefinition(definition);
    }

    public SubsetImpl(IDatabase database, IDimension dimension, int type, String username) {
        this.database = database;
        this.dimension = dimension;
        this.type = type;
        setDefinition(null);
    }

    @Override
    public void add(SubsetFilter filter) {
        filters.add(filter);
    }

    @Override
    public boolean contains(IElement element) {
        return elements.contains(element);
    }

    @Override
    public Subset2 copy() {
        return new SubsetImpl(name,database,definition,type,username);
    }

    @Override
    public String getDefinition() {
        return definition;
    }

    @Override
    public IDimension getDimension() {
        return dimension;
    }

    @Override
    public IElement[] getElements() {
        if (modified) {
            save();
            elements.clear();
            IElement[] elems = dimension.getElements(false); //TODO fix me
            elements.addAll(Arrays.asList(elems));
            modified = false;
            return elems;
        }
        return elements.toArray(new IElement[0]);
    }

    @Override
    public SubsetFilter getFilter(int type) {
        for (SubsetFilter f : filters) {
            if (f.getType() == type) return f;
        }
        return null;
    }

    @Override
    public SubsetFilter[] getFilters() {
        return filters.toArray(new SubsetFilter[filters.size()]);
    }

    @Override
    public int getIndent() {
        return indent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IElement[] getRootNodes() {
        Set<IElement> roots = new LinkedHashSet<IElement>();
        for (IElement e : elements) {
            if (e.getParentCount() == 0) roots.add(e);
        }
        return roots.toArray(new IElement[0]);
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean isActive(int filterType) {
        return false;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void modified() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(SubsetFilter filter) {
        filters.remove(filter);
    }

    @Override
    public void rename(String newName) {
        name = newName;
    }

    @Override
    public void reset() {
        try {
            indent = 1;
            filters.clear();
            SortingFilter sort = new SortingFilter(this);
            sort.setWhole(1);
            sort.setSortingCriteria(0);
            sort.setTypeLimitation(0);
            sort.setReverse(0);
            sort.setShowDuplicates(true);
            definition = serializeAsString();
        }
        catch (Exception e) {}; //TODO rethink exception handling
    }

    public String serializeAsString() {
        try {
            Element root = new Element("subset","http://www.jedox.com/palo/SubsetXML");
            if (id != null) root.setAttribute("id", String.valueOf(id));
            root.setAttribute("sourceDimensionId", String.valueOf(dimension.getId()));
            Document doc = new Document(root);
            ProcessingInstruction pi = new ProcessingInstruction("palosubset");
            pi.setPseudoAttribute("version", "1");
            doc.addContent(pi);
            Element indentElem = new Element("indent");
            Element value = new Element("value");
            value.setText(String.valueOf(indent));
            indentElem.addContent(value);
            root.addContent(indentElem);
            for (SubsetFilter f : filters) {
                root.addContent(f.serializeAsXML());
            }
            StringWriter writer = new StringWriter();
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            outputter.output(doc, writer);
            return writer.toString();
        } catch (Exception e) {
            //TODO rethink exception handling
            return null;
        }
    }

    @Override
    public void save() {
       this.definition = serializeAsString();

    }

    @Override
    public Subset2 setDefinition(String definition) {
        if (definition == null) {
            reset();
            return this;
        } else {
            this.definition = definition;
            return parseFromDefinition(definition);
        }
    }

    @Override
    public void setIndent(int indent) {
        this.indent = indent;
    }

    @Override
    public boolean validate() {
        return getElements().length > 0;
    }

    private Subset2 parseFromDefinition(String definition) {
        try {
            Element subset = XMLUtil.stringTojdom(definition);
            int dimensionId = Integer.parseInt(subset.getAttributeValue("sourceDimensionId"));
            this.id = Long.parseLong(subset.getAttributeValue("id"));
            for (IDimension d : database.getDimensions()) {
                if (d.getId() == dimensionId) {
                    dimension = d;
                    break;
                }
            }
            Namespace ns = Namespace.getNamespace(namespace);
            Element indentElement = subset.getChild("indent",ns);
            if (indentElement != null) {
                indent = Integer.parseInt(indentElement.getChildTextNormalize("value",ns));
            }
            Element picklistFilter = subset.getChild("picklist_filter",ns);
            if (picklistFilter != null) {
                new PicklistFilter(this,picklistFilter);
            }
            Element sortingFilter = subset.getChild("sorting_filter",ns);
            if (sortingFilter != null) {
                new SortingFilter(this,sortingFilter);
            }
            return this;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public IDatabase getDatabase() {
        return database;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
