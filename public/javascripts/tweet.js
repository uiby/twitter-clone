$(function() {
	favorite();
	retweet();
	tweetBox();
	reply();
});

var favorite = function() {
	$('.fav_button').on('click', function(event) {
		event.preventDefault();
	var favoriteBack = {
		dataType: 'json',
		success: function(jsonData, textStatus, jqXHR) {
			updateTweet(jsonData);
		},
		error: onError
	}

	jsRoutes.controllers.TweetController.favorite($(this).val().toString()).ajax(favoriteBack);
	});
};

var retweet = function() {
	$('.retweet_button').on('click', function(event) {
		event.preventDefault();
  	var favoriteBack = {
  		dataType: 'json',
  		success: function(jsonData, textStatus, jqXHR) {
  			updateTweet(jsonData);
  		},
		  error: onError
	  }

  	jsRoutes.controllers.TweetController.retweet($(this).val().toString()).ajax(favoriteBack);
	});
}

var reply = function() {
	$('#reply_button').on('click', function(event) {
		var id = $(this).val();
		var message = document.forms.reply_form.reply_message.value;
		event.preventDefault();
  	var feedBack = {
	  	success: function() { showMainTweet(document.forms.reply_form.reply_button.value);},
		  error: onError
	  }

	  console.log(id+":"+message);

  	jsRoutes.controllers.TweetController.reply(id, message).ajax(feedBack);
  	document.forms.reply_form.reply_message.value = "";
	});	
}

var  onSuccess = function(data) {
    alert("success"+data);
}

var onError = function(error) {
    alert("error"+error);
}

var tweetBox = function() {
	$('.click-box').on('click', function(event) {
		$("#overlay").fadeIn();
		showMainTweet($(this).val());
	});

	$('#close').on('click', function(event) {
		$("#overlay").fadeOut();
	});
}

var showMainTweet = function(tweet_id) {
  console.log("show main tweet:"+tweet_id);
	jsRoutes.controllers.TweetController.getTweet(tweet_id).ajax({
		dataType: 'json',
		success: function(jsonData, textStatus, jqXHR) {
		  //var data = JSON.stringify(jsonData);
		  //console.log(data+""+jsonData.main_tweet.user_name);
		  var items = new Array();
		  $("#overlay-main-tweet").children().remove();
		  //main_tweet生成
	  	items.push('<div class="tweet-box">');
		  	items.push('<div class="name">');
   		  	items.push('<a href="@controllers.routes.TweetController.userTweetList('+jsonData.main_tweet.user_id+')">'+jsonData.main_tweet.user_name+' '+jsonData.main_tweet.user_id+'</a>');
	    	items.push('</div>');
	    	items.push('<div class="date">'+jsonData.main_tweet.date_time+'</div>');
	  	  items.push('<div class="content">'+jsonData.main_tweet.messages+'</div>');
	  	  items.push('<div class="form">');
	  	    items.push('<form class="button"> <button class="fav_button" value="'+jsonData.main_tweet.tweet_id+'" type="submit">fav</button> </form>'+jsonData.main_tweet.favorite_count);
	  	    items.push('<form class="button"> <button class="retweet_button" value="'+jsonData.main_tweet.tweet_id+'" type="submit">retweet</button> </form>'+jsonData.main_tweet.retweet_count);
      items.push('</div></div>');
		  //リプライフォーム
		  document.forms.reply_form.reply_button.value = jsonData.main_tweet.tweet_id;

		  $("#overlay-main-tweet").append(items.join(''));

		  //リプライ一覧
		  $("#overlay-reply-tweet").children().remove();
		  var len = jsonData.reply_tweet.length;
		  items = new Array();
		  if (len != 0) {
 			  for (var i = 0; i < len; i++) {
    	  	items.push('<div class="tweet-box">');
   		  	items.push('<div class="name">');
    		  	items.push('<a href="@controllers.routes.TweetController.userTweetList('+jsonData.reply_tweet[i].user_id+')">'+jsonData.reply_tweet[i].user_name+' '+jsonData.reply_tweet[i].user_id+'</a>');
  	    	items.push('</div>');
  	    	items.push('<div class="date">'+jsonData.reply_tweet[i].date_time+'</div>');
  		 	  items.push('<div class="content">'+jsonData.reply_tweet[i].messages+'</div>');
  	  	  items.push('<div class="form">');
  	  	    items.push('<form class="button"> <button class="fav_button" value="'+jsonData.reply_tweet[i].tweet_id+'" type="submit">fav</button> </form>'+jsonData.reply_tweet[i].favorite_count);
  	  	    items.push('<form class="button"> <button class="retweet_button" value="'+jsonData.reply_tweet[i].tweet_id+'" type="submit">retweet</button> </form>'+jsonData.reply_tweet[i].retweet_count);
          items.push('</div></div>');			  			  	
 			  }
 			}
		  $("#overlay-reply-tweet").append(items.join(''));
		}
	});
}

var updateTweet = function(jsonTweetInfo) {
  var el = document.getElementById(jsonTweetInfo.tweet_id);
  console.log("update tweet:"+jsonTweetInfo.tweet_id);
  //el.children().remove();
  var items = new Array();
  //main_tweet生成
  	items.push('<div class="name">');
 		  items.push('<a href="@controllers.routes.TweetController.userTweetList('+jsonTweetInfo.user_id+')">'+jsonTweetInfo.user_name+' '+jsonTweetInfo.user_id+'</a>');
   	items.push('</div>');
   	items.push('<div class="date">'+jsonTweetInfo.date_time+'</div>');
 	  items.push('<div class="content">'+jsonTweetInfo.messages+'</div>');
 	  items.push('<div class="form">');
   	  items.push('<button class= "click-box" value="'+jsonTweetInfo.tweet_id+'" type="submit"> detail</button>');
 	    items.push('<form class="button"> <button class="fav_button" value="'+jsonTweetInfo.tweet_id+'" type="submit">fav</button> </form>'+jsonTweetInfo.favorite_count);
 	    items.push('<form class="button"> <button class="retweet_button" value="'+jsonTweetInfo.tweet_id+'" type="submit">retweet</button> </form>'+jsonTweetInfo.retweet_count);
    items.push('</div>');
  el.innerHTML = items.join('');
}