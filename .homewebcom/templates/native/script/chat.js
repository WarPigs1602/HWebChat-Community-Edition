var win = document.defaultView;

function scrollToEnd(block, duration) {
    // if block not passed scroll to end of page
    block = block || $("html, body");
    duration = duration || 100;

    // you can pass also block's jQuery selector instead of jQuery object
    if(typeof block === 'string') {
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
let cw = document.createElement("cw");
let chat_window = document.getElementById("chat_window");
var socket = new WebSocket("%PATH_[frame_chat_new]%?skin=%skin%");


// callback-Funktion wird gerufen, wenn die Verbindung erfolgreich aufgebaut werden konnte

socket.onopen = function () {

    console.log("Verbindung wurde erfolgreich aufgebaut");

};

    
   
// callback-Funktion wird gerufen, wenn eine neue Websocket-Nachricht eintrifft

socket.onmessage = function (messageEvent) {
    var msg = JSON.parse(messageEvent.data);
    var message = msg.message;
    var category = msg.category;
    if(category == "error") {
       cw.innerHTML += message;     
       add_window();
    } else if(category.startsWith('privchat_user_')) {
		var user = category.substring(14, category.length());
		if(message == "<!-- scroll -->") {
           scrollToEnd("#chat_window",100);
        } else if(message == "<!-- Ping? -->") {
           return;
        } else if(message == "<!-- Pong! -->") {
           return;
        } else if(message.startsWith('<!-- chat_msg: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var color = text.split("|")[1]; 
           var content = text.split("|")[2]; 
		   text = chat_msg(nick, color, content);
           cw.innerHTML += text;
           chat_window.appendChild(cw);
        }
    } else if(category == "chat") {
        if(message == "<!-- scroll -->") {
           scrollToEnd("#chat_window",100);
        } else if(message == "<!-- Ping? -->") {
           return;
        } else if(message == "<!-- Pong! -->") {
           return;
        } else if(message == "<!-- draw_frame -->") {
           drawFrame();
        } else if(message == "<!-- remove_all_users -->") {
           removeAllUsers();
        } else if(message.startsWith('<!-- set_session_id: ')) {
           var sid = message.match(/"(.*?)"/)[1];
           setSessionId(sid);
        } else if(message.startsWith('<!-- set_own_nick_name: ')) {
           var nick = message.match(/"(.*?)"/)[1];
           setOwnNickName(nick);
        } else if(message.startsWith('<!-- remove_user: ')) {
           var nick = message.match(/"(.*?)"/)[1];
           removeUser(nick);
        } else if(message.startsWith('<!-- redirect: ')) {
           var url = message.match(/"(.*?)"/)[1];
           redirect(url);
        } else if(message.startsWith('<!-- redirect_chat: ')) {
           var url = message.match(/"(.*?)"/)[1];
           redirect_chat(url);
        } else if(message.startsWith('<!-- nick_change: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var new_nick = text.split("|")[1]; 
		   nick_change(nick, new_nick);
        } else if(message.startsWith('<!-- remove_away: ')) {
           var nick = message.match(/"(.*?)"/)[1];
           removeAway(nick);
        } else if(message.startsWith('<!-- remove_gag: ')) {
           var nick = message.match(/"(.*?)"/)[1];
           removeGag(nick);
        } else if(message.startsWith('<!-- set_lock: ')) {
           var lock = message.match(/"(.*?)"/)[1];
           setLock(lock);
        } else if(message.startsWith('<!-- add_gag: ')) {
           var nick = message.match(/"(.*?)"/)[1];
           addGag(nick);
        } else if(message.startsWith('<!-- add_away: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var reason = text.split("|")[1];		   
           addAway(nick,reason);
        } else if(message.startsWith('<!-- set_topic: ')) {
           var topic = message.match(/"(.*?)"/)[1];
           setTopic(topic);
        } else if(message.startsWith('<!-- has_birthday: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var hasBirthday = text.split("|")[1];
           setBirthday(nick,hasBirthday);
        } else if(message.startsWith('<!-- set_status: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var status = text.split("|")[1];
           setStatus(nick,status);
        } else if(message.startsWith('<!-- set_color: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var color = text.split("|")[1];
           setColor(nick,color);
        } else if(message.startsWith('<!-- set_room: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var room = text.split("|")[0];
           var locked = text.split("|")[1];
           var standard = text.split("|")[2];
           var nosmilies = text.split("|")[3];
           var topic = text.split("|")[4];
           if(topic == "null") {
             topic = "";
           }
           setRoom(room, locked, standard, nosmilies, topic);
        } else if(message.startsWith('<!-- add_user: ')) {
           var text = message.match(/"(.*?)"/)[1];
           var nick = text.split("|")[0];
           var gender = text.split("|")[1];
           var registered = text.split("|")[2];
           var color = text.split("|")[3];
           var status = text.split("|")[4];
           var away = text.split("|")[5];
           var awayReason = text.split("|")[6];
           var gag = text.split("|")[7];
           addUser(nick, gender, registered, color, status, away, awayReason, gag);
        } else if(message == "<!-- clear -->") {
           cw.innerHTML = '';
           add_window();
        } else {
           cw.innerHTML += message;
           add_window();
		}
    } else if(category.startsWith('private_chat_')) {
		cw.innerHTML += "<p><span style=\"font-weight: bold\">Fehler:</span> Der Privatchat ist noch nicht implementiert!</p>";
        add_window();
        scrollToEnd("#chat_window",100);
	} else {
		cw.innerHTML += "<p><span style=\"font-weight: bold\">Fehler:</span> Unbekannte Kategorie <span style=\"font-weight: bold\">"+category+"</span></p>";
        add_window();
        scrollToEnd("#chat_window",100);		
	}
};


// callback-Funktion wird gerufen, wenn ein Fehler auftritt

socket.onerror = function (errorEvent) {
	var time = new Date();
	var hour = time.getHours();
	var minute = (time.getMinutes() < 10 ? '0' + time.getMinutes() : time.getMinutes());
	var second = (time.getSeconds() < 10 ? '0' + time.getSeconds() : time.getSeconds());
    cw.innerHTML += "<p><span style=\"font-weight: bold\">"+hour+":"+minute+":"+second+" Fehler:</span> Die Verbindung wurde unerwartet geschlossen!</p>";
    add_window();
    scrollToEnd("#chat_window",100);
};


socket.onclose = function (closeEvent) {
	var time = new Date();
	var hour = time.getHours();
	var minute = (time.getMinutes() < 10 ? '0' + time.getMinutes() : time.getMinutes());
	var second = (time.getSeconds() < 10 ? '0' + time.getSeconds() : time.getSeconds());	
    cw.innerHTML += '<p><span style="font-weight: bold">'+hour+':'+minute+':'+second+' Die Verbindung wurde geschlossen<br>'+hour+':'+minute+':'+second+' Code:</span> ' + closeEvent.code + '<br><span style="font-weight: bold">'+hour+':'+minute+':'+second+' Grund:</span> ' + closeEvent.reason + '</p>';
    add_window();
    scrollToEnd("#chat_window",100);
};
