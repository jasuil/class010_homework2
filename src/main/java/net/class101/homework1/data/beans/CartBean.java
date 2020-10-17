package net.class101.homework1.data.beans;

import lombok.Data;
import net.class101.homework1.exceptions.BizException;

import java.util.List;

@Data
public class CartBean {
    static final String KLASS_NAME = "KLASS";
    List<OrderBean> orderList;

    @Data
    public class OrderBean {
        public Integer id;
        public String name;
        public String category;
        public Integer price;
        public Integer stock;
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
            throw new BizException(KLASS_NAME + " 종류는 1개만 담으실 수 있습니다.");
        } else {
            orderList.add(order);
        }
    }
}
