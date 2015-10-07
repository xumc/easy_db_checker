package com.fw.edc;

/**
 * dbhistory
 */
public class App {
    private UI ui;

    public App() {
        if (ui != null) {
            ui.dispose();
        }
        ui = new UI();

    }

    public static void main(String[] args) throws Exception {
        App app = new App();
    }

}