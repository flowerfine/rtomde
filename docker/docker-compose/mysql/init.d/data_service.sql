DROP
database if exists `data_service`;
CREATE
database `data_service` default character set utf8mb4 collate utf8mb4_unicode_ci;
USE
`data_service`;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE `sys_user`
(
    `id`          BIGINT ( 20 ) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(64)  NOT NULL COMMENT '密码',
    `nickname`    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '昵称',
    `email`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '邮箱',
    `phone`       VARCHAR(18)  NOT NULL DEFAULT '' COMMENT '手机号码',
    `deleted`     TINYINT ( 4 ) UNSIGNED NOT NULL COMMENT '删除标识。0: 未删除, 1: 已删除',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator`     VARCHAR(255) NOT NULL DEFAULT 'system' COMMENT '创建人 ',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `updater`     VARCHAR(255) NOT NULL DEFAULT 'system' COMMENT '修改者',
    `comments`    VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY           idx_update_time ( update_time )
) ENGINE = INNODB COMMENT = '用户信息表';

INSERT INTO `sys_user`(`username`, `password`, `nickname`, `email`, `phone`) VALUES ('wangqi', '123', '王奇', 'wangqi@sliew.cn', '19802186317');