/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.domain;

import org.hibernate.metamodel.model.domain.spi.NavigableContainer;

/**
 * @author Steve Ebersole
 */
public interface SqmFrom extends SqmPath {
	@Override
	NavigableContainer getReferencedNavigable();
}
