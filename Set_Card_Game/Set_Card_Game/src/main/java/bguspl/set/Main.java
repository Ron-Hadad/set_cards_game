package bguspl.set;

import bguspl.set.ex.Dealer;
import bguspl.set.ex.Player;
import bguspl.set.ex.Table;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.*;

/**
 * This class contains the game's main function.
 */
public class Main {

    /**
     * The game's main function. Creates all data structures and initializes the threads.
     *
     * @param args - unused.
     */
    public static void main(String[] args) {

        // create the game environment objects
        Logger logger = initLogger(args.length > 0);
        Config config = new Config(logger, "config.properties");
        UserInterfaceImpl ui = new UserInterfaceImpl(logger, config);
        EventQueue.invokeLater(() -> ui.setVisible(true));
        Env env = new Env(logger, config, ui, new UtilImpl(config));

        // create the game entities
        Player[] players = new Player[env.config.players];
        Table table = new Table(env);
        Dealer dealer = new Dealer(env, table, players);
        for (int i = 0; i < players.length; i++)
            players[i] = new Player(env, dealer, table, i, i < env.config.humanPlayers);
        ui.addKeyListener(new InputManager(env, players));
        ui.addWindowListener(new WindowManager(env, dealer));

        // start the dealer thread
        Thread dealerThread = new Thread(dealer, "dealer");
        dealerThread.start();

        try {dealerThread.join();} catch (InterruptedException ignored) {}
        env.logger.log(Level.INFO, "Thread " + Thread.currentThread().getName() + " terminated.");
        for(Handler h:env.logger.getHandlers())
            h.close();
    }

    private static Logger initLogger(boolean disableTimestamp) {

        FileHandler fh;
        //just to make our log file nicer :)
        SimpleDateFormat format = new SimpleDateFormat("M-d_HH-mm-ss");
        try {
            //noinspection ResultOfMethodCallIgnored
            new File("./logs/").mkdirs();
            fh = new FileHandler("./logs/" + format.format(Calendar.getInstance().getTime()) + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("SetGameLogger");
        logger.setUseParentHandlers(false);
        fh.setFormatter(new SimpleFormatter() {
            private static final String formatWithTimestamp = "[%1$tF %1$tT] [%2$-7s] %3$s%n";
            private static final String formatWithoutTimestamp = "[%2$-7s] %3$s%n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(disableTimestamp ? formatWithoutTimestamp : formatWithTimestamp,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        });
        logger.addHandler(fh);

        return logger;
    }
}