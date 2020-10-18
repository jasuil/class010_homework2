package net.class101.homework1;

import net.class101.homework1.data.DataConnect;
import net.class101.homework1.data.beans.CartBean;
import net.class101.homework1.data.beans.ProductBean;
import net.class101.homework1.exceptions.BizException;
import net.class101.homework1.utils.SqlXmlParserUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static net.class101.homework1.constants.OrderConstants.*;

public class OrderApplication {

    static DataConnect dataConnect;
    static InputStream inputstream;
    static InputStreamReader inputStreamReader;
    static BufferedReader br;

    public static void main(String[] args) throws IOException, SQLException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException, BizException, SAXException, ParserConfigurationException {

        dataConnect = new DataConnect();

        inputstream = System.in;
        inputStreamReader = new InputStreamReader(inputstream);
        br = new BufferedReader(inputStreamReader);
        String answer;

        selectLoop: while (true) {
            System.out.print("\r" + NOTICE_MSG + " : ");
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
    private static void orderPrepare(CartBean cartBean) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException, BizException, SAXException, ParserConfigurationException {
        ProductBean wishProduct = new ProductBean();//before putting into the cart

        System.out.println(PRODUCT_ID_MSG + "\t" + PRODUCT_NAME_MSG + "\t" + PRICE_MSG + "\t" + STOCK_MSG);
        List<ProductBean> productList = dataConnect.fetchQuery(SqlXmlParserUtil.parseSqlXml("selectAll", new HashMap<>()), new ProductBean());
        productList.forEach(d -> System.out.println(d.getId() + "\t" + d.getName() + "\t" + d.getPrice() + "\t" + d.getStock()));
        String answer;
        CartBean.OrderBean orderBean = cartBean.new OrderBean();

        while(true) {
            if(wishProduct.getId() == null) {
                System.out.print("\r" + PRODUCT_ID_MSG + " : ");
            } else {
                System.out.print("\r" + AMOUNT_MSG + " : ");
            }

            answer = br.readLine();
            if(wishProduct.getId() == null && answer.equals(" ")) {
                if(cartBean.getOrderList().size() == 0) {
                    System.out.println(ORDER_PLEASE_MSG);
                    continue;
                }
                System.out.println(CART_MSG + " : ");
                orderExecute(cartBean);
                break;
            }

            if(wishProduct.getId() == null) {
                //validation
                try {
                    Integer inputId = Integer.valueOf(answer);
                    List<ProductBean> matchList = productList.stream().filter(product -> {
                        if(product.getId().equals(inputId)) {
                            return true;
                        } else {
                            return false;
                        }
                    }).collect(Collectors.toList());

                    if(matchList.size() == 0) {
                        System.out.println(ID_NOT_IN_LIST_MSG);
                        continue;
                    } else {
                        ProductBean matchedProduct = matchList.get(0);
                        boolean alreadyHave = cartBean.getOrderList().stream().anyMatch(product -> {
                            if(product.getId().equals(inputId)) {
                                return true;
                            } else {
                                return false;
                            }
                        });

                        if(matchedProduct.getStock().compareTo(0) < 1) {
                            System.out.println(SOLD_OUT_MSG);
                        } else if(alreadyHave) {
                            System.out.print("\r" + CHANGE_PRODUCT_MSG);
                            answer = br.readLine();
                            while(true) {//change already having product in wish list?
                                if (answer.toLowerCase().equals("y")) {
                                    orderBean.setId(inputId);
                                    BeanUtils.copyProperties(wishProduct, matchedProduct);
                                    CartBean.OrderBean removeOrder = null;
                                    for(CartBean.OrderBean order : cartBean.getOrderList()) {
                                        if(order.getId().equals(matchedProduct.getId())) {
                                            removeOrder = order;
                                        }
                                    }
                                    cartBean.getOrderList().remove(removeOrder);
                                    break;
                                } else if(answer.toLowerCase().equals("n")) {
                                    break;
                                }
                            }

                        } else {
                            orderBean.setId(inputId);
                            BeanUtils.copyProperties(wishProduct, matchedProduct);
                        }
                    }
                } catch(NumberFormatException e) {
                    System.out.println(ID_INPUT_PLEASE_MSG);
                    continue;
                }

            } else if(wishProduct.getId() != null) {
                //validation
                try {

                    Integer amount = Integer.valueOf(answer);
                    Integer stock = wishProduct.getStock();

                    if(amount.compareTo(0) < 1) {
                        System.out.println(AMOUNT_IS_NATURAL_NUMBER_MSG);
                        continue;
                    } else if (wishProduct.getStock().compareTo(amount) < 0) {
                        System.out.println(AMOUNT_LESS_THAN_STOCK_MSG);
                        continue;
                    } else {
                        BeanUtils.copyProperties(orderBean, wishProduct);
                        orderBean.setAmount(amount);
                        wishProduct = new ProductBean();
                    }

                    //move it to the cart bean
                    cartBean.addCart(orderBean);
                    orderBean = cartBean.new OrderBean();

                } catch(NumberFormatException e) {
                    System.out.println(AMOUNT_IS_NATURAL_NUMBER_MSG);
                } catch(BizException e) {// KLASS CATEGORY IS ONLY ONE IN THE CART
                    if(e.getErrCode().equals(101)) {
                        orderBean = cartBean.new OrderBean();
                        wishProduct = new ProductBean();
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * order to the system is to be synchronized
     * @param cartBean
     * @throws SQLException
     * @throws NoSuchFieldException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws BizException
     * @throws IOException
     */
    synchronized private static void orderExecute(CartBean cartBean) throws SQLException, NoSuchFieldException,
            InstantiationException, IllegalAccessException, InvocationTargetException, ParserConfigurationException,
            SAXException, BizException, IOException {

        showCartOrder(cartBean);

        List<String> updateList = new ArrayList<>();
        List<Integer> idList = new ArrayList<>();
        for(CartBean.OrderBean orderBean : cartBean.getOrderList()) {
            if(!orderBean.getCategory().equals(KLASS_NAME)) {
                Map<String, Object> sqlSetter = new HashMap<>();
                sqlSetter.put("id", orderBean.getId());
                sqlSetter.put("stock", orderBean.getAmount());
                String sql = SqlXmlParserUtil.parseSqlXml("stockUpdate", sqlSetter);
                updateList.add(sql);
                idList.add(orderBean.getId());
            }
        }
        Map<String, Object> sqlSetter = new HashMap<>();
        sqlSetter.put("idList", idList);
        totalSum(cartBean.getOrderList());

        String validateSql = SqlXmlParserUtil.parseSqlXml("selectByIdList", sqlSetter);
        findOutSoldOut(validateSql, cartBean);

        dataConnect.executeQuery(updateList);
    }

    private static void showCartOrder(CartBean cartBean) {
        System.out.println(PARTITION);
        for(CartBean.OrderBean orderBean : cartBean.getOrderList()){
            System.out.println(orderBean.getName() + HYPHEN_MSG + orderBean.getAmount() + AMOUNT_UNIT_MSG);
        }
        System.out.println(PARTITION);
    }

    private static void totalSum(List<CartBean.OrderBean> orderList) {
        boolean isKlass = false;
        Integer totalFee = 0;

        for(CartBean.OrderBean order : orderList) {
            if(order.getCategory().equals(KLASS_NAME)) {
                isKlass = true;
            }
            totalFee += order.getAmount() * order.getPrice();
        }
        DecimalFormat df = new DecimalFormat("#,###");

        System.out.println(TOTAL_SUM_MSG + " : " + df.format(totalFee));
        System.out.println(PARTITION);

        if(totalFee.compareTo(DELIVERY_LIMIT) < 0 && !isKlass) {
            totalFee += DELIVERY_FEE;
        }

        System.out.println(TOTAL_SUM_MSG + " : " + df.format(totalFee));
        System.out.println(PARTITION);
    }

    private static void findOutSoldOut(String validateSql, CartBean cartBean) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException, BizException {
        List<ProductBean> validateList = dataConnect.fetchQuery(validateSql, new ProductBean());
        for(ProductBean product : validateList) {
            for (CartBean.OrderBean order : cartBean.getOrderList()) {
                if (!order.getCategory().equals(KLASS_NAME) && order.getId().equals(product.getId())) {
                    if (order.getAmount().compareTo(product.getStock()) > 0) {
                        throw new BizException("SoldOutException");
                    }
                    break;//avoid wasting full search
                }
            }
        }
    }
}
