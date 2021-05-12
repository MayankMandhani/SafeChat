# SafeChat
An Android text messaging app with End to End Advanced Encryption Standard method and Phone Number Authentication. Built using Java and Firebase.<br>
<a href="https://drive.google.com/file/d/1CrTHF9yVNqhTmqaRxyZz0Yo9xlOV9Rq8/view?usp=sharing">APK Link</a>

**Technologies and Libraries used:**<br>
- Java
- XML
- Firebase Authentication
- Firebase Realtime Database
- javax.crypto

**Phone number authentication using FirebaseAuth:** 

[<img src="https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-171357.png?alt=media&token=5564c105-b929-47ee-baaa-305b7dd83810" 
width="150" height="300">](https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-171357.png?alt=media&token=5564c105-b929-47ee-baaa-305b7dd83810) &emsp; [<img src="https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-173331.png?alt=media&token=1a9f7340-7e26-41dd-b96c-262d262f783f" 
width="150" height="300">](https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-173331.png?alt=media&token=1a9f7340-7e26-41dd-b96c-262d262f783f)

**Chat List Screen:**
- A RecyclerView with a list of chats with users fetched from Firebase Database
- A Logout button
- A new chat button displaying list of contacts to start a chat with in a new screen

[<img src="https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-215247.png?alt=media&token=d36f82b2-2f26-480a-9145-082b2f9ad8f0" 
width="150" height="300">](https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-215247.png?alt=media&token=d36f82b2-2f26-480a-9145-082b2f9ad8f0)

**Contact List Screen:**
- A RecyclerView with a list of contacts in the user's phone that have accounts on SafeChat.

[<img src="https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-215206.png?alt=media&token=34a6969c-4823-41e0-8942-69770dd398ba" 
width="150" height="300">](https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-215206.png?alt=media&token=34a6969c-4823-41e0-8942-69770dd398ba)

**Chat Screen:**
- A RecyclerView with a list of messages between the corresponding users fetched from Firebase Database.
- An EditText to type the message and a send button.

[<img src="https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-171842.png?alt=media&token=2dd4259b-2a28-4af3-9c93-ebd968fd3ba5" 
width="150" height="300">](https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/Screenshot_20210506-171842.png?alt=media&token=2dd4259b-2a28-4af3-9c93-ebd968fd3ba5)

All the messages between all users are end to end encrypted using the AES method with the help of javax.crypto library. This is what the messages look like in the database:

[<img src="https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/aesfirebase.png?alt=media&token=46cc03a2-a034-4f03-9aa0-4f7cd7fafd1a" 
width="150" height="300">](https://firebasestorage.googleapis.com/v0/b/safechat-3e2e3.appspot.com/o/aesfirebase.png?alt=media&token=46cc03a2-a034-4f03-9aa0-4f7cd7fafd1a)
