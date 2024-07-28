!function() {
const EMOJIES = {"_parser":{Z:['FE0F',''],V:['1F467','girl'],W:['1F466','boy'],X:['1F469','woman'],Y:['1F468','man'],L:['1F3FB','light skin tone'],M:['1F3FC','medium-light skin tone'],N:['1F3FD','medium skin tone'],O:['1F3FE','medium-dark skin tone'],P:['1F3FF','dark skin tone'],'&':['200D','']},"Smileys & People":{"face-positive":{"1F600":"grinning face","1F601":"beaming face with smiling eyes","1F602":"face with tears of joy","1F923":"rolling on the floor laughing","1F603":"grinning face with big eyes","1F604":"grinning face with smiling eyes","1F605":"grinning face with sweat","1F606":"grinning squinting face","1F609":"winking face","1F60A":"smiling face with smiling eyes","1F60B":"face savoring food","1F60E":"smiling face with sunglasses","1F60D":"smiling face with heart-eyes","1F618":"face blowing a kiss","1F617":"kissing face","1F619":"kissing face with smiling eyes","1F61A":"kissing face with closed eyes","263A Z":"smiling face","263A":"smiling face","1F642":"slightly smiling face","1F917":"hugging face","1F929":"star-struck"},"face-neutral":{"1F914":"thinking face","1F928":"face with raised eyebrow","1F610":"neutral face","1F611":"expressionless face","1F636":"face without mouth","1F644":"face with rolling eyes","1F60F":"smirking face","1F623":"persevering face","1F625":"sad but relieved face","1F62E":"face with open mouth","1F910":"zipper-mouth face","1F62F":"hushed face","1F62A":"sleepy face","1F62B":"tired face","1F634":"sleeping face","1F60C":"relieved face","1F61B":"face with tongue","1F61C":"winking face with tongue","1F61D":"squinting face with tongue","1F924":"drooling face","1F612":"unamused face","1F613":"downcast face with sweat","1F614":"pensive face","1F615":"confused face","1F643":"upside-down face","1F911":"money-mouth face","1F632":"astonished face"},"face-negative":{"2639 Z":"frowning face","2639":"frowning face","1F641":"slightly frowning face","1F616":"confounded face","1F61E":"disappointed face","1F61F":"worried face","1F624":"face with steam from nose","1F622":"crying face","1F62D":"loudly crying face","1F626":"frowning face with open mouth","1F627":"anguished face","1F628":"fearful face","1F629":"weary face","1F92F":"exploding head","1F62C":"grimacing face","1F630":"anxious face with sweat","1F631":"face screaming in fear","1F633":"flushed face","1F92A":"crazy face","1F635":"dizzy face","1F621":"pouting face","1F620":"angry face","1F92C":"face with symbols on mouth"},"face-sick":{"1F637":"face with medical mask","1F912":"face with thermometer","1F915":"face with head-bandage","1F922":"nauseated face","1F92E":"face vomiting","1F927":"sneezing face"},"face-role":{"1F607":"smiling face with halo","1F920":"cowboy hat face","1F921":"clown face","1F925":"lying face","1F92B":"shushing face","1F92D":"face with hand over mouth","1F9D0":"face with monocle","1F913":"nerd face"},"face-fantasy":{"1F608":"smiling face with horns","1F47F":"angry face with horns","1F479":"ogre","1F47A":"goblin","1F480":"skull","2620 Z":"skull and crossbones","2620":"skull and crossbones","1F47B":"ghost","1F47D":"alien","1F47E":"alien monster","1F916":"robot face","1F4A9":"pile of poo"},"cat-face":{"1F63A":"grinning cat face","1F638":"grinning cat face with smiling eyes","1F639":"cat face with tears of joy","1F63B":"smiling cat face with heart-eyes","1F63C":"cat face with wry smile","1F63D":"kissing cat face","1F640":"weary cat face","1F63F":"crying cat face","1F63E":"pouting cat face"},"monkey-face":{"1F648":"see-no-evil monkey","1F649":"hear-no-evil monkey","1F64A":"speak-no-evil monkey"},person:{"1F476":"baby","1F476 L":"baby: L","1F476 M":"baby: M","1F476 N":"baby: N","1F476 O":"baby: O","1F476 P":"baby: P","1F9D2":"child","1F9D2 L":"child: L","1F9D2 M":"child: M","1F9D2 N":"child: N","1F9D2 O":"child: O","1F9D2 P":"child: P","W":"W","W L":"W: L","W M":"W: M","W N":"W: N","W O":"W: O","W P":"W: P","V":"V","V L":"V: L","V M":"V: M","V N":"V: N","V O":"V: O","V P":"V: P","1F9D1":"adult","1F9D1 L":"adult: L","1F9D1 M":"adult: M","1F9D1 N":"adult: N","1F9D1 O":"adult: O","1F9D1 P":"adult: P","Y":"Y","Y L":"Y: L","Y M":"Y: M","Y N":"Y: N","Y O":"Y: O","Y P":"Y: P","X":"X","X L":"X: L","X M":"X: M","X N":"X: N","X O":"X: O","X P":"X: P","1F9D3":"older adult","1F9D3 L":"older adult: L","1F9D3 M":"older adult: M","1F9D3 N":"older adult: N","1F9D3 O":"older adult: O","1F9D3 P":"older adult: P","1F474":"old Y","1F474 L":"old Y: L","1F474 M":"old Y: M","1F474 N":"old Y: N","1F474 O":"old Y: O","1F474 P":"old Y: P","1F475":"old X","1F475 L":"old X: L","1F475 M":"old X: M","1F475 N":"old X: N","1F475 O":"old X: O","1F475 P":"old X: P"},"person-role":{"Y & 2695 Z":"Y health worker","Y & 2695":"Y health worker","Y L & 2695 Z":"Y health worker: L","Y L & 2695":"Y health worker: L","Y M & 2695 Z":"Y health worker: M","Y M & 2695":"Y health worker: M","Y N & 2695 Z":"Y health worker: N","Y N & 2695":"Y health worker: N","Y O & 2695 Z":"Y health worker: O","Y O & 2695":"Y health worker: O","Y P & 2695 Z":"Y health worker: P","Y P & 2695":"Y health worker: P","X & 2695 Z":"X health worker","X & 2695":"X health worker","X L & 2695 Z":"X health worker: L","X L & 2695":"X health worker: L","X M & 2695 Z":"X health worker: M","X M & 2695":"X health worker: M","X N & 2695 Z":"X health worker: N","X N & 2695":"X health worker: N","X O & 2695 Z":"X health worker: O","X O & 2695":"X health worker: O","X P & 2695 Z":"X health worker: P","X P & 2695":"X health worker: P","Y & 1F393":"Y student","Y L & 1F393":"Y student: L","Y M & 1F393":"Y student: M","Y N & 1F393":"Y student: N","Y O & 1F393":"Y student: O","Y P & 1F393":"Y student: P","X & 1F393":"X student","X L & 1F393":"X student: L","X M & 1F393":"X student: M","X N & 1F393":"X student: N","X O & 1F393":"X student: O","X P & 1F393":"X student: P","Y & 1F3EB":"Y teacher","Y L & 1F3EB":"Y teacher: L","Y M & 1F3EB":"Y teacher: M","Y N & 1F3EB":"Y teacher: N","Y O & 1F3EB":"Y teacher: O","Y P & 1F3EB":"Y teacher: P","X & 1F3EB":"X teacher","X L & 1F3EB":"X teacher: L","X M & 1F3EB":"X teacher: M","X N & 1F3EB":"X teacher: N","X O & 1F3EB":"X teacher: O","X P & 1F3EB":"X teacher: P","Y & 2696 Z":"Y judge","Y & 2696":"Y judge","Y L & 2696 Z":"Y judge: L","Y L & 2696":"Y judge: L","Y M & 2696 Z":"Y judge: M","Y M & 2696":"Y judge: M","Y N & 2696 Z":"Y judge: N","Y N & 2696":"Y judge: N","Y O & 2696 Z":"Y judge: O","Y O & 2696":"Y judge: O","Y P & 2696 Z":"Y judge: P","Y P & 2696":"Y judge: P","X & 2696 Z":"X judge","X & 2696":"X judge","X L & 2696 Z":"X judge: L","X L & 2696":"X judge: L","X M & 2696 Z":"X judge: M","X M & 2696":"X judge: M","X N & 2696 Z":"X judge: N","X N & 2696":"X judge: N","X O & 2696 Z":"X judge: O","X O & 2696":"X judge: O","X P & 2696 Z":"X judge: P","X P & 2696":"X judge: P","Y & 1F33E":"Y farmer","Y L & 1F33E":"Y farmer: L","Y M & 1F33E":"Y farmer: M","Y N & 1F33E":"Y farmer: N","Y O & 1F33E":"Y farmer: O","Y P & 1F33E":"Y farmer: P","X & 1F33E":"X farmer","X L & 1F33E":"X farmer: L","X M & 1F33E":"X farmer: M","X N & 1F33E":"X farmer: N","X O & 1F33E":"X farmer: O","X P & 1F33E":"X farmer: P","Y & 1F373":"Y cook","Y L & 1F373":"Y cook: L","Y M & 1F373":"Y cook: M","Y N & 1F373":"Y cook: N","Y O & 1F373":"Y cook: O","Y P & 1F373":"Y cook: P","X & 1F373":"X cook","X L & 1F373":"X cook: L","X M & 1F373":"X cook: M","X N & 1F373":"X cook: N","X O & 1F373":"X cook: O","X P & 1F373":"X cook: P","Y & 1F527":"Y mechanic","Y L & 1F527":"Y mechanic: L","Y M & 1F527":"Y mechanic: M","Y N & 1F527":"Y mechanic: N","Y O & 1F527":"Y mechanic: O","Y P & 1F527":"Y mechanic: P","X & 1F527":"X mechanic","X L & 1F527":"X mechanic: L","X M & 1F527":"X mechanic: M","X N & 1F527":"X mechanic: N","X O & 1F527":"X mechanic: O","X P & 1F527":"X mechanic: P","Y & 1F3ED":"Y factory worker","Y L & 1F3ED":"Y factory worker: L","Y M & 1F3ED":"Y factory worker: M","Y N & 1F3ED":"Y factory worker: N","Y O & 1F3ED":"Y factory worker: O","Y P & 1F3ED":"Y factory worker: P","X & 1F3ED":"X factory worker","X L & 1F3ED":"X factory worker: L","X M & 1F3ED":"X factory worker: M","X N & 1F3ED":"X factory worker: N","X O & 1F3ED":"X factory worker: O","X P & 1F3ED":"X factory worker: P","Y & 1F4BC":"Y office worker","Y L & 1F4BC":"Y office worker: L","Y M & 1F4BC":"Y office worker: M","Y N & 1F4BC":"Y office worker: N","Y O & 1F4BC":"Y office worker: O","Y P & 1F4BC":"Y office worker: P","X & 1F4BC":"X office worker","X L & 1F4BC":"X office worker: L","X M & 1F4BC":"X office worker: M","X N & 1F4BC":"X office worker: N","X O & 1F4BC":"X office worker: O","X P & 1F4BC":"X office worker: P","Y & 1F52C":"Y scientist","Y L & 1F52C":"Y scientist: L","Y M & 1F52C":"Y scientist: M","Y N & 1F52C":"Y scientist: N","Y O & 1F52C":"Y scientist: O","Y P & 1F52C":"Y scientist: P","X & 1F52C":"X scientist","X L & 1F52C":"X scientist: L","X M & 1F52C":"X scientist: M","X N & 1F52C":"X scientist: N","X O & 1F52C":"X scientist: O","X P & 1F52C":"X scientist: P","Y & 1F4BB":"Y technologist","Y L & 1F4BB":"Y technologist: L","Y M & 1F4BB":"Y technologist: M","Y N & 1F4BB":"Y technologist: N","Y O & 1F4BB":"Y technologist: O","Y P & 1F4BB":"Y technologist: P","X & 1F4BB":"X technologist","X L & 1F4BB":"X technologist: L","X M & 1F4BB":"X technologist: M","X N & 1F4BB":"X technologist: N","X O & 1F4BB":"X technologist: O","X P & 1F4BB":"X technologist: P","Y & 1F3A4":"Y singer","Y L & 1F3A4":"Y singer: L","Y M & 1F3A4":"Y singer: M","Y N & 1F3A4":"Y singer: N","Y O & 1F3A4":"Y singer: O","Y P & 1F3A4":"Y singer: P","X & 1F3A4":"X singer","X L & 1F3A4":"X singer: L","X M & 1F3A4":"X singer: M","X N & 1F3A4":"X singer: N","X O & 1F3A4":"X singer: O","X P & 1F3A4":"X singer: P","Y & 1F3A8":"Y artist","Y L & 1F3A8":"Y artist: L","Y M & 1F3A8":"Y artist: M","Y N & 1F3A8":"Y artist: N","Y O & 1F3A8":"Y artist: O","Y P & 1F3A8":"Y artist: P","X & 1F3A8":"X artist","X L & 1F3A8":"X artist: L","X M & 1F3A8":"X artist: M","X N & 1F3A8":"X artist: N","X O & 1F3A8":"X artist: O","X P & 1F3A8":"X artist: P","Y & 2708 Z":"Y pilot","Y & 2708":"Y pilot","Y L & 2708 Z":"Y pilot: L","Y L & 2708":"Y pilot: L","Y M & 2708 Z":"Y pilot: M","Y M & 2708":"Y pilot: M","Y N & 2708 Z":"Y pilot: N","Y N & 2708":"Y pilot: N","Y O & 2708 Z":"Y pilot: O","Y O & 2708":"Y pilot: O","Y P & 2708 Z":"Y pilot: P","Y P & 2708":"Y pilot: P","X & 2708 Z":"X pilot","X & 2708":"X pilot","X L & 2708 Z":"X pilot: L","X L & 2708":"X pilot: L","X M & 2708 Z":"X pilot: M","X M & 2708":"X pilot: M","X N & 2708 Z":"X pilot: N","X N & 2708":"X pilot: N","X O & 2708 Z":"X pilot: O","X O & 2708":"X pilot: O","X P & 2708 Z":"X pilot: P","X P & 2708":"X pilot: P","Y & 1F680":"Y astronaut","Y L & 1F680":"Y astronaut: L","Y M & 1F680":"Y astronaut: M","Y N & 1F680":"Y astronaut: N","Y O & 1F680":"Y astronaut: O","Y P & 1F680":"Y astronaut: P","X & 1F680":"X astronaut","X L & 1F680":"X astronaut: L","X M & 1F680":"X astronaut: M","X N & 1F680":"X astronaut: N","X O & 1F680":"X astronaut: O","X P & 1F680":"X astronaut: P","Y & 1F692":"Y firefighter","Y L & 1F692":"Y firefighter: L","Y M & 1F692":"Y firefighter: M","Y N & 1F692":"Y firefighter: N","Y O & 1F692":"Y firefighter: O","Y P & 1F692":"Y firefighter: P","X & 1F692":"X firefighter","X L & 1F692":"X firefighter: L","X M & 1F692":"X firefighter: M","X N & 1F692":"X firefighter: N","X O & 1F692":"X firefighter: O","X P & 1F692":"X firefighter: P","1F46E":"police officer","1F46E L":"police officer: L","1F46E M":"police officer: M","1F46E N":"police officer: N","1F46E O":"police officer: O","1F46E P":"police officer: P","1F46E & 2642 Z":"Y police officer","1F46E & 2642":"Y police officer","1F46E L & 2642 Z":"Y police officer: L","1F46E L & 2642":"Y police officer: L","1F46E M & 2642 Z":"Y police officer: M","1F46E M & 2642":"Y police officer: M","1F46E N & 2642 Z":"Y police officer: N","1F46E N & 2642":"Y police officer: N","1F46E O & 2642 Z":"Y police officer: O","1F46E O & 2642":"Y police officer: O","1F46E P & 2642 Z":"Y police officer: P","1F46E P & 2642":"Y police officer: P","1F46E & 2640 Z":"X police officer","1F46E & 2640":"X police officer","1F46E L & 2640 Z":"X police officer: L","1F46E L & 2640":"X police officer: L","1F46E M & 2640 Z":"X police officer: M","1F46E M & 2640":"X police officer: M","1F46E N & 2640 Z":"X police officer: N","1F46E N & 2640":"X police officer: N","1F46E O & 2640 Z":"X police officer: O","1F46E O & 2640":"X police officer: O","1F46E P & 2640 Z":"X police officer: P","1F46E P & 2640":"X police officer: P","1F575 Z":"detective","1F575":"detective","1F575 L":"detective: L","1F575 M":"detective: M","1F575 N":"detective: N","1F575 O":"detective: O","1F575 P":"detective: P","1F575 Z & 2642 Z":"Y detective","1F575 & 2642 Z":"Y detective","1F575 Z & 2642":"Y detective","1F575 & 2642":"Y detective","1F575 L & 2642 Z":"Y detective: L","1F575 L & 2642":"Y detective: L","1F575 M & 2642 Z":"Y detective: M","1F575 M & 2642":"Y detective: M","1F575 N & 2642 Z":"Y detective: N","1F575 N & 2642":"Y detective: N","1F575 O & 2642 Z":"Y detective: O","1F575 O & 2642":"Y detective: O","1F575 P & 2642 Z":"Y detective: P","1F575 P & 2642":"Y detective: P","1F575 Z & 2640 Z":"X detective","1F575 & 2640 Z":"X detective","1F575 Z & 2640":"X detective","1F575 & 2640":"X detective","1F575 L & 2640 Z":"X detective: L","1F575 L & 2640":"X detective: L","1F575 M & 2640 Z":"X detective: M","1F575 M & 2640":"X detective: M","1F575 N & 2640 Z":"X detective: N","1F575 N & 2640":"X detective: N","1F575 O & 2640 Z":"X detective: O","1F575 O & 2640":"X detective: O","1F575 P & 2640 Z":"X detective: P","1F575 P & 2640":"X detective: P","1F482":"guard","1F482 L":"guard: L","1F482 M":"guard: M","1F482 N":"guard: N","1F482 O":"guard: O","1F482 P":"guard: P","1F482 & 2642 Z":"Y guard","1F482 & 2642":"Y guard","1F482 L & 2642 Z":"Y guard: L","1F482 L & 2642":"Y guard: L","1F482 M & 2642 Z":"Y guard: M","1F482 M & 2642":"Y guard: M","1F482 N & 2642 Z":"Y guard: N","1F482 N & 2642":"Y guard: N","1F482 O & 2642 Z":"Y guard: O","1F482 O & 2642":"Y guard: O","1F482 P & 2642 Z":"Y guard: P","1F482 P & 2642":"Y guard: P","1F482 & 2640 Z":"X guard","1F482 & 2640":"X guard","1F482 L & 2640 Z":"X guard: L","1F482 L & 2640":"X guard: L","1F482 M & 2640 Z":"X guard: M","1F482 M & 2640":"X guard: M","1F482 N & 2640 Z":"X guard: N","1F482 N & 2640":"X guard: N","1F482 O & 2640 Z":"X guard: O","1F482 O & 2640":"X guard: O","1F482 P & 2640 Z":"X guard: P","1F482 P & 2640":"X guard: P","1F477":"construction worker","1F477 L":"construction worker: L","1F477 M":"construction worker: M","1F477 N":"construction worker: N","1F477 O":"construction worker: O","1F477 P":"construction worker: P","1F477 & 2642 Z":"Y construction worker","1F477 & 2642":"Y construction worker","1F477 L & 2642 Z":"Y construction worker: L","1F477 L & 2642":"Y construction worker: L","1F477 M & 2642 Z":"Y construction worker: M","1F477 M & 2642":"Y construction worker: M","1F477 N & 2642 Z":"Y construction worker: N","1F477 N & 2642":"Y construction worker: N","1F477 O & 2642 Z":"Y construction worker: O","1F477 O & 2642":"Y construction worker: O","1F477 P & 2642 Z":"Y construction worker: P","1F477 P & 2642":"Y construction worker: P","1F477 & 2640 Z":"X construction worker","1F477 & 2640":"X construction worker","1F477 L & 2640 Z":"X construction worker: L","1F477 L & 2640":"X construction worker: L","1F477 M & 2640 Z":"X construction worker: M","1F477 M & 2640":"X construction worker: M","1F477 N & 2640 Z":"X construction worker: N","1F477 N & 2640":"X construction worker: N","1F477 O & 2640 Z":"X construction worker: O","1F477 O & 2640":"X construction worker: O","1F477 P & 2640 Z":"X construction worker: P","1F477 P & 2640":"X construction worker: P","1F934":"prince","1F934 L":"prince: L","1F934 M":"prince: M","1F934 N":"prince: N","1F934 O":"prince: O","1F934 P":"prince: P","1F478":"princess","1F478 L":"princess: L","1F478 M":"princess: M","1F478 N":"princess: N","1F478 O":"princess: O","1F478 P":"princess: P","1F473":"person wearing turban","1F473 L":"person wearing turban: L","1F473 M":"person wearing turban: M","1F473 N":"person wearing turban: N","1F473 O":"person wearing turban: O","1F473 P":"person wearing turban: P","1F473 & 2642 Z":"Y wearing turban","1F473 & 2642":"Y wearing turban","1F473 L & 2642 Z":"Y wearing turban: L","1F473 L & 2642":"Y wearing turban: L","1F473 M & 2642 Z":"Y wearing turban: M","1F473 M & 2642":"Y wearing turban: M","1F473 N & 2642 Z":"Y wearing turban: N","1F473 N & 2642":"Y wearing turban: N","1F473 O & 2642 Z":"Y wearing turban: O","1F473 O & 2642":"Y wearing turban: O","1F473 P & 2642 Z":"Y wearing turban: P","1F473 P & 2642":"Y wearing turban: P","1F473 & 2640 Z":"X wearing turban","1F473 & 2640":"X wearing turban","1F473 L & 2640 Z":"X wearing turban: L","1F473 L & 2640":"X wearing turban: L","1F473 M & 2640 Z":"X wearing turban: M","1F473 M & 2640":"X wearing turban: M","1F473 N & 2640 Z":"X wearing turban: N","1F473 N & 2640":"X wearing turban: N","1F473 O & 2640 Z":"X wearing turban: O","1F473 O & 2640":"X wearing turban: O","1F473 P & 2640 Z":"X wearing turban: P","1F473 P & 2640":"X wearing turban: P","1F472":"Y with Chinese cap","1F472 L":"Y with Chinese cap: L","1F472 M":"Y with Chinese cap: M","1F472 N":"Y with Chinese cap: N","1F472 O":"Y with Chinese cap: O","1F472 P":"Y with Chinese cap: P","1F9D5":"X with headscarf","1F9D5 L":"X with headscarf: L","1F9D5 M":"X with headscarf: M","1F9D5 N":"X with headscarf: N","1F9D5 O":"X with headscarf: O","1F9D5 P":"X with headscarf: P","1F9D4":"bearded person","1F9D4 L":"bearded person: L","1F9D4 M":"bearded person: M","1F9D4 N":"bearded person: N","1F9D4 O":"bearded person: O","1F9D4 P":"bearded person: P","1F471":"blond-haired person","1F471 L":"blond-haired person: L","1F471 M":"blond-haired person: M","1F471 N":"blond-haired person: N","1F471 O":"blond-haired person: O","1F471 P":"blond-haired person: P","1F471 & 2642 Z":"blond-haired Y","1F471 & 2642":"blond-haired Y","1F471 L & 2642 Z":"blond-haired Y: L","1F471 L & 2642":"blond-haired Y: L","1F471 M & 2642 Z":"blond-haired Y: M","1F471 M & 2642":"blond-haired Y: M","1F471 N & 2642 Z":"blond-haired Y: N","1F471 N & 2642":"blond-haired Y: N","1F471 O & 2642 Z":"blond-haired Y: O","1F471 O & 2642":"blond-haired Y: O","1F471 P & 2642 Z":"blond-haired Y: P","1F471 P & 2642":"blond-haired Y: P","1F471 & 2640 Z":"blond-haired X","1F471 & 2640":"blond-haired X","1F471 L & 2640 Z":"blond-haired X: L","1F471 L & 2640":"blond-haired X: L","1F471 M & 2640 Z":"blond-haired X: M","1F471 M & 2640":"blond-haired X: M","1F471 N & 2640 Z":"blond-haired X: N","1F471 N & 2640":"blond-haired X: N","1F471 O & 2640 Z":"blond-haired X: O","1F471 O & 2640":"blond-haired X: O","1F471 P & 2640 Z":"blond-haired X: P","1F471 P & 2640":"blond-haired X: P","1F935":"Y in tuxedo","1F935 L":"Y in tuxedo: L","1F935 M":"Y in tuxedo: M","1F935 N":"Y in tuxedo: N","1F935 O":"Y in tuxedo: O","1F935 P":"Y in tuxedo: P","1F470":"bride with veil","1F470 L":"bride with veil: L","1F470 M":"bride with veil: M","1F470 N":"bride with veil: N","1F470 O":"bride with veil: O","1F470 P":"bride with veil: P","1F930":"pregnant X","1F930 L":"pregnant X: L","1F930 M":"pregnant X: M","1F930 N":"pregnant X: N","1F930 O":"pregnant X: O","1F930 P":"pregnant X: P","1F931":"breast-feeding","1F931 L":"breast-feeding: L","1F931 M":"breast-feeding: M","1F931 N":"breast-feeding: N","1F931 O":"breast-feeding: O","1F931 P":"breast-feeding: P"},"person-fantasy":{"1F47C":"baby angel","1F47C L":"baby angel: L","1F47C M":"baby angel: M","1F47C N":"baby angel: N","1F47C O":"baby angel: O","1F47C P":"baby angel: P","1F385":"Santa Claus","1F385 L":"Santa Claus: L","1F385 M":"Santa Claus: M","1F385 N":"Santa Claus: N","1F385 O":"Santa Claus: O","1F385 P":"Santa Claus: P","1F936":"Mrs. Claus","1F936 L":"Mrs. Claus: L","1F936 M":"Mrs. Claus: M","1F936 N":"Mrs. Claus: N","1F936 O":"Mrs. Claus: O","1F936 P":"Mrs. Claus: P","1F9D9":"mage","1F9D9 L":"mage: L","1F9D9 M":"mage: M","1F9D9 N":"mage: N","1F9D9 O":"mage: O","1F9D9 P":"mage: P","1F9D9 & 2640 Z":"X mage","1F9D9 & 2640":"X mage","1F9D9 L & 2640 Z":"X mage: L","1F9D9 L & 2640":"X mage: L","1F9D9 M & 2640 Z":"X mage: M","1F9D9 M & 2640":"X mage: M","1F9D9 N & 2640 Z":"X mage: N","1F9D9 N & 2640":"X mage: N","1F9D9 O & 2640 Z":"X mage: O","1F9D9 O & 2640":"X mage: O","1F9D9 P & 2640 Z":"X mage: P","1F9D9 P & 2640":"X mage: P","1F9D9 & 2642 Z":"Y mage","1F9D9 & 2642":"Y mage","1F9D9 L & 2642 Z":"Y mage: L","1F9D9 L & 2642":"Y mage: L","1F9D9 M & 2642 Z":"Y mage: M","1F9D9 M & 2642":"Y mage: M","1F9D9 N & 2642 Z":"Y mage: N","1F9D9 N & 2642":"Y mage: N","1F9D9 O & 2642 Z":"Y mage: O","1F9D9 O & 2642":"Y mage: O","1F9D9 P & 2642 Z":"Y mage: P","1F9D9 P & 2642":"Y mage: P","1F9DA":"fairy","1F9DA L":"fairy: L","1F9DA M":"fairy: M","1F9DA N":"fairy: N","1F9DA O":"fairy: O","1F9DA P":"fairy: P","1F9DA & 2640 Z":"X fairy","1F9DA & 2640":"X fairy","1F9DA L & 2640 Z":"X fairy: L","1F9DA L & 2640":"X fairy: L","1F9DA M & 2640 Z":"X fairy: M","1F9DA M & 2640":"X fairy: M","1F9DA N & 2640 Z":"X fairy: N","1F9DA N & 2640":"X fairy: N","1F9DA O & 2640 Z":"X fairy: O","1F9DA O & 2640":"X fairy: O","1F9DA P & 2640 Z":"X fairy: P","1F9DA P & 2640":"X fairy: P","1F9DA & 2642 Z":"Y fairy","1F9DA & 2642":"Y fairy","1F9DA L & 2642 Z":"Y fairy: L","1F9DA L & 2642":"Y fairy: L","1F9DA M & 2642 Z":"Y fairy: M","1F9DA M & 2642":"Y fairy: M","1F9DA N & 2642 Z":"Y fairy: N","1F9DA N & 2642":"Y fairy: N","1F9DA O & 2642 Z":"Y fairy: O","1F9DA O & 2642":"Y fairy: O","1F9DA P & 2642 Z":"Y fairy: P","1F9DA P & 2642":"Y fairy: P","1F9DB":"vampire","1F9DB L":"vampire: L","1F9DB M":"vampire: M","1F9DB N":"vampire: N","1F9DB O":"vampire: O","1F9DB P":"vampire: P","1F9DB & 2640 Z":"X vampire","1F9DB & 2640":"X vampire","1F9DB L & 2640 Z":"X vampire: L","1F9DB L & 2640":"X vampire: L","1F9DB M & 2640 Z":"X vampire: M","1F9DB M & 2640":"X vampire: M","1F9DB N & 2640 Z":"X vampire: N","1F9DB N & 2640":"X vampire: N","1F9DB O & 2640 Z":"X vampire: O","1F9DB O & 2640":"X vampire: O","1F9DB P & 2640 Z":"X vampire: P","1F9DB P & 2640":"X vampire: P","1F9DB & 2642 Z":"Y vampire","1F9DB & 2642":"Y vampire","1F9DB L & 2642 Z":"Y vampire: L","1F9DB L & 2642":"Y vampire: L","1F9DB M & 2642 Z":"Y vampire: M","1F9DB M & 2642":"Y vampire: M","1F9DB N & 2642 Z":"Y vampire: N","1F9DB N & 2642":"Y vampire: N","1F9DB O & 2642 Z":"Y vampire: O","1F9DB O & 2642":"Y vampire: O","1F9DB P & 2642 Z":"Y vampire: P","1F9DB P & 2642":"Y vampire: P","1F9DC":"merperson","1F9DC L":"merperson: L","1F9DC M":"merperson: M","1F9DC N":"merperson: N","1F9DC O":"merperson: O","1F9DC P":"merperson: P","1F9DC & 2640 Z":"mermaid","1F9DC & 2640":"mermaid","1F9DC L & 2640 Z":"mermaid: L","1F9DC L & 2640":"mermaid: L","1F9DC M & 2640 Z":"mermaid: M","1F9DC M & 2640":"mermaid: M","1F9DC N & 2640 Z":"mermaid: N","1F9DC N & 2640":"mermaid: N","1F9DC O & 2640 Z":"mermaid: O","1F9DC O & 2640":"mermaid: O","1F9DC P & 2640 Z":"mermaid: P","1F9DC P & 2640":"mermaid: P","1F9DC & 2642 Z":"merY","1F9DC & 2642":"merY","1F9DC L & 2642 Z":"merY: L","1F9DC L & 2642":"merY: L","1F9DC M & 2642 Z":"merY: M","1F9DC M & 2642":"merY: M","1F9DC N & 2642 Z":"merY: N","1F9DC N & 2642":"merY: N","1F9DC O & 2642 Z":"merY: O","1F9DC O & 2642":"merY: O","1F9DC P & 2642 Z":"merY: P","1F9DC P & 2642":"merY: P","1F9DD":"elf","1F9DD L":"elf: L","1F9DD M":"elf: M","1F9DD N":"elf: N","1F9DD O":"elf: O","1F9DD P":"elf: P","1F9DD & 2640 Z":"X elf","1F9DD & 2640":"X elf","1F9DD L & 2640 Z":"X elf: L","1F9DD L & 2640":"X elf: L","1F9DD M & 2640 Z":"X elf: M","1F9DD M & 2640":"X elf: M","1F9DD N & 2640 Z":"X elf: N","1F9DD N & 2640":"X elf: N","1F9DD O & 2640 Z":"X elf: O","1F9DD O & 2640":"X elf: O","1F9DD P & 2640 Z":"X elf: P","1F9DD P & 2640":"X elf: P","1F9DD & 2642 Z":"Y elf","1F9DD & 2642":"Y elf","1F9DD L & 2642 Z":"Y elf: L","1F9DD L & 2642":"Y elf: L","1F9DD M & 2642 Z":"Y elf: M","1F9DD M & 2642":"Y elf: M","1F9DD N & 2642 Z":"Y elf: N","1F9DD N & 2642":"Y elf: N","1F9DD O & 2642 Z":"Y elf: O","1F9DD O & 2642":"Y elf: O","1F9DD P & 2642 Z":"Y elf: P","1F9DD P & 2642":"Y elf: P","1F9DE":"genie","1F9DE & 2640 Z":"X genie","1F9DE & 2640":"X genie","1F9DE & 2642 Z":"Y genie","1F9DE & 2642":"Y genie","1F9DF":"zombie","1F9DF & 2640 Z":"X zombie","1F9DF & 2640":"X zombie","1F9DF & 2642 Z":"Y zombie","1F9DF & 2642":"Y zombie"},"person-gesture":{"1F64D":"person frowning","1F64D L":"person frowning: L","1F64D M":"person frowning: M","1F64D N":"person frowning: N","1F64D O":"person frowning: O","1F64D P":"person frowning: P","1F64D & 2642 Z":"Y frowning","1F64D & 2642":"Y frowning","1F64D L & 2642 Z":"Y frowning: L","1F64D L & 2642":"Y frowning: L","1F64D M & 2642 Z":"Y frowning: M","1F64D M & 2642":"Y frowning: M","1F64D N & 2642 Z":"Y frowning: N","1F64D N & 2642":"Y frowning: N","1F64D O & 2642 Z":"Y frowning: O","1F64D O & 2642":"Y frowning: O","1F64D P & 2642 Z":"Y frowning: P","1F64D P & 2642":"Y frowning: P","1F64D & 2640 Z":"X frowning","1F64D & 2640":"X frowning","1F64D L & 2640 Z":"X frowning: L","1F64D L & 2640":"X frowning: L","1F64D M & 2640 Z":"X frowning: M","1F64D M & 2640":"X frowning: M","1F64D N & 2640 Z":"X frowning: N","1F64D N & 2640":"X frowning: N","1F64D O & 2640 Z":"X frowning: O","1F64D O & 2640":"X frowning: O","1F64D P & 2640 Z":"X frowning: P","1F64D P & 2640":"X frowning: P","1F64E":"person pouting","1F64E L":"person pouting: L","1F64E M":"person pouting: M","1F64E N":"person pouting: N","1F64E O":"person pouting: O","1F64E P":"person pouting: P","1F64E & 2642 Z":"Y pouting","1F64E & 2642":"Y pouting","1F64E L & 2642 Z":"Y pouting: L","1F64E L & 2642":"Y pouting: L","1F64E M & 2642 Z":"Y pouting: M","1F64E M & 2642":"Y pouting: M","1F64E N & 2642 Z":"Y pouting: N","1F64E N & 2642":"Y pouting: N","1F64E O & 2642 Z":"Y pouting: O","1F64E O & 2642":"Y pouting: O","1F64E P & 2642 Z":"Y pouting: P","1F64E P & 2642":"Y pouting: P","1F64E & 2640 Z":"X pouting","1F64E & 2640":"X pouting","1F64E L & 2640 Z":"X pouting: L","1F64E L & 2640":"X pouting: L","1F64E M & 2640 Z":"X pouting: M","1F64E M & 2640":"X pouting: M","1F64E N & 2640 Z":"X pouting: N","1F64E N & 2640":"X pouting: N","1F64E O & 2640 Z":"X pouting: O","1F64E O & 2640":"X pouting: O","1F64E P & 2640 Z":"X pouting: P","1F64E P & 2640":"X pouting: P","1F645":"person gesturing NO","1F645 L":"person gesturing NO: L","1F645 M":"person gesturing NO: M","1F645 N":"person gesturing NO: N","1F645 O":"person gesturing NO: O","1F645 P":"person gesturing NO: P","1F645 & 2642 Z":"Y gesturing NO","1F645 & 2642":"Y gesturing NO","1F645 L & 2642 Z":"Y gesturing NO: L","1F645 L & 2642":"Y gesturing NO: L","1F645 M & 2642 Z":"Y gesturing NO: M","1F645 M & 2642":"Y gesturing NO: M","1F645 N & 2642 Z":"Y gesturing NO: N","1F645 N & 2642":"Y gesturing NO: N","1F645 O & 2642 Z":"Y gesturing NO: O","1F645 O & 2642":"Y gesturing NO: O","1F645 P & 2642 Z":"Y gesturing NO: P","1F645 P & 2642":"Y gesturing NO: P","1F645 & 2640 Z":"X gesturing NO","1F645 & 2640":"X gesturing NO","1F645 L & 2640 Z":"X gesturing NO: L","1F645 L & 2640":"X gesturing NO: L","1F645 M & 2640 Z":"X gesturing NO: M","1F645 M & 2640":"X gesturing NO: M","1F645 N & 2640 Z":"X gesturing NO: N","1F645 N & 2640":"X gesturing NO: N","1F645 O & 2640 Z":"X gesturing NO: O","1F645 O & 2640":"X gesturing NO: O","1F645 P & 2640 Z":"X gesturing NO: P","1F645 P & 2640":"X gesturing NO: P","1F646":"person gesturing OK","1F646 L":"person gesturing OK: L","1F646 M":"person gesturing OK: M","1F646 N":"person gesturing OK: N","1F646 O":"person gesturing OK: O","1F646 P":"person gesturing OK: P","1F646 & 2642 Z":"Y gesturing OK","1F646 & 2642":"Y gesturing OK","1F646 L & 2642 Z":"Y gesturing OK: L","1F646 L & 2642":"Y gesturing OK: L","1F646 M & 2642 Z":"Y gesturing OK: M","1F646 M & 2642":"Y gesturing OK: M","1F646 N & 2642 Z":"Y gesturing OK: N","1F646 N & 2642":"Y gesturing OK: N","1F646 O & 2642 Z":"Y gesturing OK: O","1F646 O & 2642":"Y gesturing OK: O","1F646 P & 2642 Z":"Y gesturing OK: P","1F646 P & 2642":"Y gesturing OK: P","1F646 & 2640 Z":"X gesturing OK","1F646 & 2640":"X gesturing OK","1F646 L & 2640 Z":"X gesturing OK: L","1F646 L & 2640":"X gesturing OK: L","1F646 M & 2640 Z":"X gesturing OK: M","1F646 M & 2640":"X gesturing OK: M","1F646 N & 2640 Z":"X gesturing OK: N","1F646 N & 2640":"X gesturing OK: N","1F646 O & 2640 Z":"X gesturing OK: O","1F646 O & 2640":"X gesturing OK: O","1F646 P & 2640 Z":"X gesturing OK: P","1F646 P & 2640":"X gesturing OK: P","1F481":"person tipping hand","1F481 L":"person tipping hand: L","1F481 M":"person tipping hand: M","1F481 N":"person tipping hand: N","1F481 O":"person tipping hand: O","1F481 P":"person tipping hand: P","1F481 & 2642 Z":"Y tipping hand","1F481 & 2642":"Y tipping hand","1F481 L & 2642 Z":"Y tipping hand: L","1F481 L & 2642":"Y tipping hand: L","1F481 M & 2642 Z":"Y tipping hand: M","1F481 M & 2642":"Y tipping hand: M","1F481 N & 2642 Z":"Y tipping hand: N","1F481 N & 2642":"Y tipping hand: N","1F481 O & 2642 Z":"Y tipping hand: O","1F481 O & 2642":"Y tipping hand: O","1F481 P & 2642 Z":"Y tipping hand: P","1F481 P & 2642":"Y tipping hand: P","1F481 & 2640 Z":"X tipping hand","1F481 & 2640":"X tipping hand","1F481 L & 2640 Z":"X tipping hand: L","1F481 L & 2640":"X tipping hand: L","1F481 M & 2640 Z":"X tipping hand: M","1F481 M & 2640":"X tipping hand: M","1F481 N & 2640 Z":"X tipping hand: N","1F481 N & 2640":"X tipping hand: N","1F481 O & 2640 Z":"X tipping hand: O","1F481 O & 2640":"X tipping hand: O","1F481 P & 2640 Z":"X tipping hand: P","1F481 P & 2640":"X tipping hand: P","1F64B":"person raising hand","1F64B L":"person raising hand: L","1F64B M":"person raising hand: M","1F64B N":"person raising hand: N","1F64B O":"person raising hand: O","1F64B P":"person raising hand: P","1F64B & 2642 Z":"Y raising hand","1F64B & 2642":"Y raising hand","1F64B L & 2642 Z":"Y raising hand: L","1F64B L & 2642":"Y raising hand: L","1F64B M & 2642 Z":"Y raising hand: M","1F64B M & 2642":"Y raising hand: M","1F64B N & 2642 Z":"Y raising hand: N","1F64B N & 2642":"Y raising hand: N","1F64B O & 2642 Z":"Y raising hand: O","1F64B O & 2642":"Y raising hand: O","1F64B P & 2642 Z":"Y raising hand: P","1F64B P & 2642":"Y raising hand: P","1F64B & 2640 Z":"X raising hand","1F64B & 2640":"X raising hand","1F64B L & 2640 Z":"X raising hand: L","1F64B L & 2640":"X raising hand: L","1F64B M & 2640 Z":"X raising hand: M","1F64B M & 2640":"X raising hand: M","1F64B N & 2640 Z":"X raising hand: N","1F64B N & 2640":"X raising hand: N","1F64B O & 2640 Z":"X raising hand: O","1F64B O & 2640":"X raising hand: O","1F64B P & 2640 Z":"X raising hand: P","1F64B P & 2640":"X raising hand: P","1F647":"person bowing","1F647 L":"person bowing: L","1F647 M":"person bowing: M","1F647 N":"person bowing: N","1F647 O":"person bowing: O","1F647 P":"person bowing: P","1F647 & 2642 Z":"Y bowing","1F647 & 2642":"Y bowing","1F647 L & 2642 Z":"Y bowing: L","1F647 L & 2642":"Y bowing: L","1F647 M & 2642 Z":"Y bowing: M","1F647 M & 2642":"Y bowing: M","1F647 N & 2642 Z":"Y bowing: N","1F647 N & 2642":"Y bowing: N","1F647 O & 2642 Z":"Y bowing: O","1F647 O & 2642":"Y bowing: O","1F647 P & 2642 Z":"Y bowing: P","1F647 P & 2642":"Y bowing: P","1F647 & 2640 Z":"X bowing","1F647 & 2640":"X bowing","1F647 L & 2640 Z":"X bowing: L","1F647 L & 2640":"X bowing: L","1F647 M & 2640 Z":"X bowing: M","1F647 M & 2640":"X bowing: M","1F647 N & 2640 Z":"X bowing: N","1F647 N & 2640":"X bowing: N","1F647 O & 2640 Z":"X bowing: O","1F647 O & 2640":"X bowing: O","1F647 P & 2640 Z":"X bowing: P","1F647 P & 2640":"X bowing: P","1F926":"person facepalming","1F926 L":"person facepalming: L","1F926 M":"person facepalming: M","1F926 N":"person facepalming: N","1F926 O":"person facepalming: O","1F926 P":"person facepalming: P","1F926 & 2642 Z":"Y facepalming","1F926 & 2642":"Y facepalming","1F926 L & 2642 Z":"Y facepalming: L","1F926 L & 2642":"Y facepalming: L","1F926 M & 2642 Z":"Y facepalming: M","1F926 M & 2642":"Y facepalming: M","1F926 N & 2642 Z":"Y facepalming: N","1F926 N & 2642":"Y facepalming: N","1F926 O & 2642 Z":"Y facepalming: O","1F926 O & 2642":"Y facepalming: O","1F926 P & 2642 Z":"Y facepalming: P","1F926 P & 2642":"Y facepalming: P","1F926 & 2640 Z":"X facepalming","1F926 & 2640":"X facepalming","1F926 L & 2640 Z":"X facepalming: L","1F926 L & 2640":"X facepalming: L","1F926 M & 2640 Z":"X facepalming: M","1F926 M & 2640":"X facepalming: M","1F926 N & 2640 Z":"X facepalming: N","1F926 N & 2640":"X facepalming: N","1F926 O & 2640 Z":"X facepalming: O","1F926 O & 2640":"X facepalming: O","1F926 P & 2640 Z":"X facepalming: P","1F926 P & 2640":"X facepalming: P","1F937":"person shrugging","1F937 L":"person shrugging: L","1F937 M":"person shrugging: M","1F937 N":"person shrugging: N","1F937 O":"person shrugging: O","1F937 P":"person shrugging: P","1F937 & 2642 Z":"Y shrugging","1F937 & 2642":"Y shrugging","1F937 L & 2642 Z":"Y shrugging: L","1F937 L & 2642":"Y shrugging: L","1F937 M & 2642 Z":"Y shrugging: M","1F937 M & 2642":"Y shrugging: M","1F937 N & 2642 Z":"Y shrugging: N","1F937 N & 2642":"Y shrugging: N","1F937 O & 2642 Z":"Y shrugging: O","1F937 O & 2642":"Y shrugging: O","1F937 P & 2642 Z":"Y shrugging: P","1F937 P & 2642":"Y shrugging: P","1F937 & 2640 Z":"X shrugging","1F937 & 2640":"X shrugging","1F937 L & 2640 Z":"X shrugging: L","1F937 L & 2640":"X shrugging: L","1F937 M & 2640 Z":"X shrugging: M","1F937 M & 2640":"X shrugging: M","1F937 N & 2640 Z":"X shrugging: N","1F937 N & 2640":"X shrugging: N","1F937 O & 2640 Z":"X shrugging: O","1F937 O & 2640":"X shrugging: O","1F937 P & 2640 Z":"X shrugging: P","1F937 P & 2640":"X shrugging: P"},"person-activity":{"1F486":"person getting massage","1F486 L":"person getting massage: L","1F486 M":"person getting massage: M","1F486 N":"person getting massage: N","1F486 O":"person getting massage: O","1F486 P":"person getting massage: P","1F486 & 2642 Z":"Y getting massage","1F486 & 2642":"Y getting massage","1F486 L & 2642 Z":"Y getting massage: L","1F486 L & 2642":"Y getting massage: L","1F486 M & 2642 Z":"Y getting massage: M","1F486 M & 2642":"Y getting massage: M","1F486 N & 2642 Z":"Y getting massage: N","1F486 N & 2642":"Y getting massage: N","1F486 O & 2642 Z":"Y getting massage: O","1F486 O & 2642":"Y getting massage: O","1F486 P & 2642 Z":"Y getting massage: P","1F486 P & 2642":"Y getting massage: P","1F486 & 2640 Z":"X getting massage","1F486 & 2640":"X getting massage","1F486 L & 2640 Z":"X getting massage: L","1F486 L & 2640":"X getting massage: L","1F486 M & 2640 Z":"X getting massage: M","1F486 M & 2640":"X getting massage: M","1F486 N & 2640 Z":"X getting massage: N","1F486 N & 2640":"X getting massage: N","1F486 O & 2640 Z":"X getting massage: O","1F486 O & 2640":"X getting massage: O","1F486 P & 2640 Z":"X getting massage: P","1F486 P & 2640":"X getting massage: P","1F487":"person getting haircut","1F487 L":"person getting haircut: L","1F487 M":"person getting haircut: M","1F487 N":"person getting haircut: N","1F487 O":"person getting haircut: O","1F487 P":"person getting haircut: P","1F487 & 2642 Z":"Y getting haircut","1F487 & 2642":"Y getting haircut","1F487 L & 2642 Z":"Y getting haircut: L","1F487 L & 2642":"Y getting haircut: L","1F487 M & 2642 Z":"Y getting haircut: M","1F487 M & 2642":"Y getting haircut: M","1F487 N & 2642 Z":"Y getting haircut: N","1F487 N & 2642":"Y getting haircut: N","1F487 O & 2642 Z":"Y getting haircut: O","1F487 O & 2642":"Y getting haircut: O","1F487 P & 2642 Z":"Y getting haircut: P","1F487 P & 2642":"Y getting haircut: P","1F487 & 2640 Z":"X getting haircut","1F487 & 2640":"X getting haircut","1F487 L & 2640 Z":"X getting haircut: L","1F487 L & 2640":"X getting haircut: L","1F487 M & 2640 Z":"X getting haircut: M","1F487 M & 2640":"X getting haircut: M","1F487 N & 2640 Z":"X getting haircut: N","1F487 N & 2640":"X getting haircut: N","1F487 O & 2640 Z":"X getting haircut: O","1F487 O & 2640":"X getting haircut: O","1F487 P & 2640 Z":"X getting haircut: P","1F487 P & 2640":"X getting haircut: P","1F6B6":"person walking","1F6B6 L":"person walking: L","1F6B6 M":"person walking: M","1F6B6 N":"person walking: N","1F6B6 O":"person walking: O","1F6B6 P":"person walking: P","1F6B6 & 2642 Z":"Y walking","1F6B6 & 2642":"Y walking","1F6B6 L & 2642 Z":"Y walking: L","1F6B6 L & 2642":"Y walking: L","1F6B6 M & 2642 Z":"Y walking: M","1F6B6 M & 2642":"Y walking: M","1F6B6 N & 2642 Z":"Y walking: N","1F6B6 N & 2642":"Y walking: N","1F6B6 O & 2642 Z":"Y walking: O","1F6B6 O & 2642":"Y walking: O","1F6B6 P & 2642 Z":"Y walking: P","1F6B6 P & 2642":"Y walking: P","1F6B6 & 2640 Z":"X walking","1F6B6 & 2640":"X walking","1F6B6 L & 2640 Z":"X walking: L","1F6B6 L & 2640":"X walking: L","1F6B6 M & 2640 Z":"X walking: M","1F6B6 M & 2640":"X walking: M","1F6B6 N & 2640 Z":"X walking: N","1F6B6 N & 2640":"X walking: N","1F6B6 O & 2640 Z":"X walking: O","1F6B6 O & 2640":"X walking: O","1F6B6 P & 2640 Z":"X walking: P","1F6B6 P & 2640":"X walking: P","1F3C3":"person running","1F3C3 L":"person running: L","1F3C3 M":"person running: M","1F3C3 N":"person running: N","1F3C3 O":"person running: O","1F3C3 P":"person running: P","1F3C3 & 2642 Z":"Y running","1F3C3 & 2642":"Y running","1F3C3 L & 2642 Z":"Y running: L","1F3C3 L & 2642":"Y running: L","1F3C3 M & 2642 Z":"Y running: M","1F3C3 M & 2642":"Y running: M","1F3C3 N & 2642 Z":"Y running: N","1F3C3 N & 2642":"Y running: N","1F3C3 O & 2642 Z":"Y running: O","1F3C3 O & 2642":"Y running: O","1F3C3 P & 2642 Z":"Y running: P","1F3C3 P & 2642":"Y running: P","1F3C3 & 2640 Z":"X running","1F3C3 & 2640":"X running","1F3C3 L & 2640 Z":"X running: L","1F3C3 L & 2640":"X running: L","1F3C3 M & 2640 Z":"X running: M","1F3C3 M & 2640":"X running: M","1F3C3 N & 2640 Z":"X running: N","1F3C3 N & 2640":"X running: N","1F3C3 O & 2640 Z":"X running: O","1F3C3 O & 2640":"X running: O","1F3C3 P & 2640 Z":"X running: P","1F3C3 P & 2640":"X running: P","1F483":"X dancing","1F483 L":"X dancing: L","1F483 M":"X dancing: M","1F483 N":"X dancing: N","1F483 O":"X dancing: O","1F483 P":"X dancing: P","1F57A":"Y dancing","1F57A L":"Y dancing: L","1F57A M":"Y dancing: M","1F57A N":"Y dancing: N","1F57A O":"Y dancing: O","1F57A P":"Y dancing: P","1F46F":"people with bunny ears","1F46F & 2642 Z":"men with bunny ears","1F46F & 2642":"men with bunny ears","1F46F & 2640 Z":"women with bunny ears","1F46F & 2640":"women with bunny ears","1F9D6":"person in steamy room","1F9D6 L":"person in steamy room: L","1F9D6 M":"person in steamy room: M","1F9D6 N":"person in steamy room: N","1F9D6 O":"person in steamy room: O","1F9D6 P":"person in steamy room: P","1F9D6 & 2640 Z":"X in steamy room","1F9D6 & 2640":"X in steamy room","1F9D6 L & 2640 Z":"X in steamy room: L","1F9D6 L & 2640":"X in steamy room: L","1F9D6 M & 2640 Z":"X in steamy room: M","1F9D6 M & 2640":"X in steamy room: M","1F9D6 N & 2640 Z":"X in steamy room: N","1F9D6 N & 2640":"X in steamy room: N","1F9D6 O & 2640 Z":"X in steamy room: O","1F9D6 O & 2640":"X in steamy room: O","1F9D6 P & 2640 Z":"X in steamy room: P","1F9D6 P & 2640":"X in steamy room: P","1F9D6 & 2642 Z":"Y in steamy room","1F9D6 & 2642":"Y in steamy room","1F9D6 L & 2642 Z":"Y in steamy room: L","1F9D6 L & 2642":"Y in steamy room: L","1F9D6 M & 2642 Z":"Y in steamy room: M","1F9D6 M & 2642":"Y in steamy room: M","1F9D6 N & 2642 Z":"Y in steamy room: N","1F9D6 N & 2642":"Y in steamy room: N","1F9D6 O & 2642 Z":"Y in steamy room: O","1F9D6 O & 2642":"Y in steamy room: O","1F9D6 P & 2642 Z":"Y in steamy room: P","1F9D6 P & 2642":"Y in steamy room: P","1F9D7":"person climbing","1F9D7 L":"person climbing: L","1F9D7 M":"person climbing: M","1F9D7 N":"person climbing: N","1F9D7 O":"person climbing: O","1F9D7 P":"person climbing: P","1F9D7 & 2640 Z":"X climbing","1F9D7 & 2640":"X climbing","1F9D7 L & 2640 Z":"X climbing: L","1F9D7 L & 2640":"X climbing: L","1F9D7 M & 2640 Z":"X climbing: M","1F9D7 M & 2640":"X climbing: M","1F9D7 N & 2640 Z":"X climbing: N","1F9D7 N & 2640":"X climbing: N","1F9D7 O & 2640 Z":"X climbing: O","1F9D7 O & 2640":"X climbing: O","1F9D7 P & 2640 Z":"X climbing: P","1F9D7 P & 2640":"X climbing: P","1F9D7 & 2642 Z":"Y climbing","1F9D7 & 2642":"Y climbing","1F9D7 L & 2642 Z":"Y climbing: L","1F9D7 L & 2642":"Y climbing: L","1F9D7 M & 2642 Z":"Y climbing: M","1F9D7 M & 2642":"Y climbing: M","1F9D7 N & 2642 Z":"Y climbing: N","1F9D7 N & 2642":"Y climbing: N","1F9D7 O & 2642 Z":"Y climbing: O","1F9D7 O & 2642":"Y climbing: O","1F9D7 P & 2642 Z":"Y climbing: P","1F9D7 P & 2642":"Y climbing: P","1F9D8":"person in lotus position","1F9D8 L":"person in lotus position: L","1F9D8 M":"person in lotus position: M","1F9D8 N":"person in lotus position: N","1F9D8 O":"person in lotus position: O","1F9D8 P":"person in lotus position: P","1F9D8 & 2640 Z":"X in lotus position","1F9D8 & 2640":"X in lotus position","1F9D8 L & 2640 Z":"X in lotus position: L","1F9D8 L & 2640":"X in lotus position: L","1F9D8 M & 2640 Z":"X in lotus position: M","1F9D8 M & 2640":"X in lotus position: M","1F9D8 N & 2640 Z":"X in lotus position: N","1F9D8 N & 2640":"X in lotus position: N","1F9D8 O & 2640 Z":"X in lotus position: O","1F9D8 O & 2640":"X in lotus position: O","1F9D8 P & 2640 Z":"X in lotus position: P","1F9D8 P & 2640":"X in lotus position: P","1F9D8 & 2642 Z":"Y in lotus position","1F9D8 & 2642":"Y in lotus position","1F9D8 L & 2642 Z":"Y in lotus position: L","1F9D8 L & 2642":"Y in lotus position: L","1F9D8 M & 2642 Z":"Y in lotus position: M","1F9D8 M & 2642":"Y in lotus position: M","1F9D8 N & 2642 Z":"Y in lotus position: N","1F9D8 N & 2642":"Y in lotus position: N","1F9D8 O & 2642 Z":"Y in lotus position: O","1F9D8 O & 2642":"Y in lotus position: O","1F9D8 P & 2642 Z":"Y in lotus position: P","1F9D8 P & 2642":"Y in lotus position: P","1F6C0":"person taking bath","1F6C0 L":"person taking bath: L","1F6C0 M":"person taking bath: M","1F6C0 N":"person taking bath: N","1F6C0 O":"person taking bath: O","1F6C0 P":"person taking bath: P","1F6CC":"person in bed","1F6CC L":"person in bed: L","1F6CC M":"person in bed: M","1F6CC N":"person in bed: N","1F6CC O":"person in bed: O","1F6CC P":"person in bed: P","1F574 Z":"Y in suit levitating","1F574":"Y in suit levitating","1F574 L":"Y in suit levitating: L","1F574 M":"Y in suit levitating: M","1F574 N":"Y in suit levitating: N","1F574 O":"Y in suit levitating: O","1F574 P":"Y in suit levitating: P","1F5E3 Z":"speaking head","1F5E3":"speaking head","1F464":"bust in silhouette","1F465":"busts in silhouette"},"person-sport":{"1F93A":"person fencing","1F3C7":"horse racing","1F3C7 L":"horse racing: L","1F3C7 M":"horse racing: M","1F3C7 N":"horse racing: N","1F3C7 O":"horse racing: O","1F3C7 P":"horse racing: P","26F7 Z":"skier","26F7":"skier","1F3C2":"snowboarder","1F3C2 L":"snowboarder: L","1F3C2 M":"snowboarder: M","1F3C2 N":"snowboarder: N","1F3C2 O":"snowboarder: O","1F3C2 P":"snowboarder: P","1F3CC Z":"person golfing","1F3CC":"person golfing","1F3CC L":"person golfing: L","1F3CC M":"person golfing: M","1F3CC N":"person golfing: N","1F3CC O":"person golfing: O","1F3CC P":"person golfing: P","1F3CC Z & 2642 Z":"Y golfing","1F3CC & 2642 Z":"Y golfing","1F3CC Z & 2642":"Y golfing","1F3CC & 2642":"Y golfing","1F3CC L & 2642 Z":"Y golfing: L","1F3CC L & 2642":"Y golfing: L","1F3CC M & 2642 Z":"Y golfing: M","1F3CC M & 2642":"Y golfing: M","1F3CC N & 2642 Z":"Y golfing: N","1F3CC N & 2642":"Y golfing: N","1F3CC O & 2642 Z":"Y golfing: O","1F3CC O & 2642":"Y golfing: O","1F3CC P & 2642 Z":"Y golfing: P","1F3CC P & 2642":"Y golfing: P","1F3CC Z & 2640 Z":"X golfing","1F3CC & 2640 Z":"X golfing","1F3CC Z & 2640":"X golfing","1F3CC & 2640":"X golfing","1F3CC L & 2640 Z":"X golfing: L","1F3CC L & 2640":"X golfing: L","1F3CC M & 2640 Z":"X golfing: M","1F3CC M & 2640":"X golfing: M","1F3CC N & 2640 Z":"X golfing: N","1F3CC N & 2640":"X golfing: N","1F3CC O & 2640 Z":"X golfing: O","1F3CC O & 2640":"X golfing: O","1F3CC P & 2640 Z":"X golfing: P","1F3CC P & 2640":"X golfing: P","1F3C4":"person surfing","1F3C4 L":"person surfing: L","1F3C4 M":"person surfing: M","1F3C4 N":"person surfing: N","1F3C4 O":"person surfing: O","1F3C4 P":"person surfing: P","1F3C4 & 2642 Z":"Y surfing","1F3C4 & 2642":"Y surfing","1F3C4 L & 2642 Z":"Y surfing: L","1F3C4 L & 2642":"Y surfing: L","1F3C4 M & 2642 Z":"Y surfing: M","1F3C4 M & 2642":"Y surfing: M","1F3C4 N & 2642 Z":"Y surfing: N","1F3C4 N & 2642":"Y surfing: N","1F3C4 O & 2642 Z":"Y surfing: O","1F3C4 O & 2642":"Y surfing: O","1F3C4 P & 2642 Z":"Y surfing: P","1F3C4 P & 2642":"Y surfing: P","1F3C4 & 2640 Z":"X surfing","1F3C4 & 2640":"X surfing","1F3C4 L & 2640 Z":"X surfing: L","1F3C4 L & 2640":"X surfing: L","1F3C4 M & 2640 Z":"X surfing: M","1F3C4 M & 2640":"X surfing: M","1F3C4 N & 2640 Z":"X surfing: N","1F3C4 N & 2640":"X surfing: N","1F3C4 O & 2640 Z":"X surfing: O","1F3C4 O & 2640":"X surfing: O","1F3C4 P & 2640 Z":"X surfing: P","1F3C4 P & 2640":"X surfing: P","1F6A3":"person rowing boat","1F6A3 L":"person rowing boat: L","1F6A3 M":"person rowing boat: M","1F6A3 N":"person rowing boat: N","1F6A3 O":"person rowing boat: O","1F6A3 P":"person rowing boat: P","1F6A3 & 2642 Z":"Y rowing boat","1F6A3 & 2642":"Y rowing boat","1F6A3 L & 2642 Z":"Y rowing boat: L","1F6A3 L & 2642":"Y rowing boat: L","1F6A3 M & 2642 Z":"Y rowing boat: M","1F6A3 M & 2642":"Y rowing boat: M","1F6A3 N & 2642 Z":"Y rowing boat: N","1F6A3 N & 2642":"Y rowing boat: N","1F6A3 O & 2642 Z":"Y rowing boat: O","1F6A3 O & 2642":"Y rowing boat: O","1F6A3 P & 2642 Z":"Y rowing boat: P","1F6A3 P & 2642":"Y rowing boat: P","1F6A3 & 2640 Z":"X rowing boat","1F6A3 & 2640":"X rowing boat","1F6A3 L & 2640 Z":"X rowing boat: L","1F6A3 L & 2640":"X rowing boat: L","1F6A3 M & 2640 Z":"X rowing boat: M","1F6A3 M & 2640":"X rowing boat: M","1F6A3 N & 2640 Z":"X rowing boat: N","1F6A3 N & 2640":"X rowing boat: N","1F6A3 O & 2640 Z":"X rowing boat: O","1F6A3 O & 2640":"X rowing boat: O","1F6A3 P & 2640 Z":"X rowing boat: P","1F6A3 P & 2640":"X rowing boat: P","1F3CA":"person swimming","1F3CA L":"person swimming: L","1F3CA M":"person swimming: M","1F3CA N":"person swimming: N","1F3CA O":"person swimming: O","1F3CA P":"person swimming: P","1F3CA & 2642 Z":"Y swimming","1F3CA & 2642":"Y swimming","1F3CA L & 2642 Z":"Y swimming: L","1F3CA L & 2642":"Y swimming: L","1F3CA M & 2642 Z":"Y swimming: M","1F3CA M & 2642":"Y swimming: M","1F3CA N & 2642 Z":"Y swimming: N","1F3CA N & 2642":"Y swimming: N","1F3CA O & 2642 Z":"Y swimming: O","1F3CA O & 2642":"Y swimming: O","1F3CA P & 2642 Z":"Y swimming: P","1F3CA P & 2642":"Y swimming: P","1F3CA & 2640 Z":"X swimming","1F3CA & 2640":"X swimming","1F3CA L & 2640 Z":"X swimming: L","1F3CA L & 2640":"X swimming: L","1F3CA M & 2640 Z":"X swimming: M","1F3CA M & 2640":"X swimming: M","1F3CA N & 2640 Z":"X swimming: N","1F3CA N & 2640":"X swimming: N","1F3CA O & 2640 Z":"X swimming: O","1F3CA O & 2640":"X swimming: O","1F3CA P & 2640 Z":"X swimming: P","1F3CA P & 2640":"X swimming: P","26F9 Z":"person bouncing ball","26F9":"person bouncing ball","26F9 L":"person bouncing ball: L","26F9 M":"person bouncing ball: M","26F9 N":"person bouncing ball: N","26F9 O":"person bouncing ball: O","26F9 P":"person bouncing ball: P","26F9 Z & 2642 Z":"Y bouncing ball","26F9 & 2642 Z":"Y bouncing ball","26F9 Z & 2642":"Y bouncing ball","26F9 & 2642":"Y bouncing ball","26F9 L & 2642 Z":"Y bouncing ball: L","26F9 L & 2642":"Y bouncing ball: L","26F9 M & 2642 Z":"Y bouncing ball: M","26F9 M & 2642":"Y bouncing ball: M","26F9 N & 2642 Z":"Y bouncing ball: N","26F9 N & 2642":"Y bouncing ball: N","26F9 O & 2642 Z":"Y bouncing ball: O","26F9 O & 2642":"Y bouncing ball: O","26F9 P & 2642 Z":"Y bouncing ball: P","26F9 P & 2642":"Y bouncing ball: P","26F9 Z & 2640 Z":"X bouncing ball","26F9 & 2640 Z":"X bouncing ball","26F9 Z & 2640":"X bouncing ball","26F9 & 2640":"X bouncing ball","26F9 L & 2640 Z":"X bouncing ball: L","26F9 L & 2640":"X bouncing ball: L","26F9 M & 2640 Z":"X bouncing ball: M","26F9 M & 2640":"X bouncing ball: M","26F9 N & 2640 Z":"X bouncing ball: N","26F9 N & 2640":"X bouncing ball: N","26F9 O & 2640 Z":"X bouncing ball: O","26F9 O & 2640":"X bouncing ball: O","26F9 P & 2640 Z":"X bouncing ball: P","26F9 P & 2640":"X bouncing ball: P","1F3CB Z":"person lifting weights","1F3CB":"person lifting weights","1F3CB L":"person lifting weights: L","1F3CB M":"person lifting weights: M","1F3CB N":"person lifting weights: N","1F3CB O":"person lifting weights: O","1F3CB P":"person lifting weights: P","1F3CB Z & 2642 Z":"Y lifting weights","1F3CB & 2642 Z":"Y lifting weights","1F3CB Z & 2642":"Y lifting weights","1F3CB & 2642":"Y lifting weights","1F3CB L & 2642 Z":"Y lifting weights: L","1F3CB L & 2642":"Y lifting weights: L","1F3CB M & 2642 Z":"Y lifting weights: M","1F3CB M & 2642":"Y lifting weights: M","1F3CB N & 2642 Z":"Y lifting weights: N","1F3CB N & 2642":"Y lifting weights: N","1F3CB O & 2642 Z":"Y lifting weights: O","1F3CB O & 2642":"Y lifting weights: O","1F3CB P & 2642 Z":"Y lifting weights: P","1F3CB P & 2642":"Y lifting weights: P","1F3CB Z & 2640 Z":"X lifting weights","1F3CB & 2640 Z":"X lifting weights","1F3CB Z & 2640":"X lifting weights","1F3CB & 2640":"X lifting weights","1F3CB L & 2640 Z":"X lifting weights: L","1F3CB L & 2640":"X lifting weights: L","1F3CB M & 2640 Z":"X lifting weights: M","1F3CB M & 2640":"X lifting weights: M","1F3CB N & 2640 Z":"X lifting weights: N","1F3CB N & 2640":"X lifting weights: N","1F3CB O & 2640 Z":"X lifting weights: O","1F3CB O & 2640":"X lifting weights: O","1F3CB P & 2640 Z":"X lifting weights: P","1F3CB P & 2640":"X lifting weights: P","1F6B4":"person biking","1F6B4 L":"person biking: L","1F6B4 M":"person biking: M","1F6B4 N":"person biking: N","1F6B4 O":"person biking: O","1F6B4 P":"person biking: P","1F6B4 & 2642 Z":"Y biking","1F6B4 & 2642":"Y biking","1F6B4 L & 2642 Z":"Y biking: L","1F6B4 L & 2642":"Y biking: L","1F6B4 M & 2642 Z":"Y biking: M","1F6B4 M & 2642":"Y biking: M","1F6B4 N & 2642 Z":"Y biking: N","1F6B4 N & 2642":"Y biking: N","1F6B4 O & 2642 Z":"Y biking: O","1F6B4 O & 2642":"Y biking: O","1F6B4 P & 2642 Z":"Y biking: P","1F6B4 P & 2642":"Y biking: P","1F6B4 & 2640 Z":"X biking","1F6B4 & 2640":"X biking","1F6B4 L & 2640 Z":"X biking: L","1F6B4 L & 2640":"X biking: L","1F6B4 M & 2640 Z":"X biking: M","1F6B4 M & 2640":"X biking: M","1F6B4 N & 2640 Z":"X biking: N","1F6B4 N & 2640":"X biking: N","1F6B4 O & 2640 Z":"X biking: O","1F6B4 O & 2640":"X biking: O","1F6B4 P & 2640 Z":"X biking: P","1F6B4 P & 2640":"X biking: P","1F6B5":"person mountain biking","1F6B5 L":"person mountain biking: L","1F6B5 M":"person mountain biking: M","1F6B5 N":"person mountain biking: N","1F6B5 O":"person mountain biking: O","1F6B5 P":"person mountain biking: P","1F6B5 & 2642 Z":"Y mountain biking","1F6B5 & 2642":"Y mountain biking","1F6B5 L & 2642 Z":"Y mountain biking: L","1F6B5 L & 2642":"Y mountain biking: L","1F6B5 M & 2642 Z":"Y mountain biking: M","1F6B5 M & 2642":"Y mountain biking: M","1F6B5 N & 2642 Z":"Y mountain biking: N","1F6B5 N & 2642":"Y mountain biking: N","1F6B5 O & 2642 Z":"Y mountain biking: O","1F6B5 O & 2642":"Y mountain biking: O","1F6B5 P & 2642 Z":"Y mountain biking: P","1F6B5 P & 2642":"Y mountain biking: P","1F6B5 & 2640 Z":"X mountain biking","1F6B5 & 2640":"X mountain biking","1F6B5 L & 2640 Z":"X mountain biking: L","1F6B5 L & 2640":"X mountain biking: L","1F6B5 M & 2640 Z":"X mountain biking: M","1F6B5 M & 2640":"X mountain biking: M","1F6B5 N & 2640 Z":"X mountain biking: N","1F6B5 N & 2640":"X mountain biking: N","1F6B5 O & 2640 Z":"X mountain biking: O","1F6B5 O & 2640":"X mountain biking: O","1F6B5 P & 2640 Z":"X mountain biking: P","1F6B5 P & 2640":"X mountain biking: P","1F3CE Z":"racing car","1F3CE":"racing car","1F3CD Z":"motorcycle","1F3CD":"motorcycle","1F938":"person cartwheeling","1F938 L":"person cartwheeling: L","1F938 M":"person cartwheeling: M","1F938 N":"person cartwheeling: N","1F938 O":"person cartwheeling: O","1F938 P":"person cartwheeling: P","1F938 & 2642 Z":"Y cartwheeling","1F938 & 2642":"Y cartwheeling","1F938 L & 2642 Z":"Y cartwheeling: L","1F938 L & 2642":"Y cartwheeling: L","1F938 M & 2642 Z":"Y cartwheeling: M","1F938 M & 2642":"Y cartwheeling: M","1F938 N & 2642 Z":"Y cartwheeling: N","1F938 N & 2642":"Y cartwheeling: N","1F938 O & 2642 Z":"Y cartwheeling: O","1F938 O & 2642":"Y cartwheeling: O","1F938 P & 2642 Z":"Y cartwheeling: P","1F938 P & 2642":"Y cartwheeling: P","1F938 & 2640 Z":"X cartwheeling","1F938 & 2640":"X cartwheeling","1F938 L & 2640 Z":"X cartwheeling: L","1F938 L & 2640":"X cartwheeling: L","1F938 M & 2640 Z":"X cartwheeling: M","1F938 M & 2640":"X cartwheeling: M","1F938 N & 2640 Z":"X cartwheeling: N","1F938 N & 2640":"X cartwheeling: N","1F938 O & 2640 Z":"X cartwheeling: O","1F938 O & 2640":"X cartwheeling: O","1F938 P & 2640 Z":"X cartwheeling: P","1F938 P & 2640":"X cartwheeling: P","1F93C":"people wrestling","1F93C & 2642 Z":"men wrestling","1F93C & 2642":"men wrestling","1F93C & 2640 Z":"women wrestling","1F93C & 2640":"women wrestling","1F93D":"person playing water polo","1F93D L":"person playing water polo: L","1F93D M":"person playing water polo: M","1F93D N":"person playing water polo: N","1F93D O":"person playing water polo: O","1F93D P":"person playing water polo: P","1F93D & 2642 Z":"Y playing water polo","1F93D & 2642":"Y playing water polo","1F93D L & 2642 Z":"Y playing water polo: L","1F93D L & 2642":"Y playing water polo: L","1F93D M & 2642 Z":"Y playing water polo: M","1F93D M & 2642":"Y playing water polo: M","1F93D N & 2642 Z":"Y playing water polo: N","1F93D N & 2642":"Y playing water polo: N","1F93D O & 2642 Z":"Y playing water polo: O","1F93D O & 2642":"Y playing water polo: O","1F93D P & 2642 Z":"Y playing water polo: P","1F93D P & 2642":"Y playing water polo: P","1F93D & 2640 Z":"X playing water polo","1F93D & 2640":"X playing water polo","1F93D L & 2640 Z":"X playing water polo: L","1F93D L & 2640":"X playing water polo: L","1F93D M & 2640 Z":"X playing water polo: M","1F93D M & 2640":"X playing water polo: M","1F93D N & 2640 Z":"X playing water polo: N","1F93D N & 2640":"X playing water polo: N","1F93D O & 2640 Z":"X playing water polo: O","1F93D O & 2640":"X playing water polo: O","1F93D P & 2640 Z":"X playing water polo: P","1F93D P & 2640":"X playing water polo: P","1F93E":"person playing handball","1F93E L":"person playing handball: L","1F93E M":"person playing handball: M","1F93E N":"person playing handball: N","1F93E O":"person playing handball: O","1F93E P":"person playing handball: P","1F93E & 2642 Z":"Y playing handball","1F93E & 2642":"Y playing handball","1F93E L & 2642 Z":"Y playing handball: L","1F93E L & 2642":"Y playing handball: L","1F93E M & 2642 Z":"Y playing handball: M","1F93E M & 2642":"Y playing handball: M","1F93E N & 2642 Z":"Y playing handball: N","1F93E N & 2642":"Y playing handball: N","1F93E O & 2642 Z":"Y playing handball: O","1F93E O & 2642":"Y playing handball: O","1F93E P & 2642 Z":"Y playing handball: P","1F93E P & 2642":"Y playing handball: P","1F93E & 2640 Z":"X playing handball","1F93E & 2640":"X playing handball","1F93E L & 2640 Z":"X playing handball: L","1F93E L & 2640":"X playing handball: L","1F93E M & 2640 Z":"X playing handball: M","1F93E M & 2640":"X playing handball: M","1F93E N & 2640 Z":"X playing handball: N","1F93E N & 2640":"X playing handball: N","1F93E O & 2640 Z":"X playing handball: O","1F93E O & 2640":"X playing handball: O","1F93E P & 2640 Z":"X playing handball: P","1F93E P & 2640":"X playing handball: P","1F939":"person juggling","1F939 L":"person juggling: L","1F939 M":"person juggling: M","1F939 N":"person juggling: N","1F939 O":"person juggling: O","1F939 P":"person juggling: P","1F939 & 2642 Z":"Y juggling","1F939 & 2642":"Y juggling","1F939 L & 2642 Z":"Y juggling: L","1F939 L & 2642":"Y juggling: L","1F939 M & 2642 Z":"Y juggling: M","1F939 M & 2642":"Y juggling: M","1F939 N & 2642 Z":"Y juggling: N","1F939 N & 2642":"Y juggling: N","1F939 O & 2642 Z":"Y juggling: O","1F939 O & 2642":"Y juggling: O","1F939 P & 2642 Z":"Y juggling: P","1F939 P & 2642":"Y juggling: P","1F939 & 2640 Z":"X juggling","1F939 & 2640":"X juggling","1F939 L & 2640 Z":"X juggling: L","1F939 L & 2640":"X juggling: L","1F939 M & 2640 Z":"X juggling: M","1F939 M & 2640":"X juggling: M","1F939 N & 2640 Z":"X juggling: N","1F939 N & 2640":"X juggling: N","1F939 O & 2640 Z":"X juggling: O","1F939 O & 2640":"X juggling: O","1F939 P & 2640 Z":"X juggling: P","1F939 P & 2640":"X juggling: P"},family:{"1F46B":"Y and X holding hands","1F46C":"two men holding hands","1F46D":"two women holding hands","1F48F":"kiss","X & 2764 Z & 1F48B & Y":"kiss: X, Y","X & 2764 & 1F48B & Y":"kiss: X, Y","Y & 2764 Z & 1F48B & Y":"kiss: Y, Y","Y & 2764 & 1F48B & Y":"kiss: Y, Y","X & 2764 Z & 1F48B & X":"kiss: X, X","X & 2764 & 1F48B & X":"kiss: X, X","1F491":"couple with heart","X & 2764 Z & Y":"couple with heart: X, Y","X & 2764 & Y":"couple with heart: X, Y","Y & 2764 Z & Y":"couple with heart: Y, Y","Y & 2764 & Y":"couple with heart: Y, Y","X & 2764 Z & X":"couple with heart: X, X","X & 2764 & X":"couple with heart: X, X","1F46A":"family","Y & X & W":"family: Y, X, W","Y & X & V":"family: Y, X, V","Y & X & V & W":"family: Y, X, V, W","Y & X & W & W":"family: Y, X, W, W","Y & X & V & V":"family: Y, X, V, V","Y & Y & W":"family: Y, Y, W","Y & Y & V":"family: Y, Y, V","Y & Y & V & W":"family: Y, Y, V, W","Y & Y & W & W":"family: Y, Y, W, W","Y & Y & V & V":"family: Y, Y, V, V","X & X & W":"family: X, X, W","X & X & V":"family: X, X, V","X & X & V & W":"family: X, X, V, W","X & X & W & W":"family: X, X, W, W","X & X & V & V":"family: X, X, V, V","Y & W":"family: Y, W","Y & W & W":"family: Y, W, W","Y & V":"family: Y, V","Y & V & W":"family: Y , V, W","Y & V & V":"family: Y , V, V","X & W":"family: X, W","X & W & W":"family: X, W, W","X & V":"family: X, V","X & V & W":"family: X, V, W","X & V & V":"family: X, V, V"},body:{"1F933":"selfie","1F933 L":"selfie: L","1F933 M":"selfie: M","1F933 N":"selfie: N","1F933 O":"selfie: O","1F933 P":"selfie: P","1F4AA":"flexed biceps","1F4AA L":"flexed biceps: L","1F4AA M":"flexed biceps: M","1F4AA N":"flexed biceps: N","1F4AA O":"flexed biceps: O","1F4AA P":"flexed biceps: P","1F448":"backhand index pointing left","1F448 L":"backhand index pointing left: L","1F448 M":"backhand index pointing left: M","1F448 N":"backhand index pointing left: N","1F448 O":"backhand index pointing left: O","1F448 P":"backhand index pointing left: P","1F449":"backhand index pointing right","1F449 L":"backhand index pointing right: L","1F449 M":"backhand index pointing right: M","1F449 N":"backhand index pointing right: N","1F449 O":"backhand index pointing right: O","1F449 P":"backhand index pointing right: P","261D Z":"index pointing up","261D":"index pointing up","261D L":"index pointing up: L","261D M":"index pointing up: M","261D N":"index pointing up: N","261D O":"index pointing up: O","261D P":"index pointing up: P","1F446":"backhand index pointing up","1F446 L":"backhand index pointing up: L","1F446 M":"backhand index pointing up: M","1F446 N":"backhand index pointing up: N","1F446 O":"backhand index pointing up: O","1F446 P":"backhand index pointing up: P","1F595":"middle finger","1F595 L":"middle finger: L","1F595 M":"middle finger: M","1F595 N":"middle finger: N","1F595 O":"middle finger: O","1F595 P":"middle finger: P","1F447":"backhand index pointing down","1F447 L":"backhand index pointing down: L","1F447 M":"backhand index pointing down: M","1F447 N":"backhand index pointing down: N","1F447 O":"backhand index pointing down: O","1F447 P":"backhand index pointing down: P","270C Z":"victory hand","270C":"victory hand","270C L":"victory hand: L","270C M":"victory hand: M","270C N":"victory hand: N","270C O":"victory hand: O","270C P":"victory hand: P","1F91E":"crossed fingers","1F91E L":"crossed fingers: L","1F91E M":"crossed fingers: M","1F91E N":"crossed fingers: N","1F91E O":"crossed fingers: O","1F91E P":"crossed fingers: P","1F596":"vulcan salute","1F596 L":"vulcan salute: L","1F596 M":"vulcan salute: M","1F596 N":"vulcan salute: N","1F596 O":"vulcan salute: O","1F596 P":"vulcan salute: P","1F918":"sign of the horns","1F918 L":"sign of the horns: L","1F918 M":"sign of the horns: M","1F918 N":"sign of the horns: N","1F918 O":"sign of the horns: O","1F918 P":"sign of the horns: P","1F919":"call me hand","1F919 L":"call me hand: L","1F919 M":"call me hand: M","1F919 N":"call me hand: N","1F919 O":"call me hand: O","1F919 P":"call me hand: P","1F590 Z":"hand with fingers splayed","1F590":"hand with fingers splayed","1F590 L":"hand with fingers splayed: L","1F590 M":"hand with fingers splayed: M","1F590 N":"hand with fingers splayed: N","1F590 O":"hand with fingers splayed: O","1F590 P":"hand with fingers splayed: P","270B":"raised hand","270B L":"raised hand: L","270B M":"raised hand: M","270B N":"raised hand: N","270B O":"raised hand: O","270B P":"raised hand: P","1F44C":"OK hand","1F44C L":"OK hand: L","1F44C M":"OK hand: M","1F44C N":"OK hand: N","1F44C O":"OK hand: O","1F44C P":"OK hand: P","1F44D":"thumbs up","1F44D L":"thumbs up: L","1F44D M":"thumbs up: M","1F44D N":"thumbs up: N","1F44D O":"thumbs up: O","1F44D P":"thumbs up: P","1F44E":"thumbs down","1F44E L":"thumbs down: L","1F44E M":"thumbs down: M","1F44E N":"thumbs down: N","1F44E O":"thumbs down: O","1F44E P":"thumbs down: P","270A":"raised fist","270A L":"raised fist: L","270A M":"raised fist: M","270A N":"raised fist: N","270A O":"raised fist: O","270A P":"raised fist: P","1F44A":"oncoming fist","1F44A L":"oncoming fist: L","1F44A M":"oncoming fist: M","1F44A N":"oncoming fist: N","1F44A O":"oncoming fist: O","1F44A P":"oncoming fist: P","1F91B":"left-facing fist","1F91B L":"left-facing fist: L","1F91B M":"left-facing fist: M","1F91B N":"left-facing fist: N","1F91B O":"left-facing fist: O","1F91B P":"left-facing fist: P","1F91C":"right-facing fist","1F91C L":"right-facing fist: L","1F91C M":"right-facing fist: M","1F91C N":"right-facing fist: N","1F91C O":"right-facing fist: O","1F91C P":"right-facing fist: P","1F91A":"raised back of hand","1F91A L":"raised back of hand: L","1F91A M":"raised back of hand: M","1F91A N":"raised back of hand: N","1F91A O":"raised back of hand: O","1F91A P":"raised back of hand: P","1F44B":"waving hand","1F44B L":"waving hand: L","1F44B M":"waving hand: M","1F44B N":"waving hand: N","1F44B O":"waving hand: O","1F44B P":"waving hand: P","1F91F":"love-you gesture","1F91F L":"love-you gesture: L","1F91F M":"love-you gesture: M","1F91F N":"love-you gesture: N","1F91F O":"love-you gesture: O","1F91F P":"love-you gesture: P","270D Z":"writing hand","270D":"writing hand","270D L":"writing hand: L","270D M":"writing hand: M","270D N":"writing hand: N","270D O":"writing hand: O","270D P":"writing hand: P","1F44F":"clapping hands","1F44F L":"clapping hands: L","1F44F M":"clapping hands: M","1F44F N":"clapping hands: N","1F44F O":"clapping hands: O","1F44F P":"clapping hands: P","1F450":"open hands","1F450 L":"open hands: L","1F450 M":"open hands: M","1F450 N":"open hands: N","1F450 O":"open hands: O","1F450 P":"open hands: P","1F64C":"raising hands","1F64C L":"raising hands: L","1F64C M":"raising hands: M","1F64C N":"raising hands: N","1F64C O":"raising hands: O","1F64C P":"raising hands: P","1F932":"palms up together","1F932 L":"palms up together: L","1F932 M":"palms up together: M","1F932 N":"palms up together: N","1F932 O":"palms up together: O","1F932 P":"palms up together: P","1F64F":"folded hands","1F64F L":"folded hands: L","1F64F M":"folded hands: M","1F64F N":"folded hands: N","1F64F O":"folded hands: O","1F64F P":"folded hands: P","1F91D":"handshake","1F485":"nail polish","1F485 L":"nail polish: L","1F485 M":"nail polish: M","1F485 N":"nail polish: N","1F485 O":"nail polish: O","1F485 P":"nail polish: P","1F442":"ear","1F442 L":"ear: L","1F442 M":"ear: M","1F442 N":"ear: N","1F442 O":"ear: O","1F442 P":"ear: P","1F443":"nose","1F443 L":"nose: L","1F443 M":"nose: M","1F443 N":"nose: N","1F443 O":"nose: O","1F443 P":"nose: P","1F463":"footprints","1F440":"eyes","1F441 Z":"eye","1F441":"eye","1F441 Z & 1F5E8 Z":"eye in speech bubble","1F441 & 1F5E8 Z":"eye in speech bubble","1F441 Z & 1F5E8":"eye in speech bubble","1F441 & 1F5E8":"eye in speech bubble","1F9E0":"brain","1F445":"tongue","1F444":"mouth"},emotion:{"1F48B":"kiss mark","1F498":"heart with arrow","2764 Z":"red heart","2764":"red heart","1F493":"beating heart","1F494":"broken heart","1F495":"two hearts","1F496":"sparkling heart","1F497":"growing heart","1F499":"blue heart","1F49A":"green heart","1F49B":"yellow heart","1F9E1":"orange heart","1F49C":"purple heart","1F5A4":"black heart","1F49D":"heart with ribbon","1F49E":"revolving hearts","1F49F":"heart decoration","2763 Z":"heavy heart exclamation","2763":"heavy heart exclamation","1F48C":"love letter","1F4A4":"zzz","1F4A2":"anger symbol","1F4A3":"bomb","1F4A5":"collision","1F4A6":"sweat droplets","1F4A8":"dashing away","1F4AB":"dizzy","1F4AC":"speech balloon","1F5E8 Z":"left speech bubble","1F5E8":"left speech bubble","1F5EF Z":"right anger bubble","1F5EF":"right anger bubble","1F4AD":"thought balloon","1F573 Z":"hole","1F573":"hole"},clothing:{"1F453":"glasses","1F576 Z":"sunglasses","1F576":"sunglasses","1F454":"necktie","1F455":"t-shirt","1F456":"jeans","1F9E3":"scarf","1F9E4":"gloves","1F9E5":"coat","1F9E6":"socks","1F457":"dress","1F458":"kimono","1F459":"bikini","1F45A":"X’s clothes","1F45B":"purse","1F45C":"handbag","1F45D":"clutch bag","1F6CD Z":"shopping bags","1F6CD":"shopping bags","1F392":"school backpack","1F45E":"Y’s shoe","1F45F":"running shoe","1F460":"high-heeled shoe","1F461":"X’s sandal","1F462":"X’s boot","1F451":"crown","1F452":"X’s hat","1F3A9":"top hat","1F393":"graduation cap","1F9E2":"billed cap","26D1 Z":"rescue worker’s helmet","26D1":"rescue worker’s helmet","1F4FF":"prayer beads","1F484":"lipstick","1F48D":"ring","1F48E":"gem stone"}},"Animals & Nature":{"animal-mammal":{"1F435":"monkey face","1F412":"monkey","1F98D":"gorilla","1F436":"dog face","1F415":"dog","1F429":"poodle","1F43A":"wolf face","1F98A":"fox face","1F431":"cat face","1F408":"cat","1F981":"lion face","1F42F":"tiger face","1F405":"tiger","1F406":"leopard","1F434":"horse face","1F40E":"horse","1F984":"unicorn face","1F993":"zebra","1F98C":"deer","1F42E":"cow face","1F402":"ox","1F403":"water buffalo","1F404":"cow","1F437":"pig face","1F416":"pig","1F417":"boar","1F43D":"pig nose","1F40F":"ram","1F411":"ewe","1F410":"goat","1F42A":"camel","1F42B":"two-hump camel","1F992":"giraffe","1F418":"elephant","1F98F":"rhinoceros","1F42D":"mouse face","1F401":"mouse","1F400":"rat","1F439":"hamster face","1F430":"rabbit face","1F407":"rabbit","1F43F Z":"chipmunk","1F43F":"chipmunk","1F994":"hedgehog","1F987":"bat","1F43B":"bear face","1F428":"koala","1F43C":"panda face","1F43E":"paw prints"},"animal-bird":{"1F983":"turkey","1F414":"chicken","1F413":"rooster","1F423":"hatching chick","1F424":"baby chick","1F425":"front-facing baby chick","1F426":"bird","1F427":"penguin","1F54A Z":"dove","1F54A":"dove","1F985":"eagle","1F986":"duck","1F989":"owl"},"animal-amphibian":{"1F438":"frog face"},"animal-reptile":{"1F40A":"crocodile","1F422":"turtle","1F98E":"lizard","1F40D":"snake","1F432":"dragon face","1F409":"dragon","1F995":"sauropod","1F996":"T-Rex"},"animal-marine":{"1F433":"spouting whale","1F40B":"whale","1F42C":"dolphin","1F41F":"fish","1F420":"tropical fish","1F421":"blowfish","1F988":"shark","1F419":"octopus","1F41A":"spiral shell","1F980":"crab","1F990":"shrimp","1F991":"squid"},"animal-bug":{"1F40C":"snail","1F98B":"butterfly","1F41B":"bug","1F41C":"ant","1F41D":"honeybee","1F41E":"lady beetle","1F997":"cricket","1F577 Z":"spider","1F577":"spider","1F578 Z":"spider web","1F578":"spider web","1F982":"scorpion"},"plant-flower":{"1F490":"bouquet","1F338":"cherry blossom","1F4AE":"white flower","1F3F5 Z":"rosette","1F3F5":"rosette","1F339":"rose","1F940":"wilted flower","1F33A":"hibiscus","1F33B":"sunflower","1F33C":"blossom","1F337":"tulip"},"plant-other":{"1F331":"seedling","1F332":"evergreen tree","1F333":"deciduous tree","1F334":"palm tree","1F335":"cactus","1F33E":"sheaf of rice","1F33F":"herb","2618 Z":"shamrock","2618":"shamrock","1F340":"four leaf clover","1F341":"maple leaf","1F342":"fallen leaf","1F343":"leaf fluttering in wind"}},"Food & Drink":{"food-fruit":{"1F347":"grapes","1F348":"melon","1F349":"watermelon","1F34A":"tangerine","1F34B":"lemon","1F34C":"banana","1F34D":"pineapple","1F34E":"red apple","1F34F":"green apple","1F350":"pear","1F351":"peach","1F352":"cherries","1F353":"strawberry","1F95D":"kiwi fruit","1F345":"tomato","1F965":"coconut"},"food-vegetable":{"1F951":"avocado","1F346":"eggplant","1F954":"potato","1F955":"carrot","1F33D":"ear of corn","1F336 Z":"hot pepper","1F336":"hot pepper","1F952":"cucumber","1F966":"broccoli","1F344":"mushroom","1F95C":"peanuts","1F330":"chestnut"},"food-prepared":{"1F35E":"bread","1F950":"croissant","1F956":"baguette bread","1F968":"pretzel","1F95E":"pancakes","1F9C0":"cheese wedge","1F356":"meat on bone","1F357":"poultry leg","1F969":"cut of meat","1F953":"bacon","1F354":"hamburger","1F35F":"french fries","1F355":"pizza","1F32D":"hot dog","1F96A":"sandwich","1F32E":"taco","1F32F":"burrito","1F959":"stuffed flatbread","1F95A":"egg","1F373":"cooking","1F958":"shallow pan of food","1F372":"pot of food","1F963":"bowl with spoon","1F957":"green salad","1F37F":"popcorn","1F96B":"canned food"},"food-asian":{"1F371":"bento box","1F358":"rice cracker","1F359":"rice ball","1F35A":"cooked rice","1F35B":"curry rice","1F35C":"steaming bowl","1F35D":"spaghetti","1F360":"roasted sweet potato","1F362":"oden","1F363":"sushi","1F364":"fried shrimp","1F365":"fish cake with swirl","1F361":"dango","1F95F":"dumpling","1F960":"fortune cookie","1F961":"takeout box"},"food-sweet":{"1F366":"soft ice cream","1F367":"shaved ice","1F368":"ice cream","1F369":"doughnut","1F36A":"cookie","1F382":"birthday cake","1F370":"shortcake","1F967":"pie","1F36B":"chocolate bar","1F36C":"candy","1F36D":"lollipop","1F36E":"custard","1F36F":"honey pot"},drink:{"1F37C":"baby bottle","1F95B":"glass of milk","2615":"hot beverage","1F375":"teacup without handle","1F376":"sake","1F37E":"bottle with popping cork","1F377":"wine glass","1F378":"cocktail glass","1F379":"tropical drink","1F37A":"beer mug","1F37B":"clinking beer mugs","1F942":"clinking glasses","1F943":"tumbler glass","1F964":"cup with straw"},dishware:{"1F962":"chopsticks","1F37D Z":"fork and knife with plate","1F37D":"fork and knife with plate","1F374":"fork and knife","1F944":"spoon","1F52A":"kitchen knife","1F3FA":"amphora"}},"Travel & Places":{"place-map":{"1F30D":"globe showing Europe-Africa","1F30E":"globe showing Americas","1F30F":"globe showing Asia-Australia","1F310":"globe with meridians","1F5FA Z":"world map","1F5FA":"world map","1F5FE":"map of Japan"},"place-geographic":{"1F3D4 Z":"snow-capped mountain","1F3D4":"snow-capped mountain","26F0 Z":"mountain","26F0":"mountain","1F30B":"volcano","1F5FB":"mount fuji","1F3D5 Z":"camping","1F3D5":"camping","1F3D6 Z":"beach with umbrella","1F3D6":"beach with umbrella","1F3DC Z":"desert","1F3DC":"desert","1F3DD Z":"desert island","1F3DD":"desert island","1F3DE Z":"national park","1F3DE":"national park"},"place-building":{"1F3DF Z":"stadium","1F3DF":"stadium","1F3DB Z":"classical building","1F3DB":"classical building","1F3D7 Z":"building construction","1F3D7":"building construction","1F3D8 Z":"houses","1F3D8":"houses","1F3D9 Z":"cityscape","1F3D9":"cityscape","1F3DA Z":"derelict house","1F3DA":"derelict house","1F3E0":"house","1F3E1":"house with garden","1F3E2":"office building","1F3E3":"Japanese post office","1F3E4":"post office","1F3E5":"hospital","1F3E6":"bank","1F3E8":"hotel","1F3E9":"love hotel","1F3EA":"convenience store","1F3EB":"school","1F3EC":"department store","1F3ED":"factory","1F3EF":"Japanese castle","1F3F0":"castle","1F492":"wedding","1F5FC":"Tokyo tower","1F5FD":"Statue of Liberty"},"place-religious":{"26EA":"church","1F54C":"mosque","1F54D":"synagogue","26E9 Z":"shinto shrine","26E9":"shinto shrine","1F54B":"kaaba"},"place-other":{"26F2":"fountain","26FA":"tent","1F301":"foggy","1F303":"night with stars","1F304":"sunrise over mountains","1F305":"sunrise","1F306":"cityscape at dusk","1F307":"sunset","1F309":"bridge at night","2668 Z":"hot springs","2668":"hot springs","1F30C":"milky way","1F3A0":"carousel horse","1F3A1":"ferris wheel","1F3A2":"roller coaster","1F488":"barber pole","1F3AA":"circus tent","1F3AD":"performing arts","1F5BC Z":"framed picture","1F5BC":"framed picture","1F3A8":"artist palette","1F3B0":"slot machine"},"transport-ground":{"1F682":"locomotive","1F683":"railway car","1F684":"high-speed train","1F685":"bullet train","1F686":"train","1F687":"metro","1F688":"light rail","1F689":"station","1F68A":"tram","1F69D":"monorail","1F69E":"mountain railway","1F68B":"tram car","1F68C":"bus","1F68D":"oncoming bus","1F68E":"trolleybus","1F690":"minibus","1F691":"ambulance","1F692":"fire engine","1F693":"police car","1F694":"oncoming police car","1F695":"taxi","1F696":"oncoming taxi","1F697":"automobile","1F698":"oncoming automobile","1F699":"sport utility vehicle","1F69A":"delivery truck","1F69B":"articulated lorry","1F69C":"tractor","1F6B2":"bicycle","1F6F4":"kick scooter","1F6F5":"motor scooter","1F68F":"bus stop","1F6E3 Z":"motorway","1F6E3":"motorway","1F6E4 Z":"railway track","1F6E4":"railway track","26FD":"fuel pump","1F6A8":"police car light","1F6A5":"horizontal traffic light","1F6A6":"vertical traffic light","1F6A7":"construction","1F6D1":"stop sign"},"transport-water":{"2693":"anchor","26F5":"sailboat","1F6F6":"canoe","1F6A4":"speedboat","1F6F3 Z":"passenger ship","1F6F3":"passenger ship","26F4 Z":"ferry","26F4":"ferry","1F6E5 Z":"motor boat","1F6E5":"motor boat","1F6A2":"ship"},"transport-air":{"2708 Z":"airplane","2708":"airplane","1F6E9 Z":"small airplane","1F6E9":"small airplane","1F6EB":"airplane departure","1F6EC":"airplane arrival","1F4BA":"seat","1F681":"helicopter","1F69F":"suspension railway","1F6A0":"mountain cableway","1F6A1":"aerial tramway","1F6F0 Z":"satellite","1F6F0":"satellite","1F680":"rocket","1F6F8":"flying saucer"},hotel:{"1F6CE Z":"bellhop bell","1F6CE":"bellhop bell","1F6AA":"door","1F6CF Z":"bed","1F6CF":"bed","1F6CB Z":"couch and lamp","1F6CB":"couch and lamp","1F6BD":"toilet","1F6BF":"shower","1F6C1":"bathtub"},time:{"231B":"hourglass done","23F3":"hourglass not done","231A":"watch","23F0":"alarm clock","23F1 Z":"stopwatch","23F1":"stopwatch","23F2 Z":"timer clock","23F2":"timer clock","1F570 Z":"Ytelpiece clock","1F570":"Ytelpiece clock","1F55B":"twelve o’clock","1F567":"twelve-thirty","1F550":"one o’clock","1F55C":"one-thirty","1F551":"two o’clock","1F55D":"two-thirty","1F552":"three o’clock","1F55E":"three-thirty","1F553":"four o’clock","1F55F":"four-thirty","1F554":"five o’clock","1F560":"five-thirty","1F555":"six o’clock","1F561":"six-thirty","1F556":"seven o’clock","1F562":"seven-thirty","1F557":"eight o’clock","1F563":"eight-thirty","1F558":"nine o’clock","1F564":"nine-thirty","1F559":"ten o’clock","1F565":"ten-thirty","1F55A":"eleven o’clock","1F566":"eleven-thirty"},"sky & weather":{"1F311":"new moon","1F312":"waxing crescent moon","1F313":"first quarter moon","1F314":"waxing gibbous moon","1F315":"full moon","1F316":"waning gibbous moon","1F317":"last quarter moon","1F318":"waning crescent moon","1F319":"crescent moon","1F31A":"new moon face","1F31B":"first quarter moon face","1F31C":"last quarter moon face","1F321 Z":"thermometer","1F321":"thermometer","2600 Z":"sun","2600":"sun","1F31D":"full moon face","1F31E":"sun with face","2B50":"white medium star","1F31F":"glowing star","1F320":"shooting star","2601 Z":"cloud","2601":"cloud","26C5":"sun behind cloud","26C8 Z":"cloud with lightning and rain","26C8":"cloud with lightning and rain","1F324 Z":"sun behind small cloud","1F324":"sun behind small cloud","1F325 Z":"sun behind large cloud","1F325":"sun behind large cloud","1F326 Z":"sun behind rain cloud","1F326":"sun behind rain cloud","1F327 Z":"cloud with rain","1F327":"cloud with rain","1F328 Z":"cloud with snow","1F328":"cloud with snow","1F329 Z":"cloud with lightning","1F329":"cloud with lightning","1F32A Z":"tornado","1F32A":"tornado","1F32B Z":"fog","1F32B":"fog","1F32C Z":"wind face","1F32C":"wind face","1F300":"cyclone","1F308":"rainbow","1F302":"closed umbrella","2602 Z":"umbrella","2602":"umbrella","2614":"umbrella with rain drops","26F1 Z":"umbrella on ground","26F1":"umbrella on ground","26A1":"high voltage","2744 Z":"snowflake","2744":"snowflake","2603 Z":"snowY","2603":"snowY","26C4":"snowY without snow","2604 Z":"comet","2604":"comet","1F525":"fire","1F4A7":"droplet","1F30A":"water wave"}},Activities:{event:{"1F383":"jack-o-lantern","1F384":"Christmas tree","1F386":"fireworks","1F387":"sparkler","2728":"sparkles","1F388":"balloon","1F389":"party popper","1F38A":"confetti ball","1F38B":"tanabata tree","1F38D":"pine decoration","1F38E":"Japanese dolls","1F38F":"carp streamer","1F390":"wind chime","1F391":"moon viewing ceremony","1F380":"ribbon","1F381":"wrapped gift","1F397 Z":"reminder ribbon","1F397":"reminder ribbon","1F39F Z":"admission tickets","1F39F":"admission tickets","1F3AB":"ticket"},"award-medal":{"1F396 Z":"military medal","1F396":"military medal","1F3C6":"trophy","1F3C5":"sports medal","1F947":"1st place medal","1F948":"2nd place medal","1F949":"3rd place medal"},sport:{"26BD":"soccer ball","26BE":"baseball","1F3C0":"basketball","1F3D0":"volleyball","1F3C8":"american football","1F3C9":"rugby football","1F3BE":"tennis","1F3B1":"pool 8 ball","1F3B3":"bowling","1F3CF":"cricket game","1F3D1":"field hockey","1F3D2":"ice hockey","1F3D3":"ping pong","1F3F8":"badminton","1F94A":"boxing glove","1F94B":"martial arts uniform","1F945":"goal net","1F3AF":"direct hit","26F3":"flag in hole","26F8 Z":"ice skate","26F8":"ice skate","1F3A3":"fishing pole","1F3BD":"running shirt","1F3BF":"skis","1F6F7":"sled","1F94C":"curling stone"},game:{"1F3AE":"video game","1F579 Z":"joystick","1F579":"joystick","1F3B2":"game die","2660 Z":"spade suit","2660":"spade suit","2665 Z":"heart suit","2665":"heart suit","2666 Z":"diamond suit","2666":"diamond suit","2663 Z":"club suit","2663":"club suit","1F0CF":"joker","1F004":"mahjong red dragon","1F3B4":"flower playing cards"}},Objects:{sound:{"1F507":"muted speaker","1F508":"speaker low volume","1F509":"speaker medium volume","1F50A":"speaker high volume","1F4E2":"loudspeaker","1F4E3":"megaphone","1F4EF":"postal horn","1F514":"bell","1F515":"bell with slash"},music:{"1F3BC":"musical score","1F3B5":"musical note","1F3B6":"musical notes","1F399 Z":"studio microphone","1F399":"studio microphone","1F39A Z":"level slider","1F39A":"level slider","1F39B Z":"control knobs","1F39B":"control knobs","1F3A4":"microphone","1F3A7":"headphone","1F4FB":"radio"},"musical-instrument":{"1F3B7":"saxophone","1F3B8":"guitar","1F3B9":"musical keyboard","1F3BA":"trumpet","1F3BB":"violin","1F941":"drum"},phone:{"1F4F1":"mobile phone","1F4F2":"mobile phone with arrow","260E Z":"telephone","260E":"telephone","1F4DE":"telephone receiver","1F4DF":"pager","1F4E0":"fax machine"},computer:{"1F50B":"battery","1F50C":"electric plug","1F4BB":"laptop computer","1F5A5 Z":"desktop computer","1F5A5":"desktop computer","1F5A8 Z":"printer","1F5A8":"printer","2328 Z":"keyboard","2328":"keyboard","1F5B1 Z":"computer mouse","1F5B1":"computer mouse","1F5B2 Z":"trackball","1F5B2":"trackball","1F4BD":"computer disk","1F4BE":"floppy disk","1F4BF":"optical disk","1F4C0":"dvd"},"light & video":{"1F3A5":"movie camera","1F39E Z":"film frames","1F39E":"film frames","1F4FD Z":"film projector","1F4FD":"film projector","1F3AC":"clapper board","1F4FA":"television","1F4F7":"camera","1F4F8":"camera with flash","1F4F9":"video camera","1F4FC":"videocassette","1F50D":"magnifying glass tilted left","1F50E":"magnifying glass tilted right","1F52C":"microscope","1F52D":"telescope","1F4E1":"satellite antenna","1F56F Z":"candle","1F56F":"candle","1F4A1":"light bulb","1F526":"flashlight","1F3EE":"red paper lantern"},"book-paper":{"1F4D4":"notebook with decorative cover","1F4D5":"closed book","1F4D6":"open book","1F4D7":"green book","1F4D8":"blue book","1F4D9":"orange book","1F4DA":"books","1F4D3":"notebook","1F4D2":"ledger","1F4C3":"page with curl","1F4DC":"scroll","1F4C4":"page facing up","1F4F0":"newspaper","1F5DE Z":"rolled-up newspaper","1F5DE":"rolled-up newspaper","1F4D1":"bookmark tabs","1F516":"bookmark","1F3F7 Z":"label","1F3F7":"label"},money:{"1F4B0":"money bag","1F4B4":"yen banknote","1F4B5":"dollar banknote","1F4B6":"euro banknote","1F4B7":"pound banknote","1F4B8":"money with wings","1F4B3":"credit card","1F4B9":"chart increasing with yen","1F4B1":"currency exchange","1F4B2":"heavy dollar sign"},mail:{"2709 Z":"envelope","2709":"envelope","1F4E7":"e-mail","1F4E8":"incoming envelope","1F4E9":"envelope with arrow","1F4E4":"outbox tray","1F4E5":"inbox tray","1F4E6":"package","1F4EB":"closed mailbox with raised flag","1F4EA":"closed mailbox with lowered flag","1F4EC":"open mailbox with raised flag","1F4ED":"open mailbox with lowered flag","1F4EE":"postbox","1F5F3 Z":"ballot box with ballot","1F5F3":"ballot box with ballot"},writing:{"270F Z":"pencil","270F":"pencil","2712 Z":"black nib","2712":"black nib","1F58B Z":"fountain pen","1F58B":"fountain pen","1F58A Z":"pen","1F58A":"pen","1F58C Z":"paintbrush","1F58C":"paintbrush","1F58D Z":"crayon","1F58D":"crayon","1F4DD":"memo"},office:{"1F4BC":"briefcase","1F4C1":"file folder","1F4C2":"open file folder","1F5C2 Z":"card index dividers","1F5C2":"card index dividers","1F4C5":"calendar","1F4C6":"tear-off calendar","1F5D2 Z":"spiral notepad","1F5D2":"spiral notepad","1F5D3 Z":"spiral calendar","1F5D3":"spiral calendar","1F4C7":"card index","1F4C8":"chart increasing","1F4C9":"chart decreasing","1F4CA":"bar chart","1F4CB":"clipboard","1F4CC":"pushpin","1F4CD":"round pushpin","1F4CE":"paperclip","1F587 Z":"linked paperclips","1F587":"linked paperclips","1F4CF":"straight ruler","1F4D0":"triangular ruler","2702 Z":"scissors","2702":"scissors","1F5C3 Z":"card file box","1F5C3":"card file box","1F5C4 Z":"file cabinet","1F5C4":"file cabinet","1F5D1 Z":"wastebasket","1F5D1":"wastebasket"},lock:{"1F512":"locked","1F513":"unlocked","1F50F":"locked with pen","1F510":"locked with key","1F511":"key","1F5DD Z":"old key","1F5DD":"old key"},tool:{"1F528":"hammer","26CF Z":"pick","26CF":"pick","2692 Z":"hammer and pick","2692":"hammer and pick","1F6E0 Z":"hammer and wrench","1F6E0":"hammer and wrench","1F5E1 Z":"dagger","1F5E1":"dagger","2694 Z":"crossed swords","2694":"crossed swords","1F52B":"pistol","1F3F9":"bow and arrow","1F6E1 Z":"shield","1F6E1":"shield","1F527":"wrench","1F529":"nut and bolt","2699 Z":"gear","2699":"gear","1F5DC Z":"clamp","1F5DC":"clamp","2697 Z":"alembic","2697":"alembic","2696 Z":"balance scale","2696":"balance scale","1F517":"link","26D3 Z":"chains","26D3":"chains"},medical:{"1F489":"syringe","1F48A":"pill"},"other-object":{"1F6AC":"cigarette","26B0 Z":"coffin","26B0":"coffin","26B1 Z":"funeral urn","26B1":"funeral urn","1F5FF":"moai","1F6E2 Z":"oil drum","1F6E2":"oil drum","1F52E":"crystal ball","1F6D2":"shopping cart"}},Symbols:{"transport-sign":{"1F3E7":"ATM sign","1F6AE":"litter in bin sign","1F6B0":"potable water","267F":"wheelchair symbol","1F6B9":"men’s room","1F6BA":"women’s room","1F6BB":"restroom","1F6BC":"baby symbol","1F6BE":"water closet","1F6C2":"passport control","1F6C3":"customs","1F6C4":"baggage claim","1F6C5":"left luggage"},warning:{"26A0 Z":"warning","26A0":"warning","1F6B8":"children crossing","26D4":"no entry","1F6AB":"prohibited","1F6B3":"no bicycles","1F6AD":"no smoking","1F6AF":"no littering","1F6B1":"non-potable water","1F6B7":"no pedestrians","1F4F5":"no mobile phones","1F51E":"no one under eighteen","2622 Z":"radioactive","2622":"radioactive","2623 Z":"biohazard","2623":"biohazard"},arrow:{"2B06 Z":"up arrow","2B06":"up arrow","2197 Z":"up-right arrow","2197":"up-right arrow","27A1 Z":"right arrow","27A1":"right arrow","2198 Z":"down-right arrow","2198":"down-right arrow","2B07 Z":"down arrow","2B07":"down arrow","2199 Z":"down-left arrow","2199":"down-left arrow","2B05 Z":"left arrow","2B05":"left arrow","2196 Z":"up-left arrow","2196":"up-left arrow","2195 Z":"up-down arrow","2195":"up-down arrow","2194 Z":"left-right arrow","2194":"left-right arrow","21A9 Z":"right arrow curving left","21A9":"right arrow curving left","21AA Z":"left arrow curving right","21AA":"left arrow curving right","2934 Z":"right arrow curving up","2934":"right arrow curving up","2935 Z":"right arrow curving down","2935":"right arrow curving down","1F503":"clockwise vertical arrows","1F504":"counterclockwise arrows button","1F519":"BACK arrow","1F51A":"END arrow","1F51B":"ON! arrow","1F51C":"SOON arrow","1F51D":"TOP arrow"},religion:{"1F6D0":"place of worship","269B Z":"atom symbol","269B":"atom symbol","1F549 Z":"om","1F549":"om","2721 Z":"star of David","2721":"star of David","2638 Z":"wheel of dharma","2638":"wheel of dharma","262F Z":"yin yang","262F":"yin yang","271D Z":"latin cross","271D":"latin cross","2626 Z":"orthodox cross","2626":"orthodox cross","262A Z":"star and crescent","262A":"star and crescent","262E Z":"peace symbol","262E":"peace symbol","1F54E":"menorah","1F52F":"dotted six-pointed star"},zodiac:{"2648":"Aries","2649":"Taurus","264A":"Gemini","264B":"Cancer","264C":"Leo","264D":"Virgo","264E":"Libra","264F":"Scorpius","2650":"Sagittarius","2651":"Capricorn","2652":"Aquarius","2653":"Pisces","26CE":"Ophiuchus"},"av-symbol":{"1F500":"shuffle tracks button","1F501":"repeat button","1F502":"repeat single button","25B6 Z":"play button","25B6":"play button","23E9":"fast-forward button","23ED Z":"next track button","23ED":"next track button","23EF Z":"play or pause button","23EF":"play or pause button","25C0 Z":"reverse button","25C0":"reverse button","23EA":"fast reverse button","23EE Z":"last track button","23EE":"last track button","1F53C":"up button","23EB":"fast up button","1F53D":"down button","23EC":"fast down button","23F8 Z":"pause button","23F8":"pause button","23F9 Z":"stop button","23F9":"stop button","23FA Z":"record button","23FA":"record button","23CF Z":"eject button","23CF":"eject button","1F3A6":"cinema","1F505":"dim button","1F506":"bright button","1F4F6":"antenna bars","1F4F3":"vibration mode","1F4F4":"mobile phone off"},"other-symbol":{"2640 Z":"female sign","2640":"female sign","2642 Z":"male sign","2642":"male sign","2695 Z":"medical symbol","2695":"medical symbol","267B Z":"recycling symbol","267B":"recycling symbol","269C Z":"fleur-de-lis","269C":"fleur-de-lis","1F531":"trident emblem","1F4DB":"name badge","1F530":"Japanese symbol for beginner","2B55":"heavy large circle","2705":"white heavy check mark","2611 Z":"ballot box with check","2611":"ballot box with check","2714 Z":"heavy check mark","2714":"heavy check mark","2716 Z":"heavy multiplication x","2716":"heavy multiplication x","274C":"cross mark","274E":"cross mark button","2795":"heavy plus sign","2796":"heavy minus sign","2797":"heavy division sign","27B0":"curly loop","27BF":"double curly loop","303D Z":"part alternation mark","303D":"part alternation mark","2733 Z":"eight-spoked asterisk","2733":"eight-spoked asterisk","2734 Z":"eight-pointed star","2734":"eight-pointed star","2747 Z":"sparkle","2747":"sparkle","203C Z":"double exclamation mark","203C":"double exclamation mark","2049 Z":"exclamation question mark","2049":"exclamation question mark","2753":"question mark","2754":"white question mark","2755":"white exclamation mark","2757":"exclamation mark","3030 Z":"wavy dash","3030":"wavy dash","00A9 Z":"copyright","00A9":"copyright","00AE Z":"registered","00AE":"registered","2122 Z":"trade mark","2122":"trade mark"},keycap:{"0023 Z 20E3":"keycap: #","0023 20E3":"keycap: #","002A Z 20E3":"keycap: *","002A 20E3":"keycap: *","0030 Z 20E3":"keycap: 0","0030 20E3":"keycap: 0","0031 Z 20E3":"keycap: 1","0031 20E3":"keycap: 1","0032 Z 20E3":"keycap: 2","0032 20E3":"keycap: 2","0033 Z 20E3":"keycap: 3","0033 20E3":"keycap: 3","0034 Z 20E3":"keycap: 4","0034 20E3":"keycap: 4","0035 Z 20E3":"keycap: 5","0035 20E3":"keycap: 5","0036 Z 20E3":"keycap: 6","0036 20E3":"keycap: 6","0037 Z 20E3":"keycap: 7","0037 20E3":"keycap: 7","0038 Z 20E3":"keycap: 8","0038 20E3":"keycap: 8","0039 Z 20E3":"keycap: 9","0039 20E3":"keycap: 9","1F51F":"keycap 10"},alphanum:{"1F4AF":"hundred points","1F520":"input latin uppercase","1F521":"input latin lowercase","1F522":"input numbers","1F523":"input symbols","1F524":"input latin letters","1F170 Z":"A button (blood type)","1F170":"A button (blood type)","1F18E":"AB button (blood type)","1F171 Z":"B button (blood type)","1F171":"B button (blood type)","1F191":"CL button","1F192":"COOL button","1F193":"FREE button","2139 Z":"information","2139":"information","1F194":"ID button","24C2 Z":"circled M","24C2":"circled M","1F195":"NEW button","1F196":"NG button","1F17E Z":"O button (blood type)","1F17E":"O button (blood type)","1F197":"OK button","1F17F Z":"P button","1F17F":"P button","1F198":"SOS button","1F199":"UP! button","1F19A":"VS button","1F201":"Japanese \"here\" button","1F202 Z":"Japanese \"service charge\" button","1F202":"Japanese \"service charge\" button","1F237 Z":"Japanese \"monthly amount\" button","1F237":"Japanese \"monthly amount\" button","1F236":"Japanese \"not free of charge\" button","1F22F":"Japanese \"reserved\" button","1F250":"Japanese \"bargain\" button","1F239":"Japanese \"discount\" button","1F21A":"Japanese \"free of charge\" button","1F232":"Japanese \"prohibited\" button","1F251":"Japanese \"acceptable\" button","1F238":"Japanese \"application\" button","1F234":"Japanese \"passing grade\" button","1F233":"Japanese \"vacancy\" button","3297 Z":"Japanese \"congratulations\" button","3297":"Japanese \"congratulations\" button","3299 Z":"Japanese \"secret\" button","3299":"Japanese \"secret\" button","1F23A":"Japanese \"open for business\" button","1F235":"Japanese \"no vacancy\" button"},geometric:{"25AA Z":"black small square","25AA":"black small square","25AB Z":"white small square","25AB":"white small square","25FB Z":"white medium square","25FB":"white medium square","25FC Z":"black medium square","25FC":"black medium square","25FD":"white medium-small square","25FE":"black medium-small square","2B1B":"black large square","2B1C":"white large square","1F536":"large orange diamond","1F537":"large blue diamond","1F538":"small orange diamond","1F539":"small blue diamond","1F53A":"red triangle pointed up","1F53B":"red triangle pointed down","1F4A0":"diamond with a dot","1F518":"radio button","1F532":"black square button","1F533":"white square button","26AA":"white circle","26AB":"black circle","1F534":"red circle","1F535":"blue circle"}},Flags:{flag:{"1F3C1":"chequered flag","1F6A9":"triangular flag","1F38C":"crossed flags","1F3F4":"black flag","1F3F3 Z":"white flag","1F3F3":"white flag","1F3F3 Z & 1F308":"rainbow flag","1F3F3 & 1F308":"rainbow flag"},"country-flag":{"1F1E6 1F1E8":"Ascension Island","1F1E6 1F1E9":"Andorra","1F1E6 1F1EA":"United Arab Emirates","1F1E6 1F1EB":"Afghanistan","1F1E6 1F1EC":"Antigua & Barbuda","1F1E6 1F1EE":"Anguilla","1F1E6 1F1F1":"Albania","1F1E6 1F1F2":"Armenia","1F1E6 1F1F4":"Angola","1F1E6 1F1F6":"Antarctica","1F1E6 1F1F7":"Argentina","1F1E6 1F1F8":"American Samoa","1F1E6 1F1F9":"Austria","1F1E6 1F1FA":"Australia","1F1E6 1F1FC":"Aruba","1F1E6 1F1FD":"Åland Islands","1F1E6 1F1FF":"Azerbaijan","1F1E7 1F1E6":"Bosnia & Herzegovina","1F1E7 1F1E7":"Barbados","1F1E7 1F1E9":"Bangladesh","1F1E7 1F1EA":"Belgium","1F1E7 1F1EB":"Burkina Faso","1F1E7 1F1EC":"Bulgaria","1F1E7 1F1ED":"Bahrain","1F1E7 1F1EE":"Burundi","1F1E7 1F1EF":"Benin","1F1E7 1F1F1":"St. Barthélemy","1F1E7 1F1F2":"Bermuda","1F1E7 1F1F3":"Brunei","1F1E7 1F1F4":"Bolivia","1F1E7 1F1F6":"Caribbean Netherlands","1F1E7 1F1F7":"Brazil","1F1E7 1F1F8":"Bahamas","1F1E7 1F1F9":"Bhutan","1F1E7 1F1FB":"Bouvet Island","1F1E7 1F1FC":"Botswana","1F1E7 1F1FE":"Belarus","1F1E7 1F1FF":"Belize","1F1E8 1F1E6":"Canada","1F1E8 1F1E8":"Cocos (Keeling) Islands","1F1E8 1F1E9":"Congo - Kinshasa","1F1E8 1F1EB":"Central African Republic","1F1E8 1F1EC":"Congo - Brazzaville","1F1E8 1F1ED":"Switzerland","1F1E8 1F1EE":"Côte d’Ivoire","1F1E8 1F1F0":"Cook Islands","1F1E8 1F1F1":"Chile","1F1E8 1F1F2":"Cameroon","1F1E8 1F1F3":"China","1F1E8 1F1F4":"Colombia","1F1E8 1F1F5":"Clipperton Island","1F1E8 1F1F7":"Costa Rica","1F1E8 1F1FA":"Cuba","1F1E8 1F1FB":"Cape Verde","1F1E8 1F1FC":"Curaçao","1F1E8 1F1FD":"Christmas Island","1F1E8 1F1FE":"Cyprus","1F1E8 1F1FF":"Czechia","1F1E9 1F1EA":"GerYy","1F1E9 1F1EC":"Diego Garcia","1F1E9 1F1EF":"Djibouti","1F1E9 1F1F0":"Denmark","1F1E9 1F1F2":"Dominica","1F1E9 1F1F4":"Dominican Republic","1F1E9 1F1FF":"Algeria","1F1EA 1F1E6":"Ceuta & Melilla","1F1EA 1F1E8":"Ecuador","1F1EA 1F1EA":"Estonia","1F1EA 1F1EC":"Egypt","1F1EA 1F1ED":"Western Sahara","1F1EA 1F1F7":"Eritrea","1F1EA 1F1F8":"Spain","1F1EA 1F1F9":"Ethiopia","1F1EA 1F1FA":"European Union","1F1EB 1F1EE":"Finland","1F1EB 1F1EF":"Fiji","1F1EB 1F1F0":"Falkland Islands","1F1EB 1F1F2":"Micronesia","1F1EB 1F1F4":"Faroe Islands","1F1EB 1F1F7":"France","1F1EC 1F1E6":"Gabon","1F1EC 1F1E7":"United Kingdom","1F1EC 1F1E9":"Grenada","1F1EC 1F1EA":"Georgia","1F1EC 1F1EB":"French Guiana","1F1EC 1F1EC":"Guernsey","1F1EC 1F1ED":"Ghana","1F1EC 1F1EE":"Gibraltar","1F1EC 1F1F1":"Greenland","1F1EC 1F1F2":"Gambia","1F1EC 1F1F3":"Guinea","1F1EC 1F1F5":"Guadeloupe","1F1EC 1F1F6":"Equatorial Guinea","1F1EC 1F1F7":"Greece","1F1EC 1F1F8":"South Georgia & South Sandwich Islands","1F1EC 1F1F9":"Guatemala","1F1EC 1F1FA":"Guam","1F1EC 1F1FC":"Guinea-Bissau","1F1EC 1F1FE":"Guyana","1F1ED 1F1F0":"Hong Kong SAR China","1F1ED 1F1F2":"Heard & McDonald Islands","1F1ED 1F1F3":"Honduras","1F1ED 1F1F7":"Croatia","1F1ED 1F1F9":"Haiti","1F1ED 1F1FA":"Hungary","1F1EE 1F1E8":"Canary Islands","1F1EE 1F1E9":"Indonesia","1F1EE 1F1EA":"Ireland","1F1EE 1F1F1":"Israel","1F1EE 1F1F2":"Isle of Man","1F1EE 1F1F3":"India","1F1EE 1F1F4":"British Indian Ocean Territory","1F1EE 1F1F6":"Iraq","1F1EE 1F1F7":"Iran","1F1EE 1F1F8":"Iceland","1F1EE 1F1F9":"Italy","1F1EF 1F1EA":"Jersey","1F1EF 1F1F2":"Jamaica","1F1EF 1F1F4":"Jordan","1F1EF 1F1F5":"Japan","1F1F0 1F1EA":"Kenya","1F1F0 1F1EC":"Kyrgyzstan","1F1F0 1F1ED":"Cambodia","1F1F0 1F1EE":"Kiribati","1F1F0 1F1F2":"Comoros","1F1F0 1F1F3":"St. Kitts & Nevis","1F1F0 1F1F5":"North Korea","1F1F0 1F1F7":"South Korea","1F1F0 1F1FC":"Kuwait","1F1F0 1F1FE":"CayY Islands","1F1F0 1F1FF":"Kazakhstan","1F1F1 1F1E6":"Laos","1F1F1 1F1E7":"Lebanon","1F1F1 1F1E8":"St. Lucia","1F1F1 1F1EE":"Liechtenstein","1F1F1 1F1F0":"Sri Lanka","1F1F1 1F1F7":"Liberia","1F1F1 1F1F8":"Lesotho","1F1F1 1F1F9":"Lithuania","1F1F1 1F1FA":"Luxembourg","1F1F1 1F1FB":"Latvia","1F1F1 1F1FE":"Libya","1F1F2 1F1E6":"Morocco","1F1F2 1F1E8":"Monaco","1F1F2 1F1E9":"Moldova","1F1F2 1F1EA":"Montenegro","1F1F2 1F1EB":"St. Martin","1F1F2 1F1EC":"Madagascar","1F1F2 1F1ED":"Marshall Islands","1F1F2 1F1F0":"Macedonia","1F1F2 1F1F1":"Mali","1F1F2 1F1F2":"Myanmar (Burma)","1F1F2 1F1F3":"Mongolia","1F1F2 1F1F4":"Macau SAR China","1F1F2 1F1F5":"Northern Mariana Islands","1F1F2 1F1F6":"Martinique","1F1F2 1F1F7":"Mauritania","1F1F2 1F1F8":"Montserrat","1F1F2 1F1F9":"Malta","1F1F2 1F1FA":"Mauritius","1F1F2 1F1FB":"Maldives","1F1F2 1F1FC":"Malawi","1F1F2 1F1FD":"Mexico","1F1F2 1F1FE":"Malaysia","1F1F2 1F1FF":"Mozambique","1F1F3 1F1E6":"Namibia","1F1F3 1F1E8":"New Caledonia","1F1F3 1F1EA":"Niger","1F1F3 1F1EB":"Norfolk Island","1F1F3 1F1EC":"Nigeria","1F1F3 1F1EE":"Nicaragua","1F1F3 1F1F1":"Netherlands","1F1F3 1F1F4":"Norway","1F1F3 1F1F5":"Nepal","1F1F3 1F1F7":"Nauru","1F1F3 1F1FA":"Niue","1F1F3 1F1FF":"New Zealand","1F1F4 1F1F2":"OY","1F1F5 1F1E6":"Panama","1F1F5 1F1EA":"Peru","1F1F5 1F1EB":"French Polynesia","1F1F5 1F1EC":"Papua New Guinea","1F1F5 1F1ED":"Philippines","1F1F5 1F1F0":"Pakistan","1F1F5 1F1F1":"Poland","1F1F5 1F1F2":"St. Pierre & Miquelon","1F1F5 1F1F3":"Pitcairn Islands","1F1F5 1F1F7":"Puerto Rico","1F1F5 1F1F8":"Palestinian Territories","1F1F5 1F1F9":"Portugal","1F1F5 1F1FC":"Palau","1F1F5 1F1FE":"Paraguay","1F1F6 1F1E6":"Qatar","1F1F7 1F1EA":"Réunion","1F1F7 1F1F4":"RoYia","1F1F7 1F1F8":"Serbia","1F1F7 1F1FA":"Russia","1F1F7 1F1FC":"Rwanda","1F1F8 1F1E6":"Saudi Arabia","1F1F8 1F1E7":"Solomon Islands","1F1F8 1F1E8":"Seychelles","1F1F8 1F1E9":"Sudan","1F1F8 1F1EA":"Sweden","1F1F8 1F1EC":"Singapore","1F1F8 1F1ED":"St. Helena","1F1F8 1F1EE":"Slovenia","1F1F8 1F1EF":"Svalbard & Jan Mayen","1F1F8 1F1F0":"Slovakia","1F1F8 1F1F1":"Sierra Leone","1F1F8 1F1F2":"San Marino","1F1F8 1F1F3":"Senegal","1F1F8 1F1F4":"Somalia","1F1F8 1F1F7":"Suriname","1F1F8 1F1F8":"South Sudan","1F1F8 1F1F9":"São Tomé & Príncipe","1F1F8 1F1FB":"El Salvador","1F1F8 1F1FD":"Sint Maarten","1F1F8 1F1FE":"Syria","1F1F8 1F1FF":"Swaziland","1F1F9 1F1E6":"Tristan da Cunha","1F1F9 1F1E8":"Turks & Caicos Islands","1F1F9 1F1E9":"Chad","1F1F9 1F1EB":"French Southern Territories","1F1F9 1F1EC":"Togo","1F1F9 1F1ED":"Thailand","1F1F9 1F1EF":"Tajikistan","1F1F9 1F1F0":"Tokelau","1F1F9 1F1F1":"Timor-Leste","1F1F9 1F1F2":"Turkmenistan","1F1F9 1F1F3":"Tunisia","1F1F9 1F1F4":"Tonga","1F1F9 1F1F7":"Turkey","1F1F9 1F1F9":"Trinidad & Tobago","1F1F9 1F1FB":"Tuvalu","1F1F9 1F1FC":"Taiwan","1F1F9 1F1FF":"Tanzania","1F1FA 1F1E6":"Ukraine","1F1FA 1F1EC":"Uganda","1F1FA 1F1F2":"U.S. Outlying Islands","1F1FA 1F1F3":"United Nations","1F1FA 1F1F8":"United States","1F1FA 1F1FE":"Uruguay","1F1FA 1F1FF":"Uzbekistan","1F1FB 1F1E6":"Vatican City","1F1FB 1F1E8":"St. Vincent & Grenadines","1F1FB 1F1EA":"Venezuela","1F1FB 1F1EC":"British Virgin Islands","1F1FB 1F1EE":"U.S. Virgin Islands","1F1FB 1F1F3":"Vietnam","1F1FB 1F1FA":"Vanuatu","1F1FC 1F1EB":"Wallis & Futuna","1F1FC 1F1F8":"Samoa","1F1FD 1F1F0":"Kosovo","1F1FE 1F1EA":"Yemen","1F1FE 1F1F9":"Mayotte","1F1FF 1F1E6":"South Africa","1F1FF 1F1F2":"Zambia","1F1FF 1F1FC":"Zimbabwe"},"subdivision-flag":{"1F3F4 E0067 E0062 E0065 E006E E0067 E007F":"England","1F3F4 E0067 E0062 E0073 E0063 E0074 E007F":"Scotland","1F3F4 E0067 E0062 E0077 E006C E0073 E007F":"Wales"}}};
this.EMOJIES = EMOJIES;































































/**
 * Shortened Replacements:
 * V = girl                   = 1F467
 * W = boy                    = 1F466
 * X = woman                  = 1F469
 * Y = man                    = 1F468
 * Z = fully qualified        = FE0F (ignore others with equal name!)
 * L = light skin tone        = 1F3FB
 * M = medium-light skin tone = 1F3FC
 * N = medium skin tone       = 1F3FD
 * O = medium-dark skin tone  = 1F3FE
 * P = dark skin tone         = 1F3FF
 * & = combiner               = 200D
 */
	function EmojiCategory(name, subs) {
		this.name = name;
		this.type = arguments.callee.name;
		let unlisted = ['length', 'name', 'type'];
        Object.defineProperty(this, 'length', {
            get: function() {
              return Object.keys(this).filter(function(v) { return !~unlisted.indexOf(v); }).length;
            }
        });
		for (let sub in subs) {
			if (subs.hasOwnProperty(sub)) {
				this[sub] = new EmojiSubCategory(sub, subs[sub]);
			}
		}
		for (let i in this) {
			this.hasOwnProperty(i) && Object.defineProperty(this, i, {
				configurable:!1,
				writable:!1,
				enumerable:!~unlisted.indexOf(i),
				value: this[i]
			});
		}  
	};
    EmojiCategory.prototype.getSubCategorieNames = function() {
        return Object.keys(this);
    }
    EmojiCategory.prototype.getSubCategories = function() {
    	return this.getSubCategorieNames().map(function(v){ return this[v]; }.bind(this));
    }
	EmojiCategory.prototype.toString = function() {
		return this.getSubCategories().toString();
	};
	function EmojiSubCategory(name, emojis) {
		this.name = name;
		this.type = arguments.callee.name;
		let unlisted = ['length', 'name', 'type'];
        Object.defineProperty(this, 'length', {
            get: function() {
              return Object.keys(this).filter(function(v) { return !~unlisted.indexOf(v); }).length;
            }
        });
		
		for (let emoji in emojis) {
			if (emojis.hasOwnProperty(emoji)) {
            	let code = emoji, name = emojis[emoji];
				for (let n in EMOJIES['_parser']) {
					if (EMOJIES['_parser'].hasOwnProperty(n)) {
						code = code.replace(new RegExp(n, 'g'), EMOJIES['_parser'][n][0]);
						name = name.replace(new RegExp(n, 'g'), EMOJIES['_parser'][n][1]);
					}
				}
				this[code] = new Emoji(code, name);
			}
		}
		for (let i in this) {
			this.hasOwnProperty(i) && Object.defineProperty(this, i, {
				configurable:!1,
				writable:!1,
				enumerable:!~unlisted.indexOf(i),
				value: this[i]
			});
		}        
	};
    EmojiSubCategory.prototype.getEmojiNames = function() {
        return Object.keys(this);
    }
    EmojiSubCategory.prototype.getEmojies = function() {
    	return this.getEmojiNames().map(function(v){ return this[v]; }.bind(this));
    }
	EmojiSubCategory.prototype.toString = function() {
		return this.getEmojies().toString();
	};
	function Emoji(code, name) {
		this.name = name;
		this.code = code;
		this.skintone = !!~this.name.indexOf('skin tone');
		this.skintonetype = -1;
		let l = ['1F3FB','1F3FC','1F3FD','1F3FE','1F3FF'];
		for (let i in l) {
			if (l.hasOwnProperty(i)) {
				if (~this.code.indexOf(l[i])) {
					this.skintonetype = l.indexOf(l[i]);
				}
			}
		}
        this.combined = !!~this.code.indexOf('200D');
		this.unicode = String.fromCodePoint.apply(String, this.code.split(' ').map(function(v) { return parseInt(v,16); }));
		this.male = false;
		l = ['men','man','boy'];
		for (let i in l) {
			if (l.hasOwnProperty(i) && ~this.name.indexOf(l[i])) {
				this.male = true;
			}
		}
		this.female = false;
		l = ['female','woman','women','girl'];
		for (let i in l) {
			if (l.hasOwnProperty(i) && ~this.name.indexOf(l[i])) {
				this.female = true;
			}
		}
		this.neutral = !this.male && !this.female;
		for (let i in this) {
			this.hasOwnProperty(i) && Object.defineProperty(this, i, {
				configurable:!1,
				writable:!1,
				enumerable:!0,
				value: this[i]
			});
		}
	};
	Emoji.prototype.toString = function() { return this.unicode; };
	function Emojilist() {
		let unlisted = ['length', 'name', 'type'];
        Object.defineProperty(this, 'length', {
            get: function() {
              return Object.keys(this).filter(function(v) { return !~unlisted.indexOf(v); }).length;
            }
        });
		if (EMOJIES && this instanceof arguments.callee) {
			for (let cat in EMOJIES) {
				if (EMOJIES.hasOwnProperty(cat) && EMOJIES[cat] != EMOJIES['_parser']) {
	                this[cat] = new EmojiCategory(cat, EMOJIES[cat]);
					
				}
			}
		}
		for (let i in this) {
			this.hasOwnProperty(i) && Object.defineProperty(this, i, {
				configurable:!1,
				writable:!1,
				enumerable:!~unlisted.indexOf(i),
				value: this[i]
			});
		}
	}
    Emojilist.prototype.getCategoryNames = function() {
    	return Object.keys(this);
    };
    Emojilist.prototype.getCategories = function() {
    	return this.getCategoryNames().map(function(v){ return this[v]; }.bind(this));
    };
	Emojilist.prototype.toString = function() {
		return this.getCategories().toString();
	};
    Emojilist.prototype.loadRecent = function(object) {
    
    };
    Emojilist.prototype.storeRecent = function() {
    	
    };
	!function() {
		let first; 
		function randomId(str) {
			return (str || '')+Math.floor(Date.now() * Math.random());
		}
		function hideEmojiToggler() {
			$(this).find('.emoji-list').find('.emoji').each(function() {
				let checks = [
					this.getAttribute('emoji-skintone-hidden'),
					this.getAttribute('emoji-family-type-hidden'),
					this.getAttribute('emoji-gender-type-hidden'),
				];
				this.style.display = checks.filter(function(i) { return i == 'true'; }).length ? 'none' : '';
			});
		};
		function switcherFunction(code) {
			$(this).find('.emoji-list').find('[emoji-skintone]').each(function() {
				this.setAttribute('emoji-skintone-hidden', this.getAttribute('emoji-skintone') != code);
			});
			hideEmojiToggler.call(this);
		};
		function typeSwitcherFunction(code) {
			$(this).find('.emoji-list').find('[emoji-family-type]').each(function() {
				if (code == '👨‍👩‍👧‍👦') {
					this.setAttribute('emoji-family-type-hidden', 'false');
				} else {
					this.setAttribute('emoji-family-type-hidden', !~this.getAttribute('emoji-code').split(' ').indexOf(code.codePointAt(0).toString(16).toUpperCase()));
				}
			});
			hideEmojiToggler.call(this);
		};
		function genderSwitcherFunction(code) {
			$(this).find('.emoji-list').find('[emoji-gender-type]').each(function() {
				if (code == '❎') { 
					this.setAttribute('emoji-gender-type-hidden', 'false');
				}
				else {
					this.setAttribute('emoji-gender-type-hidden', !~this.getAttribute('emoji-code').split(' ').indexOf(code.codePointAt(0).toString(16).toUpperCase()));
				}
			});
			hideEmojiToggler.call(this);
		};
		Emojilist.prototype.attach = function(conf) {
			let start = Date.now();
			let wrapper = document.createElement('div'),template = [
			  '<input id="emoji-search" class="form-control form-control-sm hidden">',
			  '<ul id="emoji-menu" class="nav nav-tabs" role="tablist"></ul>',
			  '<div id="emoji-list" class="tab-content bg-light emoji-list"></div>'].join('');
			//wrapper.setAttribute('style', 'position: absolute;');
			wrapper.className = 'emoji-wrapper';
			wrapper.innerHTML = template;
			let search = wrapper.querySelector('#emoji-search'),
				menu = wrapper.querySelector('#emoji-menu'),
				list = wrapper.querySelector('#emoji-list');
			[search,menu,list].forEach(function(e) { e.removeAttribute('id'); });
			if (first) {
				wrapper = Emojilist.getCloneWrapper();
			}
			/*
			for (let k in conf) {
				if (conf.hasOwnProperty(k)) {
					if (!~['node', 'insert', 'innerHTML', 'placement', 'pageTitle', 'container', 'isMobile', 'template', 'offset', 'prepend', 'categoryclick'].indexOf(k)) {
						wrapper.setAttribute(k, conf[k]);
					}
				}
			}
			*/
			let node = conf.node;
			let clickaction = function(){};
			if (conf.hasOwnProperty('insert')) {
				if (typeof conf.insert == 'function') {
					clickaction = conf.insert;
				}
			}
			if (first) {
				$(wrapper).find('.nav-item > a').each(function() {
					this.onclick = conf.categoryclick;
				});
				$(wrapper).find('.emoji-type-picker').each(function() {
					$(this).find('[type=radio]').each(function() {
						this.onchange = function() {
							typeSwitcherFunction.call(wrapper, this.value);
						}
					});
				});
				$(wrapper).find('.emoji-gender-picker').each(function() {
					$(this).find('[type=radio]').each(function() {
						this.onchange = function() {
							genderSwitcherFunction.call(wrapper, this.value);
						}
					});
				});
				$(wrapper).find('.emoji-style-picker').each(function() {
					$(this).find('[type=radio]').each(function() {
						this.onchange = function() {
							switcherFunction.call(wrapper, this.value);
						}
					});
				});
				$(wrapper).find('.emoji-list-group > button').each(function() {
					this.onclick = function(ev) {
						clickaction.apply(node, [this.innerHTML, ev]);
					};
				});
			} else {
				function addNav(id, title) {
					if (typeof conf.pageTitle == 'function') {
						title = conf.pageTitle(title);
					}
					let li = document.createElement('li');
					li.setAttribute('class', 'nav-item');
					li.appendChild(document.createElement('a'));
					li.firstChild.setAttribute('class', menu.childNodes.length ? 'nav-link' : 'nav-link active');
					li.firstChild.setAttribute('id', randomId(id+'-'));
					li.firstChild.setAttribute('data-toggle', 'tab');
					li.firstChild.setAttribute('href', '#'+randomId('emoji-tab-'));
					li.firstChild.setAttribute('role', 'tab');
					li.firstChild.setAttribute('aria-controls', li.firstChild.getAttribute('href').substr(1));
					li.firstChild.setAttribute('aria-selected', !menu.childNodes.length);
					li.firstChild.innerHTML = title;
					if (typeof conf.categoryclick == 'function') {
						let click = conf.categoryclick;
						li.firstChild.onclick = click;
					}
					return menu.appendChild(li);
				};
				function addPage(id, controledbyId) {
					let div = document.createElement('div');
					div.className = wrapper.querySelectorAll('[role=tablist] > li').length > 1 ? 'tab-pane fade' : 'tab-pane fade active show';
					div.setAttribute('id', id);
					div.setAttribute('role', 'tabpanel');
					div.setAttribute('aria-labelledby', controledbyId);
					return list.appendChild(div);
				}
				/*
				let recent = addNav('recent', 'Recent'),
					recentId = recent.firstChild.id,
					recentPageId = recent.firstChild.getAttribute('aria-controls'),
					recentPage = addPage(recentPageId, recentId);
				*/
				let node = conf.node;
				this.getCategories().forEach(function(cat,catIndex) {
					let nav = addNav(catIndex, cat.name), page = addPage(nav.firstChild.getAttribute('aria-controls'), nav.firstChild.id),
						skinswitcher = !1,typeSwitcher = !1,genderSwitcher = !1;
					cat.getSubCategories().forEach(function(sub) {
						page.appendChild(document.createElement('div'));
						page.lastChild.setAttribute('class', 'bg-light emoji-list-group');
						let list = page.lastChild;
						let dontAdd = [],categoryHasSkintones = !1,categoryHasFamilyType = !1,categoryHasGenders = !1;
						sub.getEmojies().forEach(function(emoji) {
							if (~emoji.code.split(' ').indexOf('FE0F')) {
								dontAdd.push(emoji.name);
							}
							if (!skinswitcher && !categoryHasSkintones) {
								['1F3FB','1F3FC','1F3FD','1F3FE','1F3FF'].forEach(function(l) {
									if (~emoji.code.split(' ').indexOf(l)) {
										categoryHasSkintones = !0;
									}
								});
							}
							if (!typeSwitcher && !categoryHasFamilyType) {
								['1F466', '1F467', '1F468', '1F469'].forEach(function(l) {
									if (~emoji.code.split(' ').indexOf(l)) {
										//console.log('Family found at '+emoji.code+' '+emoji.name);
										categoryHasFamilyType = !0;
									}
								});
							}
							if (!genderSwitcher && !categoryHasGenders) {
								['2640', '2642'].forEach(function(l) {
									if (~emoji.code.split(' ').indexOf(l) && emoji.code != l && emoji.code != l+' FE0F') {
										//console.log('Gender found at '+emoji.code+' '+emoji.name);
										categoryHasGenders = !0;
									}
								});
							}
						});
						if (categoryHasFamilyType && !typeSwitcher) {
							typeSwitcher = !0;
							let switcher = document.createElement('div');
							switcher.setAttribute('class', 'btn-group btn-group-toggle emoji-type-picker m-1');
							switcher.setAttribute('data-toggle', 'buttons');
							let radioname = randomId('radio-');
							['👨‍👩‍👧‍👦', '👦','👧','👨','👩'].forEach(function(type,i) {
								let e = document.createElement('label');
								e.appendChild(document.createElement('input'));
								e.firstChild.setAttribute('type', 'radio');
								e.firstChild.setAttribute('name', radioname);
								e.firstChild.setAttribute('id', randomId(radioname));
								e.firstChild.setAttribute('value', type);
								e.firstChild.setAttribute('autocomplete', 'off');
								if (!i) {
									e.setAttribute('class', 'btn btn-light btn-sm border-secondary active');
									e.firstChild.setAttribute('checked','');
								} else {
									e.setAttribute('class', 'btn btn-light btn-sm border-secondary');
								}
								e.firstChild.onchange = function() {
									typeSwitcherFunction.call(wrapper, this.value);
								};
								e.appendChild(document.createTextNode(type));
								switcher.appendChild(e);
							});
							if (!~page.firstChild.className.split(' ').indexOf('emoji-filters')) {
								page.insertBefore(document.createElement('div'), page.firstChild);
								page.firstChild.className = 'emoji-filters';
							}
							page.firstChild.appendChild(switcher);
						}
						if (categoryHasGenders && !genderSwitcher) {
							genderSwitcher = !0;
							let switcher = document.createElement('div');
							switcher.setAttribute('class', 'btn-group btn-group-toggle emoji-gender-picker m-1');
							switcher.setAttribute('data-toggle', 'buttons');
							let radioname = randomId('radio-');
							['❎','♂','♀'].forEach(function(type,i) {
								let e = document.createElement('label');
								e.appendChild(document.createElement('input'));
								e.firstChild.setAttribute('type', 'radio');
								e.firstChild.setAttribute('name', radioname);
								e.firstChild.setAttribute('id', randomId(radioname));
								e.firstChild.setAttribute('value', type);
								e.firstChild.setAttribute('autocomplete', 'off');
								if (!i) {
									e.setAttribute('class', 'btn btn-light btn-sm border-secondary active');
									e.firstChild.setAttribute('checked','');
								} else {
									e.setAttribute('class', 'btn btn-light btn-sm border-secondary');
								}
								e.firstChild.onchange = function() {
									genderSwitcherFunction.call(wrapper, this.value);
								};
								e.appendChild(document.createTextNode(type));
								switcher.appendChild(e);
							});
							if (!~page.firstChild.className.split(' ').indexOf('emoji-filters')) {
								page.insertBefore(document.createElement('div'), page.firstChild);
								page.firstChild.className = 'emoji-filters';
							}
							page.firstChild.appendChild(switcher);
						}
						if (categoryHasSkintones && !skinswitcher) {
							skinswitcher = !0;
							let switcher = document.createElement('div');
							switcher.setAttribute('class', 'btn-group btn-group-toggle emoji-style-picker m-1');
							switcher.setAttribute('data-toggle', 'buttons');
							let radioname = randomId('radio-');
							[-1,'🏻', '🏼', '🏽', '🏾', '🏿'].forEach(function(type,i) {
								let e = document.createElement('label');
								e.appendChild(document.createElement('input'));
								e.firstChild.setAttribute('type', 'radio');
								e.firstChild.setAttribute('name', radioname);
								e.firstChild.setAttribute('id', randomId(radioname));
								e.firstChild.setAttribute('value', type);
								e.firstChild.setAttribute('autocomplete', 'off');
								if (!i) {
									e.setAttribute('class', 'btn btn-light btn-sm border-secondary active');
									e.firstChild.setAttribute('checked','');
								} else {
									e.setAttribute('class', 'btn btn-light btn-sm border-secondary');
								}
								e.firstChild.onchange = function() {
									switcherFunction.call(wrapper, this.value);
								};
								e.appendChild(document.createTextNode(type == -1 ? '❎' : type));
								switcher.appendChild(e);
							});
							if (!~page.firstChild.className.split(' ').indexOf('emoji-filters')) {
								page.insertBefore(document.createElement('div'), page.firstChild);
								page.firstChild.className = 'emoji-filters';
							}
							page.firstChild.appendChild(switcher);
						}
						sub.getEmojies().forEach(function(emoji) {
							let splitted = emoji.code.split(' '),
								inKeyPadRange = !!~splitted.indexOf('20E3'),
								hasFE0F = !!~splitted.indexOf('FE0F');
								hasCleanLastFE0F = hasFE0F && emoji.code.split('FE0F')[1] == '';
							if (!~dontAdd.indexOf(emoji.name) || hasFE0F && hasCleanLastFE0F && !inKeyPadRange || inKeyPadRange && !hasFE0F) {
								list.appendChild(document.createElement('button'));
								let b = list.lastChild;
								b.innerHTML = emoji.unicode;
								b.setAttribute('title', emoji.name + ' (' + emoji.code + ')');
								b.setAttribute('alt', emoji.name + ' (' + emoji.code + ')');
								b.setAttribute('emoji-code', emoji.code);
								if (emoji.skintonetype > -1) {
									['1F3FB','1F3FC','1F3FD','1F3FE','1F3FF'].forEach(function(l) {
										if (~emoji.code.indexOf(l)) {
											$(list).find('[emoji-code="'+emoji.code.replace(' '+l,'')+'"]').each(function() {
												this.setAttribute('emoji-skintone', '-1');
											});
											$(list).find('[emoji-code="'+emoji.code.replace(' '+l,'')+' FE0F"]').each(function() {
												this.setAttribute('emoji-skintone', '-1');
											});
										}
									});
									b.setAttribute('emoji-skintone', ['🏻', '🏼', '🏽', '🏾', '🏿'][emoji.skintonetype]);
								}
								['1F466','1F467','1F468', '1F469'].forEach(function(l) {
									if(~emoji.code.indexOf(l)) {
										b.setAttribute('emoji-family-type','');
									}
								});
								['2640', '2642'].forEach(function(l) {
									if(~emoji.code.indexOf(l) && emoji.code != l && emoji.code != l+' FE0F') {
										b.setAttribute('emoji-gender-type','');
									}
								});
								b.className = 'btn btn-light btn-sm emoji';
								b.onclick = function(ev) {
									clickaction.apply(node, [this.innerHTML, ev]);
								};
							}
						});
					});
					if (skinswitcher) {
						switcherFunction.call(wrapper, '-1');
					}
					if (typeSwitcher) {
						typeSwitcherFunction.call(wrapper, '👨‍👩‍👧‍👦');
					}
					if (typeSwitcher) {
						genderSwitcherFunction.call(wrapper, '❎');
					}
				});
			}
			let success = false;
			if (conf.node) {
				try {
					let node = conf.node;
					if (!~node.parentNode.className.indexOf('input-group')) {
						node.parentNode.className += ' input-group';
					}
					let p = document.createElement('p');
					if (conf.prepend) {
						p.innerHTML = '<div class="input-group-prepend"><button class="btn btn-secondary btn-sm" data-toggle="popover" aria-haspopup="true" aria-expanded="false">🙂</button></div>';
						p.firstChild.firstChild.onfocus = function() {
							node.focus();
						};
						$(p.firstChild.firstChild).popover({
							animate: false,
							content: wrapper,
							html: true,
							container: conf.container || 'body',
							trigger: 'click',
							boundary: 'viewport',
							placement: conf.placement || 'top',
							offset: conf.offset || 0,
							template: conf.template || '<div class="popover" style="width: 50vw!important; max-width: 50vw!important; min-width: 50vw; height: 50vh!important; max-height: 50vh!important; min-height: 50vh!important;" role="tooltip"><div class="arrow"></div><div class="popover-body" style="position: relative; width: 100%!important; max-width: 100%!important; min-width: 100%; height: 100%!important; max-height: 100%!important; min-height: 100%!important; overflow-y: scroll;"></div></div>'
						});
						node.parentNode.insertBefore(p.firstChild, node);
					} else {
						p.innerHTML = '<div class="input-group-append"><button class="btn btn-secondary btn-sm" data-toggle="popover" aria-haspopup="true" aria-expanded="false">🙂</button></div>';
						$(p.firstChild.firstChild).popover({
							animate: false,
							content: wrapper,
							html: true,
							container: conf.container || 'body',
							trigger: 'click',
							boundary: 'viewport',
							placement: conf.placement || 'top',
							offset: conf.offset || 0,
							template: conf.template || '<div class="popover" style="width: 50vw!important; max-width: 50vw!important; min-width: 50vw; height: 50vh!important; max-height: 50vh!important; min-height: 50vh!important;" role="tooltip"><div class="arrow"></div><div class="popover-body" style="position: relative; width: 100%!important; max-width: 100%!important; min-width: 100%; height: 100%!important; max-height: 100%!important; min-height: 100%!important; overflow-y: scroll;"></div></div>'
						});
						node.nextSibling.parentNode.insertBefore(p.firstChild, node.nextSibling);
					}
					if (!first) {
						first = wrapper;
					}
					success = true;
				} catch (e) { console.error(e); }
			}
			return { node: wrapper, attached: success, duration: Date.now()-start };
		};
		Object.defineProperty(Emojilist,'getCloneWrapper', {
			writable: !1,
			enumerable:!1,
			configurable:!1,
			value: function getCloneWrapper() {
				let ignore = [];
				let clone = first.cloneNode(true);
				$(clone).find('[id]').each(function() {
					if (!~ignore.indexOf(this)) {
						let id = this.id; splitted = id.split('-');
						if (this.getAttribute('aria-controls')) {
							let controlls = $(clone).find(this.getAttribute('href'));
							let controllsId = this.getAttribute('aria-controls');
							controllsId = controllsId.split('-');
							controllsId.pop();
							
							this.setAttribute('aria-controls', randomId(controllsId.join('-')+'-'));
							this.setAttribute('href', '#'+this.getAttribute('aria-controls'));
							splitted.pop();
							this.setAttribute('id', randomId(splitted.join('-')+'-'));
							let controller = this;
							controlls.each(function() {
								this.setAttribute('id', controller.getAttribute('aria-controls'));
								this.setAttribute('aria-labelledby', controller.getAttribute('id'));
								ignore.push(this);
							});
						} else {
							splitted.pop();
							this.setAttribute('id', randomId(splitted.join('-')+'-'));
						}
					}
				});
				return clone;
			}
		});
	}();
	
	Object.defineProperties(this,{
		'Emoji':{
			configurable:!1,
			writable:!1,
			enumerable:!0,
			value: Emoji			
		},
		'EmojiCategory':{
			configurable:!1,
			writable:!1,
			enumerable:!0,
			value: EmojiCategory
		},
		'EmojiSubCategory':{
			configurable:!1,
			writable:!1,
			enumerable:!0,
			value: EmojiSubCategory			
		},
		'Emojilist':{
			configurable:!1,
			writable:!1,
			enumerable:!0,
			value: Emojilist
		},
	});
}(window);
