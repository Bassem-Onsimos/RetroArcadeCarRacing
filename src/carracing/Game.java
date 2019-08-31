package carracing;

import GameClock.StopWatch;
import GameEngine.AbstractGame;
import GameEngine.Graphics.BufferedImageLoader;
import GameEngine.Graphics.SpriteSheet;
import GamePanel.DoublePanelItem;
import GamePanel.GameData;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.lang.Math.*;
import java.util.Vector;
import javafx.util.Pair;
import menus.PauseMenu;
import menus.StartMenu;

public class Game extends AbstractGame {
    
    private Vector<Pair<Float, Float>> track; 
    //
    private float carPosition;
    private float carSpeed;
    //
    private boolean accelerating = false;
    //
    private float distance;
    private float trackDistance;
    private int trackSection;
    private float curvature;
    private float trackCurvature;
    private float playerCurvature;
    //
    private SpriteSheet carSprite;
    private int carWidth, carHeight;
    private double animation;
    private int target;
    private enum Direction {
        straight, left, right
    };
    private Direction direction;
    //
    private Quality quality;
    private int height, width;
    private float scale;
    //
    private DoublePanelItem speed;
    private DoublePanelItem totalDistance;
    private StopWatch time;
    //
    private Color roadColor =  Color.gray;

    public Game(int width, int height, float scale, String windowTitle) {
        super(width, height, scale, windowTitle);
    }
    
    @Override
    public void initiate() {  
        setPausable(true);
        setResizable(true);
        setFPSlimited(true);
        setDebugInfoDisplayed(false);
        
        speed = new DoublePanelItem("Speed", 0.0);
        totalDistance = new DoublePanelItem("Distance", 0.0);
        time = new StopWatch(this);
        
        addGamePanel(new GameData() {
            @Override
            public void initiate() {
                addItem(time);
                addItem(totalDistance);
                addItem(speed);
            }
        }, new Color(3, 4, 72), new Color(35, 247, 50), new Color(35, 247, 50), 17);
        
        setStartMenu(new StartMenu(this));
        setPauseMenu(new PauseMenu(this));
        
        BufferedImageLoader loader = new BufferedImageLoader();
        try {
                carSprite = new SpriteSheet(loader.loadImage("/img/carSprite.png"));
                carWidth = carSprite.getWidth() / 7;
                carHeight = carSprite.getHeight();
        } catch (IOException e) {
        }
        
        quality = Quality.medium;
    }
    
    @Override
    public void reset() {
        loadGame();
        
        time.reset();
        totalDistance.setValue(0);
        speed.setValue(0);
        
        carPosition = 0;
        carSpeed = 0;
        distance = 0;
        curvature = 0;
        trackCurvature = 0;
        trackDistance = 0;
        playerCurvature = 0;
        
        direction = Direction.straight;
        animation = 4;
        
        track = new Vector();
        designTrack();
        
        time.start();
    }
    
    public void loadGame() {
        
        switch(quality) {
            
            case high: {
                scale = 1.0f;
                width = (int)(getWidth() / scale);
                height = (int)(getHeight() / scale);
                break;
            }
    
            case medium: {
                scale = 2.0f;
                width = (int)(getWidth() / scale);
                height = (int)(getHeight() / scale);
                break;
            }
    
            case low: {
                scale = 3.0f;
                width = (int)(getWidth() / scale);
                height = (int)(getHeight() / scale);
                break;
            }
        }
    }
    
    public void designTrack() {
        track.add(new Pair(0.0f, 20.0f));
        track.add(new Pair(0.0f, 500.0f));
        track.add(new Pair(1.0f, 300.0f));
        track.add(new Pair(0.0f, 500.0f));
        track.add(new Pair(-1.0f, 200.0f));
        track.add(new Pair(0.0f, 300.0f));
        track.add(new Pair(-1.0f, 300.0f));
        track.add(new Pair(1.0f, 300.0f));
        track.add(new Pair(0.0f, 300.0f));
        track.add(new Pair(0.3f, 600.0f));
        track.add(new Pair(0.0f, 300.0f));
        
        for(Pair<Float, Float> p : track)
            trackDistance += p.getValue();
    }

