/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public interface QueryLogger extends BasicLogger {
	String LOGGER_NAME = "org.hibernate.orm.query";

	Logger QUERY_LOGGER = Logger.getLogger( LOGGER_NAME );

	boolean DEBUG_ENABLED = QUERY_LOGGER.isDebugEnabled();
}
