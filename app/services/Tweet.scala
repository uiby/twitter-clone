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

  val simple = {
  	get[BigInt]("tweets.tweet_id")~
    get[String]("tweets.messages")~
    get[String]("tweets.user_id")~
    get[Int]("tweets.favorite_count") ~
    get[Int]("tweets.retweet_count")~
    get[DateTime]("tweets.date_time") ~
    get[String]("original_user_id") map {
      case tweet_id ~ messages ~ user_id ~ favorite_count ~ retweet_count ~ date_time ~ original_user_id
      => Tweets(tweet_id, messages, user_id, favorite_count, retweet_count, date_time, original_user_id)
    }
  }

  val tweetInfo = {
    get[BigInt]("tweets.tweet_id")~
    get[String]("tweets.user_id")~
    get[String]("users.user_name")~
    get[String]("tweets.messages")~
    get[Int]("tweets.favorite_count") ~
    get[Int]("tweets.retweet_count")~
    get[DateTime]("tweets.date_time") map {
      case tweet_id ~ user_id ~ user_name ~ messages ~ favorite_count ~ retweet_count ~ date_time 
      => TweetInfo(tweet_id, user_id, user_name, messages, favorite_count, retweet_count, date_time)
    }
  }

  //IDでついーと検索
  def findTweetById(user_id: String): Seq[Tweets] = {
    val user = userService.findUserById(user_id)
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM tweets WHERE user_id = {id}").on('id -> user_id).as(simple *)
    }
  }

  def findTweetByWord(word: String): Seq[TweetInfo] = {
    db.withConnection { implicit connection =>
      SQL(
        s"""SELECT distinct tweets.tweet_id, tweets.user_id, users.user_name, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time, tweets.original_user_id
        FROM tweets 
        INNER JOIN users
        ON tweets.user_id = users.user_id
        WHERE messages LIKE "%$word%" 
        ORDER BY date_time DESC"""
        ).on().as(tweetInfo *)
    }
  }

  def getTimeline(user_id: String): Seq[Tweets] = {
    db.withConnection { implicit connection =>
      SQL(    
        """
          SELECT distinct tweets.tweet_id, tweets.user_id, tweets.messages, tweets.user_id, tweets.favorite_count, tweets.retweet_count, tweets.date_time
          FROM tweets 
          inner join relations
          on tweets.user_id = {id} or (relations.user_id = {id} and tweets.user_id = relations.follower_id) 
          order by tweets.date_time desc
        """).on('id -> user_id).as(simple *)
    }
  }

  //新しいツイート
  def insertNewTweet(id: String, message: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        insert into tweets (messages, user_id) values ({messages}, {user_id})
        """
      ).on(
        'messages -> message,
        'user_id -> id
      ).executeInsert()
    }
  }
  //新しいツイート
  def insertNewTweet(id: String, message: String, original_user_id: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        insert into tweets (messages, user_id, original_user_id) values ({messages}, {user_id}, {original_user_id})
        """
      ).on(
        'messages -> message,
        'user_id -> id,
        'original_user_id -> original_user_id,
      ).executeInsert()
    }
  }

  def favorite(tweet_id: BigInt, user_id: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        insert into favorites values ({tweet_id}, {user_id})
        """
      ).on(
        'tweet_id -> tweet_id,
        'user_id -> user_id
      ).executeInsert()

      var fav_count = SQL(s"""select favorite_count from tweets where tweet_id = $tweet_id"""
        ).as(get[Int]("favorite_count").singleOpt)

      if (fav_count != None) {
        SQL (
          """
            update tweets
            set favorite_count = {favorite_count}
            where tweet_id = {id}
          """
        ).on(
          'id -> tweet_id,
          'favorite_count -> (fav_count.get + 1).toString
        ).executeUpdate()
      }
    }
  }

  def retweet(tweet_id: BigInt, user_id: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into retweets values ({tweet_id}, {user_id})
        """
      ).on(
        'tweet_id -> tweet_id,
        'user_id -> user_id
      ).executeInsert()

      var result = SQL(s"""select * from tweets where tweet_id = $tweet_id""").as(simple.singleOpt)

      if (result != None) {
        SQL (
          """
            update tweets
            set retweet_count = {retweet_count}
            where tweet_id = {id}
          """
        ).on(
          'id -> tweet_id,
          'retweet_count -> (result.get.retweet_count + 1).toString
        ).executeUpdate()

        insertNewTweet(user_id, result.get.messages, result.get.user_id)
      }
    }

  }
}