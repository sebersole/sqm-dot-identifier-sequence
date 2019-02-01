/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.model.AttributeContainer;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSqmQualifiedPathJoin extends AbstractSqmPathJoin implements SqmQualifiedPathJoin {
	private SqmPredicate joinPredicate;

	@SuppressWarnings("WeakerAccess")
	protected AbstractSqmQualifiedPathJoin(
			AttributeContainer navigable,
			NavigablePath navigablePath,
			SqmJoinType joinType) {
		this( navigable, navigablePath, joinType, null );
	}

	@SuppressWarnings("WeakerAccess")
	protected AbstractSqmQualifiedPathJoin(
			AttributeContainer navigable,
			NavigablePath navigablePath,
			SqmJoinType joinType,
			String explicitAlias) {
		super( navigable, navigablePath, joinType, explicitAlias );
	}

	@Override
	public AttributeContainer getReferencedNavigable() {
		return (AttributeContainer) super.getReferencedNavigable();
	}

	@Override
	public SqmPredicate getJoinPredicate() {
		return joinPredicate;
	}

	@Override
	public void setJoinPredicate(SqmPredicate joinPredicate) {
		this.joinPredicate = joinPredicate;
	}


}
