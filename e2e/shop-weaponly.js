const mineflayer = require('mineflayer');
const rcon = require('./rcon');
const sleep = ms => new Promise(r => setTimeout(r, ms));
(async () => {
  const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'Tester', version: '1.19.4' });
  bot.on('message', m => console.log('[chat]', m.toString()));
  bot.on('kicked', r => console.log('kicked', r));
  await new Promise(r => bot.once('spawn', r));
  await rcon('op Tester'); await sleep(1500);
  bot.chat('/dungeon admin givemoney Tester 5000'); await sleep(700);
  // --- shop buy
  bot.chat('/shop'); await sleep(1000);
  const dbg = w => w ? (w.type + '/' + w.slots.length + ' id' + w.id) : 'none';
  console.log('win1', dbg(bot.currentWindow));
  await bot.simpleClick.leftMouse(2); await sleep(1200);
  console.log('win2', dbg(bot.currentWindow));
  await bot.simpleClick.leftMouse(0); await sleep(1200);
  console.log('win3', dbg(bot.currentWindow));
  if (bot.currentWindow) bot.closeWindow(bot.currentWindow); await sleep(300);
  bot.chat('/shop balance'); await sleep(700);
  console.log('inv after buy:', bot.inventory.items().map(i => i.name + 'x' + i.count).join(', '));
  // --- weaponly
  await rcon('give Tester iron_sword'); await sleep(800);
  const sword = bot.inventory.items().find(i => i.name === 'iron_sword');
  await bot.equip(sword, 'hand'); await sleep(500);
  for (const c of ['/weaponly', '/weaponly info', '/weaponly reforge', '/weaponly reforge precise', '/weaponly modify 重',
                   '/weaponly gem apply atk', '/weaponly info', '/weaponly awaken', '/weaponly synthesize', '/weaponly unmodify', '/weaponly info']) {
    console.log('>>', c); bot.chat(c); await sleep(900);
  }
  const held = bot.heldItem;
  console.log('held displayName:', held && held.displayName, '| customName:', held && held.customName);
  bot.chat('/dungeon info'); await sleep(500);
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
