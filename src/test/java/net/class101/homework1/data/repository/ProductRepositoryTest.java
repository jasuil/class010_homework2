package net.class101.homework1.data.repository;

import net.class101.homework1.data.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    /**
     * data confirm test
     */
    @Test
    public void findAllSanityTest() {
        List<Product> list = (List<Product>) productRepository.findAll();
        Assert.notEmpty(list, "data is null");
    }

    @Test
    public void findAllByIdListTest() {
        List<Product> list = productRepository.findAllByIdIn(Arrays.asList(60538, 83791));
        Assert.isTrue(list.size() == 2, "data is null");
    }

    @Test
    public void updateTest() {
        Optional<Product> product = productRepository.findById(97166);
        Integer stock = product.get().getStock();
        product.ifPresent(data -> {
            data.setStock(data.getStock() - 1);
            productRepository.save(data);
        });
        Assert.isTrue(stock - 1 == productRepository.findById(97166).get().getStock(), "something wrong");
    }

}
