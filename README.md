# Twitter Clone 

## 課題内容
- [ ] アカウント作成
- [ ] タイムライン
- [ ] ツイート
- [ ] フォロー
- [ ] リツイート
- [ ] リプライ
- [ ] ライク

## 詳細
* アカウント作成
  - [ ] ユーザ名、ID、メアド、パスワードを所有
  - [ ] フォーム作成
  - [ ] userテーブルで管理
* タイムライン
  - [ ] フォローユーザと自分のツイートを一覧にする
  - [ ] 最新順にソート
* ツイート
  - [ ] ツイートID、メッセージ、ツイートした人のID、ライク数、リツイート数、ツイート時間、原文のユーザIDを所有
  - [ ] フォーム作成
  - [ ] tweetsテーブルで管理
* フォロー
  - [ ] フォローボタンでフォロー
  - [ ] relationsテーブルで管理
* リツイート
  - [ ] リツイートボタンで本ツイートを自分でツイート
  - [ ] リツイート数を更新
  - [ ] retweetsテーブルで管理
* リプライ
  - [ ] 対象ツイートを表示
  - [ ] フォームを作成
  - [ ] 送信後、対象リツイートの下にリスト形式で表示
  - [ ] replysテーブルで管理
* ライク
  - [ ] ライクボタンでライク
  - [ ] ライク数を更新
  - [ ] favoritesテーブルで管理

## その他
- [ ] サインイン
- [ ] サインアウト
- [ ] ツイート検索(検索ワードを含むツイートを表示する)
- [ ] ユーザのライク一覧

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
| user_id | char(140) | NO |  | NULL | 
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
