package com.shan.chat.adapter.out.persistence.room.repository;

import com.shan.chat.adapter.out.persistence.room.entity.RoomParticipantId;
import com.shan.chat.adapter.out.persistence.room.entity.RoomParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomParticipantJpaRepository extends JpaRepository<RoomParticipantJpaEntity, RoomParticipantId> {

    List<RoomParticipantJpaEntity> findByIdRoomId(String roomId);

    List<RoomParticipantJpaEntity> findByIdMemberId(String memberId);

    boolean existsByIdRoomIdAndIdMemberId(String roomId, String memberId);

    void deleteByIdRoomIdAndIdMemberId(String roomId, String memberId);
}
