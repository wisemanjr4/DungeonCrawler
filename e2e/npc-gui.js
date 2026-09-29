const mineflayer = require('mineflayer');
const rcon = require('./rcon');
const sleep = ms => new Promise(r => setTimeout(r, ms));
const strip = t => { try { const j = JSON.parse(t); const f = o => typeof o === 'string' ? o : ((o.text||'') + (o.extra||[]).map(f).join('')); return f(j); } catch { return t; } };
(async () => {
  const bot = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'Tester', version: '1.19.4' });
  bot.on('message', m => console.log('[chat]', m.toString()));
  let win = null;
  bot.on('windowOpen', w => { win = w; console.log('[GUI]', strip(w.title), 'slots', w.slots.length,
     '| items:', w.slots.filter(Boolean).map(i => (i.customName ? strip(JSON.stringify(i.customName)) : i.displayName) ).slice(0, 12).join(', ')); });
  bot.on('kicked', r => console.log('kicked', r));
  await new Promise(r => bot.once('spawn', r));
  await rcon('op Tester'); await sleep(1500);
  for (const c of ['/dungeon setup spawn','/dungeon npc dungeon','/dungeon npc shop','/dungeon npc safebox','/dungeon npc blacksmith','/dungeon npc relief','/dungeon npc blackmarket','/dungeon admin givemoney Tester 5000'])
    { bot.chat(c); await sleep(900); }
  const npcs = Object.values(bot.entities).filter(e => e.name === 'villager');
  console.log('villagers nearby:', npcs.length);
  for (const e of npcs) {
    const meta = e.metadata; 
    win = null;
    await bot.activateEntity(e); await sleep(1200);
    console.log('  npc at', e.position.toString(), '->', win ? 'GUI opened' : 'no GUI');
    if (bot.currentWindow) { bot.closeWindow(bot.currentWindow); await sleep(300); }
  }
  // /shop menu -> buy
  win = null; bot.chat('/shop'); await sleep(1200);
  if (win) { await bot.clickWindow(11, 0, 0).catch(e => console.log('click err', e.message)); await sleep(1000); }
  if (bot.currentWindow) { console.log('after click title:', strip(bot.currentWindow.title)); await bot.clickWindow(0, 0, 0).catch(()=>{}); await sleep(1000); bot.closeWindow(bot.currentWindow); }
  bot.chat('/shop balance'); await sleep(600);
  console.log('inv:', bot.inventory.items().map(i => i.name + 'x' + i.count).join(', '));
  bot.quit(); await sleep(500); process.exit(0);
})().catch(e => { console.log('ERR', e); process.exit(1); });
