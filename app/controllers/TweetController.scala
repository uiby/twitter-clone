package controllers

import models._
import services._

import play.api.libs.json._
import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.RequestHeader
import play.api.routing._

import play.api.data._
import play.api.data.Form
import play.api.data.Forms._

import play.filters.csrf.CSRF

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class TweetController @Inject()(tweetService: TweetService, mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {
  implicit val TweetInfoWrites = new Writes[TweetInfo] {
    def writes(tweet: TweetInfo) = Json.obj(
      "tweet_id" -> tweet.tweetId.toString,
      "user_name" -> tweet.userName,
      "user_id" -> tweet.userId,
      "messages" -> tweet.messages,
      "favorite_count" -> tweet.favoriteCount,
      "retweet_count" -> tweet.retweetCount,
      "date_time" -> tweet.dateTime.toString("yyyy/MM/dd")      
    )
  }

  //ツイートページ
  val tweetForm: Form[String] = Form("messages" -> nonEmptyText)
  def tweet() = Action {implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      Ok(views.html.tweet(tweetForm, Some(id)))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  //ツイート
  def addNewTweet() = Action {implicit request: MessagesRequest[AnyContent] =>
    //エラー処理
    val errorFunction = { formWithErrors: Form[String] =>
      request.session.get("user_id").map { id => //ログイン済みの場合
        BadRequest(views.html.tweet(formWithErrors, Some(id)))
      }.getOrElse {
        Redirect(routes.UserController.signin())
      }
    }

    val successFunction = { message: String =>
      request.session.get("user_id").map { id => //ログイン済みの場合
        tweetService.insertNewTweet(id, message)
        Redirect(routes.TweetController.userTweetList(id))
      }.getOrElse {
        Redirect(routes.UserController.signin())
      }
    }

    val formValidationResult = tweetForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  //ユーザのツイート一覧
  def userTweetList(userId: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    val tweetList = tweetService.findTweetById(userId)
    request.session.get("user_id").map { id => //ログイン済みの場合
      Ok(views.html.userTweetList(tweetList, userId, Some(id)))
    }.getOrElse {
      Ok(views.html.userTweetList(tweetList, userId, None))
    }
  }
  //ユーザのファボ一覧
  def userFavoriteList(userId: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    val tweetList = tweetService.findTweetByFavorite(userId)
    request.session.get("user_id").map { id => //ログイン済みの場合
      Ok(views.html.userTweetList(tweetList, userId, Some(id)))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  def showTimeline() = Action {implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      val timeline = tweetService.getTimeline(id)
      Ok(views.html.timeline(timeline, id))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  def showTweetList() = Action{implicit request: MessagesRequest[AnyContent] =>
    val tweetList = tweetService.findTweet()
    request.session.get("user_id").map { id =>
      Ok(views.html.tweetList("", tweetList, Some(id))) 
    }.getOrElse {
      Ok(views.html.tweetList("", tweetList, None)) 
    }
  }

  var question: String = ""
  def search() = Action { implicit request: MessagesRequest[AnyContent] =>
    var qk: Map[String,String] = request.queryString.map { case (k,v) => k -> v.mkString }
    if (qk.contains("q"))
      question = qk("q")
    val tweetList: Seq[TweetInfo] = tweetService.findTweetByWord(question)
    request.session.get("user_id").map { id =>
      Ok(views.html.tweetList(question, tweetList, Some(id))) 
    }.getOrElse {
      Ok(views.html.tweetList(question, tweetList, None)) 
    }
  }

  def favorite(tweetId: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      val temp = BigInt(tweetId)
      tweetService.favorite(temp, id)
      Ok(Json.toJson(tweetService.findTweetByTweetId(tweetId).get))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  def retweet(tweetId: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      val temp = BigInt(tweetId)
      tweetService.retweet(temp, id)
      Ok(Json.toJson(tweetService.findTweetByTweetId(tweetId).get))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  def reply(sendTweetId: String, messages: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { userId =>
      val temp = BigInt(sendTweetId)
      tweetService.reply(temp, messages, userId)
      Ok
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  case class MainTweet(mainTweet: TweetInfo, replyTweets: Seq[TweetInfo])
  def getTweet(tweetId: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    val tweetInfo = tweetService.findTweetByTweetId(tweetId)
    val replyInfo = tweetService.getReply(tweetId)

    implicit val MainTweetWrites = new Writes[MainTweet] {
      def writes(mainTweet: MainTweet) = Json.obj(
      "main_tweet" -> mainTweet.mainTweet,
      "reply_tweet" -> mainTweet.replyTweets
      )
    }

    val jsonObject = Json.toJson(MainTweet(tweetInfo.get, replyInfo))
    Ok(jsonObject)
  }

  def javascriptRoutes() = Action {implicit request: MessagesRequest[AnyContent] =>
    Ok(
        JavaScriptReverseRouter("jsRoutes")(
          routes.javascript.TweetController.favorite,
          routes.javascript.TweetController.retweet,
          routes.javascript.TweetController.getTweet,
          routes.javascript.TweetController.reply,
          routes.javascript.UserController.follow,
        )
    ).as("text/javascript")
  }
}
