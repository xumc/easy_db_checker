package com.fw.edc;


import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by mxu2 on 6/1/15.
 */
public class Config {
    public static final String CONFIG_PATH = "/.simple_db_check_config.properties";
    private static Config instance = new Config();
    private String hostname;
    private int port;
    private String user;
    private String password;
    private String alwaysOnTop;
    private Rectangle bounds;
    private List<EventFilter> ignoreFilters;

    private Config() {
        Properties props = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(getConfigFile());
            props.load(in);
            hostname = props.getProperty("hostname", "localhost");
            port = Integer.parseInt(props.getProperty("port", "3306"));
            user = props.getProperty("user", "maui");
            password = props.getProperty("password", "fwadmin_maui");
            bounds = new Rectangle(
                    Integer.parseInt(props.getProperty("bounds.x", String.valueOf(Util.getScreenWidth() - UI.DEFAULT_LOCATION_X))),
                    Integer.parseInt(props.getProperty("bounds.y", String.valueOf(Util.getScreenWidth() - UI.DEFAULT_LOCATION_X))),
                    Integer.parseInt(props.getProperty("bounds.width", String.valueOf(UI.DEFAULT_LOCATION_X))),
                    Integer.parseInt(props.getProperty("bounds.height", String.valueOf(UI.DEFAULT_LOCATION_Y)))
            );
            alwaysOnTop = props.getProperty("ontop", "false");
            String ignoreFiltersStr = props.getProperty("ignoreFilters", null);
            ignoreFilters = new ArrayList<EventFilter>();
            if (ignoreFiltersStr != null && !"".equals(ignoreFiltersStr)) {
                for (String ignoreStr : ignoreFiltersStr.split(",")) {
                    String[] efs = ignoreStr.split("\\.");
                    EventFilter et = new EventFilter(efs[0], efs[1], efs[2]);
                    ignoreFilters.add(et);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Config getInstance() {
        return instance;
    }

    private static File getConfigFile() throws IOException {
        String userHome = System.getProperty("user.home");
        String configPath = userHome + CONFIG_PATH;

        File file = new File(configPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public void saveConfig() {
        Properties properties = new Properties();
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(getConfigFile());
            properties.setProperty("hostname", hostname);
            properties.setProperty("port", String.valueOf(port));
            properties.setProperty("user", user);
            properties.setProperty("password", password);
            properties.setProperty("bounds.x", String.valueOf((int) bounds.getX()));
            properties.setProperty("bounds.y", String.valueOf((int) bounds.getY()));
            properties.setProperty("bounds.width", String.valueOf((int) bounds.getWidth()));
            properties.setProperty("bounds.height", String.valueOf((int) bounds.getHeight()));
            properties.setProperty("ontop", alwaysOnTop);
            StringBuilder sb = new StringBuilder();
            for (EventFilter ef : ignoreFilters) {
                if (sb.length() != 0) {
                    sb.append(",");
                }
                sb.append(ef.toString());
            }
            properties.setProperty("ignoreFilters", sb.toString());
            properties.store(outputStream, "change config");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public List<EventFilter> getIgnoreFilters() {
        return ignoreFilters;
    }

    public void setIgnoreFilters(List<EventFilter> ignoreFilters) {
        this.ignoreFilters = ignoreFilters;
    }

	public boolean isAlwaysOnTop() {
		return "true".equals(alwaysOnTop);
	}

	public void setAlwaysOnTop(boolean alwaysOnTop) {
		this.alwaysOnTop = String.valueOf(alwaysOnTop);
	}
    
    
}
