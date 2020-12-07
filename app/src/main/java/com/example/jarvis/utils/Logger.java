package com.example.jarvis.utils;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_APPEND;

public class Logger {
    public static void i(String tag, String message) {
        Log.i(tag, message);
        Logger.appendToFile("I", tag, message);
    }
    public static void e(String tag, String message) {
        Log.e(tag, message);
        Logger.appendToFile("E", tag, message);
    }
    public static void w(String tag, String message) {
        Log.w(tag, message);
        Logger.appendToFile("W", tag, message);
    }
    public static void d(String tag, String message) {
        Log.d(tag, message);
        Logger.appendToFile("D", tag, message);
    }

    public static int appendToFile(String type, String tag, String message) {
        try {
            if (Storage.getContext() == null) { return -1; }
            FileOutputStream fileout = Storage.getContext().openFileOutput("jarvis.log", MODE_APPEND);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(type + "/" + tag + ": " + message + "\n");
            outputWriter.close();
            return 0;
        } catch (IOException e) {
            Log.e("Jarvis", "[LOGGER]: " + Log.getStackTraceString(e));
            return 1;
        }
    }
}
