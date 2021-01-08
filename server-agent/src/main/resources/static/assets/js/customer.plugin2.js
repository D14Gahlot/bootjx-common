(function () {
	var maxHeight = 500;
	var minHeight = 80;
	var maxWidth = 400;
	var minWidth = 80;
    var div = document.createElement("div");
    document.getElementsByTagName('body')[0].appendChild(div);
    div.outerHTML = "<div id='botDiv' style='height: "+minHeight+"px; width: "+minWidth+"px; position: fixed; bottom: 10px; right:10px; z-index: 1000; background-color: transparent; overflow:hidden;border-radius: 15px!important;padding:0px!important; margin:0px!important ;' >" +
    					"<div id='botTitleBar' style='height: "+minHeight+"px; width: "+minWidth+"px; position:fixed; cursor: pointer;'></div>" +
    					"<div id='botFrame' style='margin: 0px 0px 0px -5px;'><iframe style='border-width: 0px; margin: 0px 0px 0px 0px; width:"+maxWidth+"px;height:"+maxHeight+"px;' width='"+maxWidth+"px' height='"+maxHeight+"px' src='https://api.lalittanwar.com/agent/pub/customer/plugin2?page=plugin2'></iframe>" +
    				"<div></div>"; 
    document.querySelector('body').addEventListener('click', function (e) {
        e.target.matches = e.target.matches || e.target.msMatchesSelector;
        if (e.target.matches('#botTitleBar')) { 
            var botDiv = document.querySelector('#botDiv'); 
            var botFrame = document.querySelector('#botFrame'); 
            if(botDiv.style.height == (maxHeight + 'px') ){
            	botDiv.style.height = (minHeight + 'px');
            	botDiv.style.width = (minWidth + 'px');
            	botFrame.style.margin = "0px 0px 0px -5px";
            } else {
            	botDiv.style.height = (maxHeight + 'px');
            	botDiv.style.width = (maxWidth + 'px');
            	botFrame.style.margin = "0px 0px 0px 0px";
            }
        };
    });
}());