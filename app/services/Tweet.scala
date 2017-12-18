package services
import javax.inject.Inject
import org.joda.time.{DateTime, DateTimeZone}

import anorm.SqlParser._ //parser API select結果をパース
import anorm._
import play.api.db.DBApi

import scala.language.postfixOps

case class Tweets(tweet_id: BigInt, messages: String, user_id: String, favorite_count: Int, retweet_count: Int, date_time: DateTime)
case class TweetInfo(user_id: String, user_name: String, messages: String, favorite_count: Int, retweet_count: Int, date_time: DateTime)

@javax.inject.Singleton
class TweetService @Inject() (dbapi: DBApi, userService: UserService) {

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
    val user = userService.findUserById(user_id)
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM tweets WHERE user_id = {id}").on('id -> user_id).as(simple *)
    }
  }

  def findTweetByWord(word: String): Seq[Tweets] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM tweets WHERE messages LIKE '%{str}%'").on('str -> word).as(simple *)
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
}