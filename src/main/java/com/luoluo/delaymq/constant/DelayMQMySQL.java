package com.luoluo.delaymq.constant;

/**
 * @Date: 2020/07/20
 * @Author: luoluo
 */
public class DelayMQMySQL {

    /**
     * CREATE_TABLE_SQL:创建表和topic
     */
    public static final String CREATE_TABLE_SQL = "CREATE TABLE `%s` (\n" +
            "  `id` varchar(64) NOT NULL COMMENT 'ID',\n" +
            "  `version` INT ( 10 ) UNSIGNED DEFAULT '0' COMMENT '版本号',\n" +
            "  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',\n" +
            "  `created_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',\n" +
            "  `score` bigint(13) DEFAULT NULL COMMENT '分数',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  KEY `index_score` (`score`)\n" +
            ") ENGINE=InnoDB COMMENT='%s顺序表';";

    public static final String GET_MESSAGE_SQL = "SELECT `id`,`version`,`updated_time`,`created_time`,`message_value` FROM `message_store` WHERE `id`=?;";

    public static final String GET_TRANSACTION_MESSAGE_SQL = "SELECT `id`,`version`,`updated_time`,`created_time`,`message_value` FROM `message_transaction_store` WHERE `id`=?;";

    public static final String INSERT_MESSAGE_SQL = "INSERT INTO `message_store` (`id`,`version`,`created_time`,`message_value`) VALUES ( ? , 0 , NOW() , ?);";

    public static final String INSERT_TRANSACTION_MESSAGE_SQL = "INSERT INTO `message_transaction_store` (`id`,`version`,`created_time`,`message_value`) VALUES ( ? , 0 , NOW() , ?);";

    public static final String DELETE_MESSAGE_SQL = "DELETE FROM `message_store` WHERE `id`=?;";

    public static final String GET_MESSAGE_CONSUMER_SQL = "SELECT `id`,`version`,`updated_time`,`created_time`,`msg_id`,`topic`, `consumer_group`,`consumer_status`,`retry_count`,`retry_next_time` FROM `message_consumer_record` WHERE `msg_id`=? AND `consumer_group`=?;";

    public static final String GET_MESSAGE_TRANSACTION_RETRY = "SELECT `retry_count` FROM `message_transaction_retry` WHERE `id` =?;";

    public static final String PULL_MESSAGE_FROM_BEGIN_TO_END = "SELECT `id` FROM `%s` WHERE `score`>=? AND `score`<=? ORDER BY `score` ASC;";

    public static final String PULL_MESSAGE_FROM_BEGIN_TO_END_LIMIT = "SELECT `id` FROM `%s` WHERE `score`>=? AND `score`<=? ORDER BY `score` ASC LIMIT ?,? ;";

    public static final String UPDATE_TOPIC_MESSAGE = "UPDATE `%s` SET `version` =`version`+1 ,updated_time = NOW(),`score` =? WHERE `id` =?;";

    public static final String DELETE_TOPIC_MESSAGE = "DELETE FROM `%s` WHERE `id`=?;";

    public static final String INSERT_TOPIC_MESSAGE = "INSERT INTO `%s` (`id` , `version` ,`created_time` ,`score`) VALUES  ( ? , 0 , NOW() , ?);";

    public static final String UPDATE_TRANSACTION_TOPIC_MESSAGE = "UPDATE `%s` SET `version` =`version`+1 ,updated_time = NOW(), `retry_count`=? WHERE `id` =?;";

    public static final String INSERT_TRANSACTION_TOPIC_MESSAGE = "INSERT INTO `%s` (`id`,`version`,`created_time`,`retry_count`) VALUES (?, 0 ,NOW() ,?);";

    public static final String UPDATE_CONSUMER_MSG_DATA = "UPDATE `message_consumer_record` SET `version` =`version`+1 , `updated_time` = NOW(), `consumer_status` =?, `retry_count` =?,`retry_next_time`=?, `consumer_time` =? WHERE `msg_id` =? AND `consumer_group` =?";

    public static final String INSERT_CONSUMER_MSG_DATA = "INSERT INTO `message_consumer_record` (`id`,`version`,`created_time`,`msg_id`,`execute_time`,`topic`, `consumer_group`,`consumer_status`,`retry_count`,`retry_next_time`,`consumer_time`) VALUES (?, 0 , NOW(), ? , ? , ? , ? , ? ,?, ?, ?);";

    public static final String GET_TOPIC_TABLE_SQL = "SELECT `id`,`version`,`updated_time`,`created_time`,`topic_data` FROM `message_topic_table` WHERE `id`= ?;";

    public static final String INSERT_TOPIC_TABLE_SQL = "INSERT INTO `message_topic_table` (`id` , `version` ,`created_time` ,`topic_data`) VALUES  ( ? , 0 , NOW() , ?);";

    public static final String INSERT_TOPIC_CONSUMER_TIME = "INSERT INTO `message_topic_consumer_time` (`version`,`created_time`,`topic_name`,`consumer_group`,`queue_num`,`consume_time`) VALUES (0 , NOW(), ? , ? , ? , ?);";

    public static final String UPDATE_TOPIC_CONSUMER_TIME = "UPDATE `message_topic_consumer_time` SET `version` =`version`+1 , `updated_time` = NOW(), `consume_time` =? WHERE `topic_name` =? AND `consumer_group` =? AND `queue_num` =? AND `consume_time` <?;";

    public static final String GET_TOPIC_CONSUMER_TIME = "SELECT `id`,`topic_name`,`consumer_group`,`queue_num`,`consume_time` FROM `message_topic_consumer_time` WHERE `topic_name` =:topic_name AND `consumer_group` =:consumer_group";

}
