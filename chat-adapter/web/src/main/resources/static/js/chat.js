/**
 * chat.js — 공통 클라이언트 스크립트 (메인 페이지)
 */

let stompClient = null;

$(document).ready(function () {
    if (typeof MY_MEMBER_ID !== 'undefined') {
        connectStomp();
        $('#send-btn').on('click', sendLobbyMessage);
        $('#message-input').on('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendLobbyMessage();
            }
        });

        // 방 만들기 모달
        $('#create-room-btn').on('click', function () {
            $('#room-name-input').val('');
            var modal = new bootstrap.Modal(document.getElementById('createRoomModal'));
            modal.show();
        });

        $('#confirm-create-room-btn').on('click', createRoom);
        $('#room-name-input').on('keydown', function (e) {
            if (e.key === 'Enter') createRoom();
        });

        // 방 이름 검색 (300ms debounce)
        let searchTimer = null;
        $('#room-search-input').on('input', function () {
            clearTimeout(searchTimer);
            const keyword = $(this).val().trim();
            searchTimer = setTimeout(function () {
                searchRooms(keyword);
            }, 300);
        });
    }
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

    loadLobbyHistory();

    stompClient.subscribe('/topic/lobby', function (frame) {
        appendLobbyMessage(JSON.parse(frame.body));
    });

    stompClient.subscribe('/topic/presence', function (frame) {
        renderOnlineMembers(JSON.parse(frame.body));
    });

    // 방 목록 실시간 구독
    stompClient.subscribe('/topic/rooms', function (frame) {
        renderRoomList(JSON.parse(frame.body));
    });

    // 내 1:1 대화 목록 실시간 구독
    stompClient.subscribe('/topic/member/' + MY_MEMBER_ID + '/direct', function (frame) {
        renderDirectRoomList(JSON.parse(frame.body));
    });

    loadOnlineMembers();
    loadRoomList();
    loadDirectRooms();
}

function loadLobbyHistory() {
    $.get('/api/lobby/history', { limit: 50 }, function (messages) {
        if (!messages || messages.length === 0) return;
        const $list = $('#message-list');
        $list.prepend('<div class="d-flex justify-content-center my-2">' +
            '<span class="badge bg-secondary">— 이전 대화 내역 —</span></div>');
        messages.forEach(function (msg) { appendLobbyMessage(msg); });
        scrollToBottom($list[0]);
    });
}

function loadOnlineMembers() {
    $.get('/api/members/online', function (members) {
        renderOnlineMembers(members);
    });
}

function loadRoomList() {
    $.get('/api/rooms', function (rooms) {
        renderRoomList(rooms);
    });
}

function loadDirectRooms() {
    $.get('/api/direct/rooms', function (rooms) {
        renderDirectRoomList(rooms);
    });
}

// ──────────────────────────────────────────────────────────
// 메시지 전송
// ──────────────────────────────────────────────────────────

function sendLobbyMessage() {
    const $input  = $('#message-input');
    const content = $input.val().trim();
    if (!content) return;
    if (!stompClient || !stompClient.connected) {
        alert('서버와 연결이 끊겼습니다. 잠시 후 다시 시도하세요.');
        return;
    }
    stompClient.publish({
        destination: '/pub/lobby/message',
        body: JSON.stringify({ content: content })
    });
    $input.val('').focus();
}

// ──────────────────────────────────────────────────────────
// 방 만들기
// ──────────────────────────────────────────────────────────

function createRoom() {
    const roomName = $('#room-name-input').val().trim();
    if (!roomName) {
        alert('방 이름을 입력하세요.');
        return;
    }
    $.ajax({
        url: '/api/rooms',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ roomName: roomName }),
        success: function (room) {
            bootstrap.Modal.getInstance(document.getElementById('createRoomModal')).hide();
            window.location.href = '/room/' + room.roomId;
        },
        error: function () {
            alert('방 생성에 실패했습니다.');
        }
    });
}

// ──────────────────────────────────────────────────────────
// 방 검색 (Querydsl)
// ──────────────────────────────────────────────────────────

function searchRooms(keyword) {
    $.get('/api/rooms/search', { keyword: keyword }, function (rooms) {
        renderRoomList(rooms);
    });
}

// ──────────────────────────────────────────────────────────
// 1:1 채팅 시작
// ──────────────────────────────────────────────────────────

function startDirectChat(targetMemberId) {
    $.ajax({
        url: '/api/direct/' + targetMemberId,
        type: 'POST',
        success: function (room) {
            window.location.href = '/room/' + room.roomId;
        },
        error: function () {
            alert('1:1 채팅을 시작할 수 없습니다.');
        }
    });
}

// ──────────────────────────────────────────────────────────
// UI 렌더링
// ──────────────────────────────────────────────────────────

function appendLobbyMessage(msg) {
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

function renderOnlineMembers(members) {
    const $list = $('#online-members');
    $list.empty();
    members.forEach(function (member) {
        const isMe = member.memberId === MY_MEMBER_ID;
        if (isMe) {
            $list.append(
                `<div class="online-user-item">
                    <span class="online-dot"></span>
                    <span class="fw-bold">${esc(member.nickname)} <small class="text-muted">(나)</small></span>
                </div>`
            );
        } else {
            $list.append(
                `<div class="online-user-item" onclick="startDirectChat('${esc(member.memberId)}')"
                      title="${esc(member.nickname)}님과 1:1 채팅">
                    <span class="online-dot"></span>
                    <span>${esc(member.nickname)}</span>
                    <small class="text-muted ms-auto">💬</small>
                </div>`
            );
        }
    });
    $('#online-count').text(members.length);
}

function renderRoomList(rooms) {
    const $list = $('#room-list');
    $list.empty();
    // DIRECT 방은 사이드바 채팅방 목록에 표시하지 않는다 (클라이언트 2차 방어)
    const groupRooms = (rooms || []).filter(r => r.roomType === 'GROUP');
    if (groupRooms.length === 0) {
        $list.append('<p class="text-muted small text-center py-2">생성된 방이 없습니다.</p>');
        return;
    }
    groupRooms.forEach(function (room) {
        $list.append(
            `<div class="room-item" onclick="window.location.href='/room/${esc(room.roomId)}'">
                <div class="room-name">${esc(room.roomName)}</div>
                <span class="badge bg-secondary">${room.participantCount}명</span>
            </div>`
        );
    });
}

function renderDirectRoomList(rooms) {
    const $list = $('#direct-room-list');
    $list.empty();
    if (!rooms || rooms.length === 0) {
        $list.append('<p class="text-muted small text-center py-2">진행 중인 대화가 없습니다.</p>');
        return;
    }
    rooms.forEach(function (room) {
        $list.append(
            `<div class="room-item" onclick="window.location.href='/room/${esc(room.roomId)}'">
                <div class="room-name">${esc(room.roomName)}</div>
                <span class="badge bg-info text-dark">${room.participantCount}명</span>
            </div>`
        );
    });
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
