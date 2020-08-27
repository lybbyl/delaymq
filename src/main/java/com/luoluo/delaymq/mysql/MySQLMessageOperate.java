package com.luoluo.delaymq.mysql;

import com.luoluo.delaymq.common.Message;
import com.luoluo.delaymq.common.MessageOperate;
import com.luoluo.delaymq.common.TopicManager;
import com.luoluo.delaymq.common.TransactionMsgData;
import com.luoluo.delaymq.config.DelayMQProperties;
import com.luoluo.delaymq.constant.DelayMQMySQL;
import com.luoluo.delaymq.constant.MQConstant;
import com.luoluo.delaymq.utils.DateNumUtil;
import com.luoluo.delaymq.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @ClassName MySQLMessageOperate
 * @Description: MYSQL消息操作类
 * @Author luoluo
 * @Date 2020/8/6
 * @Version V1.0
 **/
@Slf4j
public class MySQLMessageOperate implements MessageOperate {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Map<Integer, Boolean> insertTopicConsumer = new HashMap<>();

    @Override
    public String get(String id) {
        MessageStore messageStore = DataAccessUtils.uniqueResult(jdbcTemplate.query(DelayMQMySQL.GET_MESSAGE_SQL, new BeanPropertyRowMapper<>(MessageStore.class), id));
        if (messageStore != null) {
            return messageStore.getMessageValue();
        }
        return null;
    }

    @Override
    public String getMessage(String id) {
        MessageStore messageStore = DataAccessUtils.uniqueResult(jdbcTemplate.query(DelayMQMySQL.GET_MESSAGE_SQL, new BeanPropertyRowMapper<>(MessageStore.class), id));
        if (messageStore != null) {
            return messageStore.getMessageValue();
        }
        return null;
    }

    @Override
    public String getTransactionMessage(String id) {
        MessageStore messageStore = DataAccessUtils.uniqueResult(jdbcTemplate.query(DelayMQMySQL.GET_TRANSACTION_MESSAGE_SQL, new BeanPropertyRowMapper<>(MessageStore.class), id));
        if (messageStore != null) {
            return messageStore.getMessageValue();
        }
        return null;
    }

    @Override
    public ConsumerMsg getConsumerMsgDataClustering(String id, String topic, String consumerGroup) {
        ConsumerMsg consumerMsg = DataAccessUtils.uniqueResult(jdbcTemplate.query(DelayMQMySQL.GET_MESSAGE_CONSUMER_SQL, new BeanPropertyRowMapper<>(ConsumerMsg.class), id, consumerGroup));
        return consumerMsg;
    }

    @Override
    public Collection<String> pullMessageFromBeginToEnd(String topicName, long beginTimeStamp, long endTimeStamp) {
        List<String> keys = jdbcTemplate.queryForList(String.format(DelayMQMySQL.PULL_MESSAGE_FROM_BEGIN_TO_END, topicName), String.class, beginTimeStamp, endTimeStamp);
        return keys;
    }

    @Override
    public Collection<String> pullMessageFromBeginToEnd(String topicName, int queueName, long beginTimeStamp, long endTimeStamp) {
        String topicMsgQueue = TopicManager.getTopicMsgQueue(topicName, queueName);
        return this.pullMessageFromBeginToEnd(topicMsgQueue, beginTimeStamp, endTimeStamp);
    }

    @Override
    public Collection<String> pullMessageFromBeginToEnd(String topicName, int queueName, long beginTimeStamp, long endTimeStamp, int offset, int pullMessageSize) {
        String topicMsgQueue = TopicManager.getTopicMsgQueue(topicName, queueName);
        List<String> keys = jdbcTemplate.queryForList(String.format(DelayMQMySQL.PULL_MESSAGE_FROM_BEGIN_TO_END_LIMIT, topicMsgQueue), String.class, beginTimeStamp, endTimeStamp, offset, pullMessageSize);
        return keys;
    }

