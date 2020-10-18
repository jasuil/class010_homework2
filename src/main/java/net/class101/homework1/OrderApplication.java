package net.class101.homework1;

import net.class101.homework1.exceptions.BizException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class OrderApplication {

    public static void main(String[] args) throws IOException, InstantiationException,
            InvocationTargetException, NoSuchFieldException, SQLException, IllegalAccessException,
            ParserConfigurationException, SAXException, BizException {
        Order order = new Order();
        order.main();
    }
}
