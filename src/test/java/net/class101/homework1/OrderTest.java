package net.class101.homework1;

import net.class101.homework1.data.DataConnect;
import net.class101.homework1.data.beans.CartBean;
import net.class101.homework1.data.beans.ProductBean;
import net.class101.homework1.exceptions.BizException;
import net.class101.homework1.utils.SqlXmlParserUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static net.class101.homework1.constants.OrderConstants.*;

public class OrderTest {
    static DataConnect dataConnect;

    private List<CartBean.OrderBean> totalSumTestSetUp() {
        List<CartBean.OrderBean> orderList = new ArrayList<>();
        CartBean cart = new CartBean();
        CartBean.OrderBean order = cart.new OrderBean();
        order.setAmount(10);
        order.setPrice(4000);
        order.setCategory("KIT");
        orderList.add(order);
        return orderList;
    }

    @Test
    public void totalSumTest() {
        List<CartBean.OrderBean> orderList = totalSumTestSetUp();

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


    /**
     * put klass which is in cart into cart test
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void validateOnProductNameTest()
            throws InvocationTargetException, IllegalAccessException {

        List<ProductBean> productList = new ArrayList<>(); //wish product put into the cart
        ProductBean productBean = new ProductBean();
        productBean.setCategory(KLASS_NAME);
        productBean.setStock(9999);
        productBean.setId(1);
        productBean.setName("test klass");
        productList.add(productBean);

        productBean = new ProductBean();
        productBean.setCategory(KLASS_NAME);
        productBean.setStock(9999);
        productBean.setId(4);
        productBean.setName("test klass4");
        productList.add(productBean);

        String answer = "2";

        CartBean cartBean = new CartBean();
        CartBean.OrderBean orderBean = cartBean.new OrderBean();
        orderBean.setName("this is not klass");
        orderBean.setCategory("KIT");
        orderBean.setId(1);
        orderBean.setAmount(2);
        List<CartBean.OrderBean> orderBeanList = new ArrayList<>();
        orderBeanList.add(orderBean);

        orderBean = cartBean.new OrderBean();
        orderBean.setCategory(KLASS_NAME);
        orderBean.setAmount(9999);
        orderBean.setId(1);
        orderBean.setName("test klass");
        orderBeanList.add(orderBean);
        cartBean.setOrderList(orderBeanList);

        ProductBean wishProductBean = new ProductBean();
        boolean testOk = false;

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
            } else {
                ProductBean matchedProductBean = matchList.get(0);
                boolean alreadyHave = cartBean.getOrderList().stream().anyMatch(product -> {
                    if(product.getId().equals(inputId)) {
                        return true;
                    } else {
                        return false;
                    }
                });

                klassValidation(matchedProductBean, cartBean.getOrderList());

                if(matchedProductBean.getStock().compareTo(0) < 1) {
                    System.out.println(SOLD_OUT_MSG);
                } else if(alreadyHave) {
                    System.out.print("\r" + CHANGE_PRODUCT_MSG);
                    answer = "y";
                    while(true) {//change already having product in wish list?
                        if (answer.toLowerCase().equals("y")) {
                            orderBean.setId(inputId);
                            BeanUtils.copyProperties(wishProductBean, matchedProductBean);
                            CartBean.OrderBean removeOrder = null;
                            for(CartBean.OrderBean order : cartBean.getOrderList()) {
                                if(order.getId().equals(matchedProductBean.getId())) {
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
                    klassValidation(matchedProductBean, cartBean.getOrderList());
                    orderBean.setId(inputId);
                    BeanUtils.copyProperties(wishProductBean, matchedProductBean);
                }
            }

        } catch(NumberFormatException | IllegalAccessException | InvocationTargetException e) {
            System.out.println(ID_INPUT_PLEASE_MSG);

        } catch (BizException e) {
            if(e.getErrCode().equals(KLASS_ONLY_ONE_IN_CART_ERROR_CODE)) {
                //BeanUtils.copyProperties(orderBean, cartBean.new OrderBean());
                //BeanUtils.copyProperties(wishProduct, new ProductBean());
                testOk = true;
                System.out.println(e.getMessage());
            }
        }

        Assert.assertTrue(testOk);
    }

    private void klassValidation(ProductBean matchedProductBean, List<CartBean.OrderBean> orderList) throws BizException {
        boolean isKlassHave = orderList.stream().anyMatch(ord -> {
            if(ord.getCategory().equals(KLASS_NAME)) {
                return true;
            } else {
                return false;
            }
        });
        if(matchedProductBean.getCategory().equals(KLASS_NAME) && isKlassHave) {
            throw new BizException(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG, KLASS_ONLY_ONE_IN_CART_ERROR_CODE);
        }
    }

    /**
     * multi thread test prepare
     */
    private List<CartBean> executePurchaseFailSetUp() {
        List<CartBean> cartBeanList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            CartBean cartBean = new CartBean();
            List<CartBean.OrderBean> orderList = new ArrayList<>();
            CartBean.OrderBean order = cartBean.new OrderBean();
            order.setAmount(3);
            order.setPrice(135800);
            order.setCategory("KIT");
            order.setName("시작에 대한 부담을 덜다. 가격 절약 아이패드 특가전");
            order.setId(60538);
            orderList.add(order);
            cartBean.setOrderList(orderList);
            cartBeanList.add(cartBean);
        }

        return cartBeanList;
    }


    /**
     * multi thread test
     * expect is biz exception with SoldOutException message
     */
    @Test
    public void executePurchaseFailTest() throws SQLException {
        dataConnect = new DataConnect();
        List<CartBean> cartBeanList = executePurchaseFailSetUp();
        AtomicReference<Boolean> failSuccess = new AtomicReference<>(false);

        cartBeanList.parallelStream().forEach(order -> {
            try {
                orderExecute(order);
            } catch (SQLException | InstantiationException |
                    SAXException | NoSuchFieldException | ParserConfigurationException |
                    IllegalAccessException | InvocationTargetException | IOException e) {
                e.printStackTrace();
            } catch (BizException e) {
                failSuccess.set(true);
            }
        });

        dataConnect.close();
        Assert.assertEquals(failSuccess.get(), true);
    }

    synchronized private void orderExecute(CartBean cartBean) throws SQLException, NoSuchFieldException,
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

    private void showCartOrder(CartBean cartBean) {
        System.out.println(PARTITION);
        for(CartBean.OrderBean orderBean : cartBean.getOrderList()){
            System.out.println(orderBean.getName() + HYPHEN_MSG + orderBean.getAmount() + AMOUNT_UNIT_MSG);
        }
        System.out.println(PARTITION);
    }

    private void totalSum(List<CartBean.OrderBean> orderList) {
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

    private void findOutSoldOut(String validateSql, CartBean cartBean) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException, BizException {
        List<ProductBean> validateList = dataConnect.fetchQuery(validateSql, new ProductBean());
        for(ProductBean product : validateList) {
            for (CartBean.OrderBean order : cartBean.getOrderList()) {
                if (!order.getCategory().equals(KLASS_NAME) && order.getId().equals(product.getId())) {
                    if (order.getAmount().compareTo(product.getStock()) > 0) {
                        throw new BizException(SOLD_OUT_EXCEPTION_MSG, SOLD_OUT_CODE);
                    }
                    break;//avoid wasting full search
                }
            }
        }
    }

}
