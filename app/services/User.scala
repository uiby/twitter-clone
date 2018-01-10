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

  val UsersMapper = {
  	get[String]("users.user_id")~
    get[String]("users.user_name")~
    get[String]("users.email")~
    get[String]("users.password") map {
      case userId ~ userName ~ email ~ password => Users(userId, userName, email, password)
    }
  }

  val relationsMapper = {
    get[String]("relations.user_id") ~
    get[String]("relations.follower_id") map {
      case userId ~ followerId
      => Relations(userId, followerId)
    }
  }

  def list(): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM users
        """
      ).as(UsersMapper *)
    }
  }

  def insert(users: Users) = {
    db.withConnection { implicit connection =>
      SQL(
        """
        INSERT INTO users VALUES ({user_id}, {user_name}, {email}, {password})
        """
      ).on(
        'user_id -> users.userId,
        'user_name -> users.userName,
        'email -> users.email,
        'password -> users.password
      ).executeInsert()
    }
  }

  def findUserById(id: String): Option[Users] ={
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM users WHERE user_id = {id}").on('id -> id).as(UsersMapper.singleOpt)
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
        """).on('id -> id).as(UsersMapper *)
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
        """).on('id -> id).as(UsersMapper *)
    }
  }

  def follow(userId: String, followId: String) = {
    db.withConnection { implicit connection =>
      var hasRela = SQL("""SELECT * FROM relations WHERE user_id = {user_id} AND follower_id = {follower_id}"""
        ).on(
          'user_id -> followId,
          'follower_id -> userId
        ).as(relationsMapper.singleOpt)

      if (hasRela.isEmpty) {
        SQL(
          """
            INSERT INTO relations VALUES ({user_id}, {follower_id})
          """
        ).on(
          'user_id -> followId,
          'follower_id -> userId
        ).executeInsert()
      }
    }
  }

  def UnFollow(userId: String, followId: String) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          DELETE FORM relations WHERE user_id = {user_id} AND follower_id = {follower_id}
        """
      ).on(
        'user_id -> followId,
        'follower_id -> userId
      ).execute()
    }
  }
}