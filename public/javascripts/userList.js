$(function() {
	follow();
});

var follow = function() {
	$('.follow_button').on('click', function(event) {
		event.preventDefault();
	var back = {
		success: onSuccess,
		error: onError
	}

	jsRoutes.controllers.UserController.follow($(this).val().toString()).ajax(back);
	});
};

var  onSuccess = function(data) {
    alert("success"+data);
}

var onError = function(error) {
    alert("error"+error);
}
