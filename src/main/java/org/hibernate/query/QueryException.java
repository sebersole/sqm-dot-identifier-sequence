/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query;

import org.hibernate.HibernateException;

/**
 * Base for all HQL parser specific exceptions.
 *
 * @author Steve Ebersole
 */
public class QueryException extends HibernateException {
	public QueryException(String message) {
		super( message );
	}

	public QueryException(String message, Exception cause) {
		super( message, cause );
	}
}
