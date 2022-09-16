# Slagalica - Online Multiplayer Game

This application is an online two-player game similar to Slagalica. It consists of three games: Reči, Moj Broj and Kviz (Ko zna zna). The players compete to see who can make the longest word from the given letters, who can calculate the required number from the received numbers, and who knows the answers to more questions.

## Connection

Players have the option to enter the appropriate IP address to access the server, whose role is to connect players and provide them with a networked game. Networking is done via TCP connection and on one side we have the server side, made up of the Server class and ClientHandler class, and on the other side we have the client side, made up of the Client class. 

Players then choose one of three options:
- to be randomly connected to an opponent, 
- to generate a code and open the game room,
- or to access the game room via a code. 

After the game is over, the player is offered the option to play again or exit the app. This option is also offered in case the opponent leaves the game at some point, either intentionally or by losing connection. 


## How to start an app

The server is started by starting the main method in the Server class, in which for each established connection with the client, a new thread is opened through the ClientHandler class whose role is to provide a networked game to its client.

The player opens his application by running the main method in the Client class. After entering the appropriate IP address of the server, new Java GUI windows are opened to guide him through the game.