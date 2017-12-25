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

  def findFollower(id: String): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL(    
        """
          select users.user_id, users.user_name, users.email, users.password
          from users 
          inner join relations
          on relations.follower_id = {id} and relations.user_id = users.user_id
        """).on('id -> id).as(simple *)
    }
  }

  def findFollow(id: String): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL(    
        """
          select users.user_id, users.user_name, users.email, users.password
          from users 
          inner join relations
          on relations.user_id = {id} and relations.follower_id = users.user_id
        """).on('id -> id).as(simple *)
    }
  }

  def follow(user_id: String, follow_id: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into relations values ({user_id}, {follower_id})
        """
      ).on(
        'user_id -> follow_id,
        'follower_id -> user_id
      ).executeInsert()
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