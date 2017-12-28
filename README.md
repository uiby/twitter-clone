# Twitter Clone 

## 課題内容
- [x] アカウント作成
- [x] タイムライン
- [x] ツイート
- [x] フォロー
- [x] リツイート
- [x] リプライ
- [x] ライク

## 環境
* scala ver.2.12.3
* play framework ver.2.6.7(多分)
* mysql ver.5.7.20
* anorm ver.2.5.3 (データベースアクセス)
* jquery ver.3.2.1

## 詳細
* アカウント作成
  - [x] ユーザ名、ID、メアド、パスワードを所有
  - [x] フォーム作成
  - [x] usersテーブルで管理
* タイムライン
  - [x] フォローユーザと自分のツイートを一覧にする
  - [x] 最新順にソート
* ツイート
  - [x] ツイートID、メッセージ、ツイートした人のID、ライク数、リツイート数、ツイート時間、原文のユーザIDを所有
  - [x] フォーム作成
  - [x] tweetsテーブルで管理
* フォロー
  - [x] フォローボタンでフォロー
  - [x] relationsテーブルで管理
* リツイート
  - [x] リツイートボタンで本ツイートを自分でツイート
  - [x] リツイート数を更新
  - [x] retweetsテーブルで管理
* リプライ
  - [x] 対象ツイートを表示
  - [x] フォームを作成
  - [x] 送信後、対象リツイートの下にリスト形式で表示
  - [x] replysテーブルで管理
* ライク
  - [x] ライクボタンでライク
  - [x] ライク数を更新
  - [x] favoritesテーブルで管理

## その他
- [x] サインイン(sessionに保持)
- [x] サインアウト(sessionを削除)
- [x] ツイート検索(検索ワードを含むツイートを一覧表示)
- [x] ユーザのツイート、フォロー、フォロワー、ライク一覧

## 不足要素
- [ ] サインイン、サインアップ、ツイートのviewの装飾
- [ ] リプライ画面のスクロール
- [ ] リツイート、ライク、フォローの取り消し
- [ ] テスト
- [ ] CSRF対策
- [ ] パスワードをハッシュで保存
- [ ] サインインしてない状態でのライク、フォロー、リプライ処理 
      -> サインイン画面に誘導

## データベース
* usersテーブル

| Field | Type | Null| Key | Default |
| ----- | ---- | --- | --- | --- |
| user_id | char(12) | NO | PRI | NULL
| user_name | varchar(255) | NO | PRI | NULL
| email | varchar(255) | NO | PRI | NULL
| password | varchar(12) | NO | PRI | NULL
* tweetsテーブル

| Field | Type | Null| Key | Default | Extra |
| ----- | ---- | --- | --- | --- | --- |
| tweet_id | bigint(20) | NO | PRI | NULL | auto_increment
| messages | varchar(140) | NO |  | NULL | 
| user_id | char(12) | NO |  | NULL | 
| favorite_count | int(11) | NO |  | 0 | 
| retweet_count | int(11) | NO |  | 0 | 
| date_time | datetime | NO |  | CURRENT_TIMESTAMP | 
| original_user_id | char(12) | YES |  |  | 

* favoritesテーブル

| Field | Type | Null| Key | Default |
| ----- | ---- | --- | --- | --- |
| tweet_id | bigint(20) | NO | PRI | NULL
| user_id | char(12) | NO | PRI | NULL
* retweetsテーブル

| Field | Type | Null| Key | Default |
| ----- | ---- | --- | --- | --- |
| tweet_id | bigint(20) | NO | PRI | NULL
| user_id | char(12) | NO | PRI | NULL
* replysテーブル

| Field | Type | Null| Key | Default |
| ----- | ---- | --- | --- | --- |
| tweet_id | bigint(20) | NO | | NULL
| reply_tweet_id | bigint(20) | NO | | NULL
| user_id | char(12) | NO | | NULL
* relationsテーブル

| Field | Type | Null| Key | Default |
| ----- | ---- | --- | --- | --- |
| user_id | char(12) | NO | PRI | NULL
| follower_id | char(12) | NO | PRI | NULL
