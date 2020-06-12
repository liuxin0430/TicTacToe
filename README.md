Tic Tac Toe game井字棋游戏

The game rule is that players take turns in the board with "X" or "O", who first put the three same marks into a horizontal line, straight line, oblique line, is the winner.

In the implementation of this app, I use buttons to represent the game board. When the game starts, there is no text on the buttons, but when the user clicks on a button, it will display a green X. The computer will then make a move on an appropriate button and turn the text to a red O. Also, a TextView widget will tell whose turn it is and when the game is over.

In this project, the game logic is encapsulated as a class called TicTacToeGame. There are several public methods that will be used to manipulate the game board, make a computer move and check if there is a winner. These public methods include TicTacToeGame(), clearBoard(), setMove(), getComputerMove() and checkForWinner().

The UI code includes activity_main.xml and MainAcivity.java. It creates the user interface and handles the interaction logic between users and the screen. The game UI will use Button widgets to represent the game board. And a Button for restarting a new game, a TextView for displaying information and other widgets will be added.