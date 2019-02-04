/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.domain;

import org.hibernate.query.seqpoc.sqm.tree.predicate.SqmPredicate;

/**
 * Common contract for qualified/restricted/predicated joins.
 *
 * @author Steve Ebersole
 */
public interface SqmQualifiedPathJoin extends SqmPathJoin {
	/**
	 * Obtain the join predicate
	 */
	SqmPredicate getJoinPredicate();

	/**
	 * Inject the join predicate
	 */
	void setJoinPredicate(SqmPredicate predicate);

}
