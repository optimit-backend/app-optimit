package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.pdp.springsecurity.entity.Message;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.mapper.MessageMapper;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.MessageDto;
import uz.pdp.springsecurity.repository.MessageRepository;
import uz.pdp.springsecurity.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageMapper mapper;
    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public MessageDto receiveMessage(@Payload MessageDto messageDto) {
        Message message = new Message();
        message.setMessage(messageDto.getMessage());
        message.setReceiverId(messageDto.getReceiverId());
        message.setSenderId(messageDto.getSenderId());
        messageRepository.save(message);
        System.out.println(messageDto);
        return messageDto;
    }

    @MessageMapping("/private-message")
    public MessageDto recMessage(@Payload MessageDto messageDto) {
        simpMessagingTemplate.convertAndSendToUser(messageDto.getReceiverName(), "/private", messageDto);
        System.out.println(messageDto.toString());
        return messageDto;
    }

    @GetMapping("/api/{senderId}/{receivedId}")
    public HttpEntity<?> getMessageList(@PathVariable UUID senderId, @PathVariable UUID receivedId) {
        List<Message> all = messageRepository.findAllBySenderIdAndReceiverId(senderId, receivedId);
        if (all.isEmpty()) {
            return ResponseEntity.status(409).body(new ApiResponse("Bo'sh", false));
        }
        List<MessageDto> mapperDtoList = mapper.toDtoList(all);
        return ResponseEntity.status(200).body(new ApiResponse("all", true, mapperDtoList));
    }

    @GetMapping("/api/getUsers/{businessId}")
    public HttpEntity<?> getUsers(@PathVariable UUID businessId) {
        List<User> allByBusinessId = userRepository.findAllByBusiness_Id(businessId);
        return ResponseEntity.status(200).body(new ApiResponse("all", true, allByBusinessId));
    }
}
