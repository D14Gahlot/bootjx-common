var MOBILE= location.search.split("number=")[1].split("&")[0];

$(function(){
    $(".heading-compose").click(function() {
      $(".side-two").css({
        "left": "0"
      });
    });

    $(".newMessage-back").click(function() {
      $(".side-two").css({
        "left": "-100%"
      });
    });
    
    function send(e){
        var id = e.target.id;
        //variables for testing, you could have all of the
        //comparisons in the 'if' statement, just using these to
        //make the 'if' statement more clear
        var notEmpty = $("#TEXT").val() != "",
            isEnterKeypress = e.type == "keypress" && e.keyCode == 13,
            isSendClick = e.type == "click" && id == "SEND";

        if( notEmpty && (isEnterKeypress || isSendClick) ) {
        	onSendMsg($("#TEXT").val());
	  	   	 return $.post(window.CONST.CONTEXT + "/dummy/messages",{
				 message :   $("#TEXT").val(), number:MOBILE
			 }).done(function(rsp){
				 $("#TEXT").val("");
			 });
        }
 
    }
    
    $("#SEND").click(send);
    $("#TEXT").keypress(send);
    

    pollMessage();
   
});


function pollMessage(){
	//return
	 $.getJSON(window.CONST.CONTEXT + "/dummy/messages",{number:MOBILE}).done(function(rsp){
		 onRcvMsg((rsp.message || "").replace(/(?:\r\n|\r|\n)/g, '<br>'),rsp.files);
	 }).always(function(rsp){
		 setTimeout(pollMessage,2000);
	 });;
}

function onSendMsg(msg){
	$hr = $('<hr/>');
	$("#botbox").append(
		$('<div class="row"></div>').append(
				$('<div class="col-lg-12">').append(
	    				$('<div class="media">').append(
	    	    				$('<a class="pull-right" href="#">').append(
	    	    					'<img class="media-object img-circle img-chat" src="https://bootdey.com/img/Content/avatar/avatar6.png" alt="">'	
	    	    				)	
	    				).append(
	    					$('<div class="media-body pull-right" >').append(
	    							'<h4 class="media-heading">Me</h4>'	
	    					).append(
	    							'<p>'+msg+'</p>'
	    					)
	    				)
				)
		)
	).append($hr );
	document.getElementById('botbox').scrollTop =  document.getElementById('botbox').scrollHeight

}

function onRcvMsg(msg,files){
	var url = "";
	if(files && files[0] && files[0].url){
		url = '<a href="'+files[0].url+'" target="_blank" >Download</a>'
	}
	$hr = $('<hr/>');
	$("#botbox").append(
		$('<div class="row"></div>').append(
				$('<div class="col-lg-12">').append(
	    				$('<div class="media">').append(
	    	    				$('<a class="pull-left" href="#">').append(
	    	    					'<img class="media-object img-circle img-chat" src="https://bootdey.com/img/Content/avatar/avatar1.png" alt="">'	
	    	    				)	
	    				).append(
	    					$('<div class="media-body">').append(
	    							'<h4 class="media-heading">Alex</h4>'	
	    					).append(
	    							'<p>'+msg+'</p>'
	    					).append(url)
	    				)
				)
		)
	).append($hr )
	 document.getElementById('botbox').scrollTop =  document.getElementById('botbox').scrollHeight
}