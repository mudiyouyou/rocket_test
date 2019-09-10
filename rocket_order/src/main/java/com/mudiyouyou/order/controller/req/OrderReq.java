package com.mudiyouyou.order.controller.req;

import lombok.Data;

@Data
public class OrderReq {
    private Integer id;
    private Integer amount;
    private String userId;

}
