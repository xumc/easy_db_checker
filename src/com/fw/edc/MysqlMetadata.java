package com.fw.edc;

import java.sql.*;
import java.util.*;

/**
 * Created by mxu2 on 5/31/15.
 */
public class MysqlMetadata {
    private static MysqlMetadata instance = null;
    private Map<String, Map<String, List<MysqlField>>> cachedMetaDatas = null;

    private MysqlMetadata() {
        cachedMetaDatas = new HashMap<String, Map<String, List<MysqlField>>>();
        Connection connection = null;
        try {
            connection = getConnection(null);
            String query = "show master status";
            ResultSet results;
            Statement stmt = connection.createStatement();
            results = stmt.executeQuery(query);
            loadCacheData("Binlog_Do_DB", results);
            if (cachedMetaDatas.isEmpty()) {
                query = "show databases";
                results = stmt.executeQuery(query);
                loadCacheData("Database", results);
            }
            stmt.close();
            results.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqlExpt) {
                sqlExpt.printStackTrace();
            }
        }

    }

    public static MysqlMetadata getInstance() {
        if (instance == null) {
            instance = new MysqlMetadata();
        }
        return instance;
    }

    private void loadCacheData(String columnName, ResultSet results) throws SQLException {
        while (results.next()) {
            String[] databases = results.getString(columnName).split(",");
            for (String db : databases) {
                if (!"".equals(db)) {
                    cachedMetaDatas.put(db, null);
                }
            }
        }
    }

    private Connection getConnection(String database) {
        Connection connection = null;
        Config config = Config.getInstance();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            if (database == null) {
                database = "";
            }
            String url = "jdbc:mysql://" + config.getHostname() + ":" + config.getPort() + "/" + database + "?" +
                    "user=" + config.getUser() + "&" +
                    "password=" + config.getPassword();
            connection = DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public Object[] getDatabaseList() {
        return cachedMetaDatas.keySet().toArray();
    }

    public Set<String> getTableList(String database) {
        return getDbMetadata(database).keySet();
    }

    private Map<String, List<MysqlField>> getDbMetadata(String database) {
        Map<String, List<MysqlField>> dbMetadata = cachedMetaDatas.get(database);
        if (dbMetadata == null) {
            try {
                dbMetadata = getMetaData(database);
                cachedMetaDatas.put(database, dbMetadata);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return dbMetadata;
    }

    public List<MysqlField> getFieldList(String database, String tableName) {
        Map<String, List<MysqlField>> databaseMetadata = getDbMetadata(database);
        if (databaseMetadata != null) {
            return databaseMetadata.get(tableName);
        } else {
            return new ArrayList<MysqlField>();
        }

    }

    private Map<String, List<MysqlField>> getMetaData(String database) throws SQLException {
        Map map = new HashMap<String, List<String>>();
        Connection connection = getConnection(database);
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tableResultSet = metaData.getTables(null, "public", null, new String[]{"TABLE"});
            try {
                while (tableResultSet.next()) {
                    String tableName = tableResultSet.getString("TABLE_NAME");
                    Statement statement = connection.createStatement();
                    ResultSet tableDesc = statement.executeQuery("desc " + tableName + ";");
                    ResultSet columnResultSet = metaData.getColumns(null, "public", tableName, null);

                    List<MysqlField> list = new ArrayList<MysqlField>();
                    try {
                        while (columnResultSet.next()) {
                            String columnName = columnResultSet.getString("COLUMN_NAME");
                            String typeName = null;
                            while (tableDesc.next()) {
                                if (tableDesc.getString("Field").equals(columnName)) {
                                    typeName = tableDesc.getString("Type");
                                    break;
                                }
                            }
                            list.add(new MysqlField(database, tableName, columnName, typeName));
                        }
                    } finally {
                        columnResultSet.close();
                    }
                    map.put(tableName, list);
                }
            } finally {
                tableResultSet.close();
            }
        } finally {
            connection.close();
        }
        return map;
    }

}
