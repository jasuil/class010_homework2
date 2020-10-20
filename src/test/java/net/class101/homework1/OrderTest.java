package net.class101.homework1;

import net.class101.homework1.data.DataConnect;
import net.class101.homework1.data.beans.CartBean;
import net.class101.homework1.data.beans.ProductBean;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static net.class101.homework1.constants.OrderConstants.*;

public class OrderTest {
    static DataConnect dataConnect;
    Order testClass;
    public OrderTest() {
        testClass = new Order();
    }

    @Test
    public void totalSumTest() {
        List<CartBean.OrderBean> orderList = new ArrayList<>();
        CartBean cart = new CartBean();
        CartBean.OrderBean order = cart.new OrderBean();
        order.setAmount(10);
        order.setPrice(4000);
        order.setCategory("KIT");
        orderList.add(order);

        boolean isKlass = false;
        Integer totalFee = 0;

        for(CartBean.OrderBean orderBean : orderList) {
            if(orderBean.getCategory().equals(KLASS_NAME)) {
                isKlass = true;
            }
            totalFee += order.getAmount() * order.getPrice();
        }
        DecimalFormat df = new DecimalFormat("#,###");

        if(totalFee.compareTo(DELIVERY_LIMIT) < 0 && !isKlass) {
            totalFee += DELIVERY_FEE;
        }
        Assert.assertEquals(Integer.valueOf(45000), totalFee);
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
            order.setAmount(2);
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
    public void orderExecuteFailTest() {

        List<CartBean> cartBeanList = executePurchaseFailSetUp();
        AtomicReference<Boolean> failSuccess = new AtomicReference<>(false);
        AtomicBoolean testOk = new AtomicBoolean(false);

        cartBeanList.parallelStream().forEach(order -> {
            try {
                Method testMethod = testClass.getClass().getDeclaredMethod("orderExecute", CartBean.class);
                testMethod.setAccessible(true);

                testMethod.invoke(testClass, order);
                //orderExecute(order);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                if(e.getCause().getMessage().equals(SOLD_OUT_EXCEPTION_MSG)) {
                    testOk.set(true);
                }

            }
        });

        Assert.assertTrue(testOk.get());
    }

    @Test
    public void klassValidationTest() {

        ProductBean productBean = new ProductBean();
        productBean.setCategory(KLASS_NAME);
        productBean.setStock(9999);
        productBean.setId(1);
        productBean.setName("test klass");

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

        boolean testOk = false;
        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("klassValidation", ProductBean.class, List.class);
            testMethod.setAccessible(true);

            testMethod.invoke(testClass, productBean, orderBeanList);
        } catch (Exception e) {
            if(e.getCause().getLocalizedMessage().equals(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG)) {
                testOk = true;
            }
        }
        Assert.assertTrue(testOk);
    }


    /**
     * KLASS product reduplication test
     * expected: wish product is null
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void validateOnProductNameTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
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

        String answer = "4";

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

        Method testMethod = testClass.getClass().getDeclaredMethod("validateOnProductName", List.class, String.class, CartBean.class, CartBean.OrderBean.class, ProductBean.class);
        testMethod.setAccessible(true);
        testMethod.invoke(testClass, productList, answer, cartBean, orderBean, wishProductBean);

        Assert.assertNull(wishProductBean.getId());
    }
    /**
     * expect: import wish product to cart bean
     */
    @Test
    public void validateOnAmountTest() {
        String answer = "1";

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

        ProductBean wishProductBean = new ProductBean();
        wishProductBean.setName("test");
        wishProductBean.setId(1212);
        wishProductBean.setCategory(KLASS_NAME);
        wishProductBean.setStock(99999);
        wishProductBean.setPrice(2000);

        boolean testOk = false;
        Object o = null;
        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("validateOnAmount", String.class, ProductBean.class, CartBean.class, CartBean.OrderBean.class);
            testMethod.setAccessible(true);

            o = testMethod.invoke(testClass, answer, wishProductBean, cartBean, orderBean);

        } catch (Exception e) {
            if (e.getCause().getLocalizedMessage().equals(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG)) {
                testOk = true;
            }
        }
        Assert.assertEquals(1, cartBean.getOrderList().size());
    }
}
