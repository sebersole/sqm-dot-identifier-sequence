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
public class AttributeDescriptor extends AbstractAttributeContainer {
	private final AttributeContainer container;
	private final NavigableRole role;

	@SafeVarargs
	public AttributeDescriptor(AttributeContainer container, String name, Function<AttributeContainer,AttributeDescriptor>... attributeCreators) {
		this.container = container;
		this.role = container.getNavigableRole().append( name );

		afterInit( attributeCreators );
	}

	public AttributeContainer getContainer() {
		return container;
	}

	@Override
	public NavigableRole getNavigableRole() {
		return role;
	}

	@Override
	public String toString() {
		return "Attribute[" + getNavigableRole().getFullPath() + "]";
	}
}
