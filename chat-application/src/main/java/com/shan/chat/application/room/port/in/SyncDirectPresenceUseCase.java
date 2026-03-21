package com.shan.chat.application.room.port.in;

public interface SyncDirectPresenceUseCase {
    /**
     * 멤버 접속/퇴장 이벤트 발생 시 해당 멤버와 1:1 채팅 중인
     * 상대방들의 대화 목록 입장 수를 실시간으로 갱신한다.
     */
    void syncDirectPresence(String memberId);

    /**
     * 특정 방의 입장 인원이 변경됐을 때 해당 방 참여자 모두의 사이드바를 갱신한다.
     * DIRECT 방에만 적용한다.
     */
    void syncDirectPresenceForRoom(String roomId);
}

