package com.proclos.colibriweb.entity.component;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(ComponentTypes.TRANSFORM_Value)
public class Transform extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5679796492291645532L;
	
	private List<Function> functions = new ArrayList<Function>();

	//@OneToMany(mappedBy="transform", fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH,CascadeType.REMOVE}, orphanRemoval=true)
	@Transient
	public List<Function> getFunctions() {
		return functions;
	}

	public void setFunctions(List<Function> functions) {
		this.functions = functions;
	}

}
