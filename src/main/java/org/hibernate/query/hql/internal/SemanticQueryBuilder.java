/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.hql.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.internal.util.collections.Stack;
import org.hibernate.internal.util.collections.StandardStack;
import org.hibernate.model.EntityDescriptor;
import org.hibernate.query.AliasCollisionException;
import org.hibernate.query.SemanticException;
import org.hibernate.query.hql.internal.path.BasicDotIdentifierHandler;
import org.hibernate.query.hql.internal.path.QualifiedJoinPathIdentifierConsumer;
import org.hibernate.query.hql.spi.DotIdentifierHandler;
import org.hibernate.query.hql.spi.FromElementRegistry;
import org.hibernate.query.hql.spi.PathRootLocator;
import org.hibernate.query.hql.spi.SqmCreationContext;
import org.hibernate.query.hql.spi.StatementProcessingState;
import org.hibernate.query.internal.QueryLogger;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.domain.SqmFrom;
import org.hibernate.query.sqm.tree.domain.SqmFromClause;
import org.hibernate.query.sqm.tree.domain.SqmFromClauseSpace;
import org.hibernate.query.sqm.tree.domain.SqmPathCrossJoin;
import org.hibernate.query.sqm.tree.domain.SqmPathJoin;
import org.hibernate.query.sqm.tree.domain.SqmPathRoot;
import org.hibernate.query.sqm.tree.domain.SqmQualifiedPathJoin;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.select.SqmQuerySpec;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.select.SqmSelectableNode;
import org.hibernate.query.sqm.tree.select.SqmSelection;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryBuilder extends HqlParserBaseVisitor {
	private final SqmCreationContext sqmCreationContext;

	private final Stack<DotIdentifierHandler> identifierConsumerStack = new StandardStack<>();
	private final Stack<StatementProcessingState> statementProcessingStateStack = new StandardStack<>();

	public SemanticQueryBuilder(SqmCreationContext sqmCreationContext) {
		assert sqmCreationContext != null;
		assert sqmCreationContext.getTypeConfiguration() != null;

		this.sqmCreationContext = sqmCreationContext;

		this.identifierConsumerStack.push(
				new BasicDotIdentifierHandler(
						statementProcessingStateStack::getCurrent,
						sqmCreationContext
				)
		);
	}

	@Override
	public SqmSelectStatement visitSelectStatement(HqlParser.SelectStatementContext ctx) {
		final SqmSelectStatement statement = new SqmSelectStatement();

		statement.setQuerySpec( visitQuerySpec( ctx.querySpec() ) );

		return statement;
	}

	@Override
	public SqmQuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		final SqmQuerySpec sqmQuerySpec = new SqmQuerySpec();

		statementProcessingStateStack.push(
				new QuerySpecProcessingStateImpl(
						sqmQuerySpec,
						statementProcessingStateStack.getCurrent()
				)
		);

		try {
			sqmQuerySpec.setFromClause( visitFromClause( ctx.fromClause() ) );
			sqmQuerySpec.setSelectClause( visitSelectClause(ctx.selectClause() ) );
		}
		finally {
			statementProcessingStateStack.pop();
		}

		return sqmQuerySpec;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// FROM-clause

	@Override
	public SqmFromClause visitFromClause(HqlParser.FromClauseContext parserFromClause) {
		final SqmFromClause fromClause = new SqmFromClause();

		for ( HqlParser.FromElementSpaceContext parserSpace : parserFromClause.fromElementSpace() ) {
			final SqmFromClauseSpace space = fromClause.makeSpace();
			space.setRoot( visitFromElementSpaceRoot( parserSpace.fromElementSpaceRoot() ) );

			for ( HqlParser.CrossJoinContext parserJoin : parserSpace.crossJoin() ) {
				space.addJoin( visitCrossJoin( parserJoin ) );
			}

			for ( HqlParser.JpaCollectionJoinContext parserJoin : parserSpace.jpaCollectionJoin() ) {
				space.addJoin( visitJpaCollectionJoin( parserJoin ) );
			}

			for ( HqlParser.QualifiedJoinContext parserJoin : parserSpace.qualifiedJoin() ) {
				space.addJoin( visitQualifiedJoin( parserJoin ) );
			}

		}

		return fromClause;
	}

	@Override
	public SqmPathRoot visitFromElementSpaceRoot(HqlParser.FromElementSpaceRootContext ctx) {
		final String name = ctx.pathRoot().dotIdentifierSequence().getText();

		QueryLogger.QUERY_LOGGER.debugf( "Handling root path - %s", name );

		final EntityDescriptor entityDescriptor = sqmCreationContext.getTypeConfiguration().findEntityDescriptor( name );
		if ( entityDescriptor == null ) {
			throw new RuntimeException( "Could not locate entity - " + name );
		}

		final SqmPathRoot pathRoot = new SqmPathRoot(
				entityDescriptor,
				visitIdentificationVariableDef( ctx.pathRoot().identificationVariableDef() )
		);

		statementProcessingStateStack.getCurrent().getFromElementRegistry().registerFromElement( pathRoot );

		return pathRoot;
	}

	@Override
	public String visitIdentificationVariableDef(HqlParser.IdentificationVariableDefContext ctx) {
		if ( ctx == null ) {
			return null;
		}

		if ( ctx.identifier() != null ) {
			return ctx.identifier().getText();
		}

		if ( ctx.IDENTIFIER() != null ) {
			return ctx.IDENTIFIER().getText();
		}

		return null;
	}

	@Override
	public SqmPathCrossJoin visitCrossJoin(HqlParser.CrossJoinContext ctx) {
		throw new IllegalStateException( "Not yet implemented" );
	}

	@Override
	public SqmQualifiedPathJoin visitQualifiedJoin(HqlParser.QualifiedJoinContext ctx) {
		final SqmJoinType joinType;
		final HqlParser.JoinTypeQualifierContext joinTypeQualifier = ctx.joinTypeQualifier();
		if ( joinTypeQualifier.OUTER() != null ) {
			// for outer joins, only left outer joins are currently supported
			if ( joinTypeQualifier.FULL() != null ) {
				throw new SemanticException( "FULL OUTER joins are not yet supported : " + ctx.getText() );
			}
			if ( joinTypeQualifier.RIGHT() != null ) {
				throw new SemanticException( "RIGHT OUTER joins are not yet supported : " + ctx.getText() );
			}

			joinType = SqmJoinType.LEFT;
		}
		else {
			joinType = SqmJoinType.INNER;
		}


		final QualifiedJoinPathIdentifierConsumer identifierConsumer = new QualifiedJoinPathIdentifierConsumer(
				joinType,
				ctx.FETCH() != null,
				visitIdentificationVariableDef( ctx.qualifiedJoinRhs().identificationVariableDef() ),
				statementProcessingStateStack.getCurrent(),
				sqmCreationContext
		);

		identifierConsumerStack.push( identifierConsumer );

		try {
			ctx.qualifiedJoinRhs().path().accept( this );
			final SqmQualifiedPathJoin join = (SqmQualifiedPathJoin) identifierConsumer.getConsumedPart();

			if ( ctx.qualifiedJoinPredicate() != null ) {
				// todo (6.0) : implement this
			}

			return join;
		}
		finally {
			identifierConsumerStack.pop();
		}
	}

	@Override
	public SqmPathJoin visitJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		throw new IllegalStateException( "Not yet implemented" );
	}




	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SELECT-clause

	@Override
	public SqmSelectClause visitSelectClause(HqlParser.SelectClauseContext ctx) {
		if ( ctx == null ) {
			return generateImplicitSelectClause();
		}

		final SqmSelectClause selectClause = new SqmSelectClause();
		for ( HqlParser.SelectionContext selectionContext : ctx.selectionList().selection() ) {
			selectClause.addSelection( visitSelection( selectionContext ) );
		}

		return selectClause;
	}

	private SqmSelectClause generateImplicitSelectClause() {
		// todo (6.0) : maybe limit this to the root query-spec of a select-statement?
		// todo (6.0) : hook in strict query compliance checking

		final StatementProcessingState processingState = statementProcessingStateStack.getCurrent();
		assert processingState instanceof QuerySpecProcessingStateImpl;

		final SqmFromClause fromClause = ( (QuerySpecProcessingStateImpl) processingState ).getInflightQuerySpec().getFromClause();

		if ( fromClause.getSpaces().size() > 1 ) {
			throw new SemanticException( "Cannot create implicit select-clause : query defined multiple from-clause spaces..." );
		}

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		return new SqmSelectClause( true, new SqmSelection( space.getRoot() ) );
	}

	@Override
	public SqmSelection visitSelection(HqlParser.SelectionContext ctx) {
		return new SqmSelection(
				visitSelectExpression( ctx.selectExpression() ),
				visitResultIdentifier( ctx.resultIdentifier() )
		);
	}

	@Override
	public String visitResultIdentifier(HqlParser.ResultIdentifierContext ctx) {
		if ( ctx != null ) {
			if ( ctx.AS() != null ) {
				return ctx.identifier().getText();
			}

			if ( ctx.IDENTIFIER() != null ) {
				return ctx.IDENTIFIER().getText();
			}
		}

		return null;
	}

	@Override
	public SqmSelectableNode visitSelectExpression(HqlParser.SelectExpressionContext ctx) {
		if ( ctx.expression() != null ) {
			return (SqmSelectableNode) ctx.expression().accept( this );
		}

		throw new UnsupportedOperationException( "Support for selection types other than `expression` not yet implemented" );
	}



	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Expressions

	@Override
	public SqmExpression visitPathExpression(HqlParser.PathExpressionContext ctx) {
		super.visitPathExpression( ctx );

		return (SqmExpression) identifierConsumerStack.getCurrent().getConsumedPart();
	}



	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Path structures


	@Override
	public Object visitDotIdentifierSequence(HqlParser.DotIdentifierSequenceContext ctx) {
		final int continuations = ctx.dotIdentifierSequenceContinuation().size();

		identifierConsumerStack.getCurrent().consumeIdentifier(
				ctx.identifier().getText(),
				true,
				continuations == 0
		);

		int i = 1;
		for ( HqlParser.DotIdentifierSequenceContinuationContext continuation : ctx.dotIdentifierSequenceContinuation() ) {
			identifierConsumerStack.getCurrent().consumeIdentifier(
					continuation.identifier().getText(),
					false,
					i++ >= continuations
			);
		}

		// generally speaking we don't care about the return - its the consumption that matters
		return ctx;
	}

	@Override
	public Object visitDotIdentifierSequenceContinuation(HqlParser.DotIdentifierSequenceContinuationContext ctx) {
		return super.visitDotIdentifierSequenceContinuation( ctx );
	}

	private class QuerySpecProcessingStateImpl implements StatementProcessingState, PathRootLocator, FromElementRegistry {
		// todo (6.0) : ultimately `parent` here needs to be able to handle DML statements as well

		private final SqmQuerySpec inflightSpec;
		private final StatementProcessingState parent;

		private final List<SqmFrom> fromElements = new ArrayList<>();
		private final Map<String, SqmFrom> fromElementsByAlias = new HashMap<>();
		private final Map<String, SqmSelection> selectionsByAlias = new HashMap<>();

		public QuerySpecProcessingStateImpl(SqmQuerySpec inflightSpec, StatementProcessingState parent) {
			assert inflightSpec != null;

			this.inflightSpec = inflightSpec;
			this.parent = parent;
		}

		public SqmQuerySpec getInflightQuerySpec() {
			return inflightSpec;
		}

		@Override
		public FromElementRegistry getFromElementRegistry() {
			return this;
		}

		@Override
		public PathRootLocator getPathRootLocator() {
			return this;
		}

		public void registerFromElement(SqmFrom sqmFrom) {
			fromElements.add( sqmFrom );

			final String alias = sqmFrom.getExplicitAlias();
			if ( alias == null ) {
				return;
			}

			final SqmFrom previous = fromElementsByAlias.put( alias, sqmFrom );
			if ( previous != null ) {
				throw new AliasCollisionException(
						String.format(
								Locale.ENGLISH,
								"Alias [%s] used for multiple from-clause-elements : %s, %s",
								alias,
								previous,
								sqmFrom
						)
				);
			}
		}

		public void registerSelection(SqmSelection selection) {
			if ( selection.getAlias() != null ) {
				checkResultVariable( selection );
				selectionsByAlias.put( selection.getAlias(), selection );
			}
		}

		private void checkResultVariable(SqmSelection selection) {
			final String alias = selection.getAlias();

			if ( selectionsByAlias.containsKey( alias ) ) {
				throw new AliasCollisionException(
						String.format(
								Locale.ENGLISH,
								"Alias [%s] is already used in same select clause",
								alias
						)
				);
			}

			final SqmFrom registeredFromElement = fromElementsByAlias.get( alias );
			if ( registeredFromElement != null ) {
				if ( ! registeredFromElement.equals( selection.getSelectableNode() ) ) {
					throw new AliasCollisionException(
							String.format(
									Locale.ENGLISH,
									"Alias [%s] used in select-clause [%s] also used in from-clause [%s]",
									alias,
									selection.getSelectableNode(),
									registeredFromElement
							)
					);
				}
			}
		}

		@Override
		public SqmFrom findPathRootByAlias(String alias) {
			final SqmFrom registered = fromElementsByAlias.get( alias );
			if ( registered != null ) {
				return registered;
			}

			if ( parent != null ) {
				return parent.getPathRootLocator().findPathRootByAlias( alias );
			}

			return null;
		}

		@Override
		public SqmFrom findPathRootByExposedNavigable(String navigableName) {

			// todo (6.0) : atm this checks every from-element every time, the idea being to make sure there is only one such element
			//		obviously that scales poorly across larger from-clauses.  Another option (configurable alt?) would be to
			//		simply pick the first one as a perf optimization

			SqmFrom found = null;
			for ( SqmFrom fromElement : fromElements ) {
				if ( definesAttribute( fromElement, navigableName ) ) {
					if ( found != null ) {
						throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + navigableName );
					}
					found = fromElement;
				}
			}

			if ( found == null ) {
				if ( parent != null ) {
					QueryLogger.QUERY_LOGGER.debugf(
							"Unable to resolve unqualified attribute [%s] in local from-clause; checking parent ",
							navigableName
					);
					found = parent.getPathRootLocator().findPathRootByExposedNavigable( navigableName );
				}
			}

			return found;
		}

		private boolean definesAttribute(SqmFrom potentialSource, String name) {
			return potentialSource.getReferencedNavigable().findAttribute( name ) != null;
		}
	}

}
