/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.seqpoc.hql.spi;

/**
 * Strategy for consuming each identifier in a dot-identifier sequence
 *
 * @author Steve Ebersole
 */
public interface DotIdentifierHandler {
	void consumeIdentifier(String identifier, boolean isBase, boolean isTerminal);

	SequencePart getConsumedPart();

	interface SequencePart {
	}
}
