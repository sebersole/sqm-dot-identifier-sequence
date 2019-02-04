/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.sqm.tree.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Steve Ebersole
 */
public class SqmFromClause {
	private List<SqmFromClauseSpace> fromClauseSpaces;

	public SqmFromClause() {
	}

	public SqmFromClause(List<SqmFromClauseSpace> fromClauseSpaces) {
		this.fromClauseSpaces = fromClauseSpaces;
	}

	/**
	 * Immutable view of the spaces.  Use {@link #setSpaces},
	 * {@link #addSpace} or {@link #makeSpace} to
	 * mutate the spaces
	 */
	public List<SqmFromClauseSpace> getSpaces() {
		return fromClauseSpaces == null ? Collections.emptyList() : Collections.unmodifiableList( fromClauseSpaces );
	}

	public void setSpaces(List<SqmFromClauseSpace> fromClauseSpaces) {
		this.fromClauseSpaces = fromClauseSpaces;
	}

	public void addSpace(SqmFromClauseSpace space) {
		if ( fromClauseSpaces == null ) {
			fromClauseSpaces = new ArrayList<>();
		}

		fromClauseSpaces.add( space );
	}

	public SqmFromClauseSpace makeSpace() {
		SqmFromClauseSpace space = new SqmFromClauseSpace();
		addSpace( space );
		return space;
	}

	public void visitSpaces(Consumer<SqmFromClauseSpace> consumer) {
		if ( fromClauseSpaces != null ) {
			fromClauseSpaces.forEach( consumer );
		}
	}
}
