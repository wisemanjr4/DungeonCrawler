const { connect, sleep } = require('./lib');
(async () => { const bot = await connect('Tester'); bot.chat('/dungeon enter'); await sleep(5000); console.log('in dungeon y=', bot.entity.position.y); bot.quit(); await sleep(800); process.exit(0); })();
