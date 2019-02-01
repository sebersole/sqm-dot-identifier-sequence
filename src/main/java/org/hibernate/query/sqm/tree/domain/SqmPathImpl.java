/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.query.NavigablePath;

/**
 * @author Steve Ebersole
 */
public class SqmPathImpl extends AbstractNonRootSqmPath {

	// todo (6.0) : like `SqmAttributePathJoinImpl` we may want to split this upstream into a number of sub-types
	//		but for these simple testing purposes, this is enough

	public SqmPathImpl(Navigable navigable, NavigablePath navigablePath) {
		super( navigable, navigablePath );
	}
}
