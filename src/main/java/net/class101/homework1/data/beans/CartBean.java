package net.class101.homework1.data.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
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

}
