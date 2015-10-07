package com.fw.edc;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by mxu2 on 6/3/15.
 */
public class Util {
    public static String truncate(Serializable serializable, int length) {
        String string = String.valueOf(serializable);
        if (string.length() > length) {
            return string.substring(0, length);
        } else {
            int blankNum = length - string.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < blankNum; i++) {
                sb.append(" ");
            }
            return string.concat(sb.toString());
        }
    }

    public static int getScreenWidth() {
        return getScreenSize().width;
    }

    public static int getScreenHeight() {
        return getScreenSize().height;
    }

    public static Dimension getScreenSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }

    public static String repeatString(int count, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
