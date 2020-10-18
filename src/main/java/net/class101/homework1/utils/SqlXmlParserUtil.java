package net.class101.homework1.utils;

import lombok.experimental.UtilityClass;
import net.class101.homework1.exceptions.BizException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@UtilityClass
public class SqlXmlParserUtil {
    final static String sqlSourcePath;
    static {
        sqlSourcePath = ClassLoader.getSystemClassLoader().getResource("productSql.xml").getPath();
    }
    public String parseSqlXml(String searchId, Map<String, Object> valueMap) throws ParserConfigurationException, IOException, SAXException, BizException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(sqlSourcePath);

        // root 구하기
        Element root = document.getDocumentElement();

        NodeList childeren = root.getChildNodes(); // 자식 노드 목록 get
        String sql = null;
        for(int i = 0; i < childeren.getLength(); i++){

            Node node = childeren.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){ // 해당 노드의 종류 판정(Element일 때)
                if(childeren.item(i).getAttributes().item(0).getNodeValue().equals(searchId)) {
                    sql = childeren.item(i).getTextContent();
                }
            }
        }

        if(sql != null) {
            return querySetter(sql, valueMap);
        } else {
            throw new BizException("no matched query");
        }
    }

    public String querySetter(String sql, Map<String, Object> valueMap) {
        Iterator<String> iterator = valueMap.keySet().iterator();

        while(iterator.hasNext()) {
            String key = iterator.next();
            if(sql.contains("{" + key + "}")) {
                String setter = "";
                Object obj = valueMap.get(key);

                if(obj instanceof List) {
                    StringBuffer memberToStrBuf = new StringBuffer();
                    for(Object member : (List) obj) {
                        if(member instanceof Integer) {
                            memberToStrBuf = memberToStrBuf.append(member).append(",");
                        } else {
                            memberToStrBuf = memberToStrBuf.append("'").append(member).append("'").append(",");
                        }
                        setter = memberToStrBuf.toString();
                        setter = setter.substring(0, setter.length() - 1);
                    }
                } else {
                    setter = (valueMap.get(key) instanceof Integer) ? String.valueOf(valueMap.get(key)) : "'" + valueMap.get(key) + "'" ;
                }
                sql = sql.replace("{" + key + "}", setter);
            }
        }

        return sql;
    }

}
