(function () {
    var input = document.getElementById('message');
    var postFrame = input && input.closest('.post_frame');
    if (!input || !postFrame || typeof socket === 'undefined') return;

    var indicator = document.createElement('div');
    indicator.className = 'typing-indicator';
    indicator.setAttribute('role', 'status');
    indicator.setAttribute('aria-live', 'polite');
    indicator.style.cssText = 'flex:0 0 auto;padding:2px 12px;color:#666;font-size:0.85em;';
    indicator.hidden = true;
    postFrame.parentNode.insertBefore(indicator, postFrame);

    var people = new Map();
    var lastSent = 0;
    var typing = false;
    var idleTimer;
    var language = document.cookie.match(/(?:^|;\s*)lang=([^;]+)/);
    var english = language ? language[1].toLowerCase().startsWith('en')
            : document.documentElement.lang.toLowerCase().startsWith('en');

    function render() {
        var entries = Array.from(people.values());
        indicator.hidden = entries.length === 0;
        indicator.textContent = '';
        entries.slice(0, 5).forEach(function (entry, index) {
            if (index > 0) indicator.appendChild(document.createTextNode(', '));
            var name = document.createElement('span');
            name.textContent = entry.name;
            if (/^[0-9a-f]{6}$/i.test(entry.color)) name.style.color = '#' + entry.color;
            indicator.appendChild(name);
        });
        if (entries.length > 5) {
            indicator.appendChild(document.createTextNode(english
                    ? ' and ' + (entries.length - 5) + ' more'
                    : ' und ' + (entries.length - 5) + ' weitere'));
        }
        if (entries.length > 0) {
            indicator.appendChild(document.createTextNode(entries.length === 1
                    ? (english ? ' is typing\u2026' : ' schreibt \u2026')
                    : (english ? ' are typing\u2026' : ' schreiben \u2026')));
        }
    }

    function stopTyping() {
        clearTimeout(idleTimer);
        if (typing && socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify({category: 'typing', active: false}));
        }
        typing = false;
        lastSent = 0;
    }

    function updateTyping() {
        var text = input.value.trim();
        if (!text || text.startsWith('/')) {
            stopTyping();
            return;
        }
        if (socket.readyState !== WebSocket.OPEN) return;
        var now = Date.now();
        if (!typing || now - lastSent >= 2000) {
            socket.send(JSON.stringify({category: 'typing', active: true}));
            lastSent = now;
            typing = true;
        }
        clearTimeout(idleTimer);
        idleTimer = setTimeout(stopTyping, 3000);
    }

    input.addEventListener('input', updateTyping);
    input.addEventListener('blur', stopTyping);
    input.addEventListener('keydown', function (event) {
        if (event.key === 'Enter') stopTyping();
    }, true);
    postFrame.addEventListener('click', function (event) {
        if (event.target.matches('input[type="button"], button')) stopTyping();
    }, true);
    socket.addEventListener('open', updateTyping);
    socket.addEventListener('close', function () {
        stopTyping();
        people.forEach(function (entry) { clearTimeout(entry.timer); });
        people.clear();
        render();
    });

    var onMessage = socket.onmessage;
    socket.onmessage = function (event) {
        var data = JSON.parse(event.data);
        if (data.category !== 'typing') {
            if (onMessage) onMessage.call(socket, event);
            return;
        }
        if (typeof data.id !== 'string' || typeof data.name !== 'string' || typeof data.active !== 'boolean') return;
        var previous = people.get(data.id);
        if (previous) clearTimeout(previous.timer);
        if (data.active) {
            people.set(data.id, {
                name: data.name,
                color: typeof data.color === 'string' ? data.color : '',
                timer: setTimeout(function () { people.delete(data.id); render(); }, 6000)
            });
        } else {
            people.delete(data.id);
        }
        render();
    };
})();
