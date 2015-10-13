package com.metazion.jm.db;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.metazion.jm.db.Table.TableField;

public class Record<T> {

	private Table<T> table = null;
	private T object = null;

	public Record(Table<T> table) {
		this.table = table;
	}

	public void setObject(T object) {
		this.object = object;
	}

	public void setSelectParams(PreparedStatement pst) throws Exception {
		setPrimaryParams(1, pst);
	}

	public void setInsertParams(PreparedStatement pst) throws Exception {
		final int start = setPrimaryParams(1, pst);
		setNormalParams(start, pst);
	}

	public void setUpdateParams(PreparedStatement pst) throws Exception {
		final int start = setNormalParams(1, pst);
		setPrimaryParams(start, pst);
	}

	public void setDeleteParams(PreparedStatement pst) throws Exception {
		setPrimaryParams(1, pst);
	}

	public int setPrimaryParams(int start, PreparedStatement pst) throws Exception {
		Table<T>.TableField primaryField = table.getPrimaryField();

		Object value = getFieldValue(primaryField);
		value = toDBValue(primaryField, value);
		pst.setObject(start, value);
		return start + 1;
	}

	public int setNormalParams(int start, PreparedStatement pst) throws Exception {
		ArrayList<Table<T>.TableField> normalFields = table.getNoramlFields();

		final int size = normalFields.size();
		for (int index = 0; index < size; ++index) {
			Table<T>.TableField tableField = normalFields.get(index);
			Object value = getFieldValue(tableField);
			value = toDBValue(tableField, value);
			pst.setObject(start + index, value);
		}
		return start + size;
	}

	public void setPrimaryResult(ResultSet rs) throws Exception {
		Table<T>.TableField primaryField = table.getPrimaryField();

		Object value = rs.getObject(primaryField.columnName);
		value = fromDBValue(primaryField, value);
		setFieldValue(primaryField, value);
	}

	public void setNormalResult(ResultSet rs) throws Exception {
		ArrayList<Table<T>.TableField> normalFields = table.getNoramlFields();

		final int size = normalFields.size();
		for (int index = 0; index < size; ++index) {
			Table<T>.TableField tableField = normalFields.get(index);
			Object value = rs.getObject(tableField.columnName);
			value = fromDBValue(tableField, value);
			setFieldValue(tableField, value);
		}
	}

	private Object getFieldValue(Table<T>.TableField tableField) throws Exception {
		Field field = object.getClass().getDeclaredField(tableField.fieldName);
		return field.get(object);
	}

	private void setFieldValue(Table<T>.TableField tableField, Object value) throws Exception {
		Field field = object.getClass().getDeclaredField(tableField.fieldName);
		field.set(object, value);
	}

	private Object toDBValue(Table<T>.TableField tableField, Object value) {
		if (tableField.type == TableField.TYPE_TIMESTAMP) {
			value = new Timestamp((long) value);
		}
		return value;
	}

	private Object fromDBValue(Table<T>.TableField tableField, Object value) {
		if (tableField.type == TableField.TYPE_TIMESTAMP) {
			value = ((Timestamp) value).getTime();
		}
		return value;
	}
}