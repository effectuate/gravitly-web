$(function() {

$('.upload-form').ajaxForm({
       beforeSend: function (request)
       {
	  request.setRequestHeader("X-Gravitly-Client-Id", "Z8DUz1xIhV");
	  request.setRequestHeader("X-Gravitly-REST-API-Key","9541afe582930637bc70db3dd495f400");
	},
	success: function (responseText,statusText,xhr,$form) {
	  window.location.replace("/admin/upload");
        }

})
});



