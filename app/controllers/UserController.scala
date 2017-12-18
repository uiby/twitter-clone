package controllers

import services._

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.RequestHeader

import play.api.data._
import play.api.data.Forms._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class UserController @Inject()(userService: UserService, mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def home() = Action { implicit request: MessagesRequest[AnyContent] =>
    request.session.get("user_id").map { id => //ログイン済みの場合
      Redirect(routes.TweetController.showTimeline())
    }.getOrElse {
      Redirect(routes.UserController.list())
      //TODO ログインしてない場合のホームページ
    }
  }

  //ユーザー一覧
  def list() = Action {implicit request: MessagesRequest[AnyContent] =>
    val items: Seq[Users] = userService.list()
    Ok(views.html.list(items))
  }

  //アカウント作成
  val userForm = Form(
    mapping(
      "user_id" -> nonEmptyText(minLength = 3, maxLength = 12),
      "user_name" -> nonEmptyText(minLength = 3, maxLength = 12),
      "email" -> email,
      "password" -> nonEmptyText(minLength = 5, maxLength = 12)
    )(
      (Users.apply)
    )(
      (Users.unapply)
    )
  )
  def signup() = Action {implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.signup(userForm))
  }

  //アカウント追加
  def userAdd() = Action {implicit request: MessagesRequest[AnyContent] =>

    //エラー処理
    val errorFunction = { formWithErrors: Form[Users] =>
      BadRequest(views.html.signup(formWithErrors))
    }

    val successFunction = { user: Users =>
      userService.findUserById(user.user_id).map { user => //既にアカウントがある場合
        BadRequest(views.html.signup(userForm))
      }.getOrElse{
        userService.insert(user)
        Redirect(routes.UserController.home()).withSession(request.session + ("user_id" -> user.user_id) + ("user_name" -> user.user_name))
      }
    }

    val formValidationResult = userForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  //アカウント作成
  val signinForm = Form(
    mapping(
      "user_id" -> nonEmptyText(minLength = 3, maxLength = 12),
      "password" -> nonEmptyText(minLength = 5, maxLength = 12)
    )(
      (SigninForm.apply)
    )(
      (SigninForm.unapply)
    )
  )

  //サインイン
  def signin() = Action {implicit request: MessagesRequest[AnyContent] => 
    //エラー処理
    val errorFunction = { formWithErrors: Form[SigninForm] =>
      BadRequest(views.html.signin(formWithErrors))
    }

    val successFunction = { signinForm: SigninForm =>
      userService.findUserById(signinForm.user_id).map { user =>
        Redirect(routes.UserController.home()).withSession(request.session + ("user_id" -> user.user_id) + ("user_name" -> user.user_name))
      }.getOrElse{
        Redirect(routes.UserController.signin())
      }
    }

    val formValidationResult = signinForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def signout() = Action {implicit request: MessagesRequest[AnyContent] => 
    Redirect(routes.UserController.home()).withNewSession
  }

  def followerList(user_id: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    val followerList: Seq[Users] = userService.findFollower(user_id)
    Ok(views.html.followerList(followerList, user_id))
  }

  def followingList(user_id: String) = Action {implicit request: MessagesRequest[AnyContent] =>
    val followList: Seq[Users] = userService.findFollow(user_id)
    Ok(views.html.followList(followList, user_id))
  }
}
