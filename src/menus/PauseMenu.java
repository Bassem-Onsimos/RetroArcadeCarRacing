
package menus;

import GameEngine.GameState.State;
import GameMenu.AbstractMenu;
import GameMenu.MenuItem;
import carracing.Game;
import java.awt.Color;

public class PauseMenu extends AbstractMenu{

    private Game game;
    
    public PauseMenu(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    public void initiate() {
        
        setBackGroundColor(new Color(3, 4, 72));
        setBackgroundOpacity(transparent);
        
        setHighlightColor(new Color(35, 247, 50));
        
        addItem(new MenuItem("Continue") {
            @Override
            public void function() {
                game.loadGame();
                game.setState(State.inGame);
            }
        });
        
        addItem(new MenuItem("New Game") {
            @Override
            public void function() {
                game.reset();
                game.setState(State.inGame);
            }
        });
        
        addItem(new MenuItem("Main Menu") {
            @Override
            public void function() {
                game.setState(State.startMenu);
            }
        });
        
        addItem(new MenuItem("Exit") {
            @Override
            public void function() {
                System.exit(0);
            }
        });
        
    }
    
}
