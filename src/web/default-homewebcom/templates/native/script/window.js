var win = document.defaultView;
function open_window(add, url) {
    mywin = win.open(add, url, 'toolbar=0, location=0, directories=0, status=0, menubar=0, scrollbars=1, resizable=0, width=800, height=600');
    mywin.focus();
}

var chatTabs = {};
var activeTabId = 'chat';
var tabCounter = 0;

function initChatTabs() {
    var tabsContainer = document.getElementById('chat_tabs');
    if (!tabsContainer) return;
    if (Object.keys(chatTabs).length > 0) return;
    tabsContainer.innerHTML = '';
    chatTabs = { 'chat': { id: 'chat', title: 'Chat', url: null, iframe: null } };
    activeTabId = 'chat';
    renderChatTabs();
}

function renderChatTabs() {
    var tabsContainer = document.getElementById('chat_tabs');
    if (!tabsContainer) return;
    tabsContainer.innerHTML = '';
    for (var id in chatTabs) {
        var tab = chatTabs[id];
        var el = document.createElement('div');
        el.className = 'chat-tab' + (id === activeTabId ? ' active' : '');
        el.setAttribute('data-tab-id', id);
        el.onclick = (function(tabId) { return function() { switchChatTab(tabId); }; })(id);
        var title = document.createElement('span');
        title.textContent = tab.title || 'Tab';
        el.appendChild(title);
        if (id !== 'chat') {
            var closeBtn = document.createElement('button');
            closeBtn.className = 'chat-tab-close';
            closeBtn.innerHTML = '&times;';
            closeBtn.setAttribute('aria-label', 'Schließen');
            closeBtn.onclick = (function(tabId) {
                return function(event) {
                    event.stopPropagation();
                    closeChatTab(tabId);
                };
            })(id);
            el.appendChild(closeBtn);
        }
        tabsContainer.appendChild(el);
    }
}

function switchChatTab(tabId) {
    if (!chatTabs[tabId]) return;
    activeTabId = tabId;
    var isPanel = (tabId !== 'chat');
    var rightFrame = document.getElementById('right');
    var postFrame = document.querySelector('.post_frame');
    if (rightFrame) rightFrame.style.display = isPanel ? 'none' : '';
    if (postFrame) postFrame.style.display = isPanel ? 'none' : '';
    for (var id in chatTabs) {
        var tab = chatTabs[id];
        if (tab.iframe) {
            tab.iframe.style.display = (id === tabId) ? '' : 'none';
        }
    }
    renderChatTabs();
}

function closeChatTab(tabId) {
    if (!chatTabs[tabId] || tabId === 'chat') return;
    var tab = chatTabs[tabId];
    if (tab.iframe && tab.iframe.parentNode) {
        tab.iframe.parentNode.removeChild(tab.iframe);
    }
    delete chatTabs[tabId];
    if (activeTabId === tabId) {
        activeTabId = 'chat';
    }
    switchChatTab(activeTabId);
}

function showChatPanel(url, title) {
    var container = document.getElementById('chat_frame_container');
    if (!container) {
        open_window(url, title || 'Panel');
        return;
    }
    var existingId = null;
    for (var id in chatTabs) {
        if (chatTabs[id].url === url) {
            existingId = id;
            break;
        }
    }
    var tabId;
    if (existingId) {
        tabId = existingId;
    } else {
        tabCounter++;
        tabId = 'panel' + tabCounter;
        chatTabs[tabId] = { id: tabId, title: title || 'Panel', url: url, iframe: null };
    }
    var tab = chatTabs[tabId];
    if (!tab.iframe) {
        var overlay = document.createElement('div');
        overlay.className = 'chat-panel-overlay';
        overlay.id = 'chat-panel-' + tabId;
        var iframe = document.createElement('iframe');
        iframe.className = 'chat-panel-iframe';
        iframe.src = url;
        iframe.setAttribute('title', title || 'Panel');
        overlay.appendChild(iframe);
        overlay.style.display = 'none';
        container.appendChild(overlay);
        tab.iframe = overlay;
    }
    switchChatTab(tabId);
}

function closeChatPanel() {
    closeChatTab(activeTabId);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initChatTabs);
} else {
    initChatTabs();
}
