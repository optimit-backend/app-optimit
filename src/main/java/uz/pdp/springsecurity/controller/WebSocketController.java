package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import uz.pdp.springsecurity.payload.MessageDto;

@Controller
public class WebSocketController {
    @Autowired
    SimpMessagingTemplate template;

//    @PostMapping("/send")
//    public ResponseEntity<Void> sendMessage(@RequestBody MessageDto messageDto) {
////        messageRepository.save(messageMapper.toEntity(messageDto));
//        template.convertAndSend("/topic/message", messageDto);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestBody MessageDto messageDto) {
        template.convertAndSend("/topic/message", messageDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @MessageMapping("/sendMessage")
    public void receiveMessage(@Payload MessageDto messageDto) {
        // receive message from client
    }

    @SendTo("/topic/message")
    public MessageDto broadcastMessage(@Payload MessageDto messageDto) {
        return messageDto;
    }
}
