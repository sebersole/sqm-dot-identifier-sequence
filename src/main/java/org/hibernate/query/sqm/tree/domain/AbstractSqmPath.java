/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.query.NavigablePath;
import org.hibernate.query.QueryLogger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmPath implements SqmPath {
	private final NavigablePath navigablePath;

	// NOTE : non-final to possibly support "criteria is-a SQM" later
	private String explicitAlias;

	protected AbstractSqmPath(NavigablePath navigablePath) {
		this( navigablePath, null );
	}

	protected AbstractSqmPath(NavigablePath navigablePath, String explicitAlias) {
		this.navigablePath = navigablePath;

		setExplicitAlias( explicitAlias );
	}

	@Override
	public NavigablePath getNavigablePath() {
		return navigablePath;
	}

	@Override
	public String getExplicitAlias() {
		return explicitAlias;
	}

	@Override
	public void setExplicitAlias(String explicitAlias) {
		if ( QueryLogger.DEBUG_ENABLED ) {
			QueryLogger.QUERY_LOGGER.debugf(
					"Setting alias for %s(%s) : %s -> %s",
					getClass().getSimpleName(),
					getNavigablePath().getFullPath(),
					this.explicitAlias,
					explicitAlias
			);
		}
		this.explicitAlias = explicitAlias;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + getNavigablePath().getFullPath() + ")";
	}
}
