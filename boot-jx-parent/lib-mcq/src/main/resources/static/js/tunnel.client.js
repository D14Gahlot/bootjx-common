var tunnelClient = (function(win) {
	var config = {
		context : "/offsite",
		user : "guest",
		token : guid(),
		reconnect : false
	};
	var $connectd = null, $dfd = null;
	var sessionToken = null;
	var tenantToken = null;
	var stompClient = null;
	var tagIds = [];
	var pong = false;
	var TUNNEL_DEBUG = false;
	var linkId = 0;
	var instances = [];
	
	win.__onsocket_connect__ = function(frame){
		console.log("__onsocket_connect__",frame);
	}
	win.__onsocket_disconnect__ = function(error, reconnect){
		if(config.reconnect){
			setTimeout(reconnect, 5000);
			console.log('STOMP: Reconecting in 5 seconds');
		} else {
			console.log('STOMP: Auto reconnect not anebaled');
		}
	}
	
	if(win.sessionStorage && win.sessionStorage.getItem)
		TUNNEL_DEBUG = !!win.sessionStorage.getItem("TUNNEL_DEBUG");
	
	function connect() {
		$dfd = $dfd || jQuery.Deferred();
		var socket = new SockJS(config.context + '/stomp-tunnel',{
			debug : TUNNEL_DEBUG,
			reconnect_delay: 2500
		});
		stompClient = Stomp.over(socket);
		stompClient.heartbeat.outgoing = 0;
		stompClient.heartbeat.incoming = 0;
		stompClient.reconnect_delay = 1000;
		if(!TUNNEL_DEBUG){
			stompClient.debug = () => {};
		}
		stompClient.connect({
			user : config.user,
			token : config.token
		}, function(frame) {
			console.log('Connected: ', frame);
			stompClient.subscribe("/app/stomp/tunnel/meta" , function(greeting) {
				var resp = JSON.parse(greeting.body);
				console.log("@SubscribeMapping",resp);
				sessionToken = resp["x-session-uid"];
				tenantToken = resp["x-tenant-token"];
				tagIds = resp["tags"] || [];
				$dfd.resolve(linkId++);
				for(var i in instances){
					instances[i].reconnect();
				}
				if(typeof win.__onsocket_connect__ == 'function'){
					win.__onsocket_connect__(frame);
				}
			});
		}, function(error){
			let details = {error : error,type : "ERROR"};
			console.error("__onsocket_disconnect__",details);
			$connectd = null;
			if(typeof win.__onsocket_disconnect__ == 'function'){
				win.__onsocket_disconnect__(details, function(){
					$dfd = null;
				   	connect();	
				});
			}
		}, function(CloseEvent){
			let details = {closeEvent : CloseEvent, type : "CLOSED"};
			console.error("__onsocket_close__",details);
			$connectd = null;
			if(typeof win.__onsocket_disconnect__ == 'function'){
				win.__onsocket_disconnect__(details, function(){
					$dfd = null;
				   	connect();	
				});
			}
		});
		return $dfd.promise();
	}
	function guid() {
		function s4() {
			return Math.floor((1 + Math.random()) * 0x10000).toString(16)
					.substring(1);
		}
		return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4()
				+ s4() + s4();
	}
	function onConnect() {
		if (!$connectd) {
			$connectd = connect();
		}
		return $connectd;
	}
	
	function TunnelClient (){
		this.ids = [];
	}
	TunnelClient.prototype = {
		on : function subscribe(topic, fun) {
			var THAT = this;
			let sub = function(greeting) {
				fun(JSON.parse(greeting.body).data, topic, greeting);
			};
			onConnect().then(function(linkId) {
				let headline = "/topic/" + tenantToken + topic;
				THAT.ids.push({ 
					linkId : linkId,
					topic : topic, fun : fun,
					headline : headline,sub : sub,
					unsub : stompClient.subscribe(headline,sub)
				});
			});
			onConnect().then(function(linkId) {
				let headline = "/queue/" + sessionToken + topic;
				THAT.ids.push({ 
					linkId : linkId,
					topic : topic,fun : fun,
					headline : headline, sub : sub,
					unsub : stompClient.subscribe(headline, sub)
				});
			});			
			onConnect().then(function() {
				tagIds.map(function(tagId){
					var headline = "/tag/" + (tenantToken + "/" + tagId) + topic;
					console.log("@sub - ",headline)
					THAT.ids.push({ 
						linkId : linkId,
						topic : topic, fun : fun,
						headline : headline, sub : sub,
						unsub : stompClient.subscribe(headline, sub)
					});
				});
			});
	
			return this;
		},	
		send : function send(topic, msg) {
			onConnect().then(function() {
				stompClient.send("/app" + topic, {}, JSON.stringify(msg));
			});
			return this;
		},
		off : function(){
			console.log(this.ids)
			for(var i in this.ids){
				this.ids[i].inactive=true;
				this.ids[i].unsub.unsubscribe();
			}
		},
		reconnect : function(){
			var THAT = this;
			onConnect().then(function(linkId){
				for(var i in THAT.ids){
					if(!THAT.ids[i].inactive){
						THAT.ids[i].unsub = stompClient.subscribe(THAT.ids[i].headline, THAT.ids[i].sub);
					}
				}
			});
		},
		ping : function send(topic, msg) {
			//if(!pong){
				this.on("/pong", function(pong,pong1,pong2,pong3){
					console.log("PONG : ",pong,pong1,pong2,pong3)
				});
				pong = true;
			//}
			this.send("/ping",{ ping : "Hello"});
            jQuery.getJSON(config.context+"/stomp/tunnel/ping").done(function(resp){
                console.log("/stomp/tunnel/ping response : ",resp)
            });
			return this;
		},
        pong :  function(){
            this.on("/stomp/tunnel/pong" , function(greeting) {
                console.log("PONG:",greeting);
            });
			return this;
        }
	}
	

	return {
		debug : false,
		global : null, 
		config : function (_config){
			for(var key in _config){
				config[key] = _config[key]
			}
			if(!this.global){
				this.global = this.instance();
				this.global.pong();
			}
			return this;
		},
		connect : function() {
			onConnect();
			return this;
		},
		instance :  function(){
			let inst = new TunnelClient();
			instances.push(inst)
			return inst;
		},
		disconnect : function disconnect() {
			if (stompClient !== null) {
				stompClient.disconnect();
			}
			setConnected(false);
			console.log("Disconnected");
			return this;
		}
	};
})(this);