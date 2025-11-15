package tmoney.co.kr.hxz.common.error.exception;

import lombok.EqualsAndHashCode;
import tmoney.co.kr.hxz.common.error.BaseCustomException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
public class DomainException extends BaseCustomException {
    private final int code;

    private final List<Object> args;

    public DomainException(int code, String message, Throwable ex) {
        super(code, message, ex);
        this.code = code;
        this.args = new ArrayList<>();
    }

    public DomainException(int code, String message, Object... args) {
        super(code, message, args);
        this.code = code;
        this.args = Arrays.asList(args);
    }

    public DomainException(int code, String message) {
        super(code, message);
        this.code = code;
        this.args = new ArrayList<>();
    }
}
