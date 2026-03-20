package com.shan.chat.adapter.in.websocket.room.broadcast;

import com.shan.chat.application.member.dto.MemberInfo;
import com.shan.chat.application.room.dto.RoomDto;
import com.shan.chat.application.room.dto.RoomMessageDto;
import com.shan.chat.application.room.port.out.BroadcastRoomPort;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomBroadcastAdapter implements BroadcastRoomPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastRoomMessage(String roomId, RoomMessageDto message) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }

    @Override
    public void broadcastRoomParticipants(String roomId, List<MemberInfo> participants) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/participants", participants);
    }

    @Override
    public void broadcastRoomList(List<RoomDto> rooms) {
        messagingTemplate.convertAndSend("/topic/rooms", rooms);
    }
}

