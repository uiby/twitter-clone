package services
import models._
import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import play.api.db.DBApi

import scala.language.postfixOps

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

  val rela = {
    get[String]("relations.user_id") ~
    get[String]("relations.follower_id") map {
      case user_id ~ follower_id
      => Relations(user_id, follower_id)
    }
  }

  def list(): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM users
        """
      ).as(simple *)
    }
  }

  def insert(users: Users) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        INSERT INTO users VALUES ({user_id}, {user_name}, {email}, {password})
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
      SQL("SELECT * FROM users WHERE user_id = {id}").on('id -> id).as(simple.singleOpt)
    }
  }

  def findFollower(id: String): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL(    
        """
          SELECT users.user_id, users.user_name, users.email, users.password
          FROM users 
          INNER join relations
          ON relations.follower_id = {id} AND relations.user_id = users.user_id
        """).on('id -> id).as(simple *)
    }
  }

  def findFollow(id: String): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL(    
        """
          SELECT users.user_id, users.user_name, users.email, users.password
          FROM users 
          INNER join relations
          ON relations.user_id = {id} AND relations.follower_id = users.user_id
        """).on('id -> id).as(simple *)
    }
  }

  def follow(user_id: String, follow_id: String) = {
    db.withConnection { implicit connection =>
      var hasRela = SQL("""SELECT * FROM relations WHERE user_id = {user_id} AND follower_id = {follower_id}"""
        ).on(
          'user_id -> follow_id,
          'follower_id -> user_id
        ).as(rela.singleOpt)

      if (hasRela == None) {
        SQL(
          """
            INSERT INTO relations VALUES ({user_id}, {follower_id})
          """
        ).on(
          'user_id -> follow_id,
          'follower_id -> user_id
        ).executeInsert()
      }
    }
  }

  def UnFollow(user_id: String, follow_id: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          DELETE FORM relations WHERE user_id = {user_id} AND follower_id = {follower_id}
        """
      ).on(
        'user_id -> follow_id,
        'follower_id -> user_id
      ).execute()
    }
  }
}