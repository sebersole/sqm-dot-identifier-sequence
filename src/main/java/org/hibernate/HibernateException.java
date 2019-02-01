/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate;

/**
 * @author Steve Ebersole
 */
public class HibernateException extends RuntimeException {
	public HibernateException(String message) {
		super( message );
	}

	public HibernateException(String message, Throwable cause) {
		super( message, cause );
	}
}
