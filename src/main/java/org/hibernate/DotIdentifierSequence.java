/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate;

import java.util.function.Consumer;

/**
 * Hibernate often deals with compound names/paths.  This interface
 * defines a standard way of interacting with them
 *
 * @author Steve Ebersole
 */
public interface DotIdentifierSequence {
	DotIdentifierSequence getParent();
	String getLocalName();
	String getFullPath();

	DotIdentifierSequence append(String subPathName);

	default boolean isRoot() {
		return getParent() == null;
	}

	default DotIdentifierSequence getRoot() {
		if ( getParent() == null ) {
			return this;
		}
		return getParent().getRoot();
	}

	default void visitPartsTerminalFirst(Consumer<String> consumer) {
		consumer.accept( getLocalName() );
		if ( getParent() != null ) {
			getParent().visitPartsTerminalFirst( consumer );
		}
	}

	default void visitPartsRootFirst(Consumer<String> consumer) {
		if ( getParent() != null ) {
			getParent().visitPartsRootFirst( consumer );
		}
		consumer.accept( getLocalName() );
	}

	default String toLoggableFragment() {
		return getLocalName();
	}
}
