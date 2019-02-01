/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.internal;

import org.hibernate.DotIdentifierSequence;

/**
 * @author Steve Ebersole
 */
public class DotIdentifierSequenceStandardImpl implements DotIdentifierSequence {
	private final DotIdentifierSequenceStandardImpl parent;
	private final String localName;
	private final String fullPath;

	public DotIdentifierSequenceStandardImpl(DotIdentifierSequenceStandardImpl parent, String navigableName) {
		this.parent = parent;
		this.localName = navigableName;

		if ( parent != null ) {
			this.fullPath =parent.getFullPath() + '.' + navigableName;
		}
		else {
			this.fullPath = navigableName;
		}
	}

	public DotIdentifierSequenceStandardImpl(String localName) {
		this( null, localName );
	}

	@Override
	public DotIdentifierSequence getParent() {
		return parent;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public String getFullPath() {
		return fullPath;
	}

	@Override
	public DotIdentifierSequence append(String subPathName) {
		return new DotIdentifierSequenceStandardImpl( this, subPathName );
	}
}
