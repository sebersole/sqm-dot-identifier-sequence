/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.hql;

import org.hibernate.query.SemanticException;
import org.hibernate.query.seqpoc.hql.internal.HqlParseTreeBuilder;
import org.hibernate.query.seqpoc.hql.internal.HqlParser;
import org.hibernate.query.seqpoc.hql.internal.SemanticQueryBuilder;
import org.hibernate.query.seqpoc.sqm.tree.SqmSelectStatement;
import org.hibernate.query.seqpoc.sqm.tree.domain.SqmFromClause;
import org.hibernate.query.seqpoc.sqm.tree.domain.SqmFromClauseSpace;
import org.hibernate.query.seqpoc.sqm.tree.domain.SqmPath;
import org.hibernate.query.seqpoc.sqm.tree.domain.SqmPathJoin;
import org.hibernate.query.seqpoc.sqm.tree.domain.SqmPathRoot;
import org.hibernate.query.seqpoc.sqm.tree.domain.SqmPathEntityJoin;
import org.hibernate.query.seqpoc.sqm.tree.expression.SqmLiteral;
import org.hibernate.query.seqpoc.sqm.tree.predicate.SqmComparisonPredicate;
import org.hibernate.query.seqpoc.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.seqpoc.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.seqpoc.sqm.tree.select.SqmSelection;
import org.hibernate.query.spi.ComparisonOperator;

import org.hibernate.testing.orm.domain.StandardDomainModel;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.SessionFactoryScopeAware;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings("WeakerAccess")

@DomainModel( standardModels = StandardDomainModel.RETAIL )
@SessionFactory
public class TheTest implements SessionFactoryScopeAware {
	private SessionFactoryScope sessionFactoryScope;

	@Test
	public void basicTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from Order as o" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Order" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "o" ) );

		assertTrue( space.getJoins().isEmpty() );
	}

	@Test
	public void noAttributeTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from Order as e join e.doesNotExist" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();

		try {
			builder.visitSelectStatement( hqlParser.selectStatement() );
		}
		catch (SemanticException expected) {
			return;
		}

		fail();
	}

	@Test
	public void attributeJoinTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from Order as e join e.salesAssociate n" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Order" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertThat( space.getJoins().size(), is( 1 ) );

		final SqmPathJoin join = space.getJoins().get( 0 );
		assertThat( join.getReferencedNavigable().getNavigableName(), is( "salesAssociate" ) );
		assertThat( join.getExplicitAlias(), is( "n" ) );


		final SqmPredicate joinPredicate = join.getJoinPredicate();
		assertThat( joinPredicate, nullValue() );
	}

	@Test
	public void restrictedAttributeJoinTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from Order as e join e.salesAssociate n on n.name.familyName = 'Smith'" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Order" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertThat( space.getJoins().size(), is( 1 ) );

		final SqmPathJoin join = space.getJoins().get( 0 );
		assertThat( join.getReferencedNavigable().getNavigableName(), is( "salesAssociate" ) );
		assertThat( join.getExplicitAlias(), is( "n" ) );

		assertThat( join.getJoinPredicate(), notNullValue() );
		final SqmComparisonPredicate joinPredicate = (SqmComparisonPredicate) join.getJoinPredicate();

		assertThat( joinPredicate.getOperator(), is( ComparisonOperator.EQUAL ) );

		assertThat( joinPredicate.getLeftHandExpression(), instanceOf( SqmPath.class ) );
		assertThat( ( (SqmPath) joinPredicate.getLeftHandExpression() ).getReferencedNavigable().getNavigableName(), is ( "familyName" ) );

		assertThat( joinPredicate.getRightHandExpression(), instanceOf( SqmLiteral.class ) );
		assertThat( ( (SqmLiteral) joinPredicate.getRightHandExpression() ).getLiteralValue(), is( "Smith" ) );
	}

	@Test
	public void entityJoinTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from Order as e join Vendor as v" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Order" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertThat( space.getJoins().size(), is( 1 ) );

		final SqmPathJoin join = space.getJoins().get( 0 );
		assertThat( join, instanceOf( SqmPathEntityJoin.class ) );
		assertThat( ( (SqmPathEntityJoin) join ).getJoinedEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Vendor" ) );
		assertThat( join.getExplicitAlias(), is( "v" ) );
	}

	@Test
	public void multiSpaceTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "select e from Order as e, Order as e2" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 2 ) );

		{
			final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

			assertThat( space.getRoot(), notNullValue() );
			assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Order" ) );
			assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

			assertThat( space.getJoins().size(), is( 0 ) );
		}

		{
			final SqmFromClauseSpace space = fromClause.getSpaces().get( 1 );

			assertThat( space.getRoot(), notNullValue() );
			assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "org.hibernate.testing.orm.domain.retail.Order" ) );
			assertThat( space.getRoot().getExplicitAlias(), is( "e2" ) );

			assertThat( space.getJoins().size(), is( 0 ) );
		}
	}

	@Test
	public void rootSelectionTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "select e from Order as e" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmSelectClause selectClause = statement.getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final SqmSelection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getSelectableNode(), instanceOf( SqmPathRoot.class ) );
		assertThat( selection.getAlias(), nullValue() );
	}

	@Test
	public void attributeSelectionTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "select e.salesAssociate from Order as e" );

		final SemanticQueryBuilder builder = createSemanticQueryBuilder();
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmSelectClause selectClause = statement.getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final SqmSelection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getSelectableNode(), instanceOf( SqmPath.class ) );
		assertThat( selection.getAlias(), nullValue() );
	}

	private SemanticQueryBuilder createSemanticQueryBuilder() {
		return new SemanticQueryBuilder( sessionFactoryScope.getSessionFactory().getMetamodel() );
	}

	@Override
	public void injectSessionFactoryScope(SessionFactoryScope scope) {
		sessionFactoryScope = scope;
	}
}
