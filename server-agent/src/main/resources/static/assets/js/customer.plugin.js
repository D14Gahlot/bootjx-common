(function () {
	var maxHeight = 400;
	var minHeight = 38;
	var maxWidth = 300;
    var div = document.createElement("div");
    document.getElementsByTagName('body')[0].appendChild(div);
    div.outerHTML = "<div id='botDiv' style='height: "+minHeight+"px; position: fixed; bottom: 0; right:0; z-index: 1000; background-color: #428bca'>" +
    					"<div id='botTitleBar' style='height: "+minHeight+"px; width: "+maxWidth+"px; position:fixed; cursor: pointer;'></div>" +
    					"<iframe style='border-width: 0px;' width='"+maxWidth+"px' height='"+maxHeight+"px' src='http://local-kwt.amxremit.com:8083/agent/pub/customer/plugin?page=plugin'></iframe>" +
    				"</div>"; 
    document.querySelector('body').addEventListener('click', function (e) {
        e.target.matches = e.target.matches || e.target.msMatchesSelector;
        if (e.target.matches('#botTitleBar')) { 
            var botDiv = document.querySelector('#botDiv'); 
            botDiv.style.height = botDiv.style.height == (maxHeight + 'px') ? (minHeight + 'px') : (maxHeight + 'px');
        };
    });
}());