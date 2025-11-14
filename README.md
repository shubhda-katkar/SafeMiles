SafeMiles – Smart Blackspot Alert System 
SafeMiles is an Android application designed to alert drivers about accident
prone blackspots using real-time geofencing, voice alerts, and admin-controlled 
management of blackspots through Firebase Firestore. 
 
Tech Stack:
Frontend (Android): 
1.Java 
2.Google Maps SDK 
3.Geofencing API 
4.Text-to-Speech (TTS) 
Backend:
1.Firebase Firestore 
2.Firebase Authentication (optional for admin) 
3.Firestore Security Rules 
Project Structure 
SafeMiles/ 
│ 
├── app/ 
│   
├── java/com/example/safemiles/    
│   
├── MainActivity.java 
├── MapActivity.java 
├── GeofenceBroadcastReceiver.java 
├── VoiceService.java 
├── AdminDashboard.java 
├── AddBlackspotsActivity.java 
├── EditBlackspotsActivity.java 
└── utils/... 
├── res/ 
├── layout/ 
└── drawable/ 
└── AndroidManifest.xml 
│ 
└── README.md 
How the Alert System Works 
1. App loads all blackspots from Firestore 
2. A geofence is created around each one 
3. When the user enters the radius: 
o Notification is shown 
o A voice alert announces: 
“You are heading to a blackspot. Drive carefully.” 
4. Alerts work even in background mode 
Setup Instructions 
1.Clone the Project 
git clone https://github.com/YOUR_USERNAME/SafeMiles.git 
2.Open in Android Studio 
3.Add your google-services.json inside: 
app/ 
4. Enable: 
 Firestore 
 Maps SDK 
 Geolocation API 
5.Run the app on device 
(Geofencing does not work on emulator) 
