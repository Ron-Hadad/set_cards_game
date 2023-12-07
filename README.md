# Set Card Game
## Introduction
Welcome to the Set Card Game project! This implementation brings the classic 'Set' game to life, offering an interactive experience where players can enjoy the game with each other or challenge bots. The game is designed with a multi-threading approach, where each player, including the dealer, is embodied as a thread.

## Game Description and Rules
Set is a card game that involves identifying sets of three cards with certain characteristics. Each card has four features: color, shape, number, and shading. A set consists of three cards where each feature is either all the same or all different across the three cards.
In this version, you can choose the cards on the screen using the numbers 1 - 9. Notice that finding a correct set will freeze you for exactly 1 second, while choosing a wrong set will result in a penalty of a 3-second freeze. 
You can choose your mode of playing by adjusting the config.java file. 
Depending on the chosen mode, you can play against others or observe bot battles.

## Implementation Details
The game is implemented using multi-threading, where each player and the dealer are separate threads. This design allows for simultaneous actions, enhancing the gaming experience. The threading model ensures that the game progresses smoothly and provides a responsive environment for player interactions.

Enjoy playing the Set Card Game! If you have any questions or feedback, please! reach out.

Happy gaming!
