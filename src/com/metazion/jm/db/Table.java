package com.metazion.jm.db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.metazion.jm.util.FileUtil;

public class Table<T> {

	public class TableField {
		public static final int TYPE_INTEGER = 1;
		public static final int TYPE_STRING = 2;
		public static final int TYPE_TIMESTAMP = 3;

		public String columnName = "";
		public String fieldName = "";
		public int type = 0;
	}

	T object = null;

	private String tableName = "";
	TableField primaryField = new TableField();
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

	public void setObject(T object) {
		this.object = object;
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

	public void setSelectParams(PreparedStatement pst) throws Exception {
		setPrimaryParams(1, pst);
	}

	public void setInsertParams(PreparedStatement pst) throws Exception {
		int start = setPrimaryParams(1, pst);
		setNormalParams(start, pst);
	}

	public void setUpdateParams(PreparedStatement pst) throws Exception {
		int start = setNormalParams(1, pst);
		setPrimaryParams(start, pst);
	}

	public void setDeleteParams(PreparedStatement pst) throws Exception {
		setPrimaryParams(1, pst);
	}

	public int setPrimaryParams(int start, PreparedStatement pst) throws Exception {
		Object value = getFieldValue(primaryField);
		value = toDBValue(primaryField, value);
		pst.setObject(start, value);
		return start + 1;
	}

	public int setNormalParams(int start, PreparedStatement pst) throws Exception {
		int size = tableFields.size();
		for (int index = 0; index < size; ++index) {
			TableField tableField = tableFields.get(index);
			Object value = getFieldValue(tableField);
			value = toDBValue(tableField, value);
			pst.setObject(start + index, value);
		}
		return start + size;
	}

	public void setPrimaryResult(ResultSet rs) throws Exception {
		Object value = rs.getObject(primaryField.columnName);
		value = fromDBValue(primaryField, value);
		setFieldValue(primaryField, value);
	}

	public void setNormalResult(ResultSet rs) throws Exception {
		int size = tableFields.size();
		for (int index = 0; index < size; ++index) {
			TableField tableField = tableFields.get(index);
			Object value = rs.getObject(tableField.columnName);
			value = fromDBValue(tableField, value);
			setFieldValue(tableField, value);
		}
	}

	private Object getFieldValue(TableField tableField) throws Exception {
		Field field = object.getClass().getDeclaredField(tableField.fieldName);
		return field.get(object);
	}

	private void setFieldValue(TableField tableField, Object value) throws Exception {
		Field field = object.getClass().getDeclaredField(tableField.fieldName);
		field.set(object, value);
	}

	private Object toDBValue(TableField tableField, Object value) {
		if (tableField.type == TableField.TYPE_TIMESTAMP) {
			value = new Timestamp((long) value);
		}
		return value;
	}

	private Object fromDBValue(TableField tableField, Object value) {
		if (tableField.type == TableField.TYPE_TIMESTAMP) {
			value = ((Timestamp) value).getTime();
		}
		return value;
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