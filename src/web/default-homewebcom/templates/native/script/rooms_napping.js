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
	let content = '';
	content = printRoom('%room2%', data);
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
	content += `Es sind <span style="font-weight: bold;">${room.length}</span> Chatter im Raum <span style="font-weight: bold;">${name}</span>:<br>`;
    if (room.length == 0) {
	    content += `Der Raum <span style="font-weight: bold;">${name}</span> ist leer!<br><br>`;
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
						content += ` font-style: italic" title="Abgemeldet: ${data[i].away}`;
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
