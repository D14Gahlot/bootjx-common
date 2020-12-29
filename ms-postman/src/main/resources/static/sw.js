importScripts('https://www.gstatic.com/firebasejs/4.12.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/4.12.0/firebase-messaging.js');

firebase.initializeApp({
  'messagingSenderId': '770916390748'
});

const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function(payload) {
  console.log("sw: ", payload);
  const notificationTitle = 'Background Title (client)';
  const notificationOptions = {
    body: 'Background Body (client)',
    icon: '/mail.png'
  };

  return self.registration.showNotification(notificationTitle,
    notificationOptions);
});

