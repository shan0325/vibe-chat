/**
 * room.js — 방 채팅 클라이언트 스크립트
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

    // 탭 닫기 / 브라우저 닫기 시 sendBeacon으로 안정적 퇴장 처리
    window.addEventListener('beforeunload', function () {
        navigator.sendBeacon('/api/rooms/' + ROOM_ID + '/leave');
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
    // 구독 즉시 서버에서 현재 목록을 브로드캐스트한다 (handleSessionSubscribe 처리)
    stompClient.subscribe('/topic/room/' + ROOM_ID + '/participants', function (frame) {
        renderParticipants(JSON.parse(frame.body));
    });

    // 이전 채팅 내역 로드 (REST)
    loadRoomHistory();
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

    // beforeunload 중복 호출 방지
    window.removeEventListener('beforeunload', arguments.callee);

    $.ajax({
        url: '/api/rooms/' + ROOM_ID + '/leave',
        type: 'POST',
        complete: function () {
            if (stompClient) stompClient.deactivate();
            window.location.href = '/main';
        }
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
