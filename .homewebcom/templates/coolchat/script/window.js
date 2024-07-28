var win = document.defaultView;
function open_window(add,url) {
    mywin = win.open(add, url, 'toolbar=0, location=0, directories=0, status=0, menubar=0, scrollbars=1, resizable=0, width=800, height=600');
    mywin.focus();
}