(function () {
    var input = document.getElementById('message');
    if (!input || !Array.isArray(window.messageHistory)
            || typeof window.addMessageHistory !== 'function'
            || typeof window.messageUp !== 'function'
            || typeof window.messageDown !== 'function') return;

    window.clearMessageHistory = function () {
        window.messageHistory = [];
        window.messageCounter = -1;
    };

    window.addMessageHistory = function (text) {
        if (!text) return;
        window.messageHistory.unshift(text);
        if (window.messageHistory.length > 21) window.messageHistory.pop();
    };

    window.messageUp = function () {
        if (!window.messageHistory.length) return;
        window.messageCounter = Math.min(window.messageCounter + 1, window.messageHistory.length - 1);
        input.value = window.messageHistory[window.messageCounter];
        input.focus();
    };

    window.messageDown = function () {
        if (window.messageCounter < 0) return;
        window.messageCounter--;
        input.value = window.messageCounter < 0 ? '' : window.messageHistory[window.messageCounter];
        input.focus();
    };

    window.clearMessageHistory();
})();
