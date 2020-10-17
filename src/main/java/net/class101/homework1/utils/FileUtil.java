package net.class101.homework1.utils;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@UtilityClass
public class FileUtil {
    /**
     * return full text from file
     * @param resourceFullPath ex) /home/tmp/test.txt
     * @return String full text
     * @throws IOException
     */
    public String fileRead(String resourceFullPath) throws IOException {
        StringBuffer returnStr = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(resourceFullPath));
        int total = 0;

        String row;
        for(int var3 = 0; (row = reader.readLine()) != null; ++total) {
            returnStr = returnStr.append(row).append("\n");
        }
        reader.close();

        return returnStr.toString();
    }
}
