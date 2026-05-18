package com.tcmseek.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TargetPredictionRabbitConfig {

    public static final String EXCHANGE = "tcmseek.target.prediction";// 目标预测交换机
    public static final String DLX = "tcmseek.target.prediction.dlx";// 死信交换机

    public static final String SUBMIT_QUEUE = "tcmseek.target.prediction.submit.q";// 提交队列
    public static final String POLL_QUEUE = "tcmseek.target.prediction.poll.q";// 轮询队列
    public static final String DLQ = "tcmseek.target.prediction.dlq";// 死信队列

    public static final String ROUTING_SUBMIT = "target.prediction.submit";// 提交路由
    public static final String ROUTING_POLL = "target.prediction.poll";// 轮询路由
    public static final String ROUTING_DLQ = "target.prediction.dlq";// 死信路由


    /**
     * 创建交换机、队列、绑定关系
     * @return
     */
    @Bean
    public Declarables targetPredictionDeclarables() {
        DirectExchange exchange = new DirectExchange(EXCHANGE, true, false);
        DirectExchange dlx = new DirectExchange(DLX, true, false);

        Queue submitQueue = QueueBuilder.durable(SUBMIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
                .build();

        Queue pollQueue = QueueBuilder.durable(POLL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
                .build();

        Queue dlq = QueueBuilder.durable(DLQ).build();


        return new Declarables(
                exchange,
                dlx,
                submitQueue,
                pollQueue,
                dlq,
                BindingBuilder.bind(submitQueue).to(exchange).with(ROUTING_SUBMIT),
                BindingBuilder.bind(pollQueue).to(exchange).with(ROUTING_POLL),
                BindingBuilder.bind(dlq).to(dlx).with(ROUTING_DLQ)
        );
    }
}
