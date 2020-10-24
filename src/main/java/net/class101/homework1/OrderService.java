package net.class101.homework1;

import net.class101.homework1.data.beans.CartBean;
import net.class101.homework1.data.beans.Product;
import net.class101.homework1.data.repository.ProductRepository;
import net.class101.homework1.exceptions.BizException;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;
import static net.class101.homework1.constants.OrderConstants.*;

@Service
public class OrderService implements CommandLineRunner {

    static InputStream inputstream;
    static InputStreamReader inputStreamReader;
    static BufferedReader br;

    @Autowired
    private ProductRepository productRepository;

    @Value(value = "${class.test}")
    boolean isTest;

    public void main() throws IOException, InvocationTargetException, IllegalAccessException {

        inputstream = System.in;
        inputStreamReader = new InputStreamReader(inputstream);
        br = new BufferedReader(inputStreamReader);
        String answer;

        selectLoop: while (true) {

            System.out.print("\r" + NOTICE_MSG + " : ");
            answer = br.readLine();

            switch (answer) {
                case "q":
                    break selectLoop;
                case "o":
                    String flag = orderProcessor(new CartBean());
                    if(flag != null && flag.equals("q")) {
                        break selectLoop;
                    }
                    break;
            }

        }
        br.close();
        inputStreamReader.close();
        inputstream.close();

        System.out.println(END_MSG);
    }

    private String orderProcessor(CartBean cartBean) throws IllegalAccessException, InvocationTargetException, IOException {
        Product wishProduct = new Product();//before putting into the cart

        System.out.println(PRODUCT_ID_MSG + "\t" + PRODUCT_NAME_MSG + "\t" + PRICE_MSG + "\t" + STOCK_MSG);
        List<Product> productList = (List<Product>) productRepository.findAll();
        productList.forEach(d -> System.out.println(d.getId() + "\t" + d.getName() + "\t" + d.getPrice() + "\t" + d.getStock()));
        String answer;
        CartBean.OrderBean orderBean = cartBean.new OrderBean(); //product in cart

        while(true) {
            if(wishProduct.getId() == null) {
                System.out.print("\r" + PRODUCT_ID_MSG + " : ");
            } else {
                System.out.print("\r" + AMOUNT_MSG + " : ");
            }

            answer = br.readLine();
            if(answer.equals("q")) {
                return "q";
            }
            if(wishProduct.getId() == null && answer.equals(" ") && cartBean.getOrderList().size() > 0) {
                executePurchase(cartBean);
                break;
            }
            if(wishProduct.getId() == null) {
                validateOnProductName(productList, answer, cartBean, orderBean, wishProduct);
            } else if(wishProduct.getId() != null) {
                validateOnAmount(answer, wishProduct, cartBean, orderBean);
            }
        }
        return null;
    }

    /**
     *
     * @param productList all product list
     * @param answer product id
     * @param cartBean
     * @param orderBean
     * @param wishProduct
     */
    private void validateOnProductName(List<Product> productList, String answer, CartBean cartBean, CartBean.OrderBean orderBean, Product wishProduct) {

        try {
            Integer inputId = Integer.valueOf(answer);
            List<Product> matchList = productList.stream().filter(product -> {
                if(product.getId().equals(inputId)) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());

            if(matchList.size() == 0) {
                System.out.println(ID_NOT_IN_LIST_MSG);
            } else {
                Product matchedProduct = matchList.get(0);
                boolean alreadyHave = cartBean.getOrderList().stream().anyMatch(product -> {
                    if(product.getId().equals(inputId)) {
                        return true;
                    } else {
                        return false;
                    }
                });

                klassValidation(matchedProduct, cartBean.getOrderList());

                if(matchedProduct.getStock().compareTo(0) < 1) {
                    System.out.println(SOLD_OUT_MSG);
                } else if(alreadyHave) {
                    System.out.print("\r" + CHANGE_PRODUCT_MSG);
                    answer = br.readLine();
                    while(true) {//change already having product in wish list?
                        if (answer.toLowerCase().equals("y")) {
                            orderBean.setId(inputId);
                            BeanUtils.copyProperties(wishProduct, matchedProduct);
                            CartBean.OrderBean removeOrder = null;
                            for(CartBean.OrderBean order : cartBean.getOrderList()) {
                                if(order.getId().equals(matchedProduct.getId())) {
                                    removeOrder = order;
                                }
                            }
                            cartBean.getOrderList().remove(removeOrder);
                            break;
                        } else if(answer.toLowerCase().equals("n")) {
                            break;
                        }
                    }

                } else {
                    klassValidation(matchedProduct, cartBean.getOrderList());
                    orderBean.setId(inputId);
                    BeanUtils.copyProperties(wishProduct, matchedProduct);
                }
            }

        } catch(NumberFormatException | IOException | IllegalAccessException | InvocationTargetException e) {
            System.out.println(ID_INPUT_PLEASE_MSG);

        } catch (BizException e) {
            if(e.getErrCode().equals(KLASS_ONLY_ONE_IN_CART_ERROR_CODE)) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void klassValidation(Product matchedProduct, List<CartBean.OrderBean> orderList) throws BizException {
        boolean isKlassHave = orderList.stream().anyMatch(ord -> {
            if(ord.getCategory().equals(KLASS_NAME)) {
                return true;
            } else {
                return false;
            }
        });
        if(matchedProduct.getCategory().equals(KLASS_NAME) && isKlassHave) {
            throw new BizException(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG, KLASS_ONLY_ONE_IN_CART_ERROR_CODE);
        }
    }
    /**
     *
     * @param answer
     * @param wishProduct
     * @param cartBean
     * @param orderBean
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void validateOnAmount(String answer, Product wishProduct, CartBean cartBean, CartBean.OrderBean orderBean) throws InvocationTargetException, IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        try {
            Integer amount = Integer.valueOf(answer);

            if(amount.compareTo(0) < 1) {
                System.out.println(AMOUNT_IS_NATURAL_NUMBER_MSG);
            } else if(wishProduct.getCategory().equals(KLASS_NAME) && amount.compareTo(1) != 0) {
                System.out.println(KLASS_NAME + KLASS_ONLY_ONE_IN_CART_MSG);
            } else if (wishProduct.getStock().compareTo(amount) < 0) {
                System.out.println(AMOUNT_LESS_THAN_STOCK_MSG);
            } else {
                BeanUtils.copyProperties(orderBean, wishProduct);
                orderBean.setAmount(amount);
                BeanUtils.copyProperties(wishProduct, new Product());

                //move it to the cart bean
                CartBean.OrderBean cpBean = cartBean.new OrderBean();
                BeanUtils.copyProperties(cpBean, orderBean);
                BeanUtils.copyProperties(orderBean, cartBean.new OrderBean());
                cartBean.getOrderList().add(cpBean);
            }
        } catch(NumberFormatException e) {
            System.out.println(AMOUNT_IS_NATURAL_NUMBER_MSG);
        }

    }

    /**
     *
     * @param cartBean
     * @return boolean-value loop continue
     */
    private void executePurchase(CartBean cartBean) {
        if(cartBean.getOrderList().size() == 0) {
            System.out.println(ORDER_PLEASE_MSG);
        }
        System.out.println(CART_MSG + " : ");
        try {
            orderExecute(cartBean);
        }catch (BizException e) {
            if(e.getErrCode().equals(SOLD_OUT_CODE)) {
                System.out.println(SOLD_OUT_MSG);
            }
        }

    }

    /**
     * order to the system is to be synchronized
     * @param cartBean
     * @throws BizException
     */
    @Transactional
    synchronized private void orderExecute(CartBean cartBean) throws BizException {

        showCartOrder(cartBean);

        List<Integer> idList = new ArrayList<>();
        Map<Integer, Integer> amountMapById = new HashMap<>();
        for(CartBean.OrderBean orderBean : cartBean.getOrderList()) {
            if(!orderBean.getCategory().equals(KLASS_NAME)) {
                amountMapById.put(orderBean.getId(), orderBean.getAmount());
                idList.add(orderBean.getId());
            }
        }
        totalSum(cartBean.getOrderList());
        findOutSoldOut(idList, cartBean);

        List<Product> product = productRepository.findAllByIdIn(idList);
        product.parallelStream().forEach(data -> {
            data.setStock(data.getStock() - amountMapById.get(data.getId()));
            productRepository.save(data);
        });
    }

    private void showCartOrder(CartBean cartBean) {
        System.out.println(PARTITION);
        for(CartBean.OrderBean orderBean : cartBean.getOrderList()){
            System.out.println(orderBean.getName() + HYPHEN_MSG + orderBean.getAmount() + AMOUNT_UNIT_MSG);
        }
        System.out.println(PARTITION);
    }

    private void totalSum(List<CartBean.OrderBean> orderList) {
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

        System.out.println(PURCHASE_SUM_MSG + " : " + df.format(totalFee));
        System.out.println(PARTITION);
    }

    private void findOutSoldOut(List<Integer> idList, CartBean cartBean) throws BizException {
        List<Product> validateList = productRepository.findAllByIdIn(idList);
        for(Product product : validateList) {
            for (CartBean.OrderBean order : cartBean.getOrderList()) {
                if (!order.getCategory().equals(KLASS_NAME) && order.getId().equals(product.getId())) {
                    if (order.getAmount().compareTo(product.getStock()) > 0) {
                        throw new BizException(SOLD_OUT_EXCEPTION_MSG, SOLD_OUT_CODE);
                    }
                    break;//avoid wasting full search
                }
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if(!isTest) {
            this.main();
            exit(0);
        }
    }
}
