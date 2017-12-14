package services

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.language.postfixOps

case class Users(user_id: String, user_name: String, email: String, password: String)
case class SigninForm(user_id: String, password: String)

@javax.inject.Singleton
class UserService @Inject() (dbapi: DBApi) {

  private val db = dbapi.database("default")

  val simple = {
  	get[String]("users.user_id")~
    get[String]("users.user_name")~
    get[String]("users.email")~
    get[String]("users.password") map {
      case user_id ~ user_name ~ email ~ password => Users(user_id, user_name, email, password)
    }
  }

  def list(): Seq[Users] = {
    db.withConnection { implicit connection =>

      SQL(
        """
          select * from users
        """
      ).as(simple *)
    }
  }

  def insert(users: Users) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        insert into users values ({user_id}, {user_name}, {email}, {password})
        """
      ).on(
        'user_id -> users.user_id,
        'user_name -> users.user_name,
        'email -> users.email,
        'password -> users.password
      ).executeInsert()
    }
  }

  def findUserById(id: String): Option[Users] ={
    db.withConnection { implicit connection =>
      SQL("select * from users where user_id = {id}").on('id -> id).as(simple.singleOpt)
    }
  }
}