package com.proclos.colibriweb.entity.component;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ComponentTypes.LOAD_Value)
public class Load extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2962833175559555203L;

}
