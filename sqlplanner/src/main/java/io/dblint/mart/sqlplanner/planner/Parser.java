package io.dblint.mart.sqlplanner.planner;

import io.dblint.mart.sqlplanner.visitors.LiteralShuffle;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserImplFactory;
import org.apache.calcite.sql.parser.babel.SqlBabelParserImpl;
import org.apache.calcite.sql.validate.SqlConformance;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Parser {
  private static Logger logger = LoggerFactory.getLogger(Parser.class);

  protected final SqlParserImplFactory factory;

  protected final Quoting quoting;
  protected final Casing unquotedCasing;
  protected final Casing quotedCasing;
  protected final SqlConformance conformance;

  /**
   * Create a SQL Parser based on Apache Calcite.
   * @param factory Implementation Factory
   * @param quoting Quoting character.
   * @param unquotedCasing Case when object is not quoted
   * @param quotedCasing Case when object is quoted
   * @param conformance Parser conformance rules
   */
  public Parser(SqlParserImplFactory factory, Quoting quoting,
                Casing unquotedCasing, Casing quotedCasing, SqlConformance conformance) {
    this.factory = factory;
    this.quoting = quoting;
    this.unquotedCasing = unquotedCasing;
    this.quotedCasing = quotedCasing;
    this.conformance = conformance;
  }

  /**
   * Sole Constructor for a SQL Parser based on Apache Calcite.
   * Uses default values for various parameters for the Calcite Parser.
   */
  public Parser(SqlParserImplFactory factory) {
    this.factory = factory;
    this.quoting = Quoting.DOUBLE_QUOTE;
    this.unquotedCasing = Casing.TO_UPPER;
    this.quotedCasing = Casing.UNCHANGED;
    this.conformance = SqlConformanceEnum.LENIENT;
  }

  public Parser() {
    this(SqlBabelParserImpl.FACTORY);
  }

  private SqlParser getParser(String sql) {
    return SqlParser.create(sql,
        SqlParser.configBuilder()
            .setParserFactory(factory)
            .setQuoting(quoting)
            .setUnquotedCasing(unquotedCasing)
            .setQuotedCasing(quotedCasing)
            .setConformance(conformance)
            .setCaseSensitive(false)
            .build());
  }


  private String trim(String sql) {
    sql = sql.trim();
    List<Character> chars = Arrays.asList(';');
    boolean found = true;
    while (found) {
      found = false;
      for (Character character : chars) {
        if (sql.charAt(sql.length() - 1) == character) {
          sql = sql.substring(0, sql.length() - 1);
          found = true;
        }
      }
    }
    return sql;
  }

  private String handleNewLine(String sql) {
    return sql.replaceAll("\\\\n", "\n").replaceAll("\\r", "\r");
  }

  /**
   * Parse a SQL string using Apache Calcite.
   * @param sql String containing the SQL query
   * @return SqlNode as Root of the parse tree
   * @throws SqlParseException A parse exception if parsing fails
   */
  public SqlNode parse(String sql) throws SqlParseException {
    String processedSql = trim(handleNewLine(sql));
    try {
      return getParser(processedSql).parseStmt();
    } catch (SqlParseException parseExc) {
      logger.error(processedSql);
      throw parseExc;
    }
  }

  /**
   * Create a query digest by replacing all constants.
   * @param sql SQL Query
   * @param dialect SQL dialect to use. For e.g. MySQL
   * @return A string containing the query digest
   * @throws SqlParseException If there is SQLParseException
   */
  public String digest(String sql, SqlDialect dialect) throws SqlParseException {
    final SqlNode sqlNode = parse(sql);
    LiteralShuffle shuffle = new LiteralShuffle();
    final SqlNode replaced = sqlNode.accept(shuffle);
    return replaced.toSqlString(dialect).getSql();
  }

  /**
   * Pretty Print a SQL Query.
   * @param sql SQL Query
   * @param dialect SQL dialect to use. For e.g. MySQL
   * @return A string containing the pretty printed query
   * @throws SqlParseException If there is SQLParseException
   */
  public String pretty(String sql, SqlDialect dialect) throws SqlParseException {
    return parse(sql).toSqlString(dialect).getSql();
  }
}
