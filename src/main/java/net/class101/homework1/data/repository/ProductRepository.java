package net.class101.homework1.data.repository;

import net.class101.homework1.data.entity.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer> {
    List<Product> findAllByIdIn(List<Integer> list);
}
