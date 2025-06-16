// Firebase 서비스 워커
importScripts('https://www.gstatic.com/firebasejs/10.12.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.12.1/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyBsnvbbi1SQSHe9v3Nzt7R23eELXlv4KMI",
  authDomain: "charti-5da7d.firebaseapp.com",
  projectId: "charti-5da7d",
  messagingSenderId: "308166362794",
  appId: "1:308166362794:web:00035f97aca288228972d3"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
  console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신:', payload);
  self.registration.showNotification(payload.notification.title, {
    body: payload.notification.body
//    icon: '/icon.png'
  });
});
