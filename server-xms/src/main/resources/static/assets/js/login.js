$( document ).ready(function() {
    var w = window.innerWidth;

    if(w > 767){
        $('#menu-jk').scrollToFixed();
    }else{
        $('#menu-jk').scrollToFixed();
    }
})


$( document ).ready(function() {

    $('.owl-carousel').owlCarousel({
        loop:true,
        margin:0,
        nav:true,
        autoplay: true,
        dots: true,
        autoplayTimeout: 5000,
        navText:['<i class="fa fa-angle-left"></i>', '<i class="fa fa-angle-right"></i>'],
        responsive:{
            0:{
                items:1
            },
            600:{
                items:1
            },
            1000:{
                items:1
            }
        }
    })

    $("#btn-login").click(function(){
 	   	 return $.post(window.CONST.CONTEXT + "/auth/login/submit",{
 	   		 username :   $("#login-username").val(),
			 password :  $("#login-password").val(),
		 }).done(function(rsp){
			 location.href = window.CONST.CONTEXT + "/app/home"
		 });
    });

});