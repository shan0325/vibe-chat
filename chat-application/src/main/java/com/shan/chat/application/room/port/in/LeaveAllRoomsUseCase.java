package com.shan.chat.application.room.port.in;

/**
 * WebSocket 세션 종료 시 해당 멤버가 참여 중인 모든 방에서 퇴장 처리
 */
public interface LeaveAllRoomsUseCase {
    void leaveAllRooms(String memberId);
}

