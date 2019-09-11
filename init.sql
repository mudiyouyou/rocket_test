CREATE SCHEMA `test` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin ;

CREATE TABLE `rocket_credit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '用户ID',
  `credit` int(11) DEFAULT NULL COMMENT '积分',
  `freeze_credit` int(11) DEFAULT NULL COMMENT '冻结积分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE `rocket_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` int(11) DEFAULT NULL COMMENT '订单金额分',
  `status` int(11) DEFAULT '1' COMMENT '0已关闭 1待支付 2支付中 3已支付 4退款中 5已退款',
  `user_id` varchar(45) COLLATE utf8_bin DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
