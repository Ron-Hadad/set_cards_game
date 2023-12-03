Set Card Game
Introduction
Welcome to the Set Card Game project! This implementation brings the classic 'Set' game to life, offering an interactive experience where players can enjoy the game with each other on the same machine or challenge intelligent bots. The game is designed with a multi-threading approach, where each player, including the dealer, is embodied as a thread.

Game Description
Set Game Rules
Set is a card game that involves identifying sets of three cards with certain characteristics. Each card has four features: color, shape, number, and shading. A set consists of three cards where each feature is either all the same or all different across the three cards.

Modes of Play
Local Multiplayer:

Play with friends or family on the same machine.
Each player interacts with the game through the console.
Player vs. Bot:

Challenge a computer-controlled opponent.
The bot is implemented as a separate thread, making decisions based on the game state.
Bot vs. Bot:

Watch intelligent bots play against each other.
Sit back and observe the strategic moves of computer players.
How to Run
Requirements:

Ensure you have a compatible compiler and threading library for your programming language.
Clone the Repository:

bash
Copy code
git clone https://github.com/your-username/set-card-game.git
Compile and Run:

Navigate to the project directory and compile the source code.
Run the executable to start the game.
Follow On-Screen Instructions:

Depending on the chosen mode, follow on-screen instructions to play against others or observe bot battles.
Implementation Details
The game is implemented using multi-threading, where each player and the dealer are separate threads. This design allows for simultaneous actions, enhancing the gaming experience. The threading model ensures that the game progresses smoothly and provides a responsive environment for player interactions.

Contributing
We welcome contributions to enhance and improve the Set Card Game. If you have ideas for new features, find bugs, or want to optimize the code, feel free to open an issue or submit a pull request.

License
This project is licensed under the MIT License. Feel free to use, modify, and distribute the code within the terms of the license.

Enjoy playing the Set Card Game! If you have any questions or feedback, please reach out to the project maintainers.

Happy gaming!
