package net.class101.homework1;

import lombok.extern.slf4j.Slf4j;

import net.class101.homework1.data.bean.CartBean;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

/**
 * repository ~ service 계층의 통합 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceIntegrationTest {

    @Autowired
    OrderService testClass;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    public void printOutSetUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * 동일한 상품에 3개의 주문이 동시에 실행될때 multi thread test
     * expect: SOLD_OUT_MSG by soldOutException
     */
    @Test
    public void orderExecuteFailTest() throws ExecutionException, InterruptedException {

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

}
