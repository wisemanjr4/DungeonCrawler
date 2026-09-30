#!/bin/bash
# 保険の返還待ちを検証するため、プレイヤーデータの期限を過去にする（サーバー停止中に実行）。
# 使い方: node -e "require('./rcon.js')('stop')" → ./expire-insurance.sh → KEEP=1 ./restart.sh → node insurance-claim-after-expiry.js
SERVER_DIR="${SERVER_DIR:-../server}"
for f in "$SERVER_DIR"/plugins/DepthCrawler/playerdata/*.yml; do
  python3 - "$f" <<'PY'
import re, sys
p = sys.argv[1]; s = open(p).read()
s2 = re.sub(r'([0-9a-f]{8}-[0-9a-f-]{27}\|)(\d+)(\|)', lambda m: m.group(1) + '1000' + m.group(3), s)
open(p, 'w').write(s2)
PY
done
