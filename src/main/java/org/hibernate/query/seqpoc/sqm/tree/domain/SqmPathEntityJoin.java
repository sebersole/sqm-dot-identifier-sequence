/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.domain;

import org.hibernate.Interceptor;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.seqpoc.sqm.tree.SqmJoinType;
import org.hibernate.query.seqpoc.sqm.tree.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public class SqmPathEntityJoin extends AbstractSqmQualifiedPathJoin {
	public SqmPathEntityJoin(EntityTypeDescriptor entityDescriptor, SqmJoinType joinType) {
		this( entityDescriptor, joinType, null );
	}

	public SqmPathEntityJoin(
			EntityTypeDescriptor entityDescriptor,
			SqmJoinType joinType,
			String explicitAlias) {
		super(
				entityDescriptor,
				new NavigablePath( entityDescriptor.getNavigableRole().getFullPath() ),
				joinType,
				explicitAlias
		);
	}

	public EntityTypeDescriptor getJoinedEntityDescriptor() {
		return getReferencedNavigable();
	}

	@Override
	public SqmJoinType getJoinType() {
		return SqmJoinType.CROSS;
	}

	@Override
	public SqmPredicate getJoinPredicate() {
		return null;
	}

	@Override
	public boolean isFetched() {
		return false;
	}

	@Override
	public EntityTypeDescriptor getReferencedNavigable() {
		return (EntityTypeDescriptor) super.getReferencedNavigable();
	}
}
