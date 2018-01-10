package models
import org.joda.time.{DateTime, DateTimeZone}

case class Tweets(tweetId: BigInt, messages: String, userId: String, favoriteCount: Int, retweetCount: Int, dateTime: DateTime, originalUserId: String)
case class Users(userId: String, userName: String, email: String, password: String)
case class TweetInfo(tweetId: BigInt, userId: String, userName: String, messages: String, favoriteCount: Int, retweetCount: Int, dateTime: DateTime)
case class SigninForm(userId: String, password: String)
case class Favorites(tweetId: BigInt, userId: String)
case class Retweets(tweetId: BigInt, userId: String)
case class Relations(userId: String, followerId: String)