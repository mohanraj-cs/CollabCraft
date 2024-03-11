package com.CollabCraft.utils;

import java.util.Random;

public class ColorUtil {

    public static String[] color = {"#FFFAFA", "#F8F8FF", "#F5F5F5",
            "#FFFAF0", "#FDF5E6", "#FAF0E6", "#FAEBD7", "#FFEFD5",
            "#FFEBCD", "#FFE4C4", "#FFDAB9", "#FFDEAD", "#FFE4B5", "#FFF8DC",
            "#FFFFF0", "#FFFACD", "#FFF5EE", "#F0FFF0", "#F5FFFA", "#F0FFFF",
            "#F0F8FF", "#E6E6FA", "#FFF0F5", "#FFE4E1", "#D3D3D3", "#87CEFA",
            "#ADD8E6", "#FAFAD2", "#FFFFE0", "#F5F5DC"};

    public static String getRandomColor() {
        String red;

        String green;

        String blue;

        Random random = new Random();

        red = Integer.toHexString(random.nextInt(256)).toUpperCase();

        green = Integer.toHexString(random.nextInt(256)).toUpperCase();

        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();


        red = red.length() == 1 ? "0" + red : red;

        green = green.length() == 1 ? "0" + green : green;

        blue = blue.length() == 1 ? "0" + blue : blue;

        return "#" + red + green + blue;
    }

    public static String getColor() {
        Random random = new Random();
        return color[random.nextInt(color.length)];
    }
}
