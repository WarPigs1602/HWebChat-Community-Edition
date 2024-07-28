function add_friend(nick, color) {
	return 'Du hast <span style="color: #' + color + '">' + nick + '</span> als Freund hinzugef&uuml;gt!';
}
function add_message(nick, color, message) {
	return 'Du hast <span style="color: #' + color + '">' + nick + '</span> folgende Nachricht hinterlassen: ' + message + '';
}
function add_topic(nick, color, topic) {
	return '<span style="color: #' + color + '">' + nick + '</span> setzt das Thema des Raums auf: <span style="font-weight: bold">' + topic + '</span>';
}
function away_end(nick, color, reason) {
	return '<span style="color: #' + color + '">' + nick + '</span> ist wieder zur&uuml;ck von: <span style="font-weight: bold">' + reason + '</span>';
}
function away_start(nick, color, reason) {
	return '<span style="color: #' + color + '">' + nick + '</span> ist kurz mal weg: <span style="font-weight: bold">' + reason + '</span>';
}
function ban_duration(duration) {
	return 'Ein Ban darf nicht l&auml;nger als <span style="font-weight: bold">' + duration + '</span> Sekunden sein!';
}
function ban_error() {
	return 'Zum bannen sind Zahlen erforderlich!';
}
function ban_min() {
	return 'Die Bandauer ist zu kurz!';
}
function ban_no_nick() {
	return 'Zum bannen ist ein Ziel erforderlich!';
}
function ban_reason() {
	return 'gebannt von <span style="color: #' + color + '">' + nick + '</span>: ' + reason + '';
}
function ban_reason_text() {
	return 'Kein genauer Grund angegeben!';
}
function ban_text(nick, reason) {
	return 'Du hast den nicht anwesenden Chatter <span style="font-weight: bold">' + nick + '</span> gebannt (' + reason + ')';
}
function banlist(name, banned_color, banner, banner_color, time, duration, reason) {
	return '  <tr>\n    <td>\n      <span style="color: #' + banned_color + '">' + name + '</span>\n    </td>\n    <td>\n      ' + reason + '\n    </td>\n    <td>\n      <span style="color: #' + banner_color + '">' + banner + '</span>\n    </td>\n    <td>\n      ' + time + '\n    </td>\n    <td>\n      ' + duration + '\n    </td>\n  </tr><br>';
}
function banlist_empty() {
	return 'niemand ist gebannt!<br>';
}
function banlist_footer() {
	return '</table>';
}
function banlist_header() {
	return '<table>\n  <tr style="font-weight: bold;">\n    <td>\n      Wer ist Gebannt?\n    </td>\n    <td>\n      Warum?\n    </td>\n    <td>\n      Von wem?\n    </td>\n    <td>\n      Wann?\n    </td>\n    <td>\n      Dauer?\n    </td>\n  </tr>\n      ';
}
function banlist_title() {
	return '<span style="font-weight: bold">Gebannte Chatter:</span><br>';
}
function banned(nick) {
	return '<span style="font-weight: bold">' + nick + '</span> ist bereits gebannt!';
}
function bright_color(color) {
	return 'Deine Wunschfarbe <span style="font-weight: bold">' + color + '</span> ist zu hell!';
}
function change_room(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> geht in den Raum <span style="font-weight: bold">' + room + '</span>.';
}
function change_room_beam(nick, color, beamer_nick, beamer_color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> wird von  <span style="color: #' + beamer_color + '">' + beamer_nick + '</span> in den Raum <span style="font-weight: bold">' + room + '</span> gebeamt.';
}
function change_room_catch(nick, color, catcher_nick, catcher_color, room) {
	return '<span style="color: #' + catcher_color + '">' + catcher_nick + '</span> zieht <span style="color: #' + color + '">' + nick + '</span> in den Raum <span style="font-weight: bold">' + room + '</span>!';
}
function chat_fade(nick, color, color1, color2, content) {
	return '<span style="color: #' + color + '">&lt;' + nick + '&gt; </span> <span style="background-image:linear-gradient(90deg, #' + color1 + ', #' + color2 + '); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">' + content + '</span><br>';
}
function chat_me(nick, color, content) {
	return '<span style="font-style: italic;">&raquo; <span style="font-weight: bold; color: #' + color + ';">' + nick + ' ' + content + '</span></span><br>';
}
function chat_msg(nick, color, content) {
	return '<span style="color: #' + color + '">&lt;' + nick + '&gt; ' + content + '</span><br>';
}
function chat_shout(content) {
	return 'schreit: ' + content.toUpperCase();
}
function chat_shutdown() {
	return '<span style="font-style: italic">Der Chat wird neu gestartet...</span>';
}
function chgrights(nick, color, old_status, new_status) {
	return 'Du hast die Rechte von dem Chatter <span style="color: #' + color + '">' + nick + '</span> von <span style="font-weight: bold">' + old_status + '</span> auf <span style="font-weight: bold">' + new_status + '</span> geändert!';
}
function chgrights_invalid() {
	return 'Du kannst aufgrund einer falschen oder ung&uuml;ltigen Eingabe, die Rechte nicht &auml;ndern. (G&uuml;tig sind nur Zahlen im Bereich von 1 bis 10)';
}
function chgrights_online(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat deine Rechte geändert!';
}
function close_room(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> sperrt den Raum zu!';
}
function del_friend(nick, color) {
	return 'Du hast <span style="color: #' + color + '">' + nick + '</span> als Freund wieder entfernt!';
}
function del_su(nick, color, su_nick, su_color) {
	return '<span style="color: #' + color + '">' + nick + '</span> entzieht <span style="color: #' + su_color + '">' + su_nick + '</span> die Superuser-Rechte!';
}
function del_supervisor() {
	return 'Supervisor Modus deaktiviert!';
}
function del_topic(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> l&ouml;scht das Thema wieder!';
}
function delete_picture(nick, color) {
	return 'Du hast das Profilbild von  <span style="color: #' + color + '">' + nick + '</span> entfernt!';
}
function dice(nick, color, number, eyes) {
	return '<span style="color: #' + color + '">' + nick + '</span> w&uuml;rfelt eine <span style="font-weight: bold">' + number + '</span> mit einem W&uuml;rfel mit <span style="font-weight: bold">' + eyes + '</span> Augen!';
}
function dice_error() {
	return 'Zum w&uuml;rfeln sind Zahlen erforderlich!';
}
function dice_max(eyes) {
	return 'Die Anzahl der Augen darf nur <span style="font-weight: bold">' + eyes + '</span> St&uuml;ck betragen!';
}
function dice_min() {
	return 'Die Anzahl der Augen darf nicht kleiner als <span style="font-weight: bold">2</span> sein!';
}
function draw_line() {
	return '<hr style="border: 0px; border-top: solid 1px #000000; border-bottom: solid 1px #FFFFFF; width: 100%;">';
}
function empty_duration() {
	return 'Permanent!';
}
function exists_room(room) {
	return 'Der Raum <span style="font-weight: bold">' + room + '</span> ist bereits vorhanden!';
}
function fade(nick, color_old, color_new) {
	return '<span style="font-weight: bold; font-style: italic; background-image:linear-gradient(90deg, #' + color_old + ', #' + color_new + '); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">&raquo; ' + nick + ' wechselt seine Farbe...</span><br>';
}
function flood_repeat_msg() {
	return 'Du wiederholst Dich! Ab jetzt werden wiederholte Eingaben ignoriert!';
}
function friend_list_no_friends() {
	return '&raquo; Du hast noch keine Freunde..<br>';
}
function friend_list_offline(user, color, login_date) {
	return '<span style="color: #' + color + '; font-weight: bold">' + user + '</span> war zuletzt am <span style="font-weight: bold">' + login_date + '</span> online.<br>';
}
function friend_list_online(user, color, login_time, idle_time) {
	return '<span style="color: #' + color + '; font-weight: bold">' + user + '</span> ist seit <span style="font-weight: bold">' + login_time + '</span> im Chat und ist seit <span style="font-weight: bold">' + idle_time + '</span> Sekunden ruhig...<br>';
}
function friend_list_title(count) {
	return '&raquo; Du hast <span style="font-weight: bold">' + count + '</span> Freunde:<br>';
}
function gagged() {
	return 'Du bist geknebelt!';
}
function getCurrentTime() {
	var time = new Date();
	var hour = time.getHours();
	var minute = (time.getMinutes() < 10 ? '0' + time.getMinutes() : time.getMinutes());
	var second = (time.getSeconds() < 10 ? '0' + time.getSeconds() : time.getSeconds());
	return hour + ':' + minute + ':' + second;
}
function has_moderator(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat bereits schon das Stimmrecht!';
}
function has_su(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> verf&uuml;gt bereits &uuml;ber Superuser-Rechte!';
}
function highlight_msg() {
	return '<span style="color: #' + color + '">%content%</span>';
}
function invalid(command) {
	return 'Der Befehl <span style="font-weight: bold">/' + command + '</span> enth&auml;lt nicht gen&uuml;gend Parameter!';
}
function invalid2(command) {
	return 'Der Befehl <span style="font-weight: bold">/' + command + '</span> hat keine weitere Parameter!';
}
function invalid_color(color) {
	return 'Deine Wunschfarbe <span style="font-weight: bold">' + color + '</span> ist ung&uuml;ltig';
}
function invite_query() {
	return 'Du hast <span style="color: #' + color + '">' + nick + '</span> zum Privatchat eingeladen!';
}
function invited_query() {
	return 'Du wurdest von <span style="color: #' + color + '">' + nick + '</span> zum Privatchat eingeladen!';
}
function is_friend(nick, color) {
	return 'Du bist bereits mit <span style="color: #' + color + '">' + nick + '</span> befreundet!';
}
function is_not_registered() {
	return 'Du bist nicht registriert!';
}
function is_you(command) {
	return 'Du kannst den Befehl <span style="font-weight: bold">/' + command + '</span> nicht an Dich selbst verwenden!';
}
function join(nick, color, skin) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat den Chat betreten.';
}
function join_friends(nick, color, skin) {
	return 'Dein Freund <span style="color: #' + color + '">' + nick + '</span> hat den Chat betreten...';
}
function join_room(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> kommt aus dem Raum <span style="font-weight: bold">' + room + '</span> herein.';
}
function join_room_beam(nick, color, beamer_nick, beamer_color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> wurde von <span style="color: #' + beamer_color + '">' + beamer_nick + '</span> aus den Raum <span style="font-weight: bold">' + room + '</span> hierher gebeamt!';
}
function join_supervisor(nick, color, ip, host, skin) {
	return '<span style="color: #' + color + '">' + nick + '</span> (' + host + ') hat den Chat betreten.';
}
function kick_room(nick, color, kick_nick, kick_color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> kickt <span style="color: #' + kick_color + '">' + kick_nick + '</span> in den Raum <span style="font-weight: bold">' + room + '</span>.';
}
function land_room(nick, color, room, kick_nick, kick_color, old_room) {
	return '<span style="color: #' + kick_color + '">' + kick_nick + '</span> wurde von <span style="color: #' + color + '">' + nick + '</span> hierher gekickt!';
}
function locked_room(room) {
	return 'Der Raum <span style="font-weight: bold">' + room + '</span> ist abgeschlossen!';
}
function locked_room_db(room, reason) {
	return 'Der Raum <span style="font-weight: bold">' + room + '</span> kann nicht betreten werden... (Grund: <span style="font-weight: bold">' + reason + '</span>)';
}
function locked_room_db_kick(nick, color, room, reason) {
	return 'Du kannst <span style="color: ' + color + '">' + nick + '</span> nicht in den Raum <span style="font-weight: bold">' + room + '</span> kicken... (Grund: <span style="font-weight: bold">' + reason + '</span>)';
}
function moderate_room_add(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> versetzt den Raum <span style="font-weight: bold">' + room + '</span> in den Moderationsmodus!';
}
function moderate_room_remove(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> entfernt den Moderatiosmodus aus den Raum <span style="font-weight: bold">' + room + '</span> ';
}
function moderated() {
	return 'Dieser Raum ist momentan moderiert!';
}
function moderated_room() {
	return 'Der Raum <span style="font-weight: bold">' + room + '</span> ist moderiert, deswegen kannst Du keine Nachrichten in den Raum <span style="font-weight: bold">' + room + '</span> &uuml;bermitteln!';
}
function moderator_remove(nick, color, moderator_nick, moderator_color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> entzieht <span style="color: #' + moderator_color + '">' + moderator_nick + '</span> das Stimmrecht!';
}
function more_power(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> ist st&auml;rker als Du, deswegen kann der Befehl nicht ausgef&uuml;hrt werden!';
}
function my_ip(host, ip, ip_type, user_agent, os, browser) {
	return 'Dein Host ist: <span style="font-weight: bold">' + host + ' (' + ip_type + ')</span> Dein Browser ist: <span style="font-weight: bold">' + browser + '</span> Dein Betriebssystem ist: <span style="font-weight: bold">' + os + '</span>';
}
function no_friend(nick, color) {
	return 'Du bist nicht mit <span style="color: #' + color + '">' + nick + '</span> befreundet!';
}
function no_lock_standard(room) {
	return 'Du kannst den Raum <span style="font-weight: bold">' + room + '</span> nicht absperren!';
}
function no_moderator(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> verf&uuml;gt &uuml;ber keine Voice-Rechte!';
}
function no_standard_room() {
	return 'Du darfst blo&szlig; Hauptr&auml;ume betreten!';
}
function no_su(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat keine Superuser-Rechte die man entziehen kann! ';
}
function not_implemented(command) {
	return 'Der Befehl <span style="font-weight: bold">/' + command + '</span> ist noch nicht implementiert worden!';
}
function not_in_room(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> befindet sich nicht im Raum!';
}
function not_invited() {
	return 'Du bist bisher nicht eingeladen worden!';
}
function not_registered(nick, color) {
	return '<span style="color: #' + color + '; font-weight: bold">' + nick + '</span> ist nicht registriert!';
}
function offline(nick) {
	return 'Es gibt keinen Benutzer mit den Namen <span style="font-weight: bold">' + nick + '</span>!';
}
function offline_reg(nick, color) {
	return 'Der Benutzer <span style="color: #' + color + '">' + nick + '</span> befindet sich zur Zeit nicht im Chat!';
}
function open_room(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> sperrt den Raum wieder auf!';
}
function power_su(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> kann man nicht die Rechte entziehen!';
}
function private_chat() {
	return ' || <a href="#" onclick="submitMessage(\'/query ' + nick + '\');">Privatchat</a>';
}
function quit(nick, color, skin, reason) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat den Chat verlassen.' + reason;
}
function quit_friends(nick, color, skin) {
	return 'Dein Freund <span style="color: #' + color + '">' + nick + '</span> hat den Chat verlassen...';
}
function quit_reason() {
	return ' (%content%)';
}
function quit_reason_flood() {
	return '<span style="color: #FF0000">Flood!</span>';
}
function quit_refresh_error() {
	return '<span style="color: #FF0000">Refreshfehler</span>';
}
function quit_relogin() {
	return '<span style="color: #FF0000">Nutzer loggt sich neu ein!</span>';
}
function quit_supervisor(nick, color, ip, host, skin, reason) {
	return '<span style="color: #' + color + '">' + nick + '</span> (' + host + ') hat den Chat verlassen.' + reason + '';
}
function quit_timeout() {
	return '<span style="color: #FF0000">Ping-Timeout</span>';
}
function refresh() {
	return 'Das Chatfenster wurde geleert!';
}
function rehash() {
	return 'Die Chatdaten werden neu eingelesen...';
}
function rehash_fail() {
	return 'Das einlesen der Daten ist fehlgeschlagen, m&ouml;glicherweise ist ein Konfigurationsfehler aufgetreten!';
}
function rehash_success() {
	return 'Die Daten wurden neu eingelesen!';
}
function room_empty(room) {
	return 'Der Raum <span style="font-weight: bold">' + room + '</span> ist momentan Leer!';
}
function same_color(color) {
	return 'Du hast bereits diese Farbe!';
}
function same_room(room) {
	return 'Du bist bereits im Raum <span style="font-weight: bold">' + room + '</span>!';
}
function same_room2(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> befindet sich bereits im Raum <span style="font-weight: bold">' + room + '</span>!';
}
function same_room_beam(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> befindet sich bereits im Raum <span style="font-weight: bold">' + room + '</span>!';
}
function send_mail(nick, color, text) {
	return 'Du hast folgende E-Mail an <span style="color: #' + color + '">' + nick + '</span> gesendet: <span style="font-weight: bold">' + text + '</span>';
}
function send_mail_error(error) {
	return 'Die E-Mail konnte nicht gesendet werden: <span style="font-weight: bold">' + error + '</span>';
}
function sepa() {
	return 'Du hat ein Sepa er&ouml;ffnet, mit /i nick kannst du weitere Chatter in diesen Raum einladen!';
}
function sepa_room(nick, color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> geht in den Raum <span style="font-weight: bold">' + room + '</span> und schlie&szlig;t hinter sich ab!';
}
function sepa_standard_room(room) {
	return 'Der Raum <span style="font-weight: bold">' + room + '</span> ist ein Hauptraum, deswegen kanst Du dort kein Sepa er&ouml;ffnen.';
}
function set_moderator(nick, color, moderator_nick, moderator_color, room) {
	return '<span style="color: #' + color + '">' + nick + '</span> verleiht <span style="color: #' + moderator_color + '">' + moderator_nick + '</span> das Stimmrecht!';
}
function set_su(nick, color, su_nick, su_color) {
	return '<span style="color: #' + color + '">' + nick + '</span> verleiht <span style="color: #' + su_color + '">' + su_nick + '</span> Superuser-Rechte!';
}
function set_supervisor() {
	return 'Supervisor Modus aktiviert!';
}
function status_low(command) {
	return 'Du bist nicht berechtigt den Befehl <span style="font-weight: bold">/' + command + '</span> zu benutzen!';
}
function status_low_supervisor(nick, color, ip, host, command) {
	return '<span style="color: #' + color + '">' + nick + '</span> (' + host + ') hat gerade versucht den Befehl <span style="font-weight: bold">/' + command + '</span> zu benutzen!';
}
function system_message(nick, color, txt) {
	return '<span style="font-weight: bold">Systemnachricht von <span style="color: #' + color + '">' + nick + '</span>: ' + txt + '</span>';
}
function system_msg(text) {
	return '<span style="font-weight: bold">System:</span> ' + text;
}
function timed_msg(text) {
	return '<span style="font-style: italic">&raquo; ' + getCurrentTime() + ' ' + text + '</span><br>';
}
function topic_room(topic) {
	return 'Thema des Raums: ' + topic;
}
function uban_error(nick) {
	return 'Den Chatter <span style="font-weight: bold">' + nick + '</span> gibt es nicht!';
}
function uban_succes(nick) {
	return 'Du hast den Chatter <span style="font-weight: bold">' + nick + '</span> entbannt!';
}
function unknown_command(command) {
	return 'Das Chatsystem kennt den Befehl <span style="font-weight: bold">/' + command + '</span> nicht!';
}
function user_gag(nick, color, gag_nick, gag_color) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat <span style="color: #' + gag_color + '">' + gag_nick + '</span> geknebelt!';
}
function user_ig(nick, color) {
	return 'Du hast <span style="color: #' + color + '">' + nick + '</span> ignoriert!';
}
function user_ig_self() {
	return 'Du kannst Dich nicht selbst ignorieren!';
}
function user_ig_target(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> ignoriert Dich!';
}
function user_invite_self() {
	return 'Du kannst Dich nicht selbst einladen!';
}
function user_invite_source(nick, color, room) {
	return 'Du hast <span style="color: #' + color + '">' + nick + '</span> in den Raum <span style="font-weight: bold">' + room + '</span> eingeladen!';
}
function user_invite_target(nick, color, room) {
	return 'Du wurdest von <span style="color: #' + color + '">' + nick + '</span> in den Raum <span style="font-weight: bold">' + room + '</span> eingeladen!';
}
function user_ungag(nick, color, gag_nick, gag_color) {
	return '<span style="color: #' + color + '">' + nick + '</span> hat <span style="color: #' + gag_color + '">' + gag_nick + '</span> vom Knebel wieder befreit!';
}
function user_unig(nick, color) {
	return 'Du ignorierst <span style="color: #' + color + '">' + nick + '</span> nicht mehr!';
}
function user_unig_target(nick, color) {
	return '<span style="color: #' + color + '">' + nick + '</span> ignoriert Dich nicht mehr!';
}
function whisper_error() {
	return 'Du hast bisher mit keinem Chatter geflüstert!';
}
function whisper_from(nick, color, message) {
	return '<span style="font-style: italic"><span style="color: #' + color + '">' + nick + '</span> fl&uuml;stert dich an: ' + message + '</span><br>';
}
function whisper_to(nick, color, message) {
	return '<span style="font-style: italic">Du fl&uuml;sterst zu <span style="color: #' + color + '">' + nick + '</span>: ' + message + '</span><br>';
}