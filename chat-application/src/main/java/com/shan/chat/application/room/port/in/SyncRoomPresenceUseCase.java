package com.shan.chat.application.room.port.in;

/**
 * 클라이언트가 방 관련 토픽을 구독할 때 현재 상태를 즉시 동기화한다.
 */
public interface SyncRoomPresenceUseCase {
    /** /topic/room/{roomId}/participants 구독 시 현재 참여자 목록 브로드캐스트 */
    void syncParticipants(String roomId);

    /** /topic/rooms 구독 시 현재 방 목록 브로드캐스트 */
    void syncRoomList();
}

