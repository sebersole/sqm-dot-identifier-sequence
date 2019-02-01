/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree;

import org.hibernate.query.sqm.tree.select.SqmQuerySpec;

/**
 * @author Steve Ebersole
 */
public class SqmSelectStatement {
	private SqmQuerySpec querySpec;

	public SqmSelectStatement() {
	}

	public SqmSelectStatement(SqmQuerySpec querySpec) {
		this.querySpec = querySpec;
	}

	public SqmQuerySpec getQuerySpec() {
		return querySpec;
	}

	public void setQuerySpec(SqmQuerySpec querySpec) {
		this.querySpec = querySpec;
	}
}
