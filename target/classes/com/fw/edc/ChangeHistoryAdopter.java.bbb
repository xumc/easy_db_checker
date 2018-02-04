package com.fw.edc;

import com.github.shyiko.mysql.binlog.event.EventType;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

/**
 * Created by mxu2 on 9/10/15.
 */
public class ChangeHistoryAdopter {
    private UI ui;
    private String env = "NO";
    private String sessionID = null;
    private boolean running = false;

    private static String STG_URL = "";
    private static String PROD_URL = "";
    
    public ChangeHistoryAdopter(UI ui, String env, String sessionID) {
        this.ui = ui;
        this.env = env;
        this.sessionID = sessionID;
    }

    public void clear() {
        this.env = "NO";
        this.sessionID = null;
    }

    public void begin() {
        new Thread(new Runnable() {
            public void run() {
                running = true;
                Connection connection = null;
                Statement stmt = null;
                ResultSet results = null;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    String url = "jdbc:mysql://localhost:3306/change_history?user=***&password=***";
                    if("STG".equals(env)){
                    	url = STG_URL;
                    }else{
                    	url = PROD_URL;
                    }
                    connection = DriverManager.getConnection(url);
                    String query = "show tables";
                    stmt = connection.createStatement();
                    results = stmt.executeQuery(query);
                    String currentTable = getCurrentTable(results);
                    int lastRecordId = getLastRecordId(stmt, currentTable);
                    while (running) {
                        query = "select * from " + currentTable + " where session_id='" + sessionID + "' and id > " + lastRecordId + " order by id asc";
                        System.out.println(query);
                        results = stmt.executeQuery(query);
                        while (results.next()) {
                            String json = results.getString("content");

                            List<PackagedEvent> packagedEvents = parseRecordJson(json);
                            ui.displayEvent(packagedEvents);

                            lastRecordId = results.getInt("id");
                        }

                        Thread.sleep(1000);
                    }

                    stmt.close();
                    results.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ui, "connect failed, please connect to US via VPN");
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
        }).start();
    }

    private List<PackagedEvent> parseRecordJson(String jsonText) {
        List<PackagedEvent> packagedEvents = new ArrayList<PackagedEvent>();
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory() {
            public List creatArrayContainer() {
                return new LinkedList();
            }

            public Map createObjectContainer() {
                return new LinkedHashMap();
            }

        };

        try {
            Map json = (Map) parser.parse(jsonText, containerFactory);
            Iterator iter = json.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ("changeset".equals(entry.getKey())) {
                    List changes = (List) entry.getValue();
                    Iterator listIter = changes.listIterator();
                    while (listIter.hasNext()) {
                        PackagedEvent packagedEvent = new PackagedEvent();

                        Map map = (Map) listIter.next();

                        Iterator mapIter = map.entrySet().iterator();
                        List changesCollection = null;
                        while (mapIter.hasNext()) {
                            Map.Entry mapEntry = (Map.Entry) mapIter.next();
                            if ("action".equals(mapEntry.getKey())) {
                                packagedEvent.setType(PackagedEvent.convertType(mapEntry.getValue().toString()));
                            } else if ("table_name".equals(mapEntry.getKey())) {
                                packagedEvent.setTableName(mapEntry.getValue().toString());
                            } else if ("changes".equals(mapEntry.getKey())) {
                                changesCollection = (List) mapEntry.getValue();
                            } else if ("name".equals(mapEntry.getKey())) {
                                packagedEvent.setEntryName(mapEntry.getValue().toString());
                            } else if ("id".equals(mapEntry.getKey())) {
                                packagedEvent.setEntryId(mapEntry.getValue().toString());
                            }
                        }

                        packagedEvent.setDatabase("");

                        List<List<PackagedEvent.Field>> changesList = new ArrayList<List<PackagedEvent.Field>>();
                        List<PackagedEvent.Field> fieldList = new ArrayList<PackagedEvent.Field>();
                        Iterator itemIter = changesCollection.iterator();
                        String field = "", oldValue = "", newValue = "";
                        while (itemIter.hasNext()) {
                            List itemList = (List) itemIter.next();
                            field = itemList.get(0).toString();
                            if (packagedEvent.getType() == EventType.WRITE_ROWS) {
                                oldValue = "";
                                if (itemList.get(1) != null) {
                                    newValue = itemList.get(1).toString();
                                }
                            } else if (packagedEvent.getType() == EventType.UPDATE_ROWS) {
                                if (itemList.get(1) != null) {
                                    oldValue = itemList.get(1).toString();
                                }
                                if (itemList.get(2) != null) {
                                    newValue = itemList.get(2).toString();
                                }
                            } else if (packagedEvent.getType() == EventType.DELETE_ROWS) {
                                if (itemList.get(1) != null) {
                                    oldValue = itemList.get(1).toString();
                                }
                                newValue = "";
                            }
                            fieldList.add(new PackagedEvent.Field(field, oldValue, newValue));
                        }
                        changesList.add(fieldList);
                        packagedEvent.setRecordFields(changesList);
                        packagedEvents.add(packagedEvent);
                    }
                }
            }

        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return packagedEvents;
    }

    private int getLastRecordId(Statement stmt, String latestTable) throws SQLException {
        String query;
        ResultSet results;
        query = "select * from " + latestTable + " where session_id = '" + sessionID + "'order by id desc limit 1";
        results = stmt.executeQuery(query);
        int lastRecordId = -1;
        while (results.next()) {
            lastRecordId = results.getInt("id");
        }
        return lastRecordId;
    }

    public void stop() {
    	clear();
        running = false;
    }

    private String getCurrentTable(ResultSet results) throws SQLException {
        List<String> tables = new ArrayList<String>();
        while (results.next()) {
            String table = results.getString("Tables_in_fwmrm_change_history");
            Pattern p = Pattern.compile("change_history_\\d{6}");
            Matcher m = p.matcher(table);
            if (m.find()) {
                tables.add(table);
            }
        }
        Collections.sort(tables);
        return tables.get(tables.size() - 2);
    }
}
