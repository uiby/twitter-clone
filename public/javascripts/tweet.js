$(function() {
	favorite();
	retweet();
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
