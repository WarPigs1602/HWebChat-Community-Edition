const users = document.querySelector(".users");
function readTextFile(file, callback) {
    let rawFile = new XMLHttpRequest();
    rawFile.overrideMimeType("application/json");
    rawFile.open("GET", file, true);
    rawFile.onreadystatechange = function() {
        if (rawFile.readyState === 4 && rawFile.status == "200") {
            callback(rawFile.responseText);
        }
    }
    rawFile.send(null);
}

//usage:
readTextFile("%PATH_[webchat_json]%", function(text){
    let data = JSON.parse(text);
	let content = '<span style="font-weight: bold;">Es sind '+data.length+' anwesende Chatter im Chat:</span><br>\n';
	let room = new Array();
    if(data.length == 0) {
		content += 'Der Chat ist leer!';
	} else {
		for (let i=0;i<data.length;++i)
		{
		    let k = room.includes(data[i].room);
			if (!k) {
				room[room.length] = data[i].room;
			}
		}
		for (let i=0;i<room.length;++i)
		{
			content += printRoom(room[i],data);
		}	
	}
	users.innerHTML = content;
});

function printRoom(name,data) {
    let content = '';
	let room = new Array(); 
    for (let i=0;i<data.length;++i)
    {
        if (data[i].room == name) {
			room[room.length] = data[i].name;
		}
    }		 
	content += 'Es sind <span style="font-weight: bold;">'+room.length+'</span> Chatter im Raum <span style="font-weight: bold;">'+name+'</span>:<br>';
    if( room.length == 0 ) {
	    content += 'Der Raum <span style="font-weight: bold;">'+name+'</span> ist leer!<br><br>';
	} else {
		for (let i=0;i<data.length;++i)
        {		    
            for (let j=0;j<room.length;++j)
			{
			    if (room[j] == data[i].name) {
					content += '<span style="color: #';
					content += data[i].color;
					content += ';';
					if (data[i].status >= 3) {
						content += ' font-weight: bold;';
					}			
					if(data[i].gagged == "true") {
						content += ' text-decoration: line-through';
					}
					if (data[i].away.length != 0) {
						content += ' font-style: italic" title="Abgemeldet: ' + data[i].away;
					}				
					content += '">';
					content += data[i].name;
					content += '</span>';
					if (room.length - 1 != j) {
						content += ', ';
					} else {
						content += '<br>';
					}
				}
			}
        }
    }
	return content;
}