#!/bin/bash
S="$(cd "$(dirname "$0")/.." && pwd)"
cd "$S/bot"
node -e "require('./rcon.js')('stop').then(console.log).catch(()=>{})" 2>/dev/null
for i in $(seq 1 20); do [ -f "$S/server/pid" ] && kill -0 "$(cat $S/server/pid)" 2>/dev/null || break; sleep 1; done
cp /home/user/DungeonCrawler/depth-crawler/target/DepthCrawler-1.1.0.jar /home/user/DungeonCrawler/hacknslash-weaponly/target/HACKnSLASH-Weaponly-0.1.0.jar "$S/server/plugins/"
cd "$S/server" && rm -rf dungeon_* ; if [ -z "$KEEP" ]; then rm -rf plugins/DepthCrawler/playerdata world/playerdata; fi
nohup java -Xmx1500M -jar paper.jar --nogui > server.log 2>&1 &
echo $! > pid
for i in $(seq 1 60); do grep -q "Done (" server.log && break; sleep 1; done
grep "Done (" server.log
