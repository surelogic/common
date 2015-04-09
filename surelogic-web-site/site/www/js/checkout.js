/*
We want to hide the email boxes unless someone toggles them.  We want to verify that the two boxes report the same thing.
*/
$(document).ready(function() {
    if($(':input[name=hasEmail][value=no]').get(0).checked) {
	$('#alt-email').hide();
    }
    $('#buy-flashlight :input[name=hasEmail][value=yes]').change(function() {
	$('#alt-email').show();
    });
    $('#buy-flashlight :input[name=hasEmail][value=no]').change(function() {
	$('#alt-email').hide();
	
    });
    $('#buy-flashlight').submit(function() {
	$('.error-message').remove();
	if($(':input[name=hasEmail][value=no]').get(0).checked) {
	    $(':input[name=os1]').val('');
	    $(':input[name=os2]').val('');
	    return true;
	} else {
	    // Check emails
	    var email = $('#buy-flashlight :input[name=os1]').val().trim();
	    var emailAgain = $('#buy-flashlight :input[name=os2]').val().trim();
	    var errorMessage;
	    if(email == emailAgain) {
		if(email.match(/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i)) {
		    return true;
		} else {
		    errorMessage = 'The address you provided, ' + email + ',  does not appear to be a valid email address.';
		}
	    } else {
		errorMessage = 'Please enter the same email address in both fields to ensure your license is delivered to the proper address.';
	    }
	    $('#alt-email').append('<span class="error-message">' + errorMessage + ' </span>');
	    return false;
	}
    });
});
