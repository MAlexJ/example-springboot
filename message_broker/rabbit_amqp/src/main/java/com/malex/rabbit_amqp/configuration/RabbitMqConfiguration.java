package com.malex.rabbit_amqp.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.ErrorHandler;

@Slf4j
@EnableRabbit
@Configuration
public class RabbitMqConfiguration {

  @Value("${rabbitmq.queue}")
  private String queueName;

  @Value("${rabbitmq.exchange}")
  private String exchange;

  @Value("${rabbitmq.routingkey}")
  private String routingkey;

  @Value("${rabbitmq.username}")
  private String username;

  @Value("${rabbitmq.password}")
  private String password;

  @Value("${rabbitmq.host}")
  private String host;

  @Value("${rabbitmq.virtualhost}")
  private String virtualHost;

  @Value("${rabbitmq.reply.timeout:1}")
  private Integer replyTimeout;

  @Value("${rabbitmq.concurrent.consumers:1}")
  private Integer concurrentConsumers;

  @Value("${rabbitmq.max.concurrent.consumers:1}")
  private Integer maxConcurrentConsumers;

  //  Dead Letter Queue configuration
  @Bean
  //  @Qualifier("rabbitmq.demoQueue")
  @Qualifier("rabbitmq.deadLetterQueue")
  public Queue queue() {
    var deadLetter = new HashMap<String, Object>();
    deadLetter.put("x-dead-letter-exchange", "rabbitmq.deadLetterExchange");
    deadLetter.put("x-max-length", 1000);
    return new Queue(queueName, true, false, false, deadLetter);
  }

  @Bean
  //  @Qualifier("rabbitmq.demoExchange")
  @Qualifier("rabbitmq.deadLetterExchange")
  public DirectExchange exchange() {
    return new DirectExchange(exchange);
  }

  @Bean
  public Binding binding(
      @Qualifier("rabbitmq.deadLetterQueue") Queue queue,
      @Qualifier("rabbitmq.deadLetterExchange") DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(routingkey);
  }

  // Queue configuration
  @Bean
  //  @Qualifier("rabbitmq.demoDLQueue")
  @Qualifier("rabbitmq.dLQueue")
  public Queue dlQueue() {
    return new Queue("rabbitmq.demoDLQueue", false);
  }

  @Bean
  //  @Qualifier("rabbitmq.demoDLExchange")
  @Qualifier("rabbitmq.dLExchange")
  public DirectExchange dlExchange() {
    return new DirectExchange("rabbitmq.dLExchange");
  }

  @Bean
  public Binding dlBinding(
      @Qualifier("rabbitmq.dLQueue") Queue queue,
      @Qualifier("rabbitmq.dLExchange") DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with("rabbitmq.routingkey");
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setVirtualHost(virtualHost);
    connectionFactory.setHost(host);
    connectionFactory.setUsername(username);
    connectionFactory.setPassword(password);

    return connectionFactory;
  }

  @Bean
  public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setDefaultReceiveQueue(queueName);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    rabbitTemplate.setReplyAddress(queue().getName());
    rabbitTemplate.setReplyTimeout(replyTimeout);
    rabbitTemplate.setUseDirectReplyToContainer(false);
    return rabbitTemplate;
  }

  @Bean
  public AmqpAdmin amqpAdmin() {
    return new RabbitAdmin(connectionFactory());
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
    final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory());
    factory.setMessageConverter(jsonMessageConverter());
    factory.setConcurrentConsumers(concurrentConsumers);
    factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
    factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
    factory.setAdviceChain(setRetries());
    return factory;
  }

  @Bean
  public ErrorHandler errorHandler() {
    return new ConditionalRejectingErrorHandler(new MyFatalExceptionStrategy());
  }

  public static class MyFatalExceptionStrategy
      extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
    @Override
    public boolean isFatal(Throwable t) {
      if (t instanceof ListenerExecutionFailedException) {
        ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;
        log.error(
            "Failed to process inbound message from queue "
                + lefe.getFailedMessage().getMessageProperties().getConsumerQueue()
                + "; failed message: "
                + lefe.getFailedMessage(),
            t);
      }
      return super.isFatal(t);
    }
  }

  @Bean("operationsInterceptor")
  public RetryOperationsInterceptor setRetries() {
    return RetryInterceptorBuilder.stateless()
        .maxAttempts(4)
        .backOffOptions(1000, 2, 10000)
        .recoverer(new RejectAndDontRequeueRecoverer())
        .build();
  }
}
