package com.shan.chat.domain.room;

import com.shan.chat.common.exception.ChatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ChatRoomTest {

    @Test
    @DisplayName("그룹 방 생성 시 GROUP 타입과 active=true 상태다")
    void create_groupRoom() {
        ChatRoom room = ChatRoom.create("room-1", "자바 스터디", "member-1");

        assertThat(room.getRoomId()).isEqualTo("room-1");
        assertThat(room.getRoomName()).isEqualTo("자바 스터디");
        assertThat(room.getCreatorId()).isEqualTo("member-1");
        assertThat(room.getRoomType()).isEqualTo(ChatRoom.RoomType.GROUP);
        assertThat(room.isActive()).isTrue();
        assertThat(room.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("방 이름이 빈 값이면 예외가 발생한다")
    void create_blankName_throwsException() {
        assertThatThrownBy(() -> ChatRoom.create("room-1", "  ", "member-1"))
                .isInstanceOf(ChatException.class)
                .hasMessageContaining("빈 값");
    }

    @Test
    @DisplayName("방 이름이 30자를 초과하면 예외가 발생한다")
    void create_tooLongName_throwsException() {
        String longName = "a".repeat(31);

        assertThatThrownBy(() -> ChatRoom.create("room-1", longName, "member-1"))
                .isInstanceOf(ChatException.class)
                .hasMessageContaining("30자");
    }

    @Test
    @DisplayName("정확히 30자인 방 이름은 허용된다")
    void create_exactly30chars_allowed() {
        String name30 = "a".repeat(30);

        assertThatCode(() -> ChatRoom.create("room-1", name30, "member-1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("1:1 방 생성 시 DIRECT 타입이다")
    void createDirect_isDirect() {
        ChatRoom room = ChatRoom.createDirect("room-2", "철수 ↔ 영희", "member-1");

        assertThat(room.getRoomType()).isEqualTo(ChatRoom.RoomType.DIRECT);
        assertThat(room.isDirect()).isTrue();
        assertThat(room.isActive()).isTrue();
    }

    @Test
    @DisplayName("그룹 방은 isDirect()가 false다")
    void groupRoom_isNotDirect() {
        ChatRoom room = ChatRoom.create("room-1", "공개방", "member-1");

        assertThat(room.isDirect()).isFalse();
    }

    @Test
    @DisplayName("deactivate() 호출 시 active가 false가 된다")
    void deactivate_setsActiveFalse() {
        ChatRoom room = ChatRoom.create("room-1", "공개방", "member-1");

        room.deactivate();

        assertThat(room.isActive()).isFalse();
    }

    @Test
    @DisplayName("영속 계층에서 복원한 방의 필드가 그대로 유지된다")
    void restore_preservesFields() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        ChatRoom room = ChatRoom.restore("room-3", "스터디방", "creator-1", now, true, ChatRoom.RoomType.GROUP);

        assertThat(room.getRoomId()).isEqualTo("room-3");
        assertThat(room.getCreatedAt()).isEqualTo(now);
        assertThat(room.getRoomType()).isEqualTo(ChatRoom.RoomType.GROUP);
    }
}

