// NotificationController.java
package com.mmb.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.mmb.dto.NotificationMessage; // 패키지 변경
import com.mmb.service.NotificationService; // 패키지 변경

@Controller
public class NotificationController {

	private final NotificationService notificationService;
	
	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	
	@MessageMapping("/send")
    public void send(NotificationMessage message) {
        this.notificationService.sendNotification(message);
    }
}