package com.mudiyouyou.rocket.exception;

public class RocketCommonException extends Exception {
    public RocketCommonException(Throwable cause) {
        super(cause);
    }

    public RocketCommonException(String message) {
        super(message);
    }

    public RocketCommonException(String message, Throwable cause) {
        super(message, cause);
    }
}
