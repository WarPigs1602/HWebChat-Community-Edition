(function () {
    var input = document.getElementById('message');
    if (!input || typeof socket === 'undefined') return;

    var completion = null;

    function nicknames() {
        if (typeof user !== 'undefined' && Array.isArray(user)) {
            return user.map(function (entry) { return entry.nick; })
                .filter(function (nick) { return typeof nick === 'string' && nick.length > 0; });
        }
        var target = new URL(socket.url).searchParams.get('target');
        return target ? [target] : [];
    }

    input.addEventListener('input', function () { completion = null; });
    input.addEventListener('blur', function () { completion = null; });
    input.addEventListener('keydown', function (event) {
        if (event.key !== 'Tab' || event.shiftKey || event.ctrlKey || event.altKey
                || event.metaKey || event.isComposing || input.selectionStart !== input.selectionEnd) return;

        var cursor = input.selectionStart;
        if (!completion || input.value !== completion.value || cursor !== completion.cursor) {
            var before = input.value.slice(0, cursor);
            var word = before.match(/\S+$/);
            if (!word) return;
            var after = input.value.slice(cursor);
            var remainingWord = after.match(/^[\p{L}\p{N}_@-]*/u)[0];
            completion = {
                prefix: word[0],
                before: before.slice(0, -word[0].length),
                after: after.slice(remainingWord.length),
                last: null
            };
        }

        var prefix = completion.prefix.toLowerCase();
        var matches = Array.from(new Set(nicknames()))
            .filter(function (nick) { return nick.toLowerCase().startsWith(prefix); })
            .sort(function (a, b) { return a.localeCompare(b); });
        if (!matches.length) {
            completion = null;
            return;
        }

        var index = matches.indexOf(completion.last);
        var nick = matches[(index + 1) % matches.length];
        var value = completion.before + nick + completion.after;
        if (input.maxLength >= 0 && value.length > input.maxLength) {
            completion = null;
            return;
        }
        event.preventDefault();
        var next = completion;
        input.value = value;
        cursor = next.before.length + nick.length;
        input.setSelectionRange(cursor, cursor);
        input.dispatchEvent(new Event('input', {bubbles: true}));
        next.last = nick;
        next.value = value;
        next.cursor = cursor;
        completion = next;
    });
})();
