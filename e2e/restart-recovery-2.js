const { connect, sleep } = require('./lib');
(async () => { const bot = await connect('Tester'); await sleep(2500); const p = bot.entity.position; console.log('after restart pos', p.x.toFixed(1), p.y.toFixed(1), p.z.toFixed(1), 'health', bot.health, 'gm', bot.game.gameMode);
  bot.chat('/dungeon info'); await sleep(600); bot.quit(); await sleep(500); process.exit(0); })();
