
package menus;

import GameEngine.GameState.State;
import GameMenu.AbstractMenu;
import GameMenu.MenuItem;
import GameMenu.SubMenuInitializer;
import carracing.Game;
import carracing.Quality;
import java.awt.Color;

public class StartMenu extends AbstractMenu {
    
    private Game game;
    
    public StartMenu(Game game) {
        super(game);
        this.game = game;
    }

    @Override
    public void initiate() {
        
        setBackGroundColor(new Color(3, 4, 72));
        setHighlightColor(new Color(35, 247, 50));
        
        addItem(new MenuItem("New Game") {
            @Override
            public void function() {
                game.reset();
                game.setState(State.inGame);
            }
        });
        
        addItem(new SubMenuInitializer("Graphics Quality") {
            @Override
            public void initiate() {
                
                addSubMenuItem(new MenuItem("Low") {
                    @Override
                    public void function() {
                        game.setQuality(Quality.low);
                    }
                });
                
                addSubMenuItem(new MenuItem("Medium") {
                    @Override
                    public void function() {
                        game.setQuality(Quality.medium);
                    }
                });
                
                addSubMenuItem(new MenuItem("High") {
                    @Override
                    public void function() {
                        game.setQuality(Quality.high);
                    }
                });             
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
