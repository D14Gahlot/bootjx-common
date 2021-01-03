var tunnelClient = (function(win) {
	var config = {
		context : "/offsite",
		user : "guest",
		token : guid()
	};
	var $connectd = null, $dfd = null;
	var sessionToken = null;
	var stompClient = null;
	var pong = false;
	function connect() {
		$dfd = $dfd || jQuery.Deferred();
		var socket = new SockJS(config.context + '/stomp-tunnel');
		stompClient = Stomp.over(socket);
		stompClient.connect({
			user : config.user,
			token : config.token
		}, function(frame) {
			console.log('Connected: ', frame);
			stompClient.subscribe("/app/stomp/tunnel/meta" , function(greeting) {
				var resp = JSON.parse(greeting.body);
				console.log("@SubscribeMapping",resp);
				sessionToken = resp["x-session-uid"];
				$dfd.resolve(frame);
			});
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
		if (!this.$connectd) {
			this.$connectd = connect();
		}
		return this.$connectd;
	}
	
	function TunnelClient (){
		this.ids = [];
	}
	TunnelClient.prototype = {
		on : function subscribe(topic, fun) {
			var THAT = this;
			onConnect().then(function() {
				THAT.ids.push(stompClient.subscribe("/topic" + topic, function(greeting) {
						fun(JSON.parse(greeting.body).data, topic, greeting);
				}));
			});
			onConnect().then(function() {
				THAT.ids.push(stompClient.subscribe("/queue/" + sessionToken + topic, function(greeting) {
						fun(JSON.parse(greeting.body).data, topic, greeting);
				}));
			});
			return this;
		},	
		send : function send(topic, msg) {
			onConnect().then(function() {
				stompClient.send("/app" + topic, {}, JSON.stringify(msg));
			});
			return this;
		},
		ping : function send(topic, msg) {
			//if(!pong){
				this.on("/pong", function(pong,pong1,pong2,pong3){
					console.log("PONG : ",pong,pong1,pong2,pong3)
				});
				pong = true;
			//}
			this.send("/ping",{ ping : "Hello"});
			return this;
		},
		off : function(){
			console.log(this.ids)
			for(var i in this.ids){
				this.ids[i].unsubscribe();
			}
		}
	}
	
	
	return {
		config : function (_config){
			for(var key in _config){
				config[key] = _config[key]
			}
			return this;
		},
		connect : function() {
			onConnect();
			return this;
		},
		instance :  function(){
			return new TunnelClient();
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