var win = document.defaultView;

function setMessageCount(count) {
    const link = document.getElementById("unread-messages-link");
    if (!link) return;
    const box = document.getElementById("chat_frame_info");
    link.setAttribute("data-count", String(count));
    link.textContent = count + " neue Nachrichten";
    if (count > 0) {
        if (box) box.classList.add("has-unread");
        if (box) box.classList.remove("zero");
    } else {
        if (box) box.classList.add("zero");
        if (box) box.classList.remove("has-unread");
    }
}

function scrollToEnd(block, duration) {
    // if block not passed scroll to end of page
    block = block || $("html, body");
    duration = duration || 100;

    // you can pass also block's jQuery selector instead of jQuery object
    if (typeof block === 'string') {
        block = $(block);
    }

    // if exists at list one block
    if (block.length) {
        block.animate({
            scrollTop: block.get(0).scrollHeight
        }, duration);
    }
}

function redirect(url) {
    win.top.location.href = url;
}

function redirect_chat(url) {
    win.location.href = url;
}

function add_window() {
	chat_window.appendChild(cw);
}
var cw = document.createElement("cw");
var chat_window = document.getElementById("chat_window");
var socket = new WebSocket("%PATH_[frame_chat_new]%?skin=%skin%");


// callback-Funktion wird gerufen, wenn die Verbindung erfolgreich aufgebaut werden konnte

socket.onopen = () => {

    console.log("Verbindung wurde erfolgreich aufgebaut");

};

    
   
// callback-Funktion wird gerufen, wenn eine neue Websocket-Nachricht eintrifft

socket.onmessage = (messageEvent) => {
    const msg = JSON.parse(messageEvent.data);
    const { message, category } = msg;
    if (category == "error") {
       cw.innerHTML += message;     
       add_window();
    } else if (category.startsWith('privchat_user_')) {
		const user = category.substring(14, category.length());
		if (message == "<!-- scroll -->") {
           scrollToEnd("#chat_window", 100);
        } else if (message == "<!-- Ping? -->") {
           return;
        } else if (message == "<!-- Pong! -->") {
           return;
        } else if (message.startsWith('<!-- chat_msg: ')) {
           const textMatch = message.match(/"(.*?)"/)[1];
           const [nick, color, content] = textMatch.split("|");
		   const text = chat_msg(nick, color, content);
           cw.innerHTML += text;
           chat_window.appendChild(cw);
        }
    } else if (category == "chat") {
        if (message == "<!-- scroll -->") {
           scrollToEnd("#chat_window", 100);
        } else if (message == "<!-- Ping? -->") {
           return;
        } else if (message == "<!-- Pong! -->") {
           return;
        } else if (message == "<!-- draw_frame -->") {
           drawFrame();
        } else if (message == "<!-- remove_all_users -->") {
           removeAllUsers();
        } else if (message.startsWith('<!-- set_session_id: ')) {
           const sid = message.match(/"(.*?)"/)[1];
           setSessionId(sid);
        } else if (message.startsWith('<!-- set_own_nick_name: ')) {
           const nick = message.match(/"(.*?)"/)[1];
           setOwnNickName(nick);
        } else if (message.startsWith('<!-- set_message_count: ')) {
           const count = parseInt(message.match(/"(.*?)"/)[1], 10) || 0;
           setMessageCount(count);
        } else if (message.startsWith('<!-- remove_user: ')) {
           const nick = message.match(/"(.*?)"/)[1];
           removeUser(nick);
        } else if (message.startsWith('<!-- redirect: ')) {
           const url = message.match(/"(.*?)"/)[1];
           redirect(url);
        } else if (message.startsWith('<!-- redirect_chat: ')) {
           const url = message.match(/"(.*?)"/)[1];
           redirect_chat(url);
        } else if (message.startsWith('<!-- nick_change: ')) {
           const text = message.match(/"(.*?)"/)[1];
           const [nick, new_nick] = text.split("|");
		   nick_change(nick, new_nick);
        } else if (message.startsWith('<!-- remove_away: ')) {
           const nick = message.match(/"(.*?)"/)[1];
           removeAway(nick);
        } else if (message.startsWith('<!-- remove_gag: ')) {
           const nick = message.match(/"(.*?)"/)[1];
           removeGag(nick);
        } else if (message.startsWith('<!-- set_lock: ')) {
           const lock = message.match(/"(.*?)"/)[1];
           setLock(lock);
        } else if (message.startsWith('<!-- add_gag: ')) {
           const nick = message.match(/"(.*?)"/)[1];
           addGag(nick);
        } else if (message.startsWith('<!-- add_away: ')) {
           const text = message.match(/"(.*?)"/)[1];
           const [nick, reason] = text.split("|");
           addAway(nick, reason);
        } else if (message.startsWith('<!-- set_topic: ')) {
           const topic = message.match(/"(.*?)"/)[1];
           setTopic(topic);
        } else if (message.startsWith('<!-- has_birthday: ')) {
           const text = message.match(/"(.*?)"/)[1];
           const [nick, hasBirthday] = text.split("|");
           setBirthday(nick, hasBirthday);
        } else if (message.startsWith('<!-- set_status: ')) {
           const text = message.match(/"(.*?)"/)[1];
           const [nick, status] = text.split("|");
           setStatus(nick, status);
        } else if (message.startsWith('<!-- set_color: ')) {
           const text = message.match(/"(.*?)"/)[1];
           const [nick, color] = text.split("|");
           setColor(nick, color);
        } else if (message.startsWith('<!-- set_room: ')) {
           const text = message.match(/"(.*?)"/)[1];
           let [room, locked, standard, nosmilies, topic] = text.split("|");
           if (topic == "null") {
             topic = "";
           }
           setRoom(room, locked, standard, nosmilies, topic);
        } else if (message.startsWith('<!-- add_user: ')) {
           const text = message.match(/"(.*?)"/)[1];
           const [nick, gender, registered, color, status, away, awayReason, gag] = text.split("|");
           addUser(nick, gender, registered, color, status, away, awayReason, gag);
        } else if (message == "<!-- clear -->") {
           cw.innerHTML = '';
           add_window();
        } else {
           cw.innerHTML += message;
           add_window();
		}
    } else if (category.startsWith('private_chat_')) {
		cw.innerHTML += "<p><span style=\"font-weight: bold\">Fehler:</span> Der Privatchat ist noch nicht implementiert!</p>";
        add_window();
        scrollToEnd("#chat_window", 100);
	} else {
		cw.innerHTML += `<p><span style="font-weight: bold">Fehler:</span> Unbekannte Kategorie <span style="font-weight: bold">${category}</span></p>`;
        add_window();
        scrollToEnd("#chat_window", 100);		
	}
};


// callback-Funktion wird gerufen, wenn ein Fehler auftritt

socket.onerror = (errorEvent) => {
	const time = new Date();
	const hour = time.getHours();
	const minute = (time.getMinutes() < 10 ? '0' + time.getMinutes() : time.getMinutes());
	const second = (time.getSeconds() < 10 ? '0' + time.getSeconds() : time.getSeconds());
    cw.innerHTML += `<p><span style="font-weight: bold">${hour}:${minute}:${second} Fehler:</span> Die Verbindung wurde unerwartet geschlossen!</p>`;
    add_window();
    scrollToEnd("#chat_window", 100);
};


socket.onclose = (closeEvent) => {
	const time = new Date();
	const hour = time.getHours();
	const minute = (time.getMinutes() < 10 ? '0' + time.getMinutes() : time.getMinutes());
	const second = (time.getSeconds() < 10 ? '0' + time.getSeconds() : time.getSeconds());	
    cw.innerHTML += `<p><span style="font-weight: bold">${hour}:${minute}:${second} Die Verbindung wurde geschlossen<br>${hour}:${minute}:${second} Code:</span> ${closeEvent.code}<br><span style="font-weight: bold">${hour}:${minute}:${second} Grund:</span> ${closeEvent.reason}</p>`;
    add_window();
    scrollToEnd("#chat_window", 100);
};
