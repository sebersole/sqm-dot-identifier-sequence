/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAttributeContainer implements AttributeContainer {
	private List<AttributeDescriptor> attributes;

	protected void afterInit(Function<AttributeContainer,AttributeDescriptor>... attributeCreators) {
		this.attributes = new ArrayList<>();
		for ( Function<AttributeContainer, AttributeDescriptor> creator : attributeCreators ) {
			attributes.add( creator.apply( this ) );
		}
	}

	@Override
	public AttributeDescriptor findAttribute(String name) {
		for ( AttributeDescriptor attribute : attributes ) {
			if ( attribute.getNavigableRole().getLocalName().equals( name ) ) {
				return attribute;
			}
		}

		return null;
	}
}
