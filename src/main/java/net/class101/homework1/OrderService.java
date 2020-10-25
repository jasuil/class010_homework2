package net.class101.homework1;

import net.class101.homework1.data.bean.CartBean;
import net.class101.homework1.data.entity.Product;
import net.class101.homework1.data.repository.ProductRepository;
import net.class101.homework1.exceptions.BizException;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

    @PostConstruct
    public void setUp() {
        inputstream = System.in;
        inputStreamReader = new InputStreamReader(inputstream);
        br = new BufferedReader(inputStreamReader);
    }

    @PreDestroy
    public void destroy() throws IOException {
        br.close();
        inputStreamReader.close();
        inputstream.close();
    }

    /**
     * 주문을 선택할지 종료할지 판단하는 메서드
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InterruptedException
     */
    public void main() throws IOException, InvocationTargetException, IllegalAccessException, InterruptedException {

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

        System.out.println(END_MSG);
    }

    /**
     * 주문목록에서 상품을 선택하고 결제하는 인터페이스가 있는 메서드
     * @param cartBean
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     */
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
                validateOnProductName(productList, answer, cartBean, wishProduct);
            } else if(wishProduct.getId() != null) {
                validateOnAmount(answer, wishProduct, cartBean, orderBean);
            }
        }
        return null;
    }

    /**
     * 주문번호를 입력받았을때 유효한 정보인지 판단하는 메서드
     * @param productList all product list
     * @param answer product id
     * @param cartBean
     * @param wishProduct product to move into cart
     */
    private void validateOnProductName(List<Product> productList, String answer, CartBean cartBean, Product wishProduct) {

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
                } else if(alreadyHave) {//카트에 이미 상품을 담고 있는데 똑같은 상품을 또 담고자 한다.
                    System.out.print("\r" + CHANGE_PRODUCT_MSG);
                    answer = br.readLine();
                    while(true) {
                        if (answer.toLowerCase().equals("y")) {
                            BeanUtils.copyProperties(wishProduct, matchedProduct);
                            CartBean.OrderBean removeOrder = null;
                            for(CartBean.OrderBean order : cartBean.getOrderList()) {//카트에 있던 똑같은 상품을 비운다.
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

    /**
     * 상품종류가 이미 카드에 있는 KLASS인지 판단하는 메서드
     * @param matchedProduct
     * @param orderList
     * @throws BizException
     */
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
     * 상품 수량에 대한 유효성을 판단하는 메서드
     * @param answer
     * @param wishProduct
     * @param cartBean
     * @param orderBean
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void validateOnAmount(String answer, Product wishProduct, CartBean cartBean, CartBean.OrderBean orderBean) throws InvocationTargetException, IllegalAccessException {

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
     * 결제하기 전에 유효성을 판단하는 메서드
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
     * 결제하는 과정에 대한 메서드
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

    /**
     * 카트에 담긴 상품내용을 보여주는 메서드
     * @param cartBean
     */
    private void showCartOrder(CartBean cartBean) {
        System.out.println(PARTITION);
        for(CartBean.OrderBean orderBean : cartBean.getOrderList()){
            System.out.println(orderBean.getName() + HYPHEN_MSG + orderBean.getAmount() + AMOUNT_UNIT_MSG);
        }
        System.out.println(PARTITION);
    }

    /**
     * 결제비용을 보여주는 메서드
     * @param orderList
     */
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

    /**
     * 상품 매진을 판단하는 메서드
     * @param idList
     * @param cartBean
     * @throws BizException
     */
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

    /**
     * spring에서  bean을 독립적으로 실행하기 위한 호출 메서드
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        if(!isTest) {
            this.main();
            exit(0);
        }
    }

}
