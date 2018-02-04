package com.fw.edc;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mxu2 on 5/31/15.
 */
public class UI extends JFrame implements WindowListener {
    public static final int DEFAULT_LOCATION_X = 610;
    public static final int DEFAULT_LOCATION_Y = 300;
    private JTextPane textAreaPane;
    private JPanel pane = null;
    private JScrollPane scrollPane = null;
    private BinaryLogClient binClient;
    private EventListener eventListener;
    private Provider provider;

    private List<PackagedEvent> globalPackagedEvent = new ArrayList<PackagedEvent>();

    public UI() {
        registerClearHotKey();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                initUI();
            }
        });
        buildBinlogConnection();
    }

    private void initUI() {
    	boolean alwaysOnTop = Config.getInstance().isAlwaysOnTop();
    	if(alwaysOnTop){
          this.setAlwaysOnTop(true);
    	}
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle bounds = Config.getInstance().getBounds();
        this.setBounds(bounds);
        this.setResizable(true);

        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.LEFT, -5, 0));
       
        textAreaPane = new JTextPane();
        Font font = new Font("Monaco", Font.PLAIN, 12);
        textAreaPane.setFont(font);
        textAreaPane.setEditable(false);
        textAreaPane.setSelectedTextColor(Color.RED);

        JButton clearBtn = new JButton("clear");
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearPackagedEvent();
            }
        });
        final JButton settingBtn = new JButton("db setting");
        settingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SettingDialog dialog = new SettingDialog();
                dialog.setVisible(true);
                dialog.addWindowListener(new DialogWindowListener());
            }
        });
        final JButton filterBtn = new JButton("filter");
        filterBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FilterDialog dialog = new FilterDialog();
            }
        });

        final Checkbox onTopCheckbox = new Checkbox("on top", null, alwaysOnTop);
        onTopCheckbox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				UI.this.setAlwaysOnTop(onTopCheckbox.getState());
				Config.getInstance().setAlwaysOnTop(onTopCheckbox.getState());
			}
        	
        });
        
        textAreaPane.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {
				Rectangle originalBound = UI.this.getBounds();
				int  marginRight = (int) (Util.getScreenWidth() - originalBound.getX() - originalBound.getWidth());
				int marginBottom = (int) (Util.getScreenHeight() -originalBound.getY() - originalBound.getHeight());
				int xPoint = 0, yPoint = 0;
				if(marginRight < originalBound.getX()){
					xPoint = 0;
				}else{
					xPoint = (int) (Util.getScreenWidth() - originalBound.getWidth());
				}
				if(marginBottom < originalBound.getY()){
					yPoint = 0;
				}else{
					yPoint = (int) (Util.getScreenHeight() - originalBound.getHeight());
				}
				Rectangle newPosition = new Rectangle(xPoint, yPoint, (int)originalBound.getWidth(),  (int)originalBound.getHeight());
				if(UI.this.isAlwaysOnTop()){
					UI.this.setBounds(newPosition);
				}

			}
			@Override
			public void mouseExited(MouseEvent e) {}
        	
        });
        
        pane.add(onTopCheckbox);
        pane.add(clearBtn);
        pane.add(settingBtn);
        pane.add(filterBtn);

        scrollPane = new JScrollPane(textAreaPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(pane, BorderLayout.NORTH);
        this.addWindowListener(this);
        this.setVisible(true);
    }

    private void clearPackagedEvent() {
        textAreaPane.setText("");
        globalPackagedEvent.clear();
    }

    public void displayEvent(List<PackagedEvent> packagedEvents) {
        globalPackagedEvent.addAll(packagedEvents);
        for (PackagedEvent packagedEvent : packagedEvents) {
            try {
                StyledDocument doc = textAreaPane.getStyledDocument();

                StringBuilder sBuilder = new StringBuilder();
                sBuilder.append(PackagedEvent.showType(packagedEvent.getType())).append("  ")
                        .append(packagedEvent.getDatabase()).append(".")
                        .append(packagedEvent.getTableName());
                if (packagedEvent.getEntryName() != null) {
                    sBuilder.append("  Name => " + packagedEvent.getEntryName() + "  ");
                }
                if (packagedEvent.getEntryId() != null) {
                    sBuilder.append("  ID => " + packagedEvent.getEntryId());
                }
                sBuilder.append("\n");
                sBuilder.append("+-------------------------+-----------------------------+-----------------------------+\n");
                sBuilder.append("|       field        |          old value          |          new value          |\n");
                sBuilder.append("+-------------------------+-----------------------------+-----------------------------+\n");

                doc.insertString(doc.getLength(), sBuilder.toString(), null);

                for (List<PackagedEvent.Field> recordField : packagedEvent.getRecordFields()) {
                    for (PackagedEvent.Field f : recordField) {
                        StringBuilder sLine = new StringBuilder();
                        sLine.append("| ").append(Util.truncate(f.name, 24));
                        sLine.append("| ").append(Util.truncate(f.oldValue, 28));
                        sLine.append("| ").append(Util.truncate(f.newValue, 28)).append("|\n");
                        if (f.updated) {
                            doc.insertString(doc.getLength(), sLine.toString(), getHighlightStyle());
                        } else {
                            doc.insertString(doc.getLength(), sLine.toString(), null);
                        }
                    }
                    String splitLine = "+-------------------------+-----------------------------+-----------------------------+\n";
                    doc.insertString(doc.getLength(), splitLine, null);
                }
                doc.insertString(doc.getLength(), "\n", null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JScrollBar sBar = scrollPane.getVerticalScrollBar();
        sBar.setValue(sBar.getMaximum());

    }

    private SimpleAttributeSet getHighlightStyle() {
        SimpleAttributeSet attrSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attrSet, Color.red);
        return attrSet;
    }

    private void buildBinlogConnection() {
        Config config = Config.getInstance();
        String hostname = config.getHostname();
        int port = config.getPort();
        String user = config.getUser();
        String password = config.getPassword();

        try {
            if (binClient != null) {
                binClient.unregisterEventListener(eventListener);
                binClient.disconnect();
            }
            binClient = new BinaryLogClient(hostname, port, user, password);
            eventListener = new EventListener(this);
            binClient.registerEventListener(eventListener);
            binClient.connect();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void registerClearHotKey() {
        provider = Provider.getCurrentProvider(true);
        provider.register(KeyStroke.getKeyStroke("shift meta C"), new HotKeyListener() {
            public void onHotKey(HotKey hotKey) {
                UI.this.clearPackagedEvent();
            }
        });
    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowClosing(WindowEvent e) {
        Config config = Config.getInstance();
        config.setBounds(new Rectangle(this.getBounds()));
        config.saveConfig();
    }

    public void windowClosed(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {

    }

    class DialogWindowListener implements WindowListener {
        public void windowOpened(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
            new Thread(new Runnable() {
                public void run() {
                    buildBinlogConnection();
                }
            });
        }

        public void windowIconified(WindowEvent e) {

        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowActivated(WindowEvent e) {

        }

        public void windowDeactivated(WindowEvent e) {

        }
    }
}


