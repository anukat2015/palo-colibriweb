/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

import org.jdom2.Element;
import java.util.List;
import java.util.ArrayList;
import org.jdom2.Namespace;

/**
 *
 * @author chris
 */
public class PicklistFilter extends AbstractSubsetFilter {

    private List<Integer> picklist = new ArrayList<Integer>();
    private int picktype;

    public PicklistFilter(Subset2 subset, Element filterDefinition) {
        bind(subset);
        setDefinition(filterDefinition);
        initialize();
    }

    @Override
    public Element serializeAsXML() {
        Element definition = new Element("picklist_filter");
        Element manual = new Element("manual_definition");
        Element value = new Element("value");
        definition.addContent(manual);
        manual.addContent(value);
        for (int i : picklist) {
            Element elem = new Element("pick_elem");
            elem.setText(String.valueOf(i));
            value.addContent(elem);
        }
        Element type = new Element("pick_type");
        Element typeValue = new Element("value");
        typeValue.setText(String.valueOf(picktype));
        definition.addContent(type);
        type.addContent(typeValue);
        setDefinition(definition);
        return definition;
    }

    @Override
    public int getType() {
        return TYPE_PICKLIST;
    }

    @Override
    public void initialize() {
        Element definition = getDefinition();
        Namespace ns = Namespace.getNamespace(Subset2.namespace);
        Element manual = definition.getChild("manual_definition",ns);
        Element value = manual.getChild("value",ns);
        List<Element> elems = value.getChildren("pick_elem",ns);
        picklist.clear();
        for (Element e : elems) {
            picklist.add(Integer.parseInt(e.getTextNormalize()));
        }
        Element type = definition.getChild("pick_type",ns);
        picktype = Integer.parseInt(type.getChildTextNormalize("value",ns));
    }

    public List<Integer> getPicklist() {
        return picklist;
    }

}
