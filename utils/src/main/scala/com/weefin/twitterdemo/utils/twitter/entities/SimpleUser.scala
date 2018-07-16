package com.weefin.twitterdemo.utils.twitter.entities

import java.util.Date

import com.danielasfregola.twitter4s.entities.User

case class SimpleUser(created_at: Date,
                      description: Option[String] = None,
                      followers_count: Int,
                      id: Long,
                      name: String,
                      screen_name: String,
                      statuses_count: Int) {
	
	def this(user: User) = this(created_at = user.created_at,
	                            description = user.description,
	                            followers_count = user.followers_count,
	                            id = user.id,
	                            name = user.name,
	                            screen_name = user.screen_name,
	                            statuses_count = user.statuses_count)
}

object SimpleUser {
	def apply(user: User) = new SimpleUser(user)
}
