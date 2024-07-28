var messageHistory = new Array(20);
var messageCounter = -1;
var browser = navigator.appName;
let message = document.getElementById("message");
clearMessageHistory();

function clearMessageHistory() {
    for(var i = 0; i <= 20; i++) {
        messageHistory[i] = "";
    }
}

function addMessageHistory(message) {
    for(var i = 20; i >= 0; i--) {
        if(i != 0) {
            messageHistory[i] = messageHistory[i - 1];
        } else {
            messageHistory[i] = message;
        }
    }
}

function submitOpera() {
   if(browser=="Opera") {
       sendText();
       return false;
   }
}

function submitChatInput(keyEvent) {
   if(browser=="Opera") {
       return true;
   }
   if(keyEvent.keyCode=="38") {
       messageUp();
       return true;
   } 
   if(keyEvent.keyCode=="40") {
       messageDown();
       return true;
   }
   if(keyEvent.keyCode=="13") {
       sendText();
       return false;
   } else {
       return true;
   }
}

function focusText() {
   clearMessage();
}

function sendText() {
   addMessageHistory(message.value);
   messageCounter = -1;
   if (message.value != "") { 
     submitText();
     $('body > .popover').popover('hide');
   }
   clearMessage();
}

function submitText() 
{
    message.value = escapeHtml(message.value);
    var msg = {
       category: "chat",
       message: message.value,
       target: ""
    };	
	socket.send(JSON.stringify(msg));
}

function submitTextMessage(text) 
{
    text = escapeHtml(text);
    var msg = {
       category: "chat",
       message: text,
       target: ""
    };	
	socket.send(JSON.stringify(msg));
}

function setTextMessage(text) 
{
    message.value = text;
	message.focus();
}

function clearMessage() { 
    message.value=""; 
    message.focus();
}

function emoticon(text) {
    message.value=message.value+text;
    message.focus();
}

function messageUp() {
    messageCounter++; 
    if (messageCounter > 20) {
        messageCounter = 20;
    }
    message.value = messageHistory[messageCounter];
    message.focus();
}

function messageDown() {
    messageCounter--;  
    if (messageCounter < 0) {
        messageCounter = 0;
        message.value = "";
    } else {
        message.value = messageHistory[messageCounter];
        message.focus();
    }
}

function escapeHtml(e){let n=document.createElement("p");return n.appendChild(document.createTextNode(e)),n.innerHTML}
function unescapeHtml(e){let n=document.createElement("p");return n.innerHTML=e,0==n.childNodes.length?"":n.childNodes[0].nodeValue}
