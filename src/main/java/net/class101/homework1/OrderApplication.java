package net.class101.homework1;

import net.class101.homework1.data.beans.CartBean;
import net.class101.homework1.data.beans.ProductBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.class101.homework1.constants.OrderConstants.*;

public class OrderApplication {

    static DataConnect dataConnect;
    static InputStream inputstream;
    static InputStreamReader inputStreamReader;
    static BufferedReader br;
    static List<ProductBean> productList;

    public static void main(String[] args) throws IOException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {

        dataConnect = new DataConnect();

        inputstream = System.in;
        inputStreamReader = new InputStreamReader(inputstream);
        br = new BufferedReader(inputStreamReader);
        String answer;

        selectLoop: while (true) {
            System.out.println(NOTICE_MSG + " : ");
            answer = br.readLine();
            switch (answer) {
                case "q":
                    break selectLoop;
                case "o":
                    orderPrepare(new CartBean());
                    break;
            }

        }
        br.close();
        inputStreamReader.close();
        inputstream.close();

        System.out.println(END_MSG);
        dataConnect.close();
    }
    private static void orderPrepare(CartBean cartBean) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        Queue<Object> wishQueue = new LinkedList<>();// idx 0: PRODUCT_ID_MSG, idx 1: AMOUNT_MSG

        System.out.println(PRODUCT_ID_MSG + "\t" + PRODUCT_NAME_MSG + "\t" + PRICE_MSG + "\t" + STOCK_MSG);
        List<ProductBean> productList = dataConnect.fetchQuery("select * from product;", new ProductBean());
        productList.forEach(d -> System.out.println(d.getId() + "\t" + d.getName() + "\t" + d.getPrice() + "\t" + d.getStock()));
        String answer;

        wishLoop: while(true) {
            if(wishQueue.size() == 0) {
                System.out.println(PRODUCT_ID_MSG + " : ");
            } else {
                System.out.println(AMOUNT_MSG + " : ");
            }

            answer = br.readLine();
            if(answer.equals(" ")) {
                break wishLoop;
            }

            if(wishQueue.size() == 0) {
                //validation
            } else if(wishQueue.size() == 1) {
                //validation
            } else if(wishQueue.size() == 2) {
                //validation
                //move it to the cart bean
            }
        }
    }

    private static void orderExecute(CartBean cartBean) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException {
        //todo
        List<ProductBean> list = dataConnect.fetchQuery("select * from product where id = 91008;", new ProductBean());
        list.forEach(d -> System.out.println(d.getId() + "\t" + d.getName() + "\t" + d.getPrice() + "\t" + d.getStock()));

        dataConnect.executeQuery("update product set stock = stock - 1 where id = 91008;");

        list = dataConnect.fetchQuery("select * from product where id = 91008;", new ProductBean());
        list.forEach(d -> System.out.println(d));
    }
}
