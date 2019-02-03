/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.model.AttributeContainer;
import org.hibernate.model.EntityDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.QueryLogger;

/**
 * @author Steve Ebersole
 */
public class SqmPathRoot implements SqmFrom {
	private final EntityDescriptor entityDescriptor;
	private final NavigablePath navigablePath;

	private String explicitAlias;

	public SqmPathRoot(EntityDescriptor entityDescriptor) {
		this( entityDescriptor, null );
	}

	public SqmPathRoot(EntityDescriptor entityDescriptor, String explicitAlias) {
		this.entityDescriptor = entityDescriptor;

		this.navigablePath = new NavigablePath( entityDescriptor.getNavigableName() );

		setExplicitAlias( explicitAlias );
	}

	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

	@Override
	public AttributeContainer getReferencedNavigable() {
		return getEntityDescriptor();
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
	public NavigablePath getNavigablePath() {
		return navigablePath;
	}
}
