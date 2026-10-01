const { connect, rcon, sleep, waitFor } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  await sleep(500);
  bot.chat('/dungeon npc blackmarket'); await sleep(900);
  const npc = Object.values(bot.entities).filter(e => e.name === 'villager').sort((a, b) => a.position.distanceTo(bot.entity.position) - b.position.distanceTo(bot.entity.position))[0];
  let win = null; bot.once('windowOpen', w => { win = w; });
  await bot.activateEntity(npc); await waitFor(() => win, 5000);
  const items = win ? win.slots.slice(0, 45).filter(Boolean) : [];
  console.log('stock after restart:', items.length, items.map(i => i.name).join(','));
  bot.quit(); await sleep(500); process.exit(0);
})();
