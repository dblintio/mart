package io.inviscid.qan.visitors;

import io.inviscid.qan.planner.SqlMartLiteral;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlShuttle;

public class LiteralShuffle extends SqlShuttle {
  @Override
  public SqlNode visit(SqlLiteral literal) {
    return SqlMartLiteral.createLiteral(literal);
  }
}
