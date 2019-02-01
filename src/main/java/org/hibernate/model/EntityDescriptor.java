/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.model;

import java.util.function.Function;

import org.hibernate.metamodel.model.domain.NavigableRole;

/**
 * @author Steve Ebersole
 */
public class EntityDescriptor extends AbstractAttributeContainer {
	private final NavigableRole role;

	@SafeVarargs
	public EntityDescriptor(String name, Function<AttributeContainer,AttributeDescriptor>... attributeCreators) {
		this.role = new NavigableRole( name );

		afterInit( attributeCreators );
	}

	@Override
	public NavigableRole getNavigableRole() {
		return role;
	}

	public String getEntityName() {
		return role.getFullPath();
	}

	@Override
	public String toString() {
		return "Entity[" + getEntityName() + "]";
	}
}
