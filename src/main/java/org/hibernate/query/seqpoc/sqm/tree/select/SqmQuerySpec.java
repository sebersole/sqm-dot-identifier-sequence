/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.select;

import org.hibernate.query.seqpoc.sqm.tree.domain.SqmFromClause;

/**
 * @author Steve Ebersole
 */
public class SqmQuerySpec {
	private SqmFromClause fromClause;
	private SqmSelectClause selectClause;

	public SqmQuerySpec() {
	}

	public SqmQuerySpec(SqmFromClause fromClause) {
		this.fromClause = fromClause;
	}

	public SqmFromClause getFromClause() {
		return fromClause;
	}

	public void setFromClause(SqmFromClause fromClause) {
		this.fromClause = fromClause;
	}

	public SqmSelectClause getSelectClause() {
		return selectClause;
	}

	public void setSelectClause(SqmSelectClause selectClause) {
		this.selectClause = selectClause;
	}
}
