var win = document.defaultView;

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
var socket = new WebSocket("%PATH_[frame_chat_new]%?skin=%skin%&target=%target%&nick=%nick%&sid=%sid%");


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
        cw.innerHTML += message;
        chat_window.appendChild(cw);
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
