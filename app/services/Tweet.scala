package services
import models._
import javax.inject.Inject
import org.joda.time.{DateTime, DateTimeZone}

import anorm.SqlParser._ //parser API select結果をパース
import anorm._
import play.api.db.DBApi

import scala.language.postfixOps


@javax.inject.Singleton
class TweetService @Inject() (dbapi: DBApi, userService: UserService) {

  private val db = dbapi.database("default")

  val tweetsMapper = {
  	get[BigInt]("tweets.tweet_id")~
    get[String]("tweets.messages")~
    get[String]("tweets.user_id")~
    get[Int]("tweets.favorite_count") ~
    get[Int]("tweets.retweet_count")~
    get[DateTime]("tweets.date_time") ~
    get[String]("original_user_id") map {
      case tweetId ~ messages ~ userId ~ favoriteCount ~ retweetCount ~ dateTime ~ originalUserId
      => Tweets(tweetId, messages, userId, favoriteCount, retweetCount, dateTime, originalUserId)
    }
  }

  val tweetInfoMapper = {
    get[BigInt]("tweets.tweet_id")~
    get[String]("tweets.user_id")~
    get[String]("users.user_name")~
    get[String]("tweets.messages")~
    get[Int]("tweets.favorite_count") ~
    get[Int]("tweets.retweet_count")~
    get[DateTime]("tweets.date_time") map {
      case tweetId ~ userId ~ userName ~ messages ~ favoriteCount ~ retweetCount ~ dateTime 
      => TweetInfo(tweetId, userId, userName, messages, favoriteCount, retweetCount, dateTime)
    }
  }

  val favoritesMapper = {
    get[BigInt]("favorites.tweet_id") ~
    get[String]("favorites.user_id") map {
      case tweetId ~ userId
      => Favorites(tweetId, userId)
    }
  }

  val retweetsMapper = {
    get[BigInt]("retweets.tweet_id") ~
    get[String]("retweets.user_id") map {
      case tweetId ~ userId
      => Retweets(tweetId, userId)
    }
  }

  //IDでついーと検索
  def findTweetById(userId: String): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        WHERE tweets.user_id = "$userId" 
        ORDER BY date_time DESC"""
        ).on().as(tweetInfoMapper *)
    }
  }

  def findTweetByTweetId(tweetId: String): Option[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        WHERE tweets.tweet_id = "$tweetId" """ 
      ).on().as(tweetInfoMapper.singleOpt)
    }
  }

  //Favでついーと検索
  def findTweetByFavorite(userId: String): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        INNER JOIN favorites
        ON favorites.user_id = "$userId"
        WHERE tweets.tweet_id = favorites.tweet_id 
        ORDER BY date_time DESC"""
        ).on().as(tweetInfoMapper *)
    }
  }

  def findTweetByWord(searchWord: String): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        WHERE messages LIKE "%$searchWord%" 
        ORDER BY date_time DESC"""
        ).on().as(tweetInfoMapper *)
    }
  }

  def findTweet(): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        ORDER BY date_time DESC"""
        ).on().as(tweetInfoMapper *)
    }    
  }

  def getTimeline(userId: String): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(    
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        INNER JOIN relations
        on tweets.user_id = "$userId" or (relations.user_id = "$userId" AND tweets.user_id = relations.follower_id) 
        ORDER BY date_time DESC"""
        ).on().as(tweetInfoMapper *)
    }
  }

  //新しいツイート
  def insertNewTweet(id: String, message: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        INSERT INTO tweets (messages, user_id) VALUES ({messages}, {user_id})
        """
      ).on(
        'messages -> message,
        'user_id -> id
      ).executeInsert() //return tweet_id
    }
  }
  //新しいツイート
  def insertNewTweet(id: String, message: String, originalUserId: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        INSERT INTO tweets (messages, user_id, original_user_id) VALUES ({messages}, {user_id}, {original_user_id})
        """
      ).on(
        'messages -> message,
        'user_id -> id,
        'original_user_id -> originalUserId,
      ).executeInsert()
    }
  }

  def favorite(tweetId: BigInt, userId: String) = {
    db.withConnection { implicit connection =>
      var hasFav = SQL("""SELECT * FROM favorites WHERE tweet_id = {tweet_id} AND user_id = {user_id}"""
        ).on(
          'tweet_id -> tweetId,
          'user_id -> userId
        ).as(favoritesMapper.singleOpt)

      if (hasFav == None) {
        SQL("""INSERT INTO favorites VALUES ({tweet_id}, {user_id})"""
        ).on(
          'tweet_id -> tweetId,
          'user_id -> userId
        ).executeInsert()
  
        var favCount = SQL("""SELECT favorite_count FROM tweets WHERE tweet_id = {tweet_id}"""
          ).on(
            'tweet_id -> tweetId
          ).as(get[Int]("favorite_count").singleOpt)
  
        if (favCount != None) {
          SQL (
            """
              UPDATE tweets
              SET favorite_count = {favorite_count}
              WHERE tweet_id = {id}
            """
          ).on(
            'id -> tweetId,
            'favorite_count -> (favCount.get + 1).toString
          ).executeUpdate()
        }
      }
    }
  }

  def retweet(tweetId: BigInt, userId: String) = {
    db.withConnection { implicit connection =>
      var hasRet = SQL("""SELECT * FROM retweets WHERE tweet_id = {tweet_id} AND user_id = {user_id}"""
        ).on(
          'tweet_id -> tweetId,
          'user_id -> userId
        ).as(favoritesMapper.singleOpt)

      if (hasRet == None) {
        SQL("""INSERT INTO retweets VALUES ({tweet_id}, {user_id})"""
        ).on(
          'tweet_id -> tweetId,
          'user_id -> userId
        ).executeInsert()
  
        var result = SQL(s"""SELECT * FROM tweets WHERE tweet_id = $tweetId""").as(tweetsMapper.singleOpt)
  
        if (result != None) {
          SQL (
            """
              UPDATE tweets
              SET retweet_count = {retweet_count}
              WHERE tweet_id = {id}
            """
          ).on(
            'id -> tweetId,
            'retweet_count -> (result.get.retweetCount + 1).toString
          ).executeUpdate()
  
          insertNewTweet(userId, result.get.messages, result.get.userId)
        }
      }
    }
  }

  def reply(sendTweetId: BigInt, message: String, userId: String) = {
    var tweetId = insertNewTweet(userId, message)
    db.withConnection { implicit connection =>
      SQL("""INSERT INTO replys VALUES ({tweet_id}, {reply_tweet_id}, {user_id})"""
      ).on(
        'tweet_id -> tweetId,
        'reply_tweet_id -> sendTweetId,
        'user_id -> userId
      ).executeInsert()
    }
  }

  def getReply(tweetId: String): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets
        INNER JOIN users
        ON tweets.user_id = users.user_id
        INNER JOIN replys
        ON tweets.tweet_id = replys.tweet_id
        WHERE replys.reply_tweet_id = "$tweetId"
        ORDER BY tweets.date_time"""
        ).on().as(tweetInfoMapper *)
    }    
  }
}