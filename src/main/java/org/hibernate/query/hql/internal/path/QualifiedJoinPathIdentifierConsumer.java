/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.hql.internal.path;

import java.util.Locale;

import org.hibernate.model.AttributeDescriptor;
import org.hibernate.model.EntityDescriptor;
import org.hibernate.query.SemanticException;
import org.hibernate.query.hql.spi.DotIdentifierHandler;
import org.hibernate.query.hql.spi.SqmCreationContext;
import org.hibernate.query.hql.spi.StatementProcessingState;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.domain.SqmAttributePathJoinImpl;
import org.hibernate.query.sqm.tree.domain.SqmFrom;
import org.hibernate.query.sqm.tree.domain.SqmPathEntityJoin;

/**
 * @author Steve Ebersole
 */
public class QualifiedJoinPathIdentifierConsumer implements DotIdentifierHandler {
	private final SqmJoinType joinType;
	private final boolean fetch;
	private final String alias;

	private final StatementProcessingState processingState;
	private final SqmCreationContext sqmCreationContext;

	private String completePath = null;
	private SqmFrom current = null;

	public QualifiedJoinPathIdentifierConsumer(
			SqmJoinType joinType,
			boolean fetch,
			String alias,
			StatementProcessingState processingState,
			SqmCreationContext sqmCreationContext) {
		this.joinType = joinType;
		this.fetch = fetch;
		this.alias = alias;
		this.processingState = processingState;
		this.sqmCreationContext = sqmCreationContext;
	}

	@Override
	public SqmFrom getConsumedPart() {
		return current;
	}

	@Override
	public void consumeIdentifier(String identifier, boolean isBase, boolean isTerminal) {
		if ( completePath == null ) {
			completePath = identifier;
		}
		else {
			completePath += ( '.' + identifier );
		}

		if ( this.current == null ) {
			final SqmFrom pathRootByAlias = processingState.getPathRootLocator().findPathRootByAlias( identifier );
			if ( pathRootByAlias != null ) {
				// identifier is an alias (identification variable)
				this.current = pathRootByAlias;
				return;
			}

			final SqmFrom pathRootByExposedNavigable = processingState.getPathRootLocator().findPathRootByExposedNavigable( identifier );
			if ( pathRootByExposedNavigable != null ) {
				// identifier is an "unqualified attribute reference".  Set `current` to the exposer,
				// but do not return - we still need to consume the identifier against the from-element
				// exposing
				current = pathRootByExposedNavigable;
				//
			}
			else {
				// the identifier could also signify an "entity join"... this could potentially need
				// to consume the entire sequence.  If we are processing the path terminus try
				// to resolve it as an entity-name
				if ( isTerminal ) {
					final EntityDescriptor entityDescriptor = sqmCreationContext.getTypeConfiguration().findEntityDescriptor( completePath );
					if ( entityDescriptor != null ) {
						current = new SqmPathEntityJoin( entityDescriptor, joinType, alias );
						return;
					}

//					throw new SemanticException( "Could not resolve domain path root - " + completePath );
				}
				else {
					// wait for the terminal
					return;
				}
			}
		}

		if ( current == null ) {
			throw new SemanticException( "Could not resolve qualified join path - " + identifier );
		}

		final AttributeDescriptor attribute = this.current.getReferencedNavigable().findAttribute( identifier );
		if ( attribute == null ) {
			throw new SemanticException(
					String.format(
							Locale.ROOT,
							"Could not resolve attribute - %s -> %s",
							current.getNavigablePath().getFullPath(),
							identifier
					)
			);
		}

		this.current = new SqmAttributePathJoinImpl(
				attribute,
				current.getNavigablePath().append( identifier ),
				joinType,
				isTerminal ? alias : null,
				fetch
		);

		processingState.getFromElementRegistry().registerFromElement( current );
	}

}
