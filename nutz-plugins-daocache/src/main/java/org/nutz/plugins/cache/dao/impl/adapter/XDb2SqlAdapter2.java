package org.nutz.plugins.cache.dao.impl.adapter;

import java.util.ArrayList;
import java.util.List;

import org.nutz.plugins.cache.dao.XSqlAdapter;

import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.db2.visitor.DB2ASTVisitorAdapter;

/**
 * 通过Druid的遍历器获取sql所操作的数据库表
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class XDb2SqlAdapter2 extends DB2ASTVisitorAdapter implements XSqlAdapter {

    protected List<String> tableNames = new ArrayList<String>();

    public boolean visit(SQLExprTableSource x) {
    	tableNames.add(x.toString());
        return super.visit(x);
    }

    public List<String> getTableNames() {
        return tableNames;
    }
}
