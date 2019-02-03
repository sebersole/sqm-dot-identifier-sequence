/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.hql.internal.path;

import java.util.Locale;
import java.util.function.Supplier;

import org.hibernate.model.AttributeContainer;
import org.hibernate.model.AttributeDescriptor;
import org.hibernate.query.SemanticException;
import org.hibernate.query.hql.spi.DotIdentifierHandler;
import org.hibernate.query.hql.spi.SqmCreationContext;
import org.hibernate.query.hql.spi.StatementProcessingState;
import org.hibernate.query.QueryLogger;
import org.hibernate.query.sqm.tree.domain.SqmFrom;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.domain.SqmPathImpl;

/**
 * @asciidoc
 *
 * DotIdentifierHandler used to interpret paths outside of any specific
 * context.  This is the handler used at the root of the handler stack.
 *
 * It can recognize any number of types of paths -
 *
 * 		* fully-qualified class names (entity or otherwise)
 * 		* static field references, e.g. `MyClass.SOME_FIELD`
 * 		* enum value references, e.g. `Sex.MALE`
 * 		* navigable-path
 * 		* others?
 *
 * @author Steve Ebersole
 */
public class BasicDotIdentifierHandler implements DotIdentifierHandler {
	private final Supplier<StatementProcessingState> processingStateSupplier;
	private final SqmCreationContext sqmCreationContext;

	private String pathSoFar;
	private LocalSequencePart currentPart;

	public BasicDotIdentifierHandler(
			Supplier<StatementProcessingState> processingStateSupplier,
			SqmCreationContext sqmCreationContext) {
		this.processingStateSupplier = processingStateSupplier;
		this.sqmCreationContext = sqmCreationContext;
	}

	@Override
	public SequencePart getConsumedPart() {
		if ( currentPart instanceof DomainReferenceSequencePart ) {
			return ( (DomainReferenceSequencePart) currentPart ).getDomainPath();
		}

		return currentPart;
	}

	@Override
	public void consumeIdentifier(String identifier, boolean isBase, boolean isTerminal) {
		if ( isBase ) {
			// each time we start a new sequence we need to reset our state
			reset();
		}

		if ( pathSoFar == null ) {
			pathSoFar = identifier;
		}
		else {
			pathSoFar += ( '.' + identifier );
		}

		QueryLogger.QUERY_LOGGER.tracef(
				"BasicDotIdentifierHandler#consumeIdentifier( %s, %s, %s ) - %s",
				identifier,
				isBase,
				isTerminal,
				pathSoFar
		);

		currentPart = currentPart.consumeIdentifier( identifier, isBase, isTerminal );
	}

	private void reset() {
		pathSoFar = null;
		currentPart = new BaseLocalSequencePart();
	}

	public interface LocalSequencePart extends SequencePart {
		LocalSequencePart consumeIdentifier(String identifier, boolean isBase, boolean isTerminal);
	}

	public class BaseLocalSequencePart implements LocalSequencePart {
		@Override
		public LocalSequencePart consumeIdentifier(String identifier, boolean isBase, boolean isTerminal) {
			if ( isBase ) {
				final StatementProcessingState processingState = processingStateSupplier.get();

				final SqmFrom pathRootByAlias = processingState.getPathRootLocator().findPathRootByAlias( identifier );
				if ( pathRootByAlias != null ) {
					// identifier is an alias (identification variable)
					return new DomainReferenceSequencePart( pathRootByAlias );
				}

				final SqmFrom pathRootByExposedNavigable = processingState.getPathRootLocator()
						.findPathRootByExposedNavigable( identifier );
				if ( pathRootByExposedNavigable != null ) {
					// identifier is an "unqualified attribute reference"
					final DomainReferenceSequencePart part = new DomainReferenceSequencePart( pathRootByExposedNavigable );
					return part.consumeIdentifier( identifier, isBase, isTerminal );
				}
			}

			// at the moment, below this point we wait to resolve the sequence until we hit the terminal
			//
			// we could check for "intermediate resolution", but that comes with a performance hit.  E.g., consider
			//
			//		`org.hibernate.test.Sex.MALE`
			//
			// we could check `org` and then `org.hibernate` and then `org.hibernate.test` and then ... until
			// we know it is a package, class or entity name.  That gets expensive though.  For now, plan on
			// resolving these at the terminal
			//
			// todo (6.0) : finish this logic.  and see above note in `! isTerminal` block

			if ( !isTerminal ) {
				return this;
			}

			throw new UnsupportedOperationException( "Not yet implemented" );
		}
	}

	public class DomainReferenceSequencePart implements LocalSequencePart {
		private SqmPath domainPath;

		public DomainReferenceSequencePart(SqmPath domainPath) {
			this.domainPath = domainPath;
		}

		public SqmPath getDomainPath() {
			return domainPath;
		}

		@Override
		public LocalSequencePart consumeIdentifier(String identifier, boolean isBase, boolean isTerminal) {
			final AttributeDescriptor attribute = ( (AttributeContainer) domainPath.getReferencedNavigable() ).findAttribute( identifier );
			if ( attribute == null ) {
				throw new SemanticException(
						String.format(
								Locale.ROOT,
								"Could not resolve attribute - %s -> %s",
								domainPath.getNavigablePath().getFullPath(),
								identifier
						)
				);
			}

			domainPath = new SqmPathImpl( attribute, domainPath.getNavigablePath().append( identifier ) );

			return this;
		}
	}
}
