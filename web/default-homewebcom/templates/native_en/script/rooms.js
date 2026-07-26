var users = document.querySelector(".users");
function readTextFile(file, callback) {
    const rawFile = new XMLHttpRequest();
    rawFile.overrideMimeType("application/json");
    rawFile.open("GET", file, true);
    rawFile.onreadystatechange = () => {
        if (rawFile.readyState === 4 && rawFile.status == "200") {
            callback(rawFile.responseText);
        }
    }
    rawFile.send(null);
}

//usage:
readTextFile("%PATH_[webchat_json]%", (text) => {
    const data = JSON.parse(text);
	let content = `<span style="font-weight: bold;">They are currently ${data.length} chatter in the chat:</span><br>\n`;
	const room = new Array();
    if (data.length == 0) {
		content += 'No one in chat!';
	} else {
		for (let i = 0; i < data.length; ++i)
		{
		    const k = room.includes(data[i].room);
			if (!k) {
				room[room.length] = data[i].room;
			}
		}
		for (let i = 0; i < room.length; ++i)
		{
			content += printRoom(room[i], data);
		}	
	}
	users.innerHTML = content;
});

function printRoom(name, data) {
    let content = '';
	const room = new Array(); 
    for (let i = 0; i < data.length; ++i)
    {
        if (data[i].room == name) {
			room[room.length] = data[i].name;
		}
    }		 
	content += `They are <span style="font-weight: bold;">${room.length}</span> chatter in the room <span style="font-weight: bold;">${name}</span>:<br>`;
    if (room.length == 0) {
	    content += `The room <span style="font-weight: bold;">${name}</span> is empty!<br><br>`;
	} else {
		for (let i = 0; i < data.length; ++i)
        {		    
            for (let j = 0; j < room.length; ++j)
			{
			    if (room[j] == data[i].name) {
					content += `<span style="color: #${data[i].color};`;
					if (data[i].status >= 3) {
						content += ' font-weight: bold;';
					}			
					if (data[i].gagged == "true") {
						content += ' text-decoration: line-through';
					}
					if (data[i].away.length != 0) {
						content += ` font-style: italic" title="Away: ${data[i].away}`;
					}				
					content += `">${data[i].name}</span>`;
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
