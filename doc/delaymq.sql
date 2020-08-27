-- ----------------------------
-- Table structure for message_consumer_record
-- ----------------------------
DROP TABLE IF EXISTS `message_consumer_record`;
CREATE TABLE `message_consumer_record`
(
    `id`              VARCHAR(64) NOT NULL COMMENT 'id',
    `version`         INT(10) UNSIGNED     DEFAULT '0' COMMENT '版本号',
    `updated_time`    datetime             DEFAULT NULL COMMENT '更新时间',
    `created_time`    TIMESTAMP            DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `execute_time`    datetime             DEFAULT NULL COMMENT '执行时间',
    `msg_id`          VARCHAR(64)          DEFAULT NULL COMMENT 'msg_id',
    `topic`           VARCHAR(64)          DEFAULT NULL COMMENT '消息主题',
    `consumer_group`  VARCHAR(64)          DEFAULT NULL COMMENT 'consumer_group',
    `consumer_status` VARCHAR(64)          DEFAULT NULL COMMENT 'consumer_status',
    `retry_count`     INT(11)              DEFAULT NULL COMMENT 'retryCount',
    `retry_next_time` datetime             DEFAULT NULL COMMENT '下次消费时间',
    `consumer_time`   datetime             DEFAULT NULL COMMENT '消费时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'message_consumer_record';

-- ----------------------------
-- Table structure for message_json
-- ----------------------------
DROP TABLE IF EXISTS `message_topic_table`;
CREATE TABLE `message_topic_table`
(
    `id`           varchar(64) NOT NULL COMMENT 'id',
    `version`      int(10) unsigned     DEFAULT '0' COMMENT '版本号',
    `updated_time` datetime             DEFAULT NULL COMMENT '更新时间',
    `created_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `topic_data`   json        NULL COMMENT '值',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = 'topic表';


-- ----------------------------
-- Table structure for message_store
-- ----------------------------
DROP TABLE IF EXISTS `message_store`;
CREATE TABLE `message_store`
(
    `id`            varchar(64) NOT NULL COMMENT 'id',
    `version`       int(10) unsigned     DEFAULT '0' COMMENT '版本号',
    `updated_time`  datetime             DEFAULT NULL COMMENT '更新时间',
    `created_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `message_value` json        NULL COMMENT '值',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '消息表';

-- ----------------------------
-- Table structure for Message:QUEUE:TRANSACTION
-- ----------------------------
DROP TABLE IF EXISTS `MMESSAGE:QUEUE:TRANSACTION`;
CREATE TABLE `MESSAGE:QUEUE:TRANSACTION`
(
    `id`           VARCHAR(64) NOT NULL COMMENT 'ID',
    `version`      INT(10) UNSIGNED     DEFAULT '0' COMMENT '版本号',
    `updated_time` datetime             DEFAULT NULL COMMENT '更新时间',
    `created_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `score`        BIGINT(13)           DEFAULT NULL COMMENT '分数',
    PRIMARY KEY (`id`),
    KEY `index_score` (`score`)
) ENGINE = INNODB COMMENT = '消息事务顺序表';

-- ----------------------------
-- Table structure for message_transaction_store
-- ----------------------------
DROP TABLE IF EXISTS `message_transaction_store`;
CREATE TABLE `message_transaction_store`
(
    `id`            varchar(64) NOT NULL COMMENT 'id',
    `version`       int(10) unsigned     DEFAULT '0' COMMENT '版本号',
    `updated_time`  datetime             DEFAULT NULL COMMENT '更新时间',
    `created_time`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `message_value` json        NULL COMMENT '值',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '事务消息表';

-- ----------------------------
-- Table structure for transaction_msg
-- ----------------------------
DROP TABLE IF EXISTS `message_transaction_retry`;
CREATE TABLE `message_transaction_retry`
(
    `id`           varchar(64) NOT NULL COMMENT 'id',
    `version`      int(10) unsigned     DEFAULT '0' COMMENT '版本号',
    `updated_time` datetime             DEFAULT NULL COMMENT '更新时间',
    `created_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `retry_count`  int(11)     NULL     DEFAULT NULL COMMENT 'retryCount',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '事务记录表';

-- ----------------------------
-- Table structure for message_topic_consumer_time
-- ----------------------------
DROP TABLE IF EXISTS `message_topic_consumer_time`;
CREATE TABLE `message_topic_consumer_time`
(
    `id`             int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `version`        int(10) unsigned          DEFAULT '0' COMMENT '版本号',
    `updated_time`   datetime                  DEFAULT NULL COMMENT '更新时间',
    `created_time`   timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `topic_name`     varchar(64)      NOT NULL COMMENT 'topic',
    `consumer_group` varchar(64)      NOT NULL COMMENT 'consumer_group',
    `queue_num`      int(11)          NOT NULL COMMENT '队列',
    `consume_time`   bigint(13)                DEFAULT NULL COMMENT '记录消费时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `topic_consumer_time` (`topic_name`, `consumer_group`, `queue_num`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 13
  DEFAULT CHARSET = utf8mb4 COMMENT ='message_topic_consumer_time:记录topic';