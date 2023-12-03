package bguspl.set.ex;

import java.util.logging.Level;

import bguspl.set.Config;
import bguspl.set.Env;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    public Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate
     * key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /**
     * The player prreses queue.
     */
    private LinkedBlockingQueue<Integer> slotPrresedQ;

    /**
     * The cards already marked by the player with tocken.
     */
    public LinkedBlockingQueue<Integer> cardTockendQ;

    /**
     * the game dealer.
     */
    private Dealer dealer;

    /**
     * the player key.
     */
    public Object playerKey;

    /**
     * player end of freeze time due to point or penalty.
     */
    public long freezeEndTime;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided
     *               manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.dealer = dealer;
        this.table = table;
        this.id = id;
        this.human = human;
        this.score = 0;
        this.terminate = false;
        this.slotPrresedQ = new LinkedBlockingQueue<>(3);
        this.cardTockendQ = new LinkedBlockingQueue<Integer>(3);
        playerKey = new Object();
        freezeEndTime = 0;
    }

    /**
     * The main player thread of each player starts here (main loop for the player
     * thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + "starting.");
        if (!human)
            createArtificialIntelligence();

        while (!terminate) {
            // TODO implement main player loop
            while (!slotPrresedQ.isEmpty()) {
                Integer slotPrress = slotPrresedQ.poll();
                Integer cardToTocken = table.slotToCard[slotPrress];
                if (!cardTockendQ.contains(cardToTocken)) {
                    if (cardTockendQ.offer(cardToTocken)) {
                        table.placeToken(id, slotPrress);
                        if (cardTockendQ.size() == 3) {
                            dealer.setsCheck.offer(id);
                            synchronized (dealer.dealerKey) {
                                dealer.dealerKey.notify();
                            }
                            synchronized (playerKey) {
                                try {
                                    playerKey.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                } else {
                    table.removeToken(id, slotPrress);
                    cardTockendQ.remove(cardToTocken);
                }
            }
        }
        if (!human)
            try {
                aiThread.join();
            } catch (InterruptedException ignored) {
            }
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of
     * this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it
     * is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                // TODO implement player key press simulator
                int randomSlot = (int) (Math.random() * env.config.tableSize);
                keyPressed(randomSlot);

                try {
                    synchronized (this) {
                        wait(800);
                    }
                } catch (InterruptedException ignored) {
                }
            }
            env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
        // for the case he is sleeping somewhere:
        playerThread.interrupt();

        // otherwise:
        // we should prob. kill him here.
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        // TODO implement
        if (table.slotToCard[slot] != null & System.currentTimeMillis() - freezeEndTime > 0) {
            slotPrresedQ.offer(slot);
        }

    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        env.ui.setScore(id, ++score);
        env.ui.setFreeze(id, env.config.pointFreezeMillis);
        freezeEndTime = System.currentTimeMillis() + env.config.pointFreezeMillis + 500;
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
        env.ui.setFreeze(id, env.config.penaltyFreezeMillis);
        freezeEndTime = System.currentTimeMillis() + env.config.penaltyFreezeMillis + 500;
    }

    public int getScore() {
        return score;
    }
}
