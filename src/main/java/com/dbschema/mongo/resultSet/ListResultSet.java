package com.dbschema.mongo.resultSet;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.dbschema.mongo.SQLAlreadyClosedException;

public class ListResultSet implements ResultSet {
    private final List<Object[]> data;
    private String[] columnNames;
    private int currentRow = -1;
    private boolean isClosed = false;

    public ListResultSet() {
        this(new ArrayList<>(), new String[0]);
    }

    public ListResultSet(List<Object[]> data, String[] columnNames) {
        this.data = data;
        this.columnNames = columnNames;
    }

    public ListResultSet(Object value, String[] columnNames) {
        this.data = new ArrayList<>();
        this.data.add(new Object[] {value});
        this.columnNames = columnNames;
    }

    public void setColumnNames(String... columnNames) {
        this.columnNames = columnNames;
    }

    public void addRow(Object[] columnValues) {
        data.add(columnValues);
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    /**
     * @see java.sql.ResultSet#next()
     */
    @Override
    public boolean next() {
        if (data == null) {
            return false;
        }
        if (currentRow < data.size() - 1) {
            currentRow++;
            return true;
        }
        return false;
    }

    /**
     * @see java.sql.ResultSet#close()
     */
    @Override
    public void close() throws SQLAlreadyClosedException {
        checkClosed();
        this.isClosed = true;
    }

    private void checkClosed() throws SQLAlreadyClosedException {
        if (isClosed)
            throw new SQLAlreadyClosedException(this.getClass().getSimpleName());
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * @see java.sql.ResultSet#wasNull()
     */
    @Override
    public boolean wasNull() {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        if (currentRow >= data.size()) {
            throw new SQLException("ResultSet exhausted, request currentRow = " + currentRow);
        }

        if (currentRow == -1) {
            next();
        }

        int adjustedColumnIndex = columnIndex - 1;
        if (adjustedColumnIndex >= data.get(currentRow).length) {
            throw new SQLException("Column index does not exist: " + columnIndex);
        }
        final Object val = data.get(currentRow)[adjustedColumnIndex];
        return val != null ? val.toString() : null;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return Boolean.parseBoolean(getString(columnIndex));
    }

    @Override
    public byte getByte(int columnIndex) {

        return 0;
    }

    /**
     * @see java.sql.ResultSet#getShort(int)
     */
    @Override
    public short getShort(int columnIndex) throws SQLException {
        checkClosed();
        return Short.parseShort(getString(columnIndex));
    }

    /**
     * @see java.sql.ResultSet#getInt(int)
     */
    @Override
    public int getInt(int columnIndex) throws SQLException {
        checkClosed();
        return Integer.parseInt(getString(columnIndex));
    }

    /**
     * @see java.sql.ResultSet#getLong(int)
     */
    @Override
    public long getLong(int columnIndex) throws SQLException {
        checkClosed();
        return Long.parseLong(getString(columnIndex));
    }

    /**
     * @see java.sql.ResultSet#getFloat(int)
     */
    @Override
    public float getFloat(int columnIndex) throws SQLException {
        checkClosed();
        return Float.parseFloat(getString(columnIndex));
    }

    /**
     * @see java.sql.ResultSet#getDouble(int)
     */
    @Override
    public double getDouble(int columnIndex) throws SQLException {
        checkClosed();
        return Double.parseDouble(getString(columnIndex));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) {

        return null;
    }

    @Override
    public byte[] getBytes(int columnIndex) {

        return null;
    }

    @Override
    public Date getDate(int columnIndex) {

        return null;
    }

    @Override
    public Time getTime(int columnIndex) {

        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) {

        return null;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) {

        return null;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) {

        return null;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) {

        return null;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        checkClosed();
        int index = -1;
        if (columnNames == null) {
            throw new SQLException("Use of columnLabel requires setColumnNames to be called first.");
        }
        for (int i = 0; i < columnNames.length; i++) {
            if (columnLabel.equals(columnNames[i])) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new SQLException("Column " + columnLabel + " doesn't exist in this ResultSet");
        }
        return getString(index + 1);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        checkClosed();

        return Boolean.parseBoolean(getString(columnLabel));
    }

    @Override
    public byte getByte(String columnLabel) {

        return 0;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {

        return Short.parseShort(getString(columnLabel));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {

        return Integer.parseInt(getString(columnLabel));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {

        return Long.parseLong(getString(columnLabel));
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {

        return Float.parseFloat(getString(columnLabel));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return Double.parseDouble(getString(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {

        return new BigDecimal(getString(columnLabel).replace(",", ""));
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return getString(columnLabel).getBytes();
    }

    @Override
    public Date getDate(String columnLabel) {

        return null;
    }

    @Override
    public Time getTime(String columnLabel) {

        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) {

        return null;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) {

        return null;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) {
        return null;
    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {


    }

    @Override
    public String getCursorName() {

        return null;
    }

    /**
     * @see java.sql.ResultSet#getMetaData()
     */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkClosed();

        if (data == null) {
            return new MongoResultSetMetaData(null, new String[0], new int[0]);
        }

        int[] columnJavaTypes = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columnJavaTypes[i] = Types.OTHER;
        }

        return new MongoResultSetMetaData(null, columnNames, columnJavaTypes);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        if (currentRow >= data.size()) {
            throw new SQLException("ResultSet exhausted, request currentRow = " + currentRow);
        }
        int adjustedColumnIndex = columnIndex - 1;
        if (adjustedColumnIndex >= data.get(currentRow).length) {
            throw new SQLException("Column index does not exist: " + columnIndex);
        }
        return data.get(currentRow)[adjustedColumnIndex];
    }

    @Override
    public Object getObject(String columnLabel) {

        return null;
    }

    @Override
    public int findColumn(String columnLabel) {

        return 0;
    }

    @Override
    public Reader getCharacterStream(int columnIndex) {

        return null;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) {

        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) {

        return null;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {

        return new BigDecimal(getString(columnLabel).replace(",", ""));
    }

    @Override
    public boolean isBeforeFirst() {

        return false;
    }

    @Override
    public boolean isAfterLast() {

        return false;
    }

    @Override
    public boolean isFirst() {

        return false;
    }

    @Override
    public boolean isLast() {

        return false;
    }

    @Override
    public void beforeFirst() {


    }

    @Override
    public void afterLast() {


    }

    @Override
    public boolean first() {

        return false;
    }

    @Override
    public boolean last() {

        return false;
    }

    @Override
    public int getRow() {

        return 0;
    }

    @Override
    public boolean absolute(int row) {

        return false;
    }

    @Override
    public boolean relative(int rows) {

        return false;
    }

    @Override
    public boolean previous() {

        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getFetchDirection() {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) {

    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    /**
     * @see java.sql.ResultSet#getType()
     */
    @Override
    public int getType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    /**
     * @see java.sql.ResultSet#getConcurrency()
     */
    @Override
    public int getConcurrency() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean rowUpdated() {

        return false;
    }

    @Override
    public boolean rowInserted() {

        return false;
    }

    @Override
    public boolean rowDeleted() {

        return false;
    }

    @Override
    public void updateNull(int columnIndex) {


    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) {


    }

    @Override
    public void updateByte(int columnIndex, byte x) {


    }

    @Override
    public void updateShort(int columnIndex, short x) {


    }

    @Override
    public void updateInt(int columnIndex, int x) {


    }

    @Override
    public void updateLong(int columnIndex, long x) {


    }

    @Override
    public void updateFloat(int columnIndex, float x) {


    }

    @Override
    public void updateDouble(int columnIndex, double x) {


    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) {


    }

    @Override
    public void updateString(int columnIndex, String x) {


    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) {


    }

    @Override
    public void updateDate(int columnIndex, Date x) {


    }

    @Override
    public void updateTime(int columnIndex, Time x) {


    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) {


    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) {


    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) {


    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) {


    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) {


    }

    @Override
    public void updateObject(int columnIndex, Object x) {


    }

    @Override
    public void updateNull(String columnLabel) {


    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) {


    }

    @Override
    public void updateByte(String columnLabel, byte x) {


    }

    @Override
    public void updateShort(String columnLabel, short x) {


    }

    @Override
    public void updateInt(String columnLabel, int x) {


    }

    @Override
    public void updateLong(String columnLabel, long x) {


    }

    @Override
    public void updateFloat(String columnLabel, float x) {


    }

    @Override
    public void updateDouble(String columnLabel, double x) {


    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) {


    }

    @Override
    public void updateString(String columnLabel, String x) {


    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) {


    }

    @Override
    public void updateDate(String columnLabel, Date x) {


    }

    @Override
    public void updateTime(String columnLabel, Time x) {


    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) {


    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) {


    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) {


    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) {


    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) {


    }

    @Override
    public void updateObject(String columnLabel, Object x) {


    }

    @Override
    public void insertRow() {


    }

    @Override
    public void updateRow() {


    }

    @Override
    public void deleteRow() {


    }

    @Override
    public void refreshRow() {


    }

    @Override
    public void cancelRowUpdates() {


    }

    @Override
    public void moveToInsertRow() {


    }

    @Override
    public void moveToCurrentRow() {


    }

    /**
     * @see java.sql.ResultSet#getStatement()
     */
    @Override
    public Statement getStatement() {
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) {

        return null;
    }

    @Override
    public Ref getRef(int columnIndex) {

        return null;
    }

    @Override
    public Blob getBlob(int columnIndex) {

        return null;
    }

    @Override
    public Clob getClob(int columnIndex) {

        return null;
    }

    @Override
    public Array getArray(int columnIndex) {

        return null;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) {

        return null;
    }

    @Override
    public Ref getRef(String columnLabel) {

        return null;
    }

    @Override
    public Blob getBlob(String columnLabel) {

        return null;
    }

    @Override
    public Clob getClob(String columnLabel) {

        return null;
    }

    @Override
    public Array getArray(String columnLabel) {

        return null;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) {

        return null;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) {

        return null;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) {

        return null;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) {

        return null;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) {

        return null;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) {

        return null;
    }

    @Override
    public URL getURL(int columnIndex) {

        return null;
    }

    @Override
    public URL getURL(String columnLabel) {

        return null;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) {


    }

    @Override
    public void updateRef(String columnLabel, Ref x) {


    }

    @Override
    public void updateBlob(int columnIndex, Blob x) {


    }

    @Override
    public void updateBlob(String columnLabel, Blob x) {


    }

    @Override
    public void updateClob(int columnIndex, Clob x) {


    }

    @Override
    public void updateClob(String columnLabel, Clob x) {


    }

    @Override
    public void updateArray(int columnIndex, Array x) {


    }

    @Override
    public void updateArray(String columnLabel, Array x) {


    }

    @Override
    public RowId getRowId(int columnIndex) {

        return null;
    }

    @Override
    public RowId getRowId(String columnLabel) {

        return null;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) {


    }

    @Override
    public void updateRowId(String columnLabel, RowId x) {


    }

    @Override
    public int getHoldability() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        checkClosed();
    }

    @Override
    public void updateNString(String columnLabel, String nString) {


    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) {


    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) {


    }

    @Override
    public NClob getNClob(int columnIndex) {

        return null;
    }

    @Override
    public NClob getNClob(String columnLabel) {

        return null;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) {

        return null;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) {

        return null;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) {


    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) {


    }

    @Override
    public String getNString(int columnIndex) {

        return null;
    }

    @Override
    public String getNString(String columnLabel) {

        return null;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) {

        return null;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) {

        return null;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) {


    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) {


    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) {


    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) {


    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) {


    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) {


    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) {


    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) {


    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) {


    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) {


    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) {


    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) {


    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) {


    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) {


    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) {


    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) {


    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) {


    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) {


    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) {


    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) {


    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) {


    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) {


    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) {


    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) {


    }

    @Override
    public void updateClob(int columnIndex, Reader reader) {


    }

    @Override
    public void updateClob(String columnLabel, Reader reader) {


    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) {


    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) {


    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) {
        return null;
    }
}
