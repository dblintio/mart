package io.inviscid.qan.enums;

import org.apache.calcite.sql.SqlNode;

/**
 * Created by rvenkatesh on 9/9/18.
 */
public interface QueryType {
  boolean isPassed(SqlNode sqlNode);
}
