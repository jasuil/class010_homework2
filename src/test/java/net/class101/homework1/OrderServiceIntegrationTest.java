package net.class101.homework1;

import lombok.extern.slf4j.Slf4j;
import net.class101.homework1.data.beans.CartBean;
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
     * multi thread test
     * expect: biz exception with SoldOutException message
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
