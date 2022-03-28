package com.usian.mq;

import com.usian.mapper.LocalMessageMapper;
import com.usian.pojo.LocalMessage;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Loser
 * @date 2021年12月06日 15:49
 */
@Component
public class MQSender implements ConfirmCallback {

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private LocalMessageMapper localMessageMapper;

    /**
     * 发送消息
     * @param localMessage
     */
    public void sendMsg(LocalMessage localMessage){
        RabbitTemplate rabbitTemplate = (RabbitTemplate) this.amqpTemplate;
        rabbitTemplate.setConfirmCallback(this);
        CorrelationData correlationData = new CorrelationData(localMessage.getTxNo());
        rabbitTemplate.convertAndSend("order_exchange","order.add", JsonUtils.objectToJson(localMessage),correlationData);
    }

    /**
     * =======4. 响应返回
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //======5. 确认回调,修改消息状态响应返回
        String txNo = correlationData.getId();
        LocalMessage localMessage = new LocalMessage();
        localMessage.setState(1);
        localMessage.setTxNo(txNo);
        localMessageMapper.updateByPrimaryKeySelective(localMessage);
    }
}
