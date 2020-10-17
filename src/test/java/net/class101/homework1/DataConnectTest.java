package net.class101.homework1;

import net.class101.homework1.data.beans.ProductBean;
import net.class101.homework1.utils.BeanMapper;
import net.class101.homework1.utils.FileUtil;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class DataConnectTest {

    static Connection conn = null;
    static Statement stmt = null;
    static ResultSet rs = null;

    @Test
    public void connectTest() throws ClassNotFoundException, SQLException, InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {

        Class.forName("org.h2.Driver");
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "", "");
        stmt = conn.createStatement();

        String script = FileUtil.fileRead(ClassLoader.getSystemClassLoader().getResource("ddl.sql").getPath());
        stmt.addBatch(script);

        script = FileUtil.fileRead(ClassLoader.getSystemClassLoader().getResource("dml.sql").getPath());
        stmt.addBatch(script);
        stmt.executeBatch();

        rs = stmt.executeQuery("select * from product");
//        while (rs.next()) {
 //           System.out.println("id " + rs.getInt("id") + " name " + rs.getString("name"));
  //      }

        List<ProductBean> list = BeanMapper.beanMap(rs, new ProductBean());

        list.stream().forEach(d -> System.out.println(d.toString()));
    }
}
