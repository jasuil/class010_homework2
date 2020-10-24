package net.class101.homework1;

import lombok.extern.slf4j.Slf4j;

import net.class101.homework1.data.beans.CartBean;
import net.class101.homework1.data.beans.Product;
import net.class101.homework1.data.repository.ProductRepository;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static net.class101.homework1.constants.OrderConstants.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    OrderService testClass;
    @Mock
    ProductRepository productRepository;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    public void printOutSetUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    public void totalSumTest() {
        this.printOutSetUp();

        List<CartBean.OrderBean> orderList = new ArrayList<>();
        CartBean cart = new CartBean();
        CartBean.OrderBean order = cart.new OrderBean();
        order.setAmount(10);
        order.setPrice(4000);
        order.setCategory("KIT");
        orderList.add(order);

        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("totalSum", List.class);
            testMethod.setAccessible(true);
            testMethod.invoke(testClass, orderList);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }

        Assert.assertEquals("주문 비용 : 40,000\n" +
                "--------------------------------\n" +
                "지불 금액 : 45,000\n" +
                "--------------------------------\n",
                outputStreamCaptor.toString().replaceAll("\r", ""));
    }

    /**
     * multi thread test
     * expect: SOLD_OUT_MSG by soldOutException
     */
    @Test
    public void orderExecuteFailTest() throws ExecutionException, InterruptedException {

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

        List<Product> productList = new ArrayList<>();
        Product product = new Product();
        product.setStock(5);
        product.setPrice(135800);
        product.setCategory("KIT");
        product.setName("시작에 대한 부담을 덜다. 가격 절약 아이패드 특가전");
        product.setId(60538);
        productList.add(product);

        List<Integer> idList = Arrays.asList(60538);

        given(productRepository.findAllByIdIn(idList)).willReturn(productList);
        this.printOutSetUp();

        ForkJoinPool forkJoinPool = new ForkJoinPool(3);
        forkJoinPool.submit(() -> cartBeanList.stream().parallel().forEach(order -> {
                try {
                    Method testMethod = testClass.getClass().getDeclaredMethod("executePurchase", CartBean.class);
                    testMethod.setAccessible(true);
                    testMethod.invoke(testClass, order);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            })
        ).get();

        Assert.assertTrue(outputStreamCaptor.toString().contains(SOLD_OUT_MSG));
    }

    /**
     * only one KLASS in cart
     * expect: error exception
     */
    @Test
    public void klassValidationTest() {
        Product Product = new Product();
        Product.setCategory(KLASS_NAME);
        Product.setStock(9999);
        Product.setId(1);
        Product.setName("test klass");

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

        InvocationTargetException exception = null;
        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("klassValidation", Product.class, List.class);
            testMethod.setAccessible(true);
            exception = assertThrows(InvocationTargetException.class, () -> testMethod.invoke(testClass, Product, orderBeanList));
        } catch (NoSuchMethodException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        boolean testOk = (exception == null || exception.getCause() == null || exception.getCause().getMessage() == null) ?
                false : exception.getCause().getMessage().equals(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG);
        Assert.assertTrue(testOk);
    }


    /**
     * KLASS product reduplication test
     * expected: wish product is null
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void validateOnProductNameTest() {
        List<Product> productList = new ArrayList<>(); //wish product put into the cart
        Product Product = new Product();
        Product.setCategory(KLASS_NAME);
        Product.setStock(9999);
        Product.setId(1);
        Product.setName("test klass");
        productList.add(Product);

        Product = new Product();
        Product.setCategory(KLASS_NAME);
        Product.setStock(9999);
        Product.setId(4);
        Product.setName("test klass4");
        productList.add(Product);

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

        Product wishProduct = new Product();
        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("validateOnProductName", List.class, String.class, CartBean.class, CartBean.OrderBean.class, Product.class);
            testMethod.setAccessible(true);
            testMethod.invoke(testClass, productList, answer, cartBean, orderBean, wishProduct);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        Assert.assertNull(wishProduct.getId());
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

        Product wishProduct = new Product();
        wishProduct.setName("test");
        wishProduct.setId(1212);
        wishProduct.setCategory(KLASS_NAME);
        wishProduct.setStock(99999);
        wishProduct.setPrice(2000);

        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("validateOnAmount", String.class, Product.class, CartBean.class, CartBean.OrderBean.class);
            testMethod.setAccessible(true);
            testMethod.invoke(testClass, answer, wishProduct, cartBean, orderBean);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        Assert.assertEquals(1, cartBean.getOrderList().size());
    }
}
