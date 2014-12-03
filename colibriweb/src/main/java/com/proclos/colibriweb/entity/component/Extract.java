package com.proclos.colibriweb.entity.component;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ComponentTypes.EXTRACT_Value)
public class Extract extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5981880245795290995L;

}