    @Override
    public void update() {
        
        time.update();
        
        float scalingFactor = getElapsedTime() * 4;     //a variable to fine tune animations and motion
        
        if(getInput().isKey(KeyEvent.VK_UP)) {
            carSpeed += 2.0f * scalingFactor;
            accelerating = true;
        }
        else {
            carSpeed -= 0.05f * scalingFactor;
            accelerating = false;
        }
            
        if(getInput().isKey(KeyEvent.VK_LEFT)) {
            if(carPosition > -1) playerCurvature -= 0.2f * scalingFactor * (1.0f - carSpeed / 2.0f);
            direction = Direction.left;
        }       
        else if(getInput().isKey(KeyEvent.VK_RIGHT)) {
            if(carPosition < 1) playerCurvature += 0.2f * scalingFactor * (1.0f - carSpeed / 2.0f);
            direction = Direction.right;
        }
        else
            direction = Direction.straight;
     
        if(abs(playerCurvature - trackCurvature) >= 0.7f) {
            carSpeed -= 3.0f * scalingFactor;
        }
        else if(abs(playerCurvature - trackCurvature) >= 0.48f)  {
            if(accelerating) carSpeed = 0.2f;
        }
        

        if(carSpeed > 1) carSpeed = 1;

        if(carSpeed < 0) carSpeed = 0;
        
        distance += (70.0f * carSpeed) * scalingFactor;
        
        speed.setValue(Math.round(carSpeed * 100 * 100d) / 100d);
        totalDistance.setValue(Math.round((totalDistance.getValue() + (carSpeed / 10)) * 100d) / 100d);
       
        scalingFactor /= 3;
        
        float offset = 0;
        trackSection = 0;
        
        if(distance > trackDistance)
            distance -= trackDistance;
        
        while(trackSection < track.size() && offset <= distance){
            offset += track.get(trackSection).getValue();
            trackSection++;
        }
        
        roadColor = (trackSection - 1)==0 ? Color.white : Color.gray;
        
        float targetCurvature = track.get(trackSection-1).getKey();
        
        float trackCurvatureDifference = (targetCurvature - curvature) * scalingFactor * carSpeed;
        curvature += trackCurvatureDifference;
        
        trackCurvature += curvature * scalingFactor * carSpeed;
        
        carPosition = playerCurvature - trackCurvature;
        
        switch(direction) {                
            case straight: {
                target = 4;               
                break;
            }
            case left: {
                target = 1;
                break;
            }
            case right: {
                target = 7;
                break;
            }
        }
        
        scalingFactor = 20;
        
        if(animation < target) {
            animation += getElapsedTime() * scalingFactor;
            
            if(animation >= target) animation = target;
        }
        else if(animation > target) {
            animation -= getElapsedTime() * scalingFactor;
            
            if(animation <= target) animation = target;
        }
        
    }
    
    @Override
    public void render(Graphics2D g2) {
        
        Graphics g = (Graphics)g2;
        
        g.setColor(new Color(3, 4, 72));
        g.fillRect(0, 0, (int)(width * scale), (int)(height/4 * scale));
        
        g.setColor(new Color(6, 11, 123));
        g.fillRect(0, (int)(height/4 * scale), (int)(width * scale), (int)(height/4 * scale));
        
        
        for(int x = 0; x < width; x++) {
            int hillHeight = (int)(abs(sin(x / (4 - scale) * 0.01f + trackCurvature) * 16.0f) * (4 - scale) );
            
            for(int i = 0; i < scale; i++)
                fillScaledRect(g, x, (height/2 - hillHeight), 1, hillHeight, new Color(122, 121, 27));
        }

        for (int y = height/2; y < height; y++) {

            float prespective = (float) (y - height/2) / (height/2);

            float midPoint = (float) (0.5f + curvature * pow(1.0f - prespective, 3));
            float roadWidth = 0.1f + prespective * 0.8f;
            float clipWidth = 0.15f * roadWidth;
            float laneLineWidth = 0.04f * roadWidth;

            roadWidth *= 0.5f;

            int leftGrass = (int) ((midPoint - roadWidth - clipWidth) * width);
            int leftClip = (int) ((midPoint - roadWidth) * width);
            int laneLineStart = (int) ((midPoint - laneLineWidth/2) * width);

            int rightGrass = (int) ((midPoint + roadWidth + clipWidth) * width);
            int rightClip = (int) ((midPoint + roadWidth) * width);
            int laneLineEnd = (int) ((midPoint + laneLineWidth/2) * width);
            
            
            Color grassColor = sin((20.0f * pow(1.0f - prespective, 3)) + (distance * 0.1f)) > 0.0f ? new Color(35, 247, 50) : new Color(11, 124, 27);
            Color clipColor = sin((40.0f * pow(1.0f - prespective, 3)) + (distance * 0.1f)) > 0.0f ? new Color(200, 0, 0) : new Color(235, 235, 235);
            Color laneLineColor = sin((5.0f * pow(1.0f - prespective, 3)) + (distance * 0.1f)) > 0.0f ? Color.white : roadColor;
            
            fillScaledRect(g, 0, y, leftGrass, 1, grassColor);
            fillScaledRect(g, leftGrass, y, leftClip - leftGrass, 1, clipColor);
            fillScaledRect(g, leftClip, y, rightClip - leftClip, 1, roadColor);
            fillScaledRect(g, laneLineStart, y, laneLineEnd - laneLineStart, 1, laneLineColor);
            fillScaledRect(g, rightClip, y, rightGrass - rightClip, 1, clipColor);
            fillScaledRect(g, rightGrass, y, width - rightGrass, 1, grassColor);
            
        }
              
        int carPos = width / 2 + (int) ((width * carPosition) / 2.0f) - (int)(carWidth / scale / 2);
        
        drawScaledImage(g, carSprite.cropImage((int)animation, 1, carWidth, carHeight), carPos, height - (int)(carHeight / scale * 2), (int)(carWidth / scale), (int)(carHeight / scale));
     
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }
    
    private void fillScaledRect(Graphics g, float startX, float startY, float width, float height, Color color) {
        g.setColor(color);
        g.fillRect((int)(startX * scale), (int)(startY * scale), (int)(width*scale), (int)(height*scale));
    }
    
    private void drawScaledImage(Graphics g, BufferedImage img, int x, int y, int width, int height) {
        g.drawImage(img, (int)(x * scale), (int)(y * scale), (int)(width * scale), (int)(height * scale), null);
    }
}
