package net.class101.homework1.data;

import net.class101.homework1.utils.BeanMapper;
import net.class101.homework1.utils.FileUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        stmt = conn.createStatement();
        stmt.execute(sql);
        stmt.close();
    }

    public void executeQuery(List<String> sqlList) throws SQLException {
        stmt = conn.createStatement();
        for(String sql : sqlList) {
            stmt.addBatch(sql);
        }
        stmt.executeBatch();
        stmt.close();
    }

    /**
     *
     * @param sql
     * @param object new instance for return class
     * @param <T> return class type
     * @return List of the object type
     * @throws SQLException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public <T> List<T> fetchQuery(String sql, T object) throws SQLException, InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(sql);
        List<T> list = BeanMapper.beanMap(rs, object);
        stmt.close();

        return list;
    }

    public void close() throws SQLException {
        conn.close();
    }

}
