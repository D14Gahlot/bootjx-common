const isToday = (someDate) => {
  const today = new Date()
  return someDate.getDate() == today.getDate() &&
    someDate.getMonth() == today.getMonth() &&
    someDate.getFullYear() == today.getFullYear()
}
function formatTime(timestamp,gap){
	const NOW = new Date();
	const DATE = new Date(timestamp);
	const DIFF = moment(NOW).diff(DATE, 'day');
	
	if(DIFF < 1 && isToday(DATE)){
		return moment(DATE).format('h:mm a');
	} else if(DIFF < 3 ){
		return moment(DATE).format('hh:mm a ddd');
	} else if(DIFF < 7 ){
		return moment(DATE).format('h:mm a ddd');
	} else {
		return moment(DATE).format('h:mm a DD MMM YY');
	}
}

function formatMessage(html_str) {
    return html_str.replace(/(?:\r\n|\r|\n)/g, '<br/>').trim();
}

function escape_HTML(html_str) {
    return $('<div>').html(html_str).text();
}

function getThumb(mediaUrl){
	var m = mediaUrl.match(/(.+)\/(res.cloudinary.com)\/([a-zA-Z0-9-_]+)\/([a-zA-Z0-9]+)\/(upload)\/([a-zA-Z0-9,_-]+)\/(.*)/);
	if(m && m.length){
		m[6] = "w_100,h_100";
		return m.slice(1).join("/");
	}
}

function scrollToBottom(){
	$('.msg_card_body')[0].scrollTop =  $('.msg_card_body')[0].scrollHeight
}

function sendMessage(data){
	return $.ajax({
	      type: 'POST',
	      dataType: "json",
	      contentType: 'application/json',
	      url: window.CONST.CONTEXT + "/api/sessions/message/send",
	      data: JSON.stringify(data),
	});
}

function postQuery(url,data){
	return $.ajax({
	      type: 'POST',
	      //dataType: "json",
	      //contentType: 'application/json',
	      url: window.CONST.CONTEXT + url,
	      data: data,
	});
}

//contacts
var WhatsApp = function (app) {
  function Contact(name, img, online, o) {
    this.id = contactList.length;
    this.name = name;
    this.img = img;
    this.online = online;
    this.messages = new Array();
    this.newmsg = 0;
    this.groups = new Array();
    this.o = o;
    this.contactId = this.o ? this.o.contactId : this.id;

    contactList.push(this);
  }

  Contact.prototype.addMessage = function (msg) {
	for(var i in  this.messages){
		if(this.messages[i].m && (this.messages[i].m.messageId == msg.m.messageId)){
			this.messages[i].m = msg.m;
			return false;
		}
	}  
    this.messages.push(msg);
    return true;
  };

  Contact.prototype.addGroup = function (group) {
    this.groups.push(group);
  };

  appContacts = Contact;
  return appContacts;
}(WhatsApp || {});

//groups
var WhatsApp = function (app) {
  function Group(name, img) {
    this.id = contactList.length;
    this.name = name;
    this.img = img;
    this.members = new Array();
    this.messages = new Array();
    this.newmsg = 0;

    contactList.push(this);
  }

  Group.prototype.addMember = function (contact) {
    this.members.push(contact);
  };

  Group.prototype.addMessage = function (msg) {
    this.messages.push(msg);
  };

  appGroups = Group;
  return appGroups;

}(WhatsApp || {});

//messages
var WhatsApp = function (app) {
  function Message(text, name, time, type,m) {
    this.text = text;
    this.name = name || (m || {}).name;
    this.time = time;
    this.type = type;
    //this.group = group;
    this.m = m || {};
    this.localId = new Date().getTime();
  }

  appMessages = Message;
  return appMessages;
}(WhatsApp || {});

//subject
/**
 * Created by oflox on 26.09.2020.
 */
var WhatsApp = function (app) {

  function Subject() {
    this.observers = [];
  };

  Subject.prototype.subscribe = function (item) {
    this.observers.push(item);
  };

  Subject.prototype.unsubscribeAll = function () {
    this.observers.length = 0;
  };

  Subject.prototype.notifyObservers = function () {
    this.observers.forEach(elem => {elem.notify();});
  };

  app.Subject = Subject;
  return app;

}(WhatsApp || {});

//model
/**
* Created by oflox on 26.09.2020.
*/
var currentChat;
var contactList = new Array();
var contactListNew = new Array();
var flags = {
	mediaSelection : false	
}

var WhatsApp = function ToDoModel(app) {
  var subject = new app.Subject();

  var Model = {
	hisLastMessage : null,
    start: function () {
    	//https://s3-us-west-2.amazonaws.com/s.cdpn.io/1089577/contacts2.json
      $.getJSON("/agent/api/sessions/assigned.json", function (data) {
        for (var i = 0; i < data.results.length; i++) {
        	app.Model._addChat(data.results[i]);
        }
        subject.notifyObservers();
        
        if(data.meta.isOnline){
      	  $(".online-toggle").addClass("toggle-active");
        } else {
      	  $(".online-toggle").removeClass("toggle-active");
        }
        $("html").removeClass("loading");
       // $(".fullloader.bar").remove();
      });
      
      $.getJSON("/agent/gallery/map/media_reply", function (data) {
          $(".media_card_body-bubbles").append(quikr.tmpl("temp_media_option",{
        	  list : data
           }));
          subject.notifyObservers();
       });
      
    },
    _addChat : function(e){
       	e.online = formatTime(e.lastInComingStamp);
    	e.img = (e.profilePic || "/agent/assets/images/profile.png");
        var contact = new appContacts(e.name, e.img, e.online, e);
        contact.contactType = e.contactType;
        contact.sessionId = e.sessionId;
        for (var j = 0; j < e.messages.length; j++) {
          var m = e.messages[j];
          m.time = m.timestamp;
          var message = new appMessages(m.text, m.name, m.time, m.type,m);
          contact.addMessage(message);
        }
        contactListNew.push(contact);
        return contact;
    },
    addOneChat : function(e){
    	for(var i in contactList){
    		if(contactList[i].sessionId == e.sessionId){
    			//e = null;
    		}
    	}
    	if(e){
    		app.Model._addChat(e);
    	}
    	subject.notifyObservers();
    },
    senMessage: function (text) {
    	var msg =  new appMessages(text, "", new Date().getTime(), true);
    	var THAT = this;
    	var template = flags.mediaSelection ?	$("[name=media]:checked").val() : null;
        sendMessage({
      	  message : msg.text,
      	  template : template,
      	  sessionId : currentChat.sessionId
        }).done(function(resp){
        	if(resp.results && resp.results[0]){
            	msg.m = resp.results[0];
            	THAT._sendMessage(msg,"local");
        	} else {
        		console.log("senMessage:Error",resp);
        	}
        });
        msg.m.template = template;
        this._sendMessage(msg,"sent");
    },
    _sendMessage : function(msg,stts){
    	$(".chat-bubble[data-local-id="+msg.localId+"]").remove();
    	if(msg.m){
    		$(".chat-bubble[data-message-id='"+msg.m.messageId+"']").remove();   		
    	}
        WhatsApp.View.printMessage(msg);
        currentChat.addMessage(msg);
        $(".input-message").val("");
        subject.notifyObservers();
        $("#" + currentChat.contactId).addClass("active-contact active");
        scrollToBottom();
    },
    writeMessage: function () {
       return this.senMessage($(".input-message").val());
    },
    readMessage: function (m) {
      for(var id in contactList){
    	  if(m.sessionId == contactList[id].sessionId){
    		  $(".chat-bubble[data-message-id='"+m.messageId+"']").remove();
    		  this.getMessage(m.text,id,m.name || contactList[id].name, m);
    		  break;
    	  }
      }
    },
    getMessage: function (text, id, name, m) {
      var msg = new appMessages(text, name, new Date().getTime(), m.type,m);
      contactList[id].addMessage(msg);
      contactList[id].online = formatTime(new Date().getTime());
      WhatsApp.View.notifyNewMessage(id,msg);
    },
    register: function (...args) {
      subject.unsubscribeAll();
      args.forEach(elem => {
        subject.subscribe(elem);
      });
    } };

  app.Model = Model;
  return app;

}(WhatsApp || {});

//view
/**
 * Created by oflox on 02.01.2017.
 */
var first = true;

PP_ICONS = {
 "FACEBOOK" : "fa-facebook", "WEBSITE" : "fa-chrome", "TELEGRAM" : "fa-telegram",  "TWITTER" : "fa-twitter"
}

var WhatsApp = function ToDoView(app) {
  var view = {
    printContact: function (c) {
      var $existing = $("#" + c.contactId);
      var lastmsg = c.messages[c.messages.length - 1];

      var $html = $(quikr.tmpl("temp_contact",{
        	c : c,lastmsg : (lastmsg),
        	contactIcon : PP_ICONS[c.contactType]
       }));

      var that = c;
      if( $existing &&  $existing.length>0){
    	  $existing.replaceWith($html);
    	  if(that.o.assigned){
    		  if(!$html.closest(".contact-list").length){
    			  $(".contact-list").prepend($html); 
    		  }
        	  if(currentChat == c){
        		  $html.addClass("active-contact active");
        	  }
          }
    	  console.log("replaceExisting",that.o)
      } else {
          if(that.o.assigned){
        	  $(".contact-list").prepend($html);    	  
          } else {
        	  $(".contact-list-unassigned").prepend($html);
          }
          console.log("appendNew")
      }
      console.log("printContact")
      WhatsApp.Ctrl.addClick($html, that);
    },
    printChat: function (cg) {
      console.log("printChat");
      WhatsApp.View.closeContactInformation();
      $(".chat-head img").attr("src", cg.img);
      $(".user_info .user_name").text(cg.name);
	    $(".user_info .user_text").text("online @ " + cg.online);
	    // Nachrichten konfigurieren
	    $(".chat-bubble,.msg_card_body-logo").remove();
	    for (var i = 0; i < cg.messages.length; i++) {
	      WhatsApp.View.printMessage(cg.messages[i]);
	    }
	    currentChat = cg;
	    WhatsApp.View.showContactInformation();
	    WhatsApp.View.printSmartTags();
	    scrollToBottom();
    },
    printSmartTags(){
    	console.log(app.hisLastMessage);
    	//return;
        if(!app.hisLastMessage || app.hisLastMessage.type){
      	  return;
        }
    	if(!app.hisLastMessage.m || !app.hisLastMessage.m.tags){
    		return;
    	}
    	var $tags = $(".msg_card_body-panel-tags");
    	
    	if($tags.data().mid == app.hisLastMessage.m.messageId){
    		return;
    	}
    	$tags.data("mid",app.hisLastMessage.m.messageId);
    	$tags.empty();

    	if(!app.hisLastMessage.m || !app.hisLastMessage.m.tags || !app.hisLastMessage.m.tags.categories){
    		return;
    	}
        $.getJSON("/agent/category/map/smart_reply.json?value="+
        		(app.hisLastMessage.m.tags || {categories : []}).categories.join(",")
        		, function (data) {
        	for(var i in data){
        		$tags.append('<span class="msg_cotainer_smart">  ' + data[i].id.subject + ' </span>')
        	}
        	scrollToBottom();
        });
    },
    printMessage: function (gc) {
      $(".msg_card_body .msg_card_body-bubbles").append(quikr.tmpl(gc.type ? "temp_message_me" : "temp_message_you",{
      	gc : gc
      }));
      if(!gc.type){
    	  app.hisLastMessage = gc;
      }
    },
    showContactInformation: function () {
    	
    	$(".card_contact_profile").html(quikr.tmpl("temp_contact_profile",{
      	  c : currentChat
        }));
    	
      return;
      $(".chat-head i").hide();
      $(".information").css("display", "flex");
      $("#close-contact-information").show();
        $(".information").append("<img src='" + currentChat.img + "'><div><h1>Name:</h1><p>" + currentChat.name + "</p></div><div id='listGroups'><h1>Gemeinsame Gruppen:</h1></div>");
        for (var i = 0; i < currentChat.groups.length; i++) {
          html = $("<div class='listGroups'><img src='" + currentChat.groups[i].img + "'><p>" + currentChat.groups[i].name + "</p></div>");
          $("#listGroups").append(html);
          $(html).click(function (e) {
            for (var i = 0; i < contactList.length; i++) {
              if ($(currentChat).find("p").text() == contactList[i].name) {
                $(".active-contact").removeClass("active-contact");
                $("#" + contactList[i].contactId).addClass("active-contact");
                WhatsApp.Groups.printChat(contactList[i]);
              }
            }
          });
        }
    },
    closeContactInformation: function () {
      $(".chat-head i").show();
      $("#close-contact-information").hide();
      $(".information >").remove();
      $(".information").hide();
    },
    
    notifyNewMessage :  function(id,msg){
    	console.log("msg",msg);
        if (contactList[id] == currentChat) {
            WhatsApp.View.printMessage(msg);
            WhatsApp.View.printContact(contactList[id]);
            WhatsApp.View.printSmartTags();
            scrollToBottom();
        } else {
        	if(msg.type == false)
        		contactList[id].newmsg++;
            WhatsApp.View.printContact(contactList[id]);
        }
    },
    //Observer-Methode
    notify: function () {
      if (first) {
        first = false;
        for (var i = 0; i < contactListNew.length; i++) {
          WhatsApp.View.printContact(contactListNew[i]);
          currentChat = contactListNew[i];
        }
        first = false;
      } else
      {
    	  if(contactListNew.length>0){
	        for (var i = 0; i < contactListNew.length; i++) {
	            WhatsApp.View.printContact(contactListNew[i]);
	            //currentChat = contactList[i];
	          }  
    	  } else if(currentChat){
    		 WhatsApp.View.printContact(currentChat);
    	  }
      }
      WhatsApp.View.printSmartTags();
    } };


  app.View = view;
  return app;

}(WhatsApp);

//controller
/**
 * Created by oflox on 26.09.2020.
 */
var start = true;

var WhatsApp = function ToDoCtrl(app) {

  $(document).ready(function () {
    app.Model.start();
  });

  var Ctrl = {
    addClick: function (html, that) {
      $(html).click(function (e) {
   	   	$(".active-contact,.active").removeClass("active-contact").removeClass("active");
        $(this).addClass("active-contact active");
        $(this).removeClass("new-message-contact");
        $("#nm" + that.contactId).remove();
        that.newmsg = 0;
        WhatsApp.View.printChat(that);
	      if(currentChat){
	    	  $(".my-input-section").removeClass("fade")
	    	  $(".type_msg.input-message").removeAttr("disabled")
	      } else {
	    	  $(".my-input-section").addClass("fade")
	    	  $(".type_msg.input-message").attr("disabled")
	      }
      });
    },
    toggelMedia :  function(show){
    	if(show === undefined){
    		show = !flags.mediaSelection;
    	}
    	if(show){
           	$(".media_card_body").removeClass("hidden");
        	$(".msg_card_body").addClass("hidden");
    	} else {
           	$(".media_card_body").addClass("hidden");
        	$(".msg_card_body").removeClass("hidden");
        	scrollToBottom();
    	}
    	flags.mediaSelection = !$(".media_card_body").hasClass("hidden")
    },
    //Observer-Methode
    notify: function () {
      if (start) {
        $(".input-message").keyup(function (ev) {
          if (currentChat && (ev.which == 13 || ev.keyCode == 13) && !ev.shiftKey) {
            app.Model.writeMessage();
            Ctrl.toggelMedia(false);
          }
        });
        $(".send_btn").click(function (ev) {
            app.Model.writeMessage();
            Ctrl.toggelMedia(false);
        });
        
        $("#show-contact-information").on("click", function () {
          WhatsApp.View.showContactInformation();
        });

        $("#close-contact-information").on("click", function () {
          WhatsApp.View.closeContactInformation();
        });
        $(".online-toggle").on("click", function () {
            $(this).toggleClass("toggle-active")
            postQuery("/auth/online/status",{status : $(this).hasClass("toggle-active")})
          });
        $('.menu_btn').click(function(){
        	//$('.menu_btn_menu').toggle();
        	document.getElementById("mySidebar").style.display = "block";
        });
        
        $('.menu_btn_close').click(function(){
        	//$('.menu_btn_menu').toggle();
        	document.getElementById("mySidebar").style.display = "none";
        });
        $('span[theme]').click(function(){
        	document.cookie=("theme="+$(this).attr("theme"));
        	window.location.reload()
        });
        
        $("body").on("click",".msg_cotainer_smart", function(event){
        	console.log($(this).text());
        	app.Model.senMessage($(this).text());
        });
        
        $('.attach_btn').click(function(){
        	if(currentChat)
        		Ctrl.toggelMedia();
        });
        
        start = false;
      }
      
    } };

  app.Ctrl = Ctrl;
  return app;

}(WhatsApp);

WhatsApp.Model.register(WhatsApp.View, WhatsApp.Ctrl);
//# sourceURL=pen.js
	tunnelClient.config({
		user : window.CONST.APP_USER,
		context : "/agent"
	}).instance().on("/agent/onmessage", function(testresponse){
		console.log("/agent/onmessage", testresponse);
		WhatsApp.Model.readMessage(testresponse);
	}).on("/dept/onassign-"+window.CONST.APP_DEPT, function(testresponse){
		console.log("/dept/onassign-"+window.CONST.APP_DEPT, testresponse);
		if(testresponse.assignedToAgent == window.CONST.APP_USER){
			WhatsApp.Model.addOneChat(testresponse);
    	} else {
    		var contactList = new Array();
    		var contactListNew = new Array();
    		
        	for(var i in contactList){
        		if(contactList[i].sessionId == testresponse.sessionId){
        			contactList[i] = null;
        			delete contactList[i];
        		}
        	}
        	for(var i in contactListNew){
        		if(contactListNew[i].sessionId == testresponse.sessionId){
        			contactListNew[i] = null;
        			delete contactListNew[i];
        		}
        	}
        	$("#" + testresponse.contactId).remove();
    	}
	}).on("/dept/onassign-__DEPT__", function(testresponse){
		console.log("/dept/onassign-__DEPT__", testresponse);
		WhatsApp.Model.addOneChat(testresponse);
	})
	