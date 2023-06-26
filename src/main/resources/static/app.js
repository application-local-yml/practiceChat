let stompClient = null;
let fromId = 0;
let ChatMessageUl = null;
let ChatMessageTime = null;
let currentDate = null; // 날짜 변수 추가

function getChatMessages() {
    console.log("fromId : " + fromId);
    fetch(`/usr/chat/rooms/${chatRoomId}/messages?fromId=${fromId}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }})
        .then(response => response.json())
        .then(body => {
            drawMessages(body);
            console.log(body);
        });
}

function drawMessages(messages) {
    console.log(messages);
    if (messages.length > 0) {
        fromId = messages[messages.length - 1].message_id;
    }

    messages.forEach((message, index) => {
        const newItem = document.createElement("li");
        console.log(message);
        console.log("memberId : " + memberId);
        console.log("memberName : " + memberName);

        if (message.sender.user_id === memberId) {
            newItem.classList.add("sender");
        } else {
            newItem.classList.add("receiver");
        }

        if (message.type == "ENTER"){
            newItem.classList.add("center");
            newItem.textContent = `${message.content}`;
        } else if (message.type == "LEAVE"){
            newItem.classList.add("center");
            newItem.textContent = `${message.content}`;
        } else {
            const createdAt = new Date(message.created_at);

            // 가공된 시간 표시 형식 (MM:SS)
            const hours = String(createdAt.getHours()).padStart(2, '0');
            const minutes = String(createdAt.getMinutes()).padStart(2, '0');
            const formattedTime = `${hours}:${minutes}`;

            // 새로운 메세지를 추가하기 전에 이전 메세지와의 시간을 비교합니다
            const lastItem = ChatMessageUl.lastChild;
            const lastItemTimeElement = lastItem && lastItem.querySelector('.message-time');

            if (lastItemTimeElement && lastItemTimeElement.textContent === formattedTime) {
                lastItemTimeElement.remove();  // .style.visibility = 'hidden'; 대신에 .remove();를 사용합니다
            }

            if (message.sender.user_id === memberId) {
                newItem.innerHTML = `<div><div class="message-content">${message.content}</div><span class="message-time">${formattedTime}</span></div> `;
            } else {
                newItem.innerHTML = `<div><div class="message-content">${message.sender.username} : ${message.content}</div> <span class="message-time">${formattedTime}</span></div>`;
            }
        }

        const messageDate = new Date(message.created_at).toLocaleDateString();
        console.log("currentDate : " + currentDate);
        if (currentDate !== messageDate) {
            const newDateItem = document.createElement("li");
            newDateItem.classList.add("center");
            newDateItem.textContent = messageDate;
            ChatMessageUl.appendChild(newDateItem);
            currentDate = messageDate;
        }

        ChatMessageUl.appendChild(newItem);
    });

    scrollToBottom();
}

function ChatWriteMessage(form) {

    form.content.value = form.content.value.trim();

    stompClient.send(`/app/chats/${chatRoomId}/sendMessage`, {}, JSON.stringify({content: form.content.value}));

    form.content.value = '';
    form.content.focus();
}

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    const headers = {
        'X-CSRF-TOKEN': token,
    };

    stompClient.connect(headers, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe(`/topic/chats/${chatRoomId}`, function (data) {
            getChatMessages();
        });

        stompClient.subscribe(`/topic/chats/${chatRoomId}/kicked`, function (data) {
            console.log(data);
            if (data.body == memberId) {
                disconnect();
                location.reload();
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", function() {
    ChatMessageUl = document.querySelector('.chat__message-ul');
    getChatMessages();
    connect();
});

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log('Disconnected');
    }
}

function scrollToBottom() {
    const chatMessages = document.querySelector('.chat-messages');
    chatMessages.scrollTop = chatMessages.scrollHeight;
}