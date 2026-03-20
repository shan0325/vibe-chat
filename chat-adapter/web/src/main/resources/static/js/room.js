/**
 * room.js — 방 채팅 클라이언트 스크립트
 *
 * room.html 에서 MY_MEMBER_ID, MY_NICKNAME, ROOM_ID, ROOM_NAME 전역 변수를 주입한 뒤
 * 이 파일을 로드해야 한다.
 */

let stompClient = null;

$(document).ready(function () {
    connectStomp();

    $('#send-btn').on('click', sendRoomMessage);
    $('#message-input').on('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendRoomMessage();
        }
    });

    $('#leave-btn').on('click', leaveRoom);

    // 뒤로가기/탭 닫기 전 퇴장
    window.addEventListener('beforeunload', function () {
        leaveRoomSync();
        if (stompClient) stompClient.deactivate();
    });
});

// ──────────────────────────────────────────────────────────
// STOMP 연결
// ──────────────────────────────────────────────────────────

function connectStomp() {
    stompClient = new StompJs.Client({
        webSocketFactory: () => new SockJS('/ws-chat'),
        connectHeaders: { memberId: MY_MEMBER_ID },
        reconnectDelay: 5000,
        onConnect: onStompConnected,
        onDisconnect: function () {
            $('#connection-badge').removeClass('bg-success').addClass('bg-secondary').text('● 연결 끊김');
        },
        onStompError: function (frame) {
            console.error('[STOMP] 오류:', frame.headers['message']);
        }
    });
    stompClient.activate();
}

function onStompConnected() {
    $('#connection-badge').removeClass('bg-secondary').addClass('bg-success').text('● 연결됨');

    // 방 메시지 구독
    stompClient.subscribe('/topic/room/' + ROOM_ID, function (frame) {
        appendRoomMessage(JSON.parse(frame.body));
    });

    // 참여자 목록 구독
    stompClient.subscribe('/topic/room/' + ROOM_ID + '/participants', function (frame) {
        renderParticipants(JSON.parse(frame.body));
    });

    // 이전 채팅 내역 로드
    loadRoomHistory();

    // 현재 참여자 목록 로드
    loadParticipants();
}

// ──────────────────────────────────────────────────────────
// 데이터 로드
// ──────────────────────────────────────────────────────────

function loadRoomHistory() {
    $.get('/api/rooms/' + ROOM_ID + '/history', { limit: 50 }, function (messages) {
        if (!messages || messages.length === 0) return;
        const $list = $('#message-list');
        $list.prepend('<div class="d-flex justify-content-center my-2">' +
            '<span class="badge bg-secondary">— 이전 대화 내역 —</span></div>');
        messages.forEach(function (msg) { appendRoomMessage(msg); });
        scrollToBottom($list[0]);
    });
}

function loadParticipants() {
    $.get('/api/rooms/' + ROOM_ID + '/participants', function (participants) {
        renderParticipants(participants);
    });
}

// ──────────────────────────────────────────────────────────
// 메시지 전송
// ──────────────────────────────────────────────────────────

function sendRoomMessage() {
    const $input  = $('#message-input');
    const content = $input.val().trim();
    if (!content) return;
    if (!stompClient || !stompClient.connected) {
        alert('서버와 연결이 끊겼습니다. 잠시 후 다시 시도하세요.');
        return;
    }
    stompClient.publish({
        destination: '/pub/room/' + ROOM_ID + '/message',
        body: JSON.stringify({ content: content })
    });
    $input.val('').focus();
}

// ──────────────────────────────────────────────────────────
// 방 나가기
// ──────────────────────────────────────────────────────────

function leaveRoom() {
    if (!confirm('방을 나가시겠습니까?')) return;
    leaveRoomSync();
    if (stompClient) stompClient.deactivate();
    window.location.href = '/main';
}

function leaveRoomSync() {
    $.ajax({
        url: '/api/rooms/' + ROOM_ID + '/leave',
        type: 'POST',
        async: false
    });
}

// ──────────────────────────────────────────────────────────
// UI 렌더링
// ──────────────────────────────────────────────────────────

function appendRoomMessage(msg) {
    const $list = $('#message-list');
    let html;

    if (msg.type === 'SYSTEM') {
        html = `<div class="d-flex justify-content-center my-1">
                    <span class="message-bubble system">${esc(msg.content)}</span>
                </div>`;
    } else {
        const isMe = msg.senderId === MY_MEMBER_ID;
        if (isMe) {
            html = `<div class="message-item mine">
                        <span class="message-time align-self-end me-1">${esc(msg.sentAt)}</span>
                        <div class="message-content">
                            <div class="message-bubble mine">${esc(msg.content)}</div>
                        </div>
                    </div>`;
        } else {
            html = `<div class="message-item">
                        <div class="message-content">
                            <div class="message-sender">${esc(msg.senderNickname)}</div>
                            <div class="message-bubble other">${esc(msg.content)}</div>
                        </div>
                        <span class="message-time align-self-end ms-1">${esc(msg.sentAt)}</span>
                    </div>`;
        }
    }

    $list.append(html);
    scrollToBottom($list[0]);
}

function renderParticipants(participants) {
    const $list = $('#participant-list');
    $list.empty();
    participants.forEach(function (member) {
        const isMe = member.memberId === MY_MEMBER_ID;
        $list.append(
            `<div class="online-user-item">
                <span class="online-dot"></span>
                <span class="${isMe ? 'fw-bold' : ''}">
                    ${esc(member.nickname)}${isMe ? ' <small class="text-muted">(나)</small>' : ''}
                </span>
            </div>`
        );
    });
    const count = participants.length;
    $('#participant-count').text(count + '명');
    $('#participant-badge').text(count);
}

// ──────────────────────────────────────────────────────────
// 유틸
// ──────────────────────────────────────────────────────────

function esc(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function scrollToBottom(el) {
    if (el) el.scrollTop = el.scrollHeight;
}

