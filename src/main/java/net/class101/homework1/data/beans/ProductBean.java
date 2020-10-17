package net.class101.homework1.data.beans;

import lombok.Data;

@Data
public class ProductBean {
    public Integer id;
    public String name;
    public String category;
    public Integer price;
    public Integer stock;
}
