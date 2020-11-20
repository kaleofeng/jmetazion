package com.metazion.jm.db;

import com.metazion.jm.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class Table<T> {

    public class TableField {
        public static final int TYPE_INTEGER = 1;
        public static final int TYPE_STRING = 2;
        public static final int TYPE_TIMESTAMP = 3;

        public String columnName = "";
        public String fieldName = "";
        public int type = 0;
    }

    private String tableName = "";
    private TableField primaryField = new TableField();
    private ArrayList<TableField> tableFields = new ArrayList<TableField>();

    private String selectAllSql = "";
    private String selectSql = "";
    private String insertSql = "";
    private String updateSql = "";
    private String deleteSql = "";

    public void load(String path) throws Exception {
        String absolutePath = FileUtil.getAbsolutePath(path);
        InputStream is = new FileInputStream(absolutePath);

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(is);

        is.close();

        Element root = document.getRootElement();

        tableName = root.attributeValue("name");
        primaryField.columnName = root.attributeValue("primaryKey");
        primaryField.fieldName = root.attributeValue("primaryField");

        @SuppressWarnings("rawtypes")
        Iterator iter = root.elementIterator();
        while (iter.hasNext()) {
            Element element = (Element) iter.next();
            String name = element.attributeValue("name");
            String field = element.attributeValue("field");
            int type = Integer.parseInt(element.attributeValue("type"));

            TableField tableField = new TableField();
            tableField.columnName = name;
            tableField.fieldName = field;
            tableField.type = type;
            tableFields.add(tableField);
        }

        selectAllSql = generateSelectAllSql();
        selectSql = generateSelectSql();
        insertSql = generateInsertSql();
        updateSql = generateUpdateSql();
        deleteSql = generateDeleteSql();
    }

    public Record<T> createRecord() {
        return new Record<T>(this);
    }

    public String getTableName() {
        return tableName;
    }

    public TableField getPrimaryField() {
        return primaryField;
    }

    public ArrayList<TableField> getNoramlFields() {
        return tableFields;
    }

    public String getSelectAllSql() {
        return selectAllSql;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public String getInsertSql() {
        return insertSql;
    }

    public String getUpdateSql() {
        return updateSql;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    private String generateSelectAllSql() {
        String sql = "SELECT " + primaryField.columnName;
        int size = tableFields.size();
        for (int index = 0; index < size; ++index) {
            TableField tableField = tableFields.get(index);
            String conjunction = ",";
            String colSql = tableField.columnName;
            sql = sql + conjunction + colSql;
        }

        sql = sql + " FROM " + tableName;
        return sql;
    }

    private String generateSelectSql() {
        String sql = "SELECT " + primaryField.columnName;
        int size = tableFields.size();
        for (int index = 0; index < size; ++index) {
            TableField tableField = tableFields.get(index);
            String conjunction = ",";
            String colSql = tableField.columnName;
            sql = sql + conjunction + colSql;
        }

        sql = sql + " FROM " + tableName;
        sql = sql + " WHERE " + primaryField.columnName + "=?";
        return sql;
    }

    private String generateInsertSql() {
        String sql = "INSERT INTO " + tableName;

        String fieldSql = "(" + primaryField.columnName + ",";
        String valueSql = " VALUES(?,";
        int size = tableFields.size();
        for (int index = 0; index < size; ++index) {
            TableField tableField = tableFields.get(index);
            String conjunction = index == 0 ? "" : ",";
            fieldSql = fieldSql + conjunction + tableField.columnName;
            valueSql = valueSql + conjunction + "?";
        }

        fieldSql += ")";
        valueSql += ")";

        sql = sql + fieldSql + valueSql;
        return sql;
    }

    private String generateUpdateSql() {
        String sql = "UPDATE " + tableName + " SET ";
        int size = tableFields.size();
        for (int index = 0; index < size; ++index) {
            TableField tableField = tableFields.get(index);
            String conjunction = index == 0 ? "" : ",";
            String colSql = tableField.columnName + " = ?";
            sql = sql + conjunction + colSql;
        }

        sql = sql + " WHERE " + primaryField.columnName + "=?";
        return sql;
    }

    private String generateDeleteSql() {
        String sql = "DELETE FROM " + tableName;
        sql = sql + " WHERE " + primaryField.columnName + "=?";
        return sql;
    }
}
