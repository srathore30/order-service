package sfa.order_service.constant;

import lombok.Getter;

@Getter
public enum ApiErrorCodes implements Error {
    INVALID_INPUT(1001, "Invalid request input"),
    NOT_FOUND(1002, "Resource not found"),
    INVALID_SEARCH_CRITERIA(1003, "Invalid search criteria"),
    PRODUCT_NOT_FOUND(1004, "Product not found"),
    PRODUCT__PRICE_NOT_FOUND(1005, "Product price not found"),
    ORDER_NOT_FOUND(1006, "Order not found"),
    TRANSACTION_NOT_FOUND(1007,"Transaction not found" ),
    INSUFFICIENT_BALANCE(1008,"Insufficient balance" ),
    MEMBER_NOT_FOUND(1009,"Member not found" ),
    CLIENT_NOT_FOUND(1010,"Client not found" ),
    INVALID_SALES_LEVEL(1011,"Invalid sales level" ),
    OUTLET_NOT_FOUND(1012,"Outlet not found" ),
    BEET_NOT_FOUND(1013,"Beet not found" ),
    NO_SALES(1014, "No sales");

    private int errorCode;
    private String errorMessage;

    ApiErrorCodes(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
    }

