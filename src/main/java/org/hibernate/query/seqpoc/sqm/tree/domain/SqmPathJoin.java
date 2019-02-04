/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.domain;

import org.hibernate.query.seqpoc.sqm.tree.SqmJoinType;
import org.hibernate.query.seqpoc.sqm.tree.predicate.SqmPredicate;

/**
 * @author Steve Ebersole
 */
public interface SqmPathJoin extends SqmFrom {
	SqmJoinType getJoinType();

	SqmPredicate getJoinPredicate();

	boolean isFetched();
}
