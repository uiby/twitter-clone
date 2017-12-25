package controllers

import models._
import services._

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

  //ツイートページ
  val tweetForm: Form[String] = Form("messages" -> nonEmptyText)
  def tweet() = Action {implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_name").map { name =>
      Ok(views.html.tweet(tweetForm))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  //ツイート
  def addNewTweet() = Action {implicit request: MessagesRequest[AnyContent] =>
    //エラー処理
    val errorFunction = { formWithErrors: Form[String] =>
      BadRequest(views.html.tweet(formWithErrors))
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
  def userTweetList(user_id: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    val tweetList = tweetService.findTweetById(user_id)
    Ok(views.html.userTweetList(tweetList, user_id))
  }
  //ユーザのファボ一覧
  def userFavoriteList(user_id: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    val tweetList = tweetService.findTweetByFavorite(user_id)
    Ok(views.html.userTweetList(tweetList, user_id))
  }

  def showTimeline() = Action {implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      val timeline = tweetService.getTimeline(id)
      Ok(views.html.timeline(timeline))
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }
  var question: String = ""
  def search() = Action { implicit request: MessagesRequest[AnyContent] =>
    var qk: Map[String,String] = request.queryString.map { case (k,v) => k -> v.mkString }
    if (qk.contains("q"))
      question = qk("q")
    val tweetList: Seq[TweetInfo] = tweetService.findTweetByWord(question)
    Ok(views.html.tweetList(question, tweetList)) 
  }

  def favorite(tweet_id: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      val temp = BigInt(tweet_id)
      tweetService.favorite(temp, id)
      Ok
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  def retweet(tweet_id: String) = Action { implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id =>
      val temp = BigInt(tweet_id)
      tweetService.retweet(temp, id)
      Ok
    }.getOrElse {
      Redirect(routes.UserController.signin())
    }
  }

  def javascriptRoutes() = Action {implicit request: MessagesRequest[AnyContent] =>
    Ok(
        JavaScriptReverseRouter("jsRoutes")(
          routes.javascript.TweetController.favorite,
          routes.javascript.TweetController.retweet
        )
    ).as("text/javascript")
  }
}
