parser grammar HqlParser;

options {
	tokenVocab=HqlLexer;
}

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.seqpoc.hql.internal;
}

@members {
	protected void logUseOfReservedWordAsIdentifier(Token token) {
	}
}


selectStatement
	: querySpec
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root sqm or sub sqm

querySpec
	:	selectClause? fromClause whereClause?
	;




// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: FROM fromElementSpace (COMMA fromElementSpace)*
	;

fromElementSpace
// todo (6.0) : fromClauseSpace
	:	fromElementSpaceRoot ( crossJoin | jpaCollectionJoin | qualifiedJoin )*
	;

fromElementSpaceRoot
// todo (6.0) : fromClauseSpaceRoot
	: pathRoot
	;

pathRoot
	: dotIdentifierSequence (identificationVariableDef)?
	;

identificationVariableDef
	: AS identifier
	| IDENTIFIER
	;

crossJoin
	: CROSS JOIN pathRoot
	;

jpaCollectionJoin
	:	COMMA IN LEFT_PAREN path RIGHT_PAREN (identificationVariableDef)?
	;

qualifiedJoin
// todo (6.0) : consider grouping `joinTypeQualifier JOIN FETCH? qualifiedJoinRhs` into a single sub-rule
//		this would allow easier handling in the walker
	: joinTypeQualifier JOIN FETCH? qualifiedJoinRhs (qualifiedJoinPredicate)?
	;


joinTypeQualifier
	: INNER?
	| (LEFT|RIGHT|FULL)? OUTER?
	;

qualifiedJoinRhs
	: path (identificationVariableDef)?
	;

qualifiedJoinPredicate
	: (ON | WITH) predicate
	;



// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	:	SELECT DISTINCT? selectionList
	;

selectionList
	: selection (COMMA selection)*
	;

selection
	: selectExpression (resultIdentifier)?
	;

selectExpression
	:	dynamicInstantiation
	|	jpaSelectObjectSyntax
	|	mapEntrySelection
	|	expression
	;

resultIdentifier
	: (AS identifier)
	| IDENTIFIER
	;


mapEntrySelection
	: ENTRY LEFT_PAREN path RIGHT_PAREN
	;

dynamicInstantiation
	: NEW dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
	;

dynamicInstantiationTarget
	: LIST
	| MAP
	| dotIdentifierSequence
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	dynamicInstantiationArgExpression (AS? identifier)?
	;

dynamicInstantiationArgExpression
	:	expression
	|	dynamicInstantiation
	;

jpaSelectObjectSyntax
	:	OBJECT LEFT_PAREN identifier RIGHT_PAREN
	;




// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	:	WHERE predicate
	;

predicate
	: LEFT_PAREN predicate RIGHT_PAREN						# GroupedPredicate
	| predicate OR predicate								# OrPredicate
	| predicate AND predicate								# AndPredicate
	| NOT predicate											# NegatedPredicate
	| expression IS (NOT)? NULL								# IsNullPredicate
	| expression IS (NOT)? EMPTY							# IsEmptyPredicate
	| expression EQUAL expression							# EqualityPredicate
	| expression NOT_EQUAL expression						# InequalityPredicate
	| expression GREATER expression							# GreaterThanPredicate
	| expression GREATER_EQUAL expression					# GreaterThanOrEqualPredicate
	| expression LESS expression							# LessThanPredicate
	| expression LESS_EQUAL expression						# LessThanOrEqualPredicate
	| expression (NOT)? IN inList							# InPredicate
	| expression (NOT)? BETWEEN expression AND expression	# BetweenPredicate
	| expression (NOT)? LIKE expression (likeEscape)?		# LikePredicate
	| MEMBER OF path										# MemberOfPredicate
	;

inList
	: ELEMENTS? LEFT_PAREN dotIdentifierSequence RIGHT_PAREN		# PersistentCollectionReferenceInList
	| LEFT_PAREN expression (COMMA expression)*	RIGHT_PAREN			# ExplicitTupleInList
	| expression													# SubQueryInList
	;

likeEscape
	: ESCAPE expression
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Expressions

expression
	: expression DOUBLE_PIPE expression			# ConcatenationExpression
	| expression PLUS expression				# AdditionExpression
	| expression MINUS expression				# SubtractionExpression
	| expression ASTERISK expression			# MultiplicationExpression
	| expression SLASH expression				# DivisionExpression
	| expression PERCENT expression				# ModuloExpression
	// todo (6.0) : should these unary plus/minus rules only apply to literals?
	//		if so, move the MINUS / PLUS recognition to the `literal` rule
	//		specificcally for numeric literals
	| MINUS expression							# UnaryMinusExpression
	| PLUS expression							# UnaryPlusExpression
	| caseStatement								# CaseExpression
	| coalesce									# CoalesceExpression
	| nullIf									# NullIfExpression
	| literal									# LiteralExpression
	| parameter									# ParameterExpression
	| entityTypeReference						# EntityTypeExpression
	| path										# PathExpression
	| function									# FunctionExpression
	| LEFT_PAREN querySpec RIGHT_PAREN		    # SubQueryExpression
	;

entityTypeReference
	: TYPE LEFT_PAREN (path | parameter) RIGHT_PAREN
	;

entityLiteralReference
	:
	;

caseStatement
	: simpleCaseStatement
	| searchedCaseStatement
	;

simpleCaseStatement
	: CASE expression (simpleCaseWhen)+ (caseOtherwise)? END
	;

simpleCaseWhen
	: WHEN expression THEN expression
	;

caseOtherwise
	: ELSE expression
	;

searchedCaseStatement
	: CASE (searchedCaseWhen)+ (caseOtherwise)? END
	;

searchedCaseWhen
	: WHEN predicate THEN expression
	;

coalesce
	: COALESCE LEFT_PAREN expression (COMMA expression)+ RIGHT_PAREN
	;

nullIf
	: NULLIF LEFT_PAREN expression COMMA expression RIGHT_PAREN
	;

literal
	: STRING_LITERAL
	| CHARACTER_LITERAL
	| INTEGER_LITERAL
	| LONG_LITERAL
	| BIG_INTEGER_LITERAL
	| FLOAT_LITERAL
	| DOUBLE_LITERAL
	| BIG_DECIMAL_LITERAL
	| HEX_LITERAL
	| OCTAL_LITERAL
	| NULL
	| TRUE
	| FALSE
	| timestampLiteral
	| dateLiteral
	| timeLiteral
	;

// todo (6.0) : expand temporal literal support to Java 8 temporal types
//		* Instant 			-> {instant '...'}
//		* LocalDate 		-> {localDate '...'}
//		* LocalDateTime 	-> {localDateTime '...'}
//		* OffsetDateTime 	-> {offsetDateTime '...'}
//		* OffsetTime 		-> {offsetTime '...'}
//		* ZonedDateTime 	-> {localDate '...'}
//		* ...
//
// Few things:
//		1) the markers above are just initial thoughts.  They are obviously verbose.  Maybe acronyms or shortened forms would be better
//		2) we may want to stay away from all of the timezone headaches by not supporting local, zoned and offset forms



timestampLiteral
	: TIMESTAMP_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

dateLiteral
	: DATE_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

timeLiteral
	: TIME_ESCAPE_START dateTimeLiteralText RIGHT_BRACE
	;

dateTimeLiteralText
	: STRING_LITERAL | CHARACTER_LITERAL
	;

parameter
	: COLON identifier					# NamedParameter
	| QUESTION_MARK INTEGER_LITERAL?	# PositionalParameter
	;

function
	: standardFunction
	| aggregateFunction
	| jpaCollectionFunction
	| hqlCollectionFunction
	| jpaNonStandardFunction
	| nonStandardFunction
	;

jpaNonStandardFunction
	: FUNCTION LEFT_PAREN jpaNonStandardFunctionName (COMMA nonStandardFunctionArguments)? RIGHT_PAREN
	;

jpaNonStandardFunctionName
	: STRING_LITERAL
	;

nonStandardFunction
	: nonStandardFunctionName LEFT_PAREN nonStandardFunctionArguments? RIGHT_PAREN
	;

nonStandardFunctionName
	: dotIdentifierSequence
	;

nonStandardFunctionArguments
	: expression (COMMA expression)*
	;

jpaCollectionFunction
	: SIZE LEFT_PAREN path RIGHT_PAREN					# CollectionSizeFunction
	| INDEX LEFT_PAREN identifier RIGHT_PAREN			# CollectionIndexFunction
	;

