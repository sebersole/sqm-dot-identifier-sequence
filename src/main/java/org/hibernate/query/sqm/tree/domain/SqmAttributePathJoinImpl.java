/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import java.util.Locale;

import org.hibernate.model.AttributeDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.tree.SqmJoinType;

/**
 * @author Steve Ebersole
 */
public class SqmAttributePathJoinImpl extends AbstractSqmQualifiedPathJoin implements SqmAttributePathJoin {

	// todo (6.0) : upstream this would be split into numerous sub-types
	//		such as `SqmSingularAttributeJoin`, `SqmListJoin`, etc.
	//		but for these simple testing purposes, this one is enough

	private final boolean fetched;

	public SqmAttributePathJoinImpl(
			AttributeDescriptor navigable,
			NavigablePath navigablePath,
			SqmJoinType joinType,
			boolean fetched) {
		this( navigable, navigablePath, joinType, null, fetched );
	}

	public SqmAttributePathJoinImpl(
			AttributeDescriptor navigable,
			NavigablePath navigablePath,
			SqmJoinType joinType,
			String alias,
			boolean fetched) {
		super( navigable, navigablePath, joinType, alias );
		this.fetched = fetched;
	}

	@Override
	public AttributeDescriptor getReferencedNavigable() {
		return (AttributeDescriptor) super.getReferencedNavigable();
	}

	@Override
	public boolean isFetched() {
		return fetched;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"SqmAttributePathJoin(%s join %s)",
				getJoinType().getText(),
				getNavigablePath().getFullPath()
		);
	}
}
