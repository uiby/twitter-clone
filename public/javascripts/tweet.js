$(function() {
	favorite();
	retweet();
	tweetBox();
});

var favorite = function() {
	$('.fav_button').on('click', function(event) {
		event.preventDefault();
	var favoriteBack = {
		success: onSuccess,
		error: onError
	}

	jsRoutes.controllers.TweetController.favorite($(this).val().toString()).ajax(favoriteBack);
	});
};

var retweet = function() {
	$('.retweet_button').on('click', function(event) {
		event.preventDefault();
  	var favoriteBack = {
	  	success: onSuccess,
		  error: onError
	  }

  	jsRoutes.controllers.TweetController.retweet($(this).val().toString()).ajax(favoriteBack);
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
		var tweet_id = $(this).attr("id");
		console.log(tweet_id);
		jsRoutes.controllers.TweetController.getTweet(tweet_id).ajax({
			dataType: 'json',
			success: function(jsonData, textStatus, jqXHR) {
			  var date = JSON.stringify(jsonData);
			  console.log(date+""+jsonData.main_tweet.user_name);
			  var items = new Array();
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
			  //リプライフォーム生成

			  $("#overlay-main-tweet").children().remove();
			  $("#overlay-main-tweet").append(items.join(''));
			}
		});
		//alert("aldkjalfj");
	});

	$('#close').on('click', function(event) {
		$("#overlay").fadeOut();
	});
}