    @Override
    public TransactionMsgData getMessageTransactionRetryData(String id) {
        TransactionMsgData transactionMsgData = DataAccessUtils.uniqueResult(jdbcTemplate.query(DelayMQMySQL.GET_MESSAGE_TRANSACTION_RETRY, new BeanPropertyRowMapper<>(TransactionMsgData.class), id));
        return transactionMsgData;
    }

    @Override
    public void persistentConsumerMsgData(String id, String consumerGroup, ConsumerMsg consumerMsg) {
        int update = jdbcTemplate.update(DelayMQMySQL.UPDATE_CONSUMER_MSG_DATA, consumerMsg.getConsumerStatus(), consumerMsg.getRetryCount(), consumerMsg.getRetryNextTime(), consumerMsg.getConsumerTime(), consumerMsg.getMsgId(), consumerGroup);
        if (update == 0) {
            jdbcTemplate.update(DelayMQMySQL.INSERT_CONSUMER_MSG_DATA, UUID.randomUUID().toString(), consumerMsg.getMsgId(), null, consumerMsg.getTopic(), consumerMsg.getConsumerGroup(), consumerMsg.getConsumerStatus(), consumerMsg.getRetryCount(), consumerMsg.getRetryNextTime(), consumerMsg.getConsumerTime());
        }
    }

    @Override
    public void delayTopicQueueMessage(String topic, int queueName, String mgsId, int retryDelayTime) {
        String topicMsgQueue = TopicManager.getTopicMsgQueue(topic + MQConstant.MSG_DELAY, queueName);
        int update = jdbcTemplate.update(String.format(DelayMQMySQL.UPDATE_TOPIC_MESSAGE, topicMsgQueue), System.currentTimeMillis() + retryDelayTime * DateNumUtil.SECOND, mgsId);
        if (update == 0) {
            jdbcTemplate.update(String.format(DelayMQMySQL.INSERT_TOPIC_MESSAGE, topicMsgQueue), mgsId, System.currentTimeMillis() + retryDelayTime * DateNumUtil.SECOND);
        }
    }

    @Override
    public void persistentMessage(String id, Message message) {
        return;
    }

    @Override
    public void delete(String msgId) {
        jdbcTemplate.update(DelayMQMySQL.DELETE_MESSAGE_SQL, msgId);
    }

    @Override
    public void removeMsgIdInQueue(String topicName, int queue, String id) {
        String topicQueue = TopicManager.getTopicMsgQueue(topicName, queue);
        jdbcTemplate.update(String.format(DelayMQMySQL.DELETE_TOPIC_MESSAGE, topicQueue), id);
    }

    @Override
    public void storeMessage(String topicQueue, Message message, String msgId) {
        jdbcTemplate.update(DelayMQMySQL.INSERT_MESSAGE_SQL, msgId, JSONUtil.toJSONString(message.getBody()));
        jdbcTemplate.update(DelayMQMySQL.INSERT_CONSUMER_MSG_DATA, msgId, msgId, new Date(message.getExecuteTime()), message.getTopicName(), null, null, null, null, null);
        jdbcTemplate.update(String.format(DelayMQMySQL.INSERT_TOPIC_MESSAGE, topicQueue), msgId, message.getExecuteTime());
    }

    @Override
    public void storeTransactionMessage(String topicQueue, Message message, String msgId) {
        jdbcTemplate.update(DelayMQMySQL.INSERT_TRANSACTION_MESSAGE_SQL, msgId, JSONUtil.toJSONString(message));
        jdbcTemplate.update(String.format(DelayMQMySQL.INSERT_TOPIC_MESSAGE, topicQueue), msgId, message.getExecuteTime());
        jdbcTemplate.update(String.format(DelayMQMySQL.INSERT_TRANSACTION_TOPIC_MESSAGE, "message_transaction_retry"), msgId, 0);
    }

    @Override
    public String getTopicQueue(String topicName) {
        TopicTable topicTable = DataAccessUtils.uniqueResult(jdbcTemplate.query(DelayMQMySQL.GET_TOPIC_TABLE_SQL, new BeanPropertyRowMapper<>(TopicTable.class), topicName));
        if (topicTable != null) {
            return topicTable.getTopicData();
        }
        return null;
    }

    @Override
    public void createTopicQueue(String topicName, String value) {
        String sql = String.format(DelayMQMySQL.CREATE_TABLE_SQL, topicName, topicName);
        //建立tableName的表
        jdbcTemplate.update(sql);
    }

    @Override
    public void createTopicQueueData(String topicName, String topicData) {
        jdbcTemplate.update(DelayMQMySQL.INSERT_TOPIC_TABLE_SQL, topicName, topicData);
    }

    @Override
    public List<RecordConsumeTime> getRecordTopicConsumeTime(String consumerGroup, String topicName) {
        // 创建 NamedParameterJdbcTemplate 对象
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        // 拼接参数
        Map<String, Object> params = new HashMap<>();
        params.put("topic_name", topicName);
        params.put("consumer_group", consumerGroup);
        // 执行查询
        return namedParameterJdbcTemplate.query(
                DelayMQMySQL.GET_TOPIC_CONSUMER_TIME, // 使用 :ids 作为占位服务
                params,
                new BeanPropertyRowMapper<>(RecordConsumeTime.class) // 结果转换成对应的对象
        );
    }

    @Override
    public void recordTopicConsumeTime(String topic, String consumerGroup, int queueNum, long newBeginTimeStamp) {
        int update = jdbcTemplate.update(DelayMQMySQL.UPDATE_TOPIC_CONSUMER_TIME, newBeginTimeStamp, topic, consumerGroup, queueNum, newBeginTimeStamp);
        if (update == 0 && insertTopicConsumer.getOrDefault(queueNum, true)) {
            try {
                jdbcTemplate.update(DelayMQMySQL.INSERT_TOPIC_CONSUMER_TIME, topic, consumerGroup, queueNum, newBeginTimeStamp);
            } catch (DuplicateKeyException e) {
                insertTopicConsumer.put(queueNum, false);
            }
        }
    }

    @Override
    public void setTopicMessage(String topicMsgQueue, String msgId, long executeTime) {
        jdbcTemplate.update(String.format(DelayMQMySQL.INSERT_TOPIC_MESSAGE, topicMsgQueue), msgId, executeTime);
    }

    @Override
    public boolean deleteTopicMessage(String topicQueueName, String key) {
        int update = jdbcTemplate.update(String.format(DelayMQMySQL.DELETE_TOPIC_MESSAGE, topicQueueName), key);
        if (update == 1) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void commitTransactionMessageToQueue(Message<?> message, String msgId) {
        this.storeMessage(TopicManager.getTopicMsgQueue(message.getTopicName(), 0), message, msgId);
        this.deleteTopicMessage(MQConstant.TRANSACTION_GLOBAL_NAME, msgId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void rollbackTransactionMessageToQueue(String msgId) {
        this.deleteTopicMessage(MQConstant.TRANSACTION_GLOBAL_NAME, msgId);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public String pushTransactionMessageToQueue(Message<?> message) {
        String msgId = UUID.randomUUID().toString();
        this.storeTransactionMessage(MQConstant.TRANSACTION_GLOBAL_NAME, message, msgId);
        return msgId;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delayTransactionMessageToQueue(String msgId, long delayTime, TransactionMsgData transactionMsgData) {
        jdbcTemplate.update(String.format(DelayMQMySQL.UPDATE_TOPIC_MESSAGE, MQConstant.TRANSACTION_GLOBAL_NAME), System.currentTimeMillis() + delayTime, msgId);
        transactionMsgData.setRetryCount(transactionMsgData.getRetryCount() + 1);
        jdbcTemplate.update(String.format(DelayMQMySQL.UPDATE_TRANSACTION_TOPIC_MESSAGE, "message_transaction_retry"), transactionMsgData.getRetryCount(), msgId);
    }
}