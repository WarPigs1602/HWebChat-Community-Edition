let roomName = "";
let roomIsLocked = "0";
let roomIsStandard = "0";
let roomNoSmilies = "0";
let roomTopic = "";
let nickName = "";
let sessionId = "";
let user = new Array();

function setOwnNickName(nick) {
    nickName = nick;
}

function setTopic(topic) {
    roomTopic = topic;
}

function setRoom(room, isLocked, isStandard, noSmilies, topic) {
    roomName = room;
    roomIsLocked = isLocked;
    roomIsStandard = isStandard;
    roomNoSmilies = noSmilies;
    roomTopic = topic;
}

function setLock(isLocked) {
    roomIsLocked = isLocked;
}

function addUser(nick, gender, registered, color, status, away, awayReason, gag) {
    if(getUser(nick)) {
        removeUser(nick);
    }
	user[user.length] = { 
	    nick: nick,
		gender: gender,
		registered: registered,
		color: color,
		status: status,
		away: away,
		awayReason: awayReason,
		gag: gag,
		birthday: ""
	}
}

function removeUser(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            for (let i2 = i + 1; i2 < user.length; i2++) {
                 user[i2 - 1] = user[i2];
            }
            user.length--;
            break;
        }
    }
}

function getUser(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            return true;
        }
    }
    return false;  
}
function nick_change(nick,new_nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].nick = new_nick;
            break;
        }
    }
    
}
function removeAllUsers() {
    user = new Array();
}

function setBirthday(nick, birthday) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].birthday = birthday;
            break;
        }
    }
}

function setColor(nick, color) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].color = color;
            break;
        }
    }
}

function setStatus(nick, status) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].status = status;
            break;
        }
    }
}

function setRegistered(nick, registered) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].registered = registered;
            break;
        }
    }
}

function setGender(nick, gender) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].gender = gender;
            break;
        }
    }
}

function addAway(nick, awayReason) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].away = "1";
            user[i].awayReason = awayReason;
            break;
        }
    }
}

function removeAway(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].away = "0";
            user[i].awayReason = "";
            break;
        }
    }
}

function addGag(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].gag = "1";
            break;
        }
    }
}

function setSessionId(sid) {
    sessionId = sid;
}

function removeGag(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            user[i].gag = "0";
            break;
        }
    }
}

function isRegistered(nick) {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nick) {
            return user[i].registered == "1";
        }
    }
    return false;
}

function getOwnStatus() {
    for (let i = 0; i < user.length; i++) {
        if (user[i].nick == nickName) {
            return user[i].status;
            break;
        }
    }
}

