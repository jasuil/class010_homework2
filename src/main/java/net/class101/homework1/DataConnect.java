package net.class101.homework1;

import net.class101.homework1.utils.FileUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class DataConnect {

    static Connection conn = null;
    static Statement stmt = null;
    static ResultSet rs = null;

    static  {
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
            stmt = conn.createStatement();

            String script = FileUtil.fileRead(ClassLoader.getSystemClassLoader().getResource("ddl.sql").getPath());
            stmt.addBatch(script);

            script = FileUtil.fileRead(ClassLoader.getSystemClassLoader().getResource("dml.sql").getPath());
            stmt.addBatch(script);
            stmt.executeBatch();

        } catch (SQLException | ClassNotFoundException | IOException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    public void executeQuery(String sql) throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
        stmt = conn.createStatement();
        stmt.execute(sql);
        stmt.close();
    }

    public List<Object> fetchQuery(String sql) throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
        stmt = conn.createStatement();
        rs = stmt.executeQuery("select * from user");
        while (rs.next()) {

            System.out.println("id " + rs.getInt("id") + " name " + rs.getString("name"));
        }
        stmt.close();

        return null;
    }

    public  void main() throws ClassNotFoundException, SQLException {


        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
        stmt = conn.createStatement();
        stmt.execute("drop table if exists user");
        stmt.execute("create table user(id int primary key, name varchar(100))");
        stmt.execute("insert into user values(1, 'hello')");
        stmt.execute("insert into user values(2, 'world')");
        rs = stmt.executeQuery("select * from user");

        while (rs.next()) {
            System.out.println("id " + rs.getInt("id") + " name " + rs.getString("name"));
        }
        stmt.close();

        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
        stmt = conn.createStatement();

         rs = stmt.executeQuery("select * from user");

        while (rs.next()) {
            System.out.println("id " + rs.getInt("id") + " name " + rs.getString("name"));
        }
        stmt.close();
    }

    public void main2() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
        stmt = conn.createStatement();

        rs = stmt.executeQuery("select * from user");

        while (rs.next()) {
            System.out.println("id " + rs.getInt("id") + " name " + rs.getString("name"));
        }
        stmt.close();
    }

    public void close() throws SQLException {
        conn.close();
    }

}
