let VALID_MD_PWD = "b13a15ce0828f4e3ddafe01072aac223";
let SUCCES_NOTIF_DELAY = 12500;

async function initSub() {

  const registration = await navigator.serviceWorker.register('/sw.js');
	
  firebase.initializeApp({
    messagingSenderId: '770916390748'
  });
  const messaging = firebase.messaging();
  
  messaging.useServiceWorker(registration);	
  
  try {
    await messaging.requestPermission();
    $(function() {
    		$(".subscribe-notifications-btn")
    		.attr("disabled", "disabled")
    		.html("Notifications enabled for browser");
    })
  } catch (e) {
    console.log('Unable to get permission', e);
    return;
  }

  navigator.serviceWorker.addEventListener('message', event => {
	  console.log("message=====",event);
    if (event.data === 'newData') {
      console.log("new Data");
    }
  });

  const currentToken = await messaging.getToken();
   fetch('/postman/subscribe/kwt-all_web?token='+currentToken, { method: 'post'});
  

  messaging.onTokenRefresh(async () => {
    console.log('token refreshed');
    const newToken = await messaging.getToken();
    fetch('/postman/subscribe/kwt-all_web?token='+newToken, { method: 'post'});
  });
  
  messaging.onMessage(function(payload) {
	console.log('Message received. ', payload);
  });
}

function objectToFormParams(obj){
	var form_data = new FormData();

	for ( var key in obj ) {
	    form_data.append(key, obj[key]);
	}
	
	return form_data;
}

function sendNotification(title, message){
	title = title || "Notification";
	message = message || "Default message"
	fetch('/postman/push/notify', {
		method: 'post',
		headers: {
	        'Accept': 'application/json, text/plain, */*',
	        'Content-Type': 'application/json'
	    },
		body: JSON.stringify({
		  "image": "string",
		  "lang": "AB",
		  "lines": [
		    message
		  ],
		  "message": message,
		  "model": {},
		  "result": {},
		  "subject": title,
		  "template": "CONTACT_US",
		  "to": [
		    "/topics/kwt-all"
		  ]
		})
	})
}

if(window.sessionStorage.getItem("SES_ID") === VALID_MD_PWD){
	$(function(){
		$(".login").hide();
		init();
		$(".app").show();
	})
} else {
	$(function(){
		$(".login").show();
	})
}

function onLoginClick(){
	var username = $(".username").val();
	var pwd = $(".password").val();
	var mdPwd = md5(username + "#" + pwd);
	if(mdPwd === VALID_MD_PWD){
		window.sessionStorage.setItem("SES_ID", mdPwd);
		$('.login').hide();
		init();
		$('.app').show();		
	} else {
		alert("invalid username or password");
	}
}

$(function(){
	$('.login-btn').on('click', onLoginClick);
	$('.password').on('keyup', function(e){
		if(e.which === 13){
			onLoginClick();
		}
	})
	$('.username').on('keyup', function(e){
		if(e.which === 13){
			onLoginClick();
		}
	})
})

function init(){
	
	$( function() {
		fetch("/postman/list/nations",{
			method: 'post'
		}).then(function(resp){
			resp.json().then(function(countries){
				var countryOpts = countries.map(function(country){
					return {
						id: country,
						text: country
					}
				});
				
				$('.country-select').select2({data: countryOpts}).val("ALL")
				.on('change', function(country){
					var dir = country.currentTarget.value === "EGYPT" ? "rtl" : "ltr"
					$("textarea.notif-msg").attr("dir", dir);
					$("input.notif-title").attr("dir", dir);
				})
				.trigger("change");
			})
		})
		
		
		fetch("/postman/list/tenant",{
			method: 'post'
		}).then(function(resp){
			resp.json().then(function(tenants){
				var tenantOpts = tenants.map(function(tenant){
					return {
						id: tenant,
						text: tenant
					}
				});
				$('.env-select').select2({
					data: [{
						id: "KWT",
						text: "KWT"
					}, {
						id:"BHR",
						text: "BHR"
					}]
				});
			})
		})
		
		$('.send-notification-btn').on('click', function() {
			var nationality = $(".country-select").val();
			var tenant = $(".env-select").val();
			var title = $(".notif-title").val() || "Default Title";
			$(".notif-title").val("");
			var message = $(".notif-msg").val() || "Default Message";
			$(".notif-msg").val("");
			var reqObj = {
				tenant: tenant, 
				nationality: nationality, 
				title: title,
				message: message
			};
			$.ajax({
			    url         : '/postman/notify/nationality',
			    data        : reqObj,
			    type: 'POST'
			}).done(function(data){
				$(".toast")
				.html("Notification with the title: '" + title + "' was successfully sent")
				.fadeIn()
				.delay(SUCCES_NOTIF_DELAY)
				.fadeOut('slow');
			});
//			
//			fetch(`/postman/notify/nationality?tenant=${tenant}&nationality=${nationality}&title=${title}&message=${message}`, {
//				method: 'post',
//				headers: {
//			        'Accept': 'application/json, text/plain, */*',
//			        'Content-Type': 'application/json'
//			    }
//			}).then(function(resp){
//				if(resp.status === 200){
//					$(".toast")
//					.html("Notification with the title: '" + title + "' was successfully sent")
//					.fadeIn()
//					.delay(SUCCES_NOTIF_DELAY)
//					.fadeOut('slow');
//				}
//			})
		})
		
		$(".send-notifications-kwt-all").on('click', function(){
			var title = $(".notif-title").val() || "Default Title";
			var message = $(".notif-msg").val() || "Default Message";
			sendNotification(title, message);
		})
	});
	
}
