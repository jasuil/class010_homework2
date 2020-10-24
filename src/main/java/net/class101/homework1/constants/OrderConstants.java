package net.class101.homework1.constants;

public class OrderConstants {
    public final static String NOTICE_MSG = "입력(o[order]: 주문, q[quit]: 종료)";
    public final static String PRODUCT_ID_MSG = "상품번호";
    public final static String STOCK_MSG = "재고수";
    public final static String AMOUNT_MSG = "수량";
    public final static String PRODUCT_NAME_MSG = "상품명";
    public final static String PRICE_MSG = "판매가격";
    public final static String END_MSG = "고객님의 주문 감사합니다.";
    public final static String CART_MSG = "주문 내역";
    public final static String TOTAL_SUM_MSG = "주문 비용";
    public final static String PURCHASE_SUM_MSG = "지불 금액";

    public final static String AMOUNT_UNIT_MSG = "개";
    public final static String HYPHEN_MSG = " - ";
    public final static String ORDER_PLEASE_MSG = "주문하실 상품을 입력하세요";
    public final static String ID_NOT_IN_LIST_MSG = "위 리스트에 있는 상품번호를 입력하세요";
    public final static String ID_INPUT_PLEASE_MSG = "상품번호를 입력하세요";
    public final static String AMOUNT_LESS_THAN_STOCK_MSG = "수량은 재고수 이하로만 입력하세요";
    public final static String AMOUNT_IS_NATURAL_NUMBER_MSG = "수량을 0이상의 숫자로 입력하세요";
    public final static String SOLD_OUT_MSG = "재고가 모두 소진되었습니다.";
    public final static String CHANGE_PRODUCT_MSG = "카트에 같은 물건이 있습니다. 바꾸시겠습니까? (y/n)";

    public final static String PARTITION = "--------------------------------";
    public static final String KLASS_NAME = "KLASS";
    public static final String KLASS_ONLY_ONE_IN_CART_MSG = " 종류는 1개만 담으실 수 있습니다.";
    public static final int KLASS_ONLY_ONE_IN_CART_ERROR_CODE = 101;
    public static final int DELIVERY_LIMIT = 50000;
    public static final int DELIVERY_FEE = 5000;

    public static final String SOLD_OUT_EXCEPTION_MSG = "SoldOutException";
    public static final int SOLD_OUT_CODE = 202;
}
