package literalTracker.utils;

import java.io.*;

public class DataManager {
    public static Object readIntermediateData(String filePath){
        Object object = null;
        File file = new File(filePath);
        if (file.exists()){
            try {
                object = new ObjectInputStream(new FileInputStream(file)).readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static void writeIntermediateData(String filePath, Object object){
        File file = new File(filePath);
        try {
            new ObjectOutputStream(new FileOutputStream(file)).writeObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
