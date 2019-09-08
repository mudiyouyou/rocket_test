package com.mudiyouyou.controller.req;

import lombok.Data;

@Data
public class OrderReq {
    private Integer id;
    private Integer amount;
    private String userId;

}
