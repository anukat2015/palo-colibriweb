package com.proclos.colibriweb.entity.component;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue(ComponentTypes.CONNECTION_Value)
public class Connection extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6240532211474824651L;

}
