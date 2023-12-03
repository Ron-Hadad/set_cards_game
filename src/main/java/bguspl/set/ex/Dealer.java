package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    /**
     * object to lock the dealer class.
     */
    public static Object dealerKey;

    /**
     * Q of players IDs that want the dealer to cheack their sets.
     */
    public LinkedBlockingQueue<Integer> setsCheck;

    /**
     * the amount of time the dealer sleep if not waken in sleepUntilWokenOrTimeout
     * function.
     * 
     */
    private long dealerTickingTime;
    private boolean warn;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        setsCheck = new LinkedBlockingQueue<Integer>(env.config.players);
        dealerKey = new Object();
        dealerTickingTime = 1000;
        warn = false;
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " starting.");
        // creating the players threads:
        for (int i = 0; i < players.length; i++) {
            Thread player = new Thread(players[i], "player" + i);
            player.start();
        }
        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }
        announceWinners();
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did
     * not time out.
     */
    private void timerLoop() {
        // we add:
        reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis + 2000;
        dealerTickingTime = 1000;
        warn = false;
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            // env.ui.setCountdown(reshuffleTime - System.currentTimeMillis(), false);
            sleepUntilWokenOrTimeout(); // only if their is only 10 sec left , called to cheak set, time out,
            updateTimerDisplay(false);// if 10 sec left - reset = false & paint in red. if called to check&correct -
                                      // reset = true,if &false - reset = false. if time out - reset = true.
            removeCardsFromTable(); // if 10 sec left - no cards to remove. if cheak set&correct - replace set, if
                                    // &false -no cards to remove.
                                    // if time out - replace all.
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     * also remove the cards from the player tocken Q if set correct
     */
    private void removeCardsFromTable() {
        // TODO implement

        while (!setsCheck.isEmpty()) {

            int playerId = setsCheck.poll();
            // moving the cards marked as tockened to a new simple array:
            int cardsTockendByPlayer[] = new int[3];
            for (int i = 0; i < 3; i++) {
                cardsTockendByPlayer[i] = players[playerId].cardTockendQ.poll();
            }
            // if we found a set:
            if (env.util.testSet(cardsTockendByPlayer)) {
                players[playerId].point();
                // restarting the timers:
                reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis + 1500;
                dealerTickingTime = 1000;
                warn = false;
                // removing the cards and ui tockens:
                int slot0 = table.cardToSlot[cardsTockendByPlayer[0]];
                int slot1 = table.cardToSlot[cardsTockendByPlayer[1]];
                int slot2 = table.cardToSlot[cardsTockendByPlayer[2]];
                table.removeCard(slot0);
                table.removeCard(slot1);
                table.removeCard(slot2);
                // removing the cards (that were replaced) from the players tockendQ:
                for (Player player : players) {
                    // for each player we'll try all the three cards that we removed from the table.
                    // additionally, we'll remove the players that want thaeir set to be cheacked
                    // from the dealer list-only if we chainged their tockend list.
                    for (int i = 0; i < 3; i++) {
                        if (player.cardTockendQ.remove(cardsTockendByPlayer[i])) {
                            setsCheck.remove(player.id);
                        }
                    }
                }
            }
            // if not correct:
            else {
                // returning the player tocken Q and poenalty:
                for (int i = 0; i < 3; i++) {
                    players[playerId].cardTockendQ.offer(cardsTockendByPlayer[i]);
                }
                players[playerId].penalty();
            }
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement
        // finding the open slots:
        if (deck.size() != 0 & table.countCards() != env.config.tableSize) {
            List<Integer> openSlots = new ArrayList<Integer>();
            for (int i = 0; i < env.config.tableSize; i++) {
                if (table.slotToCard[i] == null) {
                    openSlots.add(i);
                }
            }
            Collections.shuffle(openSlots);
            Collections.shuffle(deck);
            // matching cards to open slots:
            while (!deck.isEmpty() & !openSlots.isEmpty()) {
                int slotChoosen = openSlots.remove(0);
                int cardChoosen = deck.remove(0);
                // update the table
                table.placeCard(cardChoosen, slotChoosen);
                // ui update
                env.ui.placeCard(cardChoosen, slotChoosen);
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some
     * purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
        synchronized (dealerKey) {
            try {
                dealerKey.wait(dealerTickingTime);
            } catch (InterruptedException e) {
            }
        }

    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement
        // showing the timer:
        if (reshuffleTime - System.currentTimeMillis() < 10000) {
            dealerTickingTime = 10;
            warn = true;
        }
        env.ui.setCountdown(reshuffleTime - System.currentTimeMillis(), warn);
        // showing the freeze time left for the players(show nothing if their is non)
        // and releasing the players.
        for (Player player : players) {
            if (player.freezeEndTime - System.currentTimeMillis() <= 0) {
                synchronized (player.playerKey) {
                    player.playerKey.notify();
                }
            }
            env.ui.setFreeze(player.id, player.freezeEndTime - System.currentTimeMillis());
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
        ArrayList<Integer> slotsToRemove = new ArrayList<Integer>();
        for (int i = 0; i < env.config.tableSize; i++) {
            slotsToRemove.add(i);
        }
        Collections.shuffle(slotsToRemove);
        for (int slot : slotsToRemove) {
            // if their is a card in the slot then return it to the deck:
            if (table.slotToCard[slot] != null) {
                deck.add(table.slotToCard[slot]);
            }
            // remove the card from the choosen slot:
            table.removeCard(slot);
        }
        // clear the playes lists and tockens:
        for (Player player : players) {
            player.cardTockendQ.clear();
            synchronized (player.playerKey) {
                player.playerKey.notify();
            }
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        int[] winners = new int[env.config.players];
        int numOfEqualScores = 1;
        int maxScore = -1;
        for (int i = 0; i < env.config.players; i++) {
            if (players[i].getScore() == maxScore) {
                winners[numOfEqualScores] = i;
                numOfEqualScores++;
            }
            if (players[i].getScore() > maxScore) {
                maxScore = players[i].getScore();
                numOfEqualScores = 1;
                winners[1] = i;
            }
        }
        int[] endListOfWinners = new int[numOfEqualScores];
        for (int i = 0; i < numOfEqualScores; i++) {
            endListOfWinners[i] = winners[i];
        }
        env.ui.announceWinner(endListOfWinners);
    }
}
