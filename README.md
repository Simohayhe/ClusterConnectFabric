# ClusterConnect Fabric

Velocity Modern Forwarding に対応した Fabric サーバー用 MOD です。
Velocity プロキシ経由の接続のみを許可し、HMAC-SHA256 署名で検証します。

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

サーバー初回起動時に `config/clusterconnect.json` が自動生成されます。

```json
{
  "secret_key": "ここにVelocityと同じキーを入力"
}
```

### secret_key の確認方法

Velocity サーバーの `velocity.toml` を開き、`forwarding-secret` の値をコピーしてください。

```toml
# velocity.toml
forwarding-secret = "your-secret-key"
```

## Velocity の設定

`velocity.toml` の `player-info-forwarding-mode` を `modern` に設定してください。

```toml
player-info-forwarding-mode = "modern"
```

## 動作の仕組み

```
プレイヤー接続
     ↓
Velocity が HMAC-SHA256 で署名して転送データを送信
     ↓
ClusterConnect が署名を検証
     ↓
✅ 一致 → 本来の UUID / 名前 / スキンで接続許可
❌ 不一致 → 接続拒否
```

Velocity を経由しない直接接続はすべて拒否されます。

## ビルド

```bash
./gradlew build
```

ビルド成果物は `build/libs/` に生成されます。

## ライセンス

All Rights Reserved
