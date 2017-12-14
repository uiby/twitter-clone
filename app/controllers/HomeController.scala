package controllers

import services._

import javax.inject._
import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(userService: UserService, mcc: MessagesControllerComponents) extends MessagesAbstractController(mcc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.index())
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
      userService.insert(user)
      Redirect(routes.HomeController.list())
    }

    val formValidationResult = userForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
