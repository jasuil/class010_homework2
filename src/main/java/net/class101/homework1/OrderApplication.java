package net.class101.homework1;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

public class OrderApplication {

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

        DataConnect dataConnect = new DataConnect();

        dataConnect.main();

        dataConnect.main2();
        InputStream inputstream = System.in;
        InputStreamReader inputStreamReader = new InputStreamReader(inputstream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String y = "";
        while (!(y = br.readLine()).equals("q")){
            System.out.println("hi");
        }

        System.out.println("=============");
        dataConnect.close();
    }
}
