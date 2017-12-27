package models
import org.joda.time.{DateTime, DateTimeZone}

case class Tweets(tweet_id: BigInt, messages: String, user_id: String, favorite_count: Int, retweet_count: Int, date_time: DateTime, original_user_id: String)
case class Users(user_id: String, user_name: String, email: String, password: String)

case class TweetInfo(tweet_id: BigInt, user_id: String, user_name: String, messages: String, favorite_count: Int, retweet_count: Int, date_time: DateTime)
case class SigninForm(user_id: String, password: String)
case class Favorites(tweet_id: BigInt, user_id: String)
case class Retweets(tweet_id: BigInt, user_id: String)