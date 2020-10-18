package net.class101.homework1.data.beans;

import lombok.Data;
import net.class101.homework1.exceptions.BizException;

import java.util.ArrayList;
import java.util.List;

import static net.class101.homework1.constants.OrderConstants.*;

@Data
public class CartBean {
    List<OrderBean> orderList;

    public CartBean() {
        orderList = new ArrayList<>();
    }

    @Data
    public class OrderBean {
        public Integer id;
        public String name;
        public String category;
        public Integer price;
        public Integer amount;
    }

    public void addCart(OrderBean order) throws BizException {
        boolean isKlassHave = orderList.stream().anyMatch(ord -> {
            if(ord.getCategory().equals(KLASS_NAME)) {
                return true;
            } else {
                return false;
            }
        });
        if(order.getCategory().equals(KLASS_NAME) && isKlassHave) {
            throw new BizException(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG, KLASS_ONLY_ONE_IN_CART_ERROR_CODE);
        } else {
            orderList.add(order);
        }
    }
}
