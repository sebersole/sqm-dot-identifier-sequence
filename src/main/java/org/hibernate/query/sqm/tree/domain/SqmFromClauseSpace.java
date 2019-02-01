/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Steve Ebersole
 */
public class SqmFromClauseSpace {
	private SqmPathRoot root;
	private List<SqmPathJoin> joins;

	public SqmFromClauseSpace() {
	}

	public SqmFromClauseSpace(SqmPathRoot root) {
		this.root = root;
	}

	public SqmPathRoot getRoot() {
		return root;
	}

	public void setRoot(SqmPathRoot root) {
		this.root = root;
	}

	/**
	 * Immutable view of the joins.  Use {@link #setJoins} or {@link #addJoin}
	 * to mutate the joins
	 */
	public List<SqmPathJoin> getJoins() {
		return joins == null ? Collections.emptyList() : Collections.unmodifiableList( joins );
	}

	public void setJoins(List<SqmPathJoin> joins) {
		this.joins = joins;
	}

	public void addJoin(SqmPathJoin join) {
		if ( this.joins == null ) {
			this.joins = new ArrayList<>();
		}

		this.joins.add( join );
	}
}
