package net.class101.homework1.data.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Product {
    @Id
    public Integer id;
    public String name;
    public String category;
    public Integer price;
    public Integer stock;
}