hqlCollectionFunction
	: MAXINDEX LEFT_PAREN path RIGHT_PAREN				# MaxIndexFunction
	| MAXELEMENT LEFT_PAREN path RIGHT_PAREN			# MaxElementFunction
	| MININDEX LEFT_PAREN path RIGHT_PAREN				# MinIndexFunction
	| MINELEMENT LEFT_PAREN path RIGHT_PAREN			# MinElementFunction
	;

aggregateFunction
	: avgFunction
	| sumFunction
	| minFunction
	| maxFunction
	| countFunction
	;

avgFunction
	: AVG LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

sumFunction
	: SUM LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

minFunction
	: MIN LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

maxFunction
	: MAX LEFT_PAREN DISTINCT? expression RIGHT_PAREN
	;

countFunction
	: COUNT LEFT_PAREN DISTINCT? (expression | ASTERISK) RIGHT_PAREN
	;

standardFunction
	:	castFunction
	|	concatFunction
	|	substringFunction
	|	trimFunction
	|	upperFunction
	|	lowerFunction
	|	lengthFunction
	|	locateFunction
	|	absFunction
	|	sqrtFunction
	|	modFunction
	|   strFunction
	|	currentDateFunction
	|	currentTimeFunction
	|	currentTimestampFunction
	|	currentInstantFunction
	|	extractFunction
	|	positionFunction
	|	charLengthFunction
	|	octetLengthFunction
	|	bitLengthFunction
	;


castFunction
	: CAST LEFT_PAREN expression AS castTarget RIGHT_PAREN
	;

castTarget
	// todo (6.0) : should we use the `identifier` rule instead of the IDENTIFIER token?
	//		- depends on whether we see need to support any key/reserved words as such a target
	: IDENTIFIER
	| INTEGER_LITERAL
	;

concatFunction
	: CONCAT LEFT_PAREN expression (COMMA expression)+ RIGHT_PAREN
	;

substringFunction
	: (SUBSTRING | SUBSTR) LEFT_PAREN expression COMMA substringFunctionStartArgument (COMMA substringFunctionLengthArgument)? RIGHT_PAREN
	;

substringFunctionStartArgument
	: expression
	;

substringFunctionLengthArgument
	: expression
	;

trimFunction
	: TRIM LEFT_PAREN trimSpecification? trimCharacter? FROM? expression RIGHT_PAREN
	;

trimSpecification
	: LEADING
	| TRAILING
	| BOTH
	;

trimCharacter
	: CHARACTER_LITERAL | STRING_LITERAL
	;

upperFunction
	: UPPER LEFT_PAREN expression RIGHT_PAREN
	;

lowerFunction
	: LOWER LEFT_PAREN expression RIGHT_PAREN
	;

lengthFunction
	: LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

locateFunction
	: LOCATE LEFT_PAREN locateFunctionSubstrArgument COMMA locateFunctionStringArgument (COMMA locateFunctionStartArgument)? RIGHT_PAREN
	;

locateFunctionSubstrArgument
	: expression
	;

locateFunctionStringArgument
	: expression
	;

locateFunctionStartArgument
	: expression
	;

absFunction
	:	ABS LEFT_PAREN expression RIGHT_PAREN
	;

sqrtFunction
	:	SQRT LEFT_PAREN expression RIGHT_PAREN
	;

modFunction
	:	MOD LEFT_PAREN modDividendArgument COMMA modDivisorArgument RIGHT_PAREN
	;

strFunction
    :   STR LEFT_PAREN expression RIGHT_PAREN
    ;

modDividendArgument
	: expression
	;

modDivisorArgument
	: expression
	;

currentDateFunction
	: CURRENT_DATE (LEFT_PAREN RIGHT_PAREN)?
	;

currentTimeFunction
	: CURRENT_TIME (LEFT_PAREN RIGHT_PAREN)?
	;

currentTimestampFunction
	: CURRENT_TIMESTAMP (LEFT_PAREN RIGHT_PAREN)?
	;

currentInstantFunction
	: CURRENT_INSTANT (LEFT_PAREN RIGHT_PAREN)?
	;

extractFunction
	: EXTRACT LEFT_PAREN extractField FROM expression RIGHT_PAREN
	;

extractField
	: datetimeField
	| timeZoneField
	;

datetimeField
	: nonSecondDatetimeField
	| SECOND
	;

nonSecondDatetimeField
	: YEAR
	| MONTH
	| DAY
	| HOUR
	| MINUTE
	;

