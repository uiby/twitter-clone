package services

import javax.inject.Inject
import org.joda.time.{DateTime, DateTimeZone}

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.language.postfixOps

case class Tweets(tweet_id: BigInt, messages: String, user_id: String, favorite_count: Int, retweet_count: Int, date_time: DateTime)
case class tweetForm(messages: String, password: String)

@javax.inject.Singleton
class TweetService @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  val simple = {
  	get[BigInt]("tweets.tweet_id")~
    get[String]("tweets.messages")~
    get[String]("tweets.user_id")~
    get[Int]("tweets.favorite_count") ~
    get[Int]("tweets.retweet_count")~
    get[DateTime]("tweets.date_time") map {
      case tweet_id ~ messages ~ user_id ~ favorite_count ~ retweet_count ~ date_time 
      => Tweets(tweet_id, messages, user_id, favorite_count, retweet_count, date_time)
    }
  }

  //IDでついーと検索
  def findTweetById(user_id: String): Seq[Tweets] = {
    db.withConnection { implicit connection =>
      SQL(
        """
          select * from tweets where user_id = {id}
        """
      ).on('id -> user_id
      ).as(simple *)
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
}