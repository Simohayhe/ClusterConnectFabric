# ClusterConnect Fabric

Velocity Modern Forwarding に対応した Fabric サーバー用 MOD です。
Velocity プロキシ経由の接続のみを許可し、HMAC-SHA256 署名を検証します。
プレイヤーの本物の UUID・スキン（textures プロパティ）も正しく反映されます。

## 動作環境

| 項目 | バージョン |
|------|-----------|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.4 以上 |
| Fabric API | 0.141.3+1.21.11 以上 |
| Java | 21 以上 |

## インストール

1. [Releases](https://github.com/Simohayhe/ClusterConnectFabric/releases) から最新の JAR をダウンロード
2. Fabric サーバーの `mods/` フォルダに配置
3. サーバーを起動（`config/clusterconnect.json` が自動生成されます）
4. `config/clusterconnect.json` を編集して `secret_key` を設定
5. サーバーを再起動

## 設定

### config/clusterconnect.json

サーバー初回起動時に自動生成されます。

```json
{
  "secret_key": "ここにVelocityと同じキーを入力"
}
```

| フィールド | 説明 |
|-----------|------|
| `secret_key` | Velocity の転送シークレット。`velocity.toml` の `forwarding-secret` と完全一致させること |

### secret_key の確認方法

**`velocity.toml` に直接書いている場合:**

```toml
# velocity.toml
forwarding-secret = "your-secret-key"
```

この `your-secret-key` の部分をそのままコピーします。

**`forwarding-secret-file` でファイル参照している場合（Velocity 3.x デフォルト）:**

```toml
# velocity.toml
forwarding-secret-file = "forwarding.secret"
```

`forwarding.secret` ファイルの中身をコピーします。
**末尾の改行・スペースは含めないでください。**

## Velocity の設定

`velocity.toml` を以下のように設定してください。

```toml
online-mode = true
player-info-forwarding-mode = "modern"
```

## バックエンドサーバー（Fabric）の設定

`server.properties` を以下のように設定してください。

```properties
online-mode=false
enforce-secure-profile=false
prevent-proxy-connections=false
```

## 動作の仕組み

```
プレイヤー接続
     ↓
Velocity がプレイヤーを認証（Mojang）
     ↓
バックエンドへ接続開始
     ↓
ClusterConnect が velocity:player_info チャンネルで問い合わせ
     ↓
Velocity が HMAC-SHA256 署名付きで転送データを送信
  → UUID（本物の Mojang UUID）
  → ユーザー名
  → textures プロパティ（スキン URL + 署名）
     ↓
ClusterConnect が署名を検証
     ↓
✅ 一致 → 本物の UUID / ユーザー名 / スキンで接続許可
❌ 不一致 → 接続拒否
```

Velocity を経由しない直接接続はすべて拒否されます。

## トラブルシューティング

### `Velocity forwarding signature mismatch`

HMAC 検証に失敗しています。

- `secret_key` が `forwarding-secret`（または `forwarding.secret` ファイルの内容）と**完全一致**しているか確認
- **末尾の改行・スペースが入っていないか**確認（よくある原因）
- 設定変更後にサーバーを**再起動**したか確認

### `Direct connections are not allowed`

Velocity を経由せず直接接続しています。Velocity 経由で接続してください。

### `secret_key is not set`

`config/clusterconnect.json` の `secret_key` が空のままです。正しい値を設定してサーバーを再起動してください。

### スキンが表示されない

- Velocity の `online-mode = true` になっているか確認
- Velocity の `player-info-forwarding-mode = "modern"` になっているか確認
- バックエンドの `online-mode = false` になっているか確認

## ビルド

```bash
./gradlew build
```

ビルド成果物は `build/libs/` に生成されます。

## ライセンス

All Rights Reserved