function drawFrame() {
    right.innerHTML = '';
    let content = "  <p>\n";
    if (roomIsLocked == "1") {
      content += "    Anwesende Chatter im Raum <span style=\"font-weight: bold; color: #BB0000\" title=\"Dieser Raum ist abgeschlossen!\">" + roomName + "</span>:\n";
    } else {
      content += "    Anwesende Chatter im Raum <span style=\"font-weight: bold; color: #00BB00\" title=\"Dieser Raum ist offen!\">" + roomName + "</span>:\n";
    }
    content += "  </p>\n";
    if (roomTopic.length > 0) {
      content += "  <p>\n";
      content += "    Raumthema: <span style=\"font-weight: bold;\">" + roomTopic + "</span>\n";
      content += "  </p>\n";
    }
    content += "  <p>\n";
    for (let i = 0; i < user.length; i++) {
      let un = user[i].nick;
      let ge = user[i].gender;
      let re = user[i].registered;
      let co = user[i].color;
      let st = user[i].status;
      let nam = un;
      if (st >= 3) {
         nam = "<span style=\"font-weight: bold\">"+un+"</span>";
      }
      let as = user[i].away;
      let sr = user[i].awayReason;
	  let birthday = user[i].birthday;
      if (as == 1) {
         nam = "<span style=\"font-style: italic\" title=\"Abgemeldet: "+sr+"\">"+nam+"</span>";
      }
      let ga = user[i].gag;
	  content += " \n";
	  content += "<div id=\"mySidenav"+un+"\" class=\"sidenav\">\n";
      content += "    <a href=\"javascript:void(0)\" class=\"closebtn\" onclick=\"closeNav('"+un+"')\">"+un+" &times;</a><br>\n";
      if(isRegistered(un)) {
         content += " <a href=\"javascript:open_window('?page=%path_profile%&user="+un+"&skin=%skin%&owner=%owner%','profile"+un+"');\">Profil anzeigen</a>\n";
      }
	  if(un != nickName) {
	     content += "    <a href=\"#\" onclick=\"submitTextMessage('/query "+un+"');\">Privatchat mit "+un+" starten...</a>\n";
      }
	  if(getOwnStatus() >= 9 && un != nickName) {
	     if(st == 1) {
	       content += "    <a href=\"#\" onclick=\"submitTextMessage('/su "+un+"');\">Superuser-Rechte vergeben?</a>\n";
	     }
		 if(st == 3) {
		   content += "    <a href=\"#\" onclick=\"submitTextMessage('/rsu "+un+"');\">Superuser-Rechte nehmen?</a>\n";
         }
         content += "    <a href=\"#\" onclick=\"submitTextMessage('/hk "+un+"');\">Chatter rauswerfen?</a>\n";
         content += "    <a href=\"#\" onclick=\"submitTextMessage('/ban -t 3600 "+un+"');\">Chatter begrenzt verbannen?</a>\n";
         content += "    <a href=\"#\" onclick=\"submitTextMessage('/ban "+un+"');\">Chatter dauerhaft verbannen?</a>\n";
         if( getOwnStatus() >= st ) {
		   content += "    <a href=\"#\" onclick=\"submitTextMessage('/k "+un+"');\">Chatter wegkicken?</a>\n";
    	   if (ga == 1) {
             content += "    <a href=\"#\" onclick=\"submitTextMessage('/gag "+un+"');\">Chatter entknebeln?</a>\n";
		   } else {
             content += "    <a href=\"#\" onclick=\"submitTextMessage('/gag "+un+"');\">Chatter knebeln?</a>\n";
		   }
		 }
      } else if(getOwnStatus() >= 5 && un != nickName) {
	     if(st == 1) {
	       content += "    <a href=\"#\" onclick=\"submitTextMessage('/su "+un+"');\">Superuser-Rechte vergeben?</a>\n";
	     }
		 if(st == 3) {
		   content += "    <a href=\"#\" onclick=\"submitTextMessage('/rsu "+un+"');\">Superuser-Rechte nehmen?</a>\n";
         }
         content += "    <a href=\"#\" onclick=\"submitTextMessage('/hk "+un+"');\">Chatter rauswerfen?</a>\n";
         content += "    <a href=\"#\" onclick=\"submitTextMessage('/ban -t 3600 "+un+"');\">Chatter begrenzt verbannen?</a>\n";
		 if( getOwnStatus() >= st ) {
           content += "    <a href=\"#\" onclick=\"submitTextMessage('/k "+un+"');\">Chatter wegkicken?</a>\n";
         if (ga == 1) {
             content += "    <a href=\"#\" onclick=\"submitTextMessage('/gag "+un+"');\">Chatter entknebeln?</a>\n";
		   } else {
             content += "    <a href=\"#\" onclick=\"submitTextMessage('/gag "+un+"');\">Chatter knebeln?</a>\n";
		   }
		 }
      } else if(getOwnStatus() >= 3 && un != nickName) {
	     if(st == 1) {
	       content += "    <a href=\"#\" onclick=\"submitTextMessage('/su "+un+"');\">Superuser-Rechte vergeben?</a>\n";
	     }
		 if(st == 3) {
		   content += "    <a href=\"#\" onclick=\"submitTextMessage('/rsu "+un+"');\">Superuser-Rechte nehmen?</a>\n";
         }
		 if( getOwnStatus() >= st ) {
		   content += "    <a href=\"#\" onclick=\"submitTextMessage('/k "+un+"');\">Chatter wegkicken?</a>\n";
           if (ga == 1) {
             content += "    <a href=\"#\" onclick=\"submitTextMessage('/gag "+un+"');\">Chatter entknebeln?</a>\n";
		   } else {
             content += "    <a href=\"#\" onclick=\"submitTextMessage('/gag "+un+"');\">Chatter knebeln?</a>\n";
		   }
		 }
      }
	  content += "</div>\n";
      if (ge == "m") {
         content += "    <span style=\"font-weight: bold;\">&male;</span> \n";
      } else if (ge == "f") {
         content += "    <span style=\"font-weight: bold;\">&female;</span> \n";
      } else if (ge == "-") {
         content += "    <span style=\"font-weight: bold;\">-</span> \n";
      } else if (ge == "?") {
         content += "    <span style=\"font-weight: bold; color: #009900\">G</span> \n";
      } else  {
         content += "    "+ge+" \n";
      }
	  if (ga == 1) {
         content += "<span onclick=\"openNav('"+un+"')\" style=\"text-decoration: line-through\"><span style=\"color: #"+co+"; cursor: pointer;\">"+nam+"</span></span>";
      } else {
	     content += "<span onclick=\"openNav('"+un+"')\"style=\"color: #"+co+"; cursor: pointer;\">"+nam+"</span>\n";
	  }
	  if (birthday == 1) {
         content += " \ud83c\udf82";
	  }
	  content += "<br>\n";

    }
    content += "  </p>\n";
    right.innerHTML = content;
}
