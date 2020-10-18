package net.class101.homework1.utils;

import net.class101.homework1.exceptions.BizException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlParserTest {
    @Test
    public void parseXmlMethodTest() throws IOException, SAXException, ParserConfigurationException, BizException {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 91008);
        map.put("stock", 1);
        String sql = SqlXmlParserUtil.parseSqlXml("stockUpdate", map);
        Assert.assertNotNull(sql);

        //list by String
        List<Object> list = new ArrayList<>();
        list.add("91008");
        list.add("39712");
        map = new HashMap<>();
        map.put("idList", list);
        sql = SqlXmlParserUtil.parseSqlXml("selectByIdList", map);
        Assert.assertNotNull(sql);

        //list by Integer
        list = new ArrayList<>();
        list.add(91008);
        list.add(39712);
        map = new HashMap<>();
        map.put("idList", list);
        sql = SqlXmlParserUtil.parseSqlXml("selectByIdList", map);
        Assert.assertNotNull(sql);
    }
}
