/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.hql.spi;

import org.hibernate.query.sqm.tree.domain.SqmFrom;

/**
 * Used to resolve a path root as we process a path (dot-identifier sequence) in the
 * parse tree.
 *
 * @author Steve Ebersole
 */
public interface PathRootLocator {
	/**
	 * Locate a path source by its alias (identification variable). Will
	 * search any parent contexts
	 *
	 * @param alias The alias by which to locate the source
	 *
	 * @return matching source, or {@code null}
	 */
	SqmFrom findPathRootByAlias(String alias);

	/**
	 * Locate a path source which exposes a Navigable of the given name.  Will
	 * search any parent contexts.  This form is considered an exception if
	 * multiple sources expose a Navigable with the given name
	 *
	 * @param navigableName The name of the Navigable
	 *
	 * @return matching source, or {@code null}
	 */
	SqmFrom findPathRootByExposedNavigable(String navigableName);
}
