/*
 * Copyright (c) 2020 Jeneral Samopal Company
 * Design and Programming by Alex Dovby
 */

package com.jsc.smarthome;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

class FileUtils {

    // =========================================================
    static void SaveFile(String filePath, String FileContent) {
        //Создание объекта файла.
//        File file = Environment.getExternalStorageDirectory();
        File file = Environment.getDataDirectory();
        System.out.println("trace SaveFile | Path " + file);
        File fhandle = new File(file.getAbsolutePath() + File.separator + filePath);

        try {
            //Если нет директорий в пути, то они будут созданы:
            File parentFile = fhandle.getParentFile();
            System.out.println("trace SaveFile | must create dir = " + fhandle);
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                    System.out.println("trace | mkdirs");
                }
            }
            System.out.println("trace next SaveFile | parentFile " + parentFile);
            //Если файл существует, то он будет перезаписан:
            fhandle.createNewFile();
            System.out.println("trace createNewFile " + parentFile);
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(FileContent);
            myOutWriter.close();
            fOut.close();
            System.out.println("trace | close NewFile " + parentFile);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("trace | Error : " + e.toString());
        }
    }

    // =========================================================
    static String readFile(Activity main, String filePath) {
        String state = Environment.getExternalStorageState();
        StringBuilder textBuilder;
        if (!(state.equals(Environment.MEDIA_MOUNTED))) {
            Toast.makeText(main, R.string.msg_no_sd, Toast.LENGTH_LONG).show();
        } else {
            BufferedReader reader = null;
            System.out.println("trace | Path " + filePath);
            try {
//                File file = Environment.getExternalStorageDirectory();
                File file = Environment.getDataDirectory();
                File textFile = new File(file.getAbsolutePath() + File.separator + filePath);
                // File textFile = new File(filePath);
                System.out.println("trace | while " + textFile);
                reader = new BufferedReader(new FileReader(textFile));
                textBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    textBuilder.append(line);
                    textBuilder.append("\n");
                }
                System.out.println("trace | Path " + filePath + ", src : " + textBuilder);
                return textBuilder.toString();

            } catch (FileNotFoundException e) {
                System.out.println("trace | Path " + filePath + ", FileNotFoundException");
                // TODO: handle exception
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
