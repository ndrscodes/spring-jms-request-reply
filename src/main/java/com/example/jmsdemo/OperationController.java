package com.example.jmsdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.jms.JMSException;

@RestController
@RequestMapping(path = "/operation")
public class OperationController {

	@Autowired
	private JmsTemplate jms;

	@Autowired
	private JmsMessagingTemplate tpl;
	
	private Logger logger = LoggerFactory.getLogger("operation");

	private record Message(String content) {
	};

	@PostMapping
	public Message callOperation(@RequestBody Message message) {
		logger.info("source is sending {}", message);
		var r = tpl.convertSendAndReceive("echo", message, Message.class);
		logger.info("source received {}", message);
		return r;
	}

	@JmsListener(destination = "echo")
	public void receive(@Payload Message payload, jakarta.jms.Message message) {
		logger.info("receiver received {}", payload);
		var res = new Message(payload.content);
		logger.info("receiver is sending {}", res);
		try {
			jms.convertAndSend(message.getJMSReplyTo(), res);
		} catch (JMSException e) {
			logger.error("failed sending response", e);
		}
	}
}
