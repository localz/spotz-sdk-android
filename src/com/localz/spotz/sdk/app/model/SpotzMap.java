package com.localz.spotz.sdk.app.model;

import android.content.Context;

import com.localz.spotz.sdk.models.Spot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of HashMap that backs up data to a local file. 
 * 
 * @author Localz
 *
 */

public class SpotzMap extends ConcurrentHashMap<String, Spot> {
	
	private static final long serialVersionUID = -7604275359152984786L;
	private static String SPOTZ_MAP_FILE = "SpotzMap";


    public static SpotzMap readCache(Context context) {
        ObjectInputStream inputStream = null;
        SpotzMap spotzMap = new SpotzMap();
        try {
            File file = new File(context.getFilesDir(), SPOTZ_MAP_FILE);
            inputStream = new ObjectInputStream(new FileInputStream(file));
            ConcurrentHashMap<String, Spot> spotzFromFile = (ConcurrentHashMap<String, Spot>) inputStream
                    .readObject();

            spotzMap.putAll(spotzFromFile);
        } catch (OptionalDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return spotzMap;
    }


    public static void writeToFile(SpotzMap spotMap, Context context) {
        ObjectOutputStream outputStream = null;
        try {
            File file = new File(context.getFilesDir(), SPOTZ_MAP_FILE);

            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject((ConcurrentHashMap<String, Spot>) spotMap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}