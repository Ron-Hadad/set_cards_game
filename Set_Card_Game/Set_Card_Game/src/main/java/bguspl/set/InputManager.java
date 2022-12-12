package bguspl.set;

import bguspl.set.ex.Player;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

/**
 * This class handles the input from the keyboard, translates it to table grid slots and dispatches accordingly.
 */
class InputManager extends KeyAdapter {

    private final static int MAX_KEY_CODE = 255;
    private final Player[] players;
    int[] keyMap = new int[MAX_KEY_CODE + 1];
    int[] keyToSlot = new int[MAX_KEY_CODE + 1];
    Env env;

    public InputManager(Env env, Player[] players) {
        this.players = players;
        this.env = env;
        
        // initialize the keys
        for (int player = 0; player < env.config.players; ++player)
            for (int i = 0; i < env.config.playerKeys(player).length; i++) {
                int keyCode = env.config.playerKeys(player)[i];
                keyMap[keyCode] = player + 1; // 1 for first player and 2 for second player
                keyToSlot[keyCode] = i;
            }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // dispatch the key event to the player according to the key map
        int keyCode = e.getKeyCode();
        int player = keyMap[keyCode] - 1;
        if (player >= 0){
            env.logger.log(Level.SEVERE, "Key " + keyCode + " was pressed by player " + player);
            players[player].keyPressed(keyToSlot[keyCode]);
        }
    }
}
