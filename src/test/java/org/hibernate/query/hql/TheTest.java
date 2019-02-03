/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.hql;

import org.hibernate.model.AttributeDescriptor;
import org.hibernate.model.EntityDescriptor;
import org.hibernate.model.Model;
import org.hibernate.query.SemanticException;
import org.hibernate.query.hql.internal.HqlParseTreeBuilder;
import org.hibernate.query.hql.internal.HqlParser;
import org.hibernate.query.hql.internal.SemanticQueryBuilder;
import org.hibernate.query.spi.ComparisonOperator;
import org.hibernate.query.sqm.tree.SqmSelectStatement;
import org.hibernate.query.sqm.tree.domain.SqmFromClause;
import org.hibernate.query.sqm.tree.domain.SqmFromClauseSpace;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.domain.SqmPathJoin;
import org.hibernate.query.sqm.tree.domain.SqmPathRoot;
import org.hibernate.query.sqm.tree.expression.SqmLiteral;
import org.hibernate.query.sqm.tree.predicate.SqmComparisonPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.select.SqmSelectClause;
import org.hibernate.query.sqm.tree.select.SqmSelection;

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
public class TheTest {
	private final Model model = createModel();

	@Test
	public void basicTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from MyEntity as e" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "MyEntity" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertTrue( space.getJoins().isEmpty() );
	}

	@Test
	public void noAttributeTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from MyEntity as e join e.doesNotExist" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );

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
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from MyEntity as e join e.name n" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "MyEntity" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertThat( space.getJoins().size(), is( 1 ) );

		final SqmPathJoin join = space.getJoins().get( 0 );
		assertThat( join.getReferencedNavigable().getNavigableName(), is( "name" ) );
		assertThat( join.getExplicitAlias(), is( "n" ) );


		final SqmPredicate joinPredicate = join.getJoinPredicate();
		assertThat( joinPredicate, nullValue() );
	}

	@Test
	public void restrictedAttributeJoinTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from MyEntity as e join e.name n on n.last = 'Smith'" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "MyEntity" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertThat( space.getJoins().size(), is( 1 ) );

		final SqmPathJoin join = space.getJoins().get( 0 );
		assertThat( join.getReferencedNavigable().getNavigableName(), is( "name" ) );
		assertThat( join.getExplicitAlias(), is( "n" ) );

		assertThat( join.getJoinPredicate(), notNullValue() );
		final SqmComparisonPredicate joinPredicate = (SqmComparisonPredicate) join.getJoinPredicate();

		assertThat( joinPredicate.getOperator(), is( ComparisonOperator.EQUAL ) );

		assertThat( joinPredicate.getLeftHandExpression(), instanceOf( SqmPath.class ) );

		assertThat( joinPredicate.getRightHandExpression(), instanceOf( SqmLiteral.class ) );
		assertThat( ( (SqmLiteral) joinPredicate.getRightHandExpression() ).getLiteralValue(), is( "Smith" ) );
	}

	@Test
	public void entityJoinTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "from MyEntity as e join MyEntity as e2" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 1 ) );

		final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

		assertThat( space.getRoot(), notNullValue() );
		assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "MyEntity" ) );
		assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

		assertThat( space.getJoins().size(), is( 1 ) );

		final SqmPathJoin join = space.getJoins().get( 0 );
		assertThat( join.getReferencedNavigable().getNavigableName(), is( "MyEntity" ) );
		assertThat( join.getExplicitAlias(), is( "e2" ) );
	}

	@Test
	public void multiSpaceTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "select e from MyEntity as e, MyEntity as e2" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmFromClause fromClause = statement.getQuerySpec().getFromClause();

		assertThat( fromClause.getSpaces().size(), is( 2 ) );

		{
			final SqmFromClauseSpace space = fromClause.getSpaces().get( 0 );

			assertThat( space.getRoot(), notNullValue() );
			assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "MyEntity" ) );
			assertThat( space.getRoot().getExplicitAlias(), is( "e" ) );

			assertThat( space.getJoins().size(), is( 0 ) );
		}

		{
			final SqmFromClauseSpace space = fromClause.getSpaces().get( 1 );

			assertThat( space.getRoot(), notNullValue() );
			assertThat( space.getRoot().getEntityDescriptor().getEntityName(), is( "MyEntity" ) );
			assertThat( space.getRoot().getExplicitAlias(), is( "e2" ) );

			assertThat( space.getJoins().size(), is( 0 ) );
		}
	}

	@Test
	public void rootSelectionTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "select e from MyEntity as e" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmSelectClause selectClause = statement.getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final SqmSelection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getSelectableNode(), instanceOf( SqmPathRoot.class ) );
		assertThat( selection.getAlias(), nullValue() );
	}

	@Test
	public void attributeSelectionTest() {
		final HqlParser hqlParser = HqlParseTreeBuilder.INSTANCE.parseHql( "select e.name from MyEntity as e" );

		final SemanticQueryBuilder builder = new SemanticQueryBuilder( () -> model );
		final SqmSelectStatement statement = builder.visitSelectStatement( hqlParser.selectStatement() );

		final SqmSelectClause selectClause = statement.getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is( 1 ) );

		final SqmSelection selection = selectClause.getSelections().get( 0 );
		assertThat( selection.getSelectableNode(), instanceOf( SqmPath.class ) );
		assertThat( selection.getAlias(), nullValue() );
	}



	private Model createModel() {
		final EntityDescriptor myEntityDescriptor = new EntityDescriptor(
				"MyEntity",
				e -> new AttributeDescriptor( e, "id" ),
				e -> new AttributeDescriptor( e, "anInt" ),
				e -> new AttributeDescriptor(
						e,
						"name",
						n -> new AttributeDescriptor( n, "first" ),
						n -> new AttributeDescriptor( n, "last" )
				)
		);

		return name -> {
			if ( "MyEntity".equals( name ) ) {
				return myEntityDescriptor;
			}

			return null;
		};
	}
}
