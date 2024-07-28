var browserCol = navigator.appName;

function submitChatInputCol(keyEvent) {
   if(browserCol=="Opera") {
       return true;
   }
   if(keyEvent.keyCode=="13") {
       sendTextCol();
       return false;
   } else {
       return true;
   }
}

function focusTextCol() {
   clearMessageCol();
}

function sendTextCol() {
   var oldMessage = msg.value;
   if (msg.value != "") { 
    msg.value = "/col "+msg.value.substring(1);
    submitTextCol();
   }
   msg.value = oldMessage;
   clearMessageCol();
}

function submitTextCol() 
{
    msg.value = escapeHtml(msg.value);
    var mesg = {
       category: "chat",
       message: msg.value,
       target: ""
    };	
	socket.send(JSON.stringify(mesg));
}

function clearMessageCol() {
    msg.focus();
	
}

function escapeHtmlCol(e){let n=document.createElement("p");return n.appendChild(document.createTextNode(e)),n.innerHTML}
function unescapeHtmlCol(e){let n=document.createElement("p");return n.innerHTML=e,0==n.childNodes.length?"":n.childNodes[0].nodeValue}
