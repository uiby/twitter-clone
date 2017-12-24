# --- First database schema

# --- !Ups
create table users (
  user_id                        char(12) not null,
  user_name                      varchar(255) not null,
  email                          varchar(255) not null,
  password                       varchar(12) not null,
  PRIMARY KEY (user_id)
);

create table tweets (
	tweet_id                        bigint not null auto_increment,
	messages                        varchar(140) not null,
  user_id                         char(12) not null,
  favorite_count                  int not null default 0,
  retweet_count                   int not null default 0,
  date_time                       DATETIME not null default current_timestamp,
  original_user_id                char(12) default "",
  PRIMARY KEY (tweet_id)
);

create table favorites (
	tweet_id                        bigint not null,
	user_id                         char(12) not null,
  PRIMARY KEY (tweet_id, user_id)
);

create table retweets (
  tweet_id                        bigint not null,
  user_id                         char(12) not null,
  PRIMARY KEY (tweet_id, user_id)
);

create table relations (
	user_id                         char(12) not null,
	follower_id                    char(12) not null
);

# --- !Downs 
drop table if exists users;
drop table if exists tweets;
drop table if exists favorites;
drop table if exists retweets;
drop table if exists relations;
