# rocket_test
分布式事务实验之可靠性消息

消息队列  rocketmq
数据库  mysql
框架  springboot
序列化  avro
持久化  mybaties + mybaties-generator

原理： 分布式事务= 事务消息 + 数据库事务

先发送消息， 再执行本地事务， 提交事务，提交消息
提供回调查询处理异常补偿
