package net.class101.homework1;

import net.class101.homework1.data.beans.CartBean;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static net.class101.homework1.constants.OrderConstants.*;

public class OrderApplicationTest {
    CartBean cart;
    List<CartBean.OrderBean> orderList;

    private void totalSumTestSetUp() {
        orderList = new ArrayList<>();
        cart = new CartBean();
        CartBean.OrderBean order = cart.new OrderBean();
        order.setAmount(10);
        order.setPrice(4000);
        order.setCategory("KIT");
        orderList.add(order);
    }

    @Test
    public void totalSumTest() {
        totalSumTestSetUp();

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
}