timeZoneField
	: TIMEZONE_HOUR
	| TIMEZONE_MINUTE
	;

positionFunction
	: POSITION LEFT_PAREN positionSubstrArgument IN positionStringArgument RIGHT_PAREN
	;

positionSubstrArgument
	: expression
	;

positionStringArgument
	: expression
	;

charLengthFunction
	: CAST LEFT_PAREN expression RIGHT_PAREN
	;

octetLengthFunction
	: OCTET_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;

bitLengthFunction
	: BIT_LENGTH LEFT_PAREN expression RIGHT_PAREN
	;





// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Path structures

dotIdentifierSequence
	: identifier dotIdentifierSequenceContinuation*
	;

dotIdentifierSequenceContinuation
	: DOT identifier
	;

/**
 * A path which needs to be resolved semantically.  This recognizes
 * any path-like structure.  Generally, the path is semantically
 * interpreted by the consumer of the parse-tree.  However, there
 * are certain cases where we can syntactically recognize a navigable
 * path; see `syntacticNavigablePath` rule
 *
 */
path
	: syntacticNavigablePath
	| generalPathFragment
	;

/**
 * Rule for cases where we syntactically know that the path is a
 * navigable path because it is one of these special cases:
 *
 * 		* TREAT( path )
 * 		* ELEMENTS( path )
 *		* VALUE( path )
 * 		* KEY( path )
 * 		* path[ selector ]
 */
syntacticNavigablePath
	: treatedNavigablePath
	| collectionElementNavigablePath
	| mapKeyNavigablePath
	| dotIdentifierSequence indexedPathAccessFragment
	;


treatedNavigablePath
	: TREAT LEFT_PAREN path AS dotIdentifierSequence RIGHT_PAREN (DOT generalPathFragment)?
	;


collectionElementNavigablePath
	: (VALUE | ELEMENTS) LEFT_PAREN path RIGHT_PAREN (DOT generalPathFragment)?
	;

mapKeyNavigablePath
	: KEY LEFT_PAREN path RIGHT_PAREN (DOT generalPathFragment)?
	;

indexedPathAccessFragment
	: LEFT_BRACKET expression RIGHT_BRACKET (DOT generalPathFragment)?
	;


/**
 * The main path rule.  Recognition for all normal path structures including
 * class, field and enum references as well as navigable paths.
 *
 * NOTE : this rule does *not* cover the special syntactic navigable path
 * cases: TREAT, KEY, ELEMENTS, VALUES
 */
generalPathFragment
	: dotIdentifierSequence (indexedPathAccessFragment)?
	;



/**
 * The `identifier` is used to provide "keyword as identifier" handling.
 *
 * The lexer hands us recognized keywords using their specific tokens.  This is important
 * for the recognition of sqm structure, especially in terms of performance!
 *
 * However we want to continue to allow users to use most keywords as identifiers (e.g., attribute names).
 * This parser rule helps with that.  Here we expect that the caller already understands their
 * context enough to know that keywords-as-identifiers are allowed.
 */
identifier
	: IDENTIFIER
	| (ABS
	| ALL
	| AND
	| ANY
	| AS
	| ASC
	| AVG
	| BY
	| BETWEEN
	| BIT_LENGTH
	| BOTH
	| CAST
	| COALESCE
	| COLLATE
	| CONCAT
	| COUNT
	| CROSS
	| DAY
	| DELETE
	| DESC
	| DISTINCT
	| ELEMENTS
	| ENTRY
	| FROM
	| FULL
	| FUNCTION
	| GROUP
	| HOUR
	| IN
	| INDEX
	| INNER
	| INSERT
	| JOIN
	| KEY
	| LEADING
	| LEFT
	| LENGTH
	| LIKE
	| LIST
	| LIMIT
	| LOWER
	| MAP
	| MAX
	| MIN
	| MINUTE
	| MEMBER
	| MONTH
	| OBJECT
	| OFFSET
	| ON
	| OR
	| ORDER
	| OUTER
	| POSITION
	| RIGHT
	| SELECT
	| SECOND
	| SET
	| SQRT
	| STR
	| SUBSTRING
	| SUM
	| TRAILING
	| TREAT
	| UPDATE
	| UPPER
	| VALUE
	| WHERE
	| WITH
	| YEAR) {
	    logUseOfReservedWordAsIdentifier(getCurrentToken());
	}
	;

