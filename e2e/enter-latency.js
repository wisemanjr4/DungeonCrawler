const { connect, rcon, sleep } = require('./lib');
(async () => {
  const bot = await connect('Tester');
  for (let run = 1; run <= 2; run++) {
    const t0 = Date.now(); let max = 0, n = 0, done = false;
    bot.chat('/dungeon enter');
    const poll = (async () => { while (!done) { const s = Date.now(); await rcon('list'); const d = Date.now() - s; max = Math.max(max, d); n++; await sleep(50); } })();
    while (!(bot.entity.position.y > 0) && Date.now() - t0 < 30000) await sleep(50);
    done = true; await poll;
    console.log(`run ${run}: entered after ${Date.now() - t0} ms | rcon polls=${n} max latency=${max} ms | y=${bot.entity.position.y.toFixed(1)}`);
    await sleep(1000); bot.chat('/kill'); await sleep(3000);
  }
  bot.quit(); await sleep(500); process.exit(0);
})();
