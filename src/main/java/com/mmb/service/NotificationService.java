// NotificationService.java
package com.mmb.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mmb.dto.NotificationMessage; // 패키지 변경

@Service
public class NotificationService {
	
	private final SimpMessagingTemplate template;
	
	public NotificationService(SimpMessagingTemplate template) {
		this.template = template;
	}

	public void sendNotification(NotificationMessage message) {
		this.template.convertAndSend("/sub/user/" + message.getRecipient(), message.getContent());
	}

}