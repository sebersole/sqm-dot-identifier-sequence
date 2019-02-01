/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.model.EntityDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public class SqmPathCrossJoin implements SqmPathJoin {
	private final EntityDescriptor joinedEntity;
	private final NavigablePath pathDescriptor;

	private String explicitAlias;

	@SuppressWarnings("unused")
	public SqmPathCrossJoin(EntityDescriptor joinedEntity) {
		this( joinedEntity, null );
	}

	public SqmPathCrossJoin(EntityDescriptor joinedEntity, String explicitAlias) {
		this.joinedEntity = joinedEntity;
		this.explicitAlias = explicitAlias;

		this.pathDescriptor = new NavigablePath( joinedEntity.getEntityName() );
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
	public EntityDescriptor getReferencedNavigable() {
		return joinedEntity;
	}

	@Override
	public String getExplicitAlias() {
		return explicitAlias;
	}

	@Override
	public void setExplicitAlias(String explicitAlias) {
		this.explicitAlias = explicitAlias;
	}

	@Override
	public NavigablePath getNavigablePath() {
		return pathDescriptor;
	}
}
