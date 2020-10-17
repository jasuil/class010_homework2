package net.class101.homework1.exceptions;

public class BizException extends Exception {
    private Integer errCode;

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String msg, int errCode) {
        super(msg);
        this.errCode = errCode;
    }

    public int getErrCode(){
        return this.errCode;
    }
}
