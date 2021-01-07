function formatTime(timestamp){
	const NOW = new Date();
	const DATE = new Date(timestamp);
	const DIFF = moment(NOW).diff(DATE, 'day');
	
	if(DIFF < 1 ){
		return moment(DATE).format('h:mm A');
	} else if(DIFF < 3 ){
		return moment(DATE).format('hh:mm A ddd');
	} else if(DIFF < 7 ){
		return moment(DATE).format('h:mm A ddd');
	} else {
		return moment(DATE).format('h:mm A DD/mm/YY');
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

//contacts
var WhatsApp = function (app) {
  function Contact(name, img, online) {
    this.id = contactList.length;
    this.name = name;
    this.img = img;
    this.online = online;
    this.messages = new Array();
    this.newmsg = 0;
    this.groups = new Array();

    contactList.push(this);
  }

  Contact.prototype.addMessage = function (msg) {
    this.messages.push(msg);
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
  function Message(text, name, time, type, group) {
    this.text = text;
    this.name = name,
    this.time = time;
    this.type = type;
    this.group = group;
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

var WhatsApp = function ToDoModel(app) {
  var subject = new app.Subject();

  var Model = {
    start: function () {
    	//https://s3-us-west-2.amazonaws.com/s.cdpn.io/1089577/contacts2.json
    	//
      $.getJSON("/agent/api/sessions/assigned.json", function (data) {
        for (var i = 0; i < data.results.length; i++) {
          var e = data.results[i];

          if (e.online == undefined && false) {
            var group = new appGroups(e.name, e.img);
            for (var j = 0; j < e.members.length; j++) {
              group.addMember(contactList[e.members[j].contact]);
              contactList[e.members[j].contact].addGroup(group);
            }
            for (var j = 0; j < e.messages.length; j++) {
              var m = e.messages[j];
              var message = new appMessages(m.text, m.name, m.time, m.type, true);
              group.addMessage(message);
            }
          } else
          {
        	e.online = formatTime(e.lastInComingStamp);
        	e.img = "/agent/assets/images/profile.png";
            var contact = new appContacts(e.name, e.img, e.online);
            contact.contactType = e.contactType;
            for (var j = 0; j < e.messages.length; j++) {
              var m = e.messages[j];
              m.time = formatTime(m.timestamp);
              var message = new appMessages(m.text, m.name, m.time, m.type, false);
              contact.addMessage(message);
            }
          }
        }
        subject.notifyObservers();
      });
    },
    writeMessage: function () {
      var msg = new appMessages($(".input-message").val(), "", new Date().getHours() + ":" + new Date().getMinutes(), true);
      
      sendMessage({
    	  message : msg,
    	  sessionId : currentChat.sessionId
      });
      
      WhatsApp.View.printMessage(msg);
      currentChat.addMessage(msg);
      $(".input-message").val("");
      console.log("writeMessage")
      subject.notifyObservers();
      $("#" + currentChat.id).addClass("active-contact active");
      scrollToBottom();
    },
    readMessage: function (m) {
    	console.log(m)
      for(var id in contactList){
    	  if(m.name == contactList[id].name){
    		  this.getMessage(m.text,id,null);
    		  break;
    	  }
      }
    },
    getMessage: function (text, id, name) {
      if (name == undefined) {
        var msg = new appMessages(text, contactList[id].name, new Date().getHours() + ":" + new Date().getMinutes(), false, false);
      } else
      {
        var msg = new appMessages(text, name, new Date().getHours() + ":" + new Date().getMinutes(), false, true);
      }
      contactList[id].addMessage(msg);
      contactList[id].online = new Date().getHours() + ":" + new Date().getMinutes();

      if (contactList[id] == currentChat) {
        WhatsApp.View.printMessage(msg);
        WhatsApp.View.printContact(contactList[id]);
      } else
      {
        contactList[id].newmsg++;
        WhatsApp.View.printContact(contactList[id]);
      }
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
 "FACEBOOK" : "fa-facebook"
}

var WhatsApp = function ToDoView(app) {
  var view = {
    printContact: function (c) {
      $("#" + c.id).remove();
      var lastmsg = c.messages[c.messages.length - 1];

      var html = $(quikr.tmpl("temp_contact",{
        	c : c,lastmsg : lastmsg
       }));

      var that = c;
      $(".contact-list").prepend(html);
      console.log("printContact")
      WhatsApp.Ctrl.addClick(html, that);
    },
    printChat: function (cg) {
      WhatsApp.View.closeContactInformation();
      $(".chat-head img").attr("src", cg.img);
      $(".user_info .user_name").text(cg.name);
	    $(".user_info .user_text").text("online @ " + cg.online);
	    // Nachrichten konfigurieren
	    $(".chat-bubble").remove();
	    for (var i = 0; i < cg.messages.length; i++) {
	      WhatsApp.View.printMessage(cg.messages[i]);
	    }
	    currentChat = cg;
	    scrollToBottom();
    },
    printMessage: function (gc) {
      $(".msg_card_body").append(quikr.tmpl(gc.type ? "temp_message_me" : "temp_message_you",{
      	gc : gc
      }));
    },
    showContactInformation: function () {
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
                $("#" + contactList[i].id).addClass("active-contact");
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

    //Observer-Methode
    notify: function () {
      if (first) {
        first = false;
        for (var i = 0; i < contactList.length; i++) {
          WhatsApp.View.printContact(contactList[i]);
          currentChat = contactList[i];
        }
        first = false;
      } else
      {
        WhatsApp.View.printContact(currentChat);
      }
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
        $("#nm" + that.id).remove();
        that.newmsg = 0;
        WhatsApp.View.printChat(that);
      });
    },

    //Observer-Methode
    notify: function () {
      if (start) {
        $(".input-message").keyup(function (ev) {
          if (ev.which == 13 || ev.keyCode == 13) {
            app.Model.writeMessage();
          }
        });

        $("#show-contact-information").on("click", function () {
          WhatsApp.View.showContactInformation();
        });

        $("#close-contact-information").on("click", function () {
          WhatsApp.View.closeContactInformation();
        });
        $(".online-toggle").on("click", function () {
            $(this).toggleClass("toggle-active")
          });
        $('.menu_btn').click(function(){
        	//$('.menu_btn_menu').toggle();
        	document.getElementById("mySidebar").style.display = "block";
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
		user : '[[${APP_USER}]]',
		context : "/agent"
	}).instance().on("/agent/onmessage", function(testresponse){
		WhatsApp.Model.readMessage(testresponse);
	}).on("/branch-user/customer-call-session/0", function(testresponse){
		console.log("===testresponse0",testresponse)
	});
	