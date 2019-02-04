/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.select;

/**
 * Represents an individual selection within a select clause.
 *
 * @author Steve Ebersole
 */
public class SqmSelection implements SqmAliasedNode {
	private final SqmSelectableNode selectableNode;
	private final String alias;

	public SqmSelection(
			SqmSelectableNode selectableNode,
			String alias) {
		this.selectableNode = selectableNode;
		this.alias = alias;
	}

	public SqmSelection(SqmSelectableNode selectableNode) {
		this( selectableNode, null );
	}

	@Override
	public SqmSelectableNode getSelectableNode() {
		return selectableNode;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
