/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query;

import java.util.Objects;

import org.hibernate.DotIdentifierSequence;
import org.hibernate.internal.util.StringHelper;

/**
 * @author Steve Ebersole
 */
public class NavigablePath implements DotIdentifierSequence {

	private final NavigablePath parent;
	private final String localName;
	private final String fullPath;

	public NavigablePath(NavigablePath parent, String navigableName) {
		this.parent = parent;
		this.localName = navigableName;

		final String prefix;
		if ( parent != null ) {
			final String resolvedParent = parent.getFullPath();
			if ( StringHelper.isEmpty( resolvedParent ) ) {
				prefix = "";
			}
			else {
				prefix = resolvedParent + '.';
			}
		}
		else {
			prefix = "";
		}

		this.fullPath = prefix + navigableName;
	}

	public NavigablePath(String localName) {
		this( null, localName );
	}

	public NavigablePath() {
		this( "" );
	}

	public NavigablePath append(String property) {
		return new NavigablePath( this, property );
	}

	public NavigablePath getParent() {
		return parent;
	}

	public String getLocalName() {
		return localName;
	}

	public String getFullPath() {
		return fullPath;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + fullPath + ']';
	}

	@Override
	public int hashCode() {
		return fullPath.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		NavigablePath path = (NavigablePath) o;
		return Objects.equals( getFullPath(), path.getFullPath() );
	}
}
