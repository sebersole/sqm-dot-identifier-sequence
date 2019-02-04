/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.predicate;

import org.hibernate.query.spi.ComparisonOperator;
import org.hibernate.query.seqpoc.sqm.tree.expression.SqmExpression;

/**
 * @author Steve Ebersole
 */
public class SqmComparisonPredicate implements SqmPredicate, NegatableSqmPredicate {

	private final SqmExpression leftHandExpression;
	private ComparisonOperator operator;
	private final SqmExpression rightHandExpression;

	public SqmComparisonPredicate(
			SqmExpression leftHandExpression,
			ComparisonOperator operator,
			SqmExpression rightHandExpression) {
		this.leftHandExpression = leftHandExpression;
		this.rightHandExpression = rightHandExpression;
		this.operator = operator;
	}

	public SqmExpression getLeftHandExpression() {
		return leftHandExpression;
	}

	public SqmExpression getRightHandExpression() {
		return rightHandExpression;
	}

	public ComparisonOperator getOperator() {
		return operator;
	}

	@Override
	public boolean isNegated() {
		return false;
	}

	@Override
	public void negate() {
		this.operator = this.operator.negated();
	}
}
