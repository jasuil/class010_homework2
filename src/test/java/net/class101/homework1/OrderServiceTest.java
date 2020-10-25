package net.class101.homework1;

import lombok.extern.slf4j.Slf4j;

import net.class101.homework1.data.bean.CartBean;
import net.class101.homework1.data.entity.Product;
import net.class101.homework1.data.repository.ProductRepository;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
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
    private final ByteArrayOutputStream outputStreamCaptor;

    OrderServiceTest() {
        outputStreamCaptor = new ByteArrayOutputStream();
    }

    public void printOutSetUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * 결제금액 표시에 대한 메서드 테스트
     * expect: 주문비용과 지불금액이 기대값과 일치하는가
     */
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
     * 동일한 상품에 3개의 주문이 동시에 실행될때 multi thread test
     * expect: SOLD_OUT_MSG by soldOutException
     */
    @Test
    public void orderExecuteFailTest() throws ExecutionException, InterruptedException {

        List<CartBean> cartBeanList = new ArrayList<>();
        for(int i = 0; i < 3; i++) {//카트에 KLASS상품 1개와 다른 종류의 상품 1개를 담는다.
            CartBean cartBean = new CartBean();
            List<CartBean.OrderBean> orderList = new ArrayList<>();
            CartBean.OrderBean order = cartBean.new OrderBean();
            order.setAmount(2);
            order.setPrice(135800);
            order.setCategory("KIT");
            order.setName("시작에 대한 부담을 덜다. 가격 절약 아이패드 특가전");
            order.setId(60538);
            orderList.add(order);

            order = cartBean.new OrderBean();
            order.setAmount(99999);
            order.setPrice(191600);
            order.setCategory(KLASS_NAME);
            order.setName("나만의 문방구를 차려요! 그리지영의 타블렛으로 굿즈 만들기");
            order.setId(74218);
            orderList.add(order);

            cartBean.setOrderList(orderList);
            cartBeanList.add(cartBean);
        }

        //orderExecute에서 KLASS상품은 무제한이므로 재고처리를 하지 않게 되어
        //데이터베이스에서 수정 대상이 아니다.
        //따라서 다음과 같이 재고처리 대상은 1개가 되게 하였다.
        List<Product> productList = new ArrayList<>();
        Product product = new Product();
        product.setStock(5);
        product.setPrice(135800);
        product.setCategory("KIT");
        product.setName("시작에 대한 부담을 덜다. 가격 절약 아이패드 특가전");
        product.setId(60538);
        productList.add(product);

        List<Integer> idList = Arrays.asList(60538);//재고처리는 단 1개 상품

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
     * 한 번의 주문에 KLASS종류의 상품이 1개만 카트에 담기는지에 대한 테스트
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
     * 상품선택시 상품 재고가 없는지 유효성 판단하는 메서드를 테스트
     * expected: sold out msg
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void validateOnProductNameBySoldOutTest() {
        List<Product> productList = new ArrayList<>(); //상품목록에 상품등록
        Product Product = new Product();
        Product.setCategory(KLASS_NAME);
        Product.setStock(9999);
        Product.setId(1);
        Product.setName("test klass");
        productList.add(Product);

        Product = new Product();
        Product.setCategory("kit");
        Product.setStock(0);
        Product.setId(4);
        Product.setName("test kit");
        productList.add(Product);

        String answer = "4";//희망상품을 선택

        CartBean cartBean = new CartBean();
        CartBean.OrderBean orderBean = cartBean.new OrderBean();
        Product wishProduct = new Product();
        this.printOutSetUp();

        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("validateOnProductName", List.class, String.class, CartBean.class, Product.class);
            testMethod.setAccessible(true);
            testMethod.invoke(testClass, productList, answer, cartBean, wishProduct);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }

        Assert.assertTrue(outputStreamCaptor.toString().contains(SOLD_OUT_MSG));
    }

    /**
     * KLASS종류의 상품이 카트에 있는 상태에서 다른 KLASS를 희망리스트에 담을 수 있는지 판단하는 테스트
     * expected: wish product is null
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void validateOnProductNameByKlassLimitTest() {
        List<Product> productList = new ArrayList<>(); //상품리스트에 상품등록
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

        String answer = "4";//4번의 KLASS항목 선택

        CartBean cartBean = new CartBean();//카트에 담긴 상품들
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

        Product wishProduct = new Product();//희망리스트에 물품이 담기면 실패다.
        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("validateOnProductName", List.class, String.class, CartBean.class, Product.class);
            testMethod.setAccessible(true);
            testMethod.invoke(testClass, productList, answer, cartBean, wishProduct);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        Assert.assertNull(wishProduct.getId());
    }
    /**
     * 상품이 카트에 담기고 orderBean(카트 넣기 전 판단단계) 대한 테스트
     * expect: 상품을 추가로 카트에 담았는가, orderBean에는 상품정보가 비어있는가?
     */
    @Test
    public void validateOnAmountTest() {

        //카트에는 이미 상품이 하나 있다.
        CartBean cartBean = new CartBean();
        CartBean.OrderBean orderBean = cartBean.new OrderBean();
        orderBean.setName("this is not klass");
        orderBean.setCategory("KIT");
        orderBean.setId(1);
        orderBean.setAmount(2);
        List<CartBean.OrderBean> orderBeanList = new ArrayList<>();
        orderBeanList.add(orderBean);
        cartBean.setOrderList(orderBeanList);

        //상품번호입력을 저장
        //validateOnAmount에서는 validateOnProductName메소드 호출 다음
        //이므로 orderBean은 빈 객체임
        orderBean = cartBean.new OrderBean();

        Product wishProduct = new Product(); //주문을 원하여 카트에 담고자 하는 상품정보
        wishProduct.setName("test");
        wishProduct.setId(1212);
        wishProduct.setCategory(KLASS_NAME);
        wishProduct.setStock(99999);
        wishProduct.setPrice(2000);

        //수량입력 부분
        String answer = "1";

        try {
            Method testMethod = testClass.getClass().getDeclaredMethod("validateOnAmount", String.class, Product.class, CartBean.class, CartBean.OrderBean.class);
            testMethod.setAccessible(true);
            testMethod.invoke(testClass, answer, wishProduct, cartBean, orderBean);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
        Assert.assertEquals(2, cartBean.getOrderList().size());
        Assert.assertEquals(null, orderBean.getName());
    }
}
