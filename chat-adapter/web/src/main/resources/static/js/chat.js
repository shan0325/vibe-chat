/**
 * chat.js — 공통 클라이언트 스크립트
 *
 * main.html 에서 MY_MEMBER_ID, MY_NICKNAME 전역 변수를 주입한 뒤
 * 이 파일을 로드해야 한다.
 *
 * 의존 라이브러리 (main.html CDN):
 *   - jQuery 3.x
 *   - SockJS 1.x
 *   - @stomp/stompjs 7.x (전역: StompJs)
 */

// ============================================================
// STOMP 클라이언트
// ============================================================
let stompClient = null;

$(document).ready(function () {
    // main.html 에서만 STOMP 연결 및 이벤트 등록
    if (typeof MY_MEMBER_ID !== 'undefined') {
        connectStomp();
        $('#send-btn').on('click', sendLobbyMessage);
        $('#message-input').on('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendLobbyMessage();
            }
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

    // 로비 메시지 구독
    stompClient.subscribe('/topic/lobby', function (frame) {
        appendLobbyMessage(JSON.parse(frame.body));
    });

    // 접속자 목록 구독
    stompClient.subscribe('/topic/presence', function (frame) {
        renderOnlineMembers(JSON.parse(frame.body));
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
// UI 렌더링
// ──────────────────────────────────────────────────────────

/** 메시지 목록에 메시지 추가 */
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
                        <div class="message-bubble mine">${esc(msg.content)}</div>
                    </div>`;
        } else {
            html = `<div class="message-item">
                        <div>
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

/** 접속자 목록 렌더링 */
function renderOnlineMembers(members) {
    const $list = $('#online-members');
    $list.empty();
    members.forEach(function (member) {
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
    $('#online-count').text(members.length);
}

// ──────────────────────────────────────────────────────────
// 유틸
// ──────────────────────────────────────────────────────────

/** XSS 방지용 HTML 이스케이프 */
function esc(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/** 날짜 → HH:MM 문자열 */
function formatTime(date) {
    const d = date || new Date();
    return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0');
}

/** 메시지 목록 최하단 스크롤 */
function scrollToBottom(el) {
    if (el) el.scrollTop = el.scrollHeight;
}
