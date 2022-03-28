package com.usian.listener;

import com.rabbitmq.client.Channel;
import com.usian.service.SearchItemService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Loser
 * @date 2021年11月29日 17:49
 */
@Component //放入容器
public class SearchMQListener {

    @Autowired
    private SearchItemService searchItemService;

    /**
     * RabbitMQ 发送消息
     * @param ItemId
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(
            value = "search_queue"),
            exchange = @Exchange(value = "item_exchange",type = ExchangeTypes.TOPIC),
            key = {"*.*"}
    ))
    public void listener(String ItemId, Message message, Channel channel) throws IOException {
        try{
            searchItemService.insertDocument(ItemId);
            //设置手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
