package com.proclos.colibriweb.session.system;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.proclos.colibriweb.common.IAccessControlled;
import com.proclos.colibriweb.entity.user.ColibriRole;
import com.proclos.colibriweb.entity.user.ColibriUser;

import at.adaptive.components.hibernate.CriteriaWrapper;

public class AccessUtil {
	
	public static final int QUARANTINED=0;
	public static final int PRIVATE=1;
	public static final int USER=2;
	public static final int PUBLIC = 3;
	
	private static Criterion getInternalCriterion(ColibriUser user) {
		return Restrictions.disjunction().add(Restrictions.ge("publicationState", USER)).add(Restrictions.eq("creator", user));
	}
	
	public static void appendAccessCriterion(ColibriUser user, CriteriaWrapper wrapper) {
		if (!user.hasRole(ColibriRole.ADMIN)) {
			wrapper.addCriterion(getInternalCriterion(user));
		}
	}
	
	public static void appendAccessCriterion(String associationPath, ColibriUser user, CriteriaWrapper wrapper) {
		if (!user.hasRole(ColibriRole.ADMIN)) {
			wrapper.addCriterion(associationPath,getInternalCriterion(user));
		}
	}
	
	public static boolean hasAccess(ColibriUser user, IAccessControlled entity) {
		if (user == null) return entity.getPublicationState() == PUBLIC;
		return (user.hasRole(ColibriRole.ADMIN) || entity.getCreator() == null || entity.getCreator().equals(user) || entity.getPublicationState() >= USER);
	}
	
	public static boolean maySetPrivate(ColibriUser user, IAccessControlled entity) {
		return (user.hasRole(ColibriRole.ADMIN) || entity.getCreator() == null || entity.getCreator().equals(user));
	}

}
