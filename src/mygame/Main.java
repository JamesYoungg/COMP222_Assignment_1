package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.PointLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import java.util.Random;

/**
 * The main class contain all the code and algorithm
 *
 * @extends SimpleApplication
 * @author Zhengyang.Chen ID:201447395 sgzche36
 */
public class Main extends SimpleApplication {

    private static Main app;
    private static AppSettings mySettings;

    private BitmapText text;

    private int score, level;
    private int paddlecheck = 0;
    private int rotationSpeed;

    private AudioNode CollisionSound, CollisionSound2, BGM;

    private Picture p = new Picture("Picture");

    private Node ball, table, paddle;
    private Geometry target1, target2, target3, target4, target5, target6;

    Vector3f velocity;

    final Vector3f upVector = new Vector3f((float) -4.7, 0, (float) -5.9);
    final Vector3f downVector = new Vector3f((float) -4.7, 0, (float) 5.9);
    final Vector3f leftVector = new Vector3f((float) -4.7, 0, (float) -5.9);
    final Vector3f rightVector = new Vector3f((float) 4.7, 0, (float) -5.9);

    Ray up = new Ray(upVector, Vector3f.UNIT_X);
    Ray down = new Ray(downVector, Vector3f.UNIT_X);
    Ray left = new Ray(leftVector, Vector3f.UNIT_Z);
    Ray right = new Ray(rightVector, Vector3f.UNIT_Z);

    private boolean GameRun = false;
    private boolean shout = false;

    @Override
    public void simpleInitApp() {
        //set the camera as stabale 
        this.flyCam.setEnabled(false);
        this.setDisplayFps(false);
        this.setDisplayStatView(false);

        //Load background music.
        initialBGM();

        //Load game sound.
        initialGameSound();

        //Initial score and level text.
        initialTextField();

        //Load the table.
        initialtable();

        //Initial all targets which need to be destroyed.  
        initialTargets();

        //Set up the paddle .  
        initialpaddle();

        //Set up keys to control.
        setkeys();

        //Set up the lights
        initialLight();

        //Set up the welcome interface
        p.move(0, 0, 1); // Make it appear.
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/Welcome.jpg", false);

        // Attach geometry to orthoNode
        guiNode.attachChild(p);
        velocity = new Vector3f(0, 0, 0);
        ball.setLocalTranslation(0, 0, 3);

    }

    public static void initialSettings() {
        mySettings = new AppSettings(true);
        //Set up the initial screen and Set appear screen to 1920*1080
        mySettings.put("Width", 1920);
        mySettings.put("Height", 1080);
        //Set title of the game
        mySettings.put("Title", "Anime Arkanoid");
    }

    //Listen the key control during the game.
    private AnalogListener analogListener = new AnalogListener() {
        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (GameRun && name.equals("MoveLeft")) {
                paddle.move(-10 * tpf, 0, 0);
            } else if (GameRun && name.equals("MoveRight")) {
                paddle.move(10 * tpf, 0, 0);
            } else if (GameRun && name.equals("RightSpeed")) {
                rotationSpeed = rotationSpeed + 1;
                velocity.setX(velocity.getX() + 0.2f);
            } else if (GameRun && name.equals("LeftSpeed")) {
                rotationSpeed = rotationSpeed - 1;
                velocity.setX(velocity.getX() - 0.2f);
            } else if (name.equals("Shot")) {
                shout = true;
            } else if (name.equals("Pause")) {
                GameRun = !GameRun;
            } else if (name.equals("Restart")) {
                //Set all targets to the garbage.
                target1.setLocalTranslation(10, 10, 10);
                target2.setLocalTranslation(10, 10, 10);
                target3.setLocalTranslation(10, 10, 10);
                target4.setLocalTranslation(10, 10, 10);
                target5.setLocalTranslation(10, 10, 10);
                target6.setLocalTranslation(10, 10, 10);
                //Let the picture dispear
                p.setWidth(0);
                p.setHeight(0);
                //Restart the level 1
                GameRun = true;
                BGM.play();
                Level_1();
            }

        }
    };

    private void initialBGM() {
        //Set up background music.
        BGM = new AudioNode(assetManager, "Sounds/BGM.wav", false);
        BGM.setPositional(false);
        BGM.setLooping(true);
        BGM.setVolume(0.1f);
        BGM.play();
    }

    private void initialGameSound() {
        CollisionSound = new AudioNode(assetManager, "Sounds/ballcollision.wav", false);
        CollisionSound.setPositional(false);
        CollisionSound.setLooping(false);
        CollisionSound.setVolume(0.5f);

        //Ball collision with target sound.
        CollisionSound2 = new AudioNode(assetManager, "Sounds/boom.wav", false);
        CollisionSound2.setPositional(false);
        CollisionSound2.setLooping(false);
        CollisionSound2.setVolume(0.5f);
    }

    private void initialTextField() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        text = new BitmapText(guiFont);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        //set the position
        text.move( // x/y coordinates and z = depth layer 0
                settings.getWidth() / 50,
                text.getLineHeight() + 500,
                0);

        text.setSize(60f);
        guiNode.attachChild(text);
    }

    private void setkeys() {
        // Set up all keys right which are needed in the game.
        inputManager.addMapping("Shot",
                new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("MoveRight",
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("MoveLeft",
                new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Restart",
                new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Pause",
                new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("RightSpeed",
                new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("LeftSpeed",
                new KeyTrigger(KeyInput.KEY_A));

        inputManager.addListener(analogListener, "Shot");
        inputManager.addListener(analogListener, "MoveRight");
        inputManager.addListener(analogListener, "MoveLeft");
        inputManager.addListener(analogListener, "Restart");
        inputManager.addListener(analogListener, "RightSpeed");
        inputManager.addListener(analogListener, "LeftSpeed");
        inputManager.addListener(analogListener, "Pause");

    }

    //Intial all targets which are needed in the three levels.
    private void initialTargets() {
        ball = (Node) assetManager.loadModel("Models/anime.j3o");
        ball.scale(0.4f);
        Material matForball = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matForball.setTexture("DiffuseMap", assetManager.loadTexture("Textures/ball.png"));
        ball.setMaterial(matForball);
        rootNode.attachChild(ball);

        //Create all targets and load them.
        target1 = new Geometry("target", new Sphere(50, 50, 0.5f));
        //Set texture for it.
        Material matForTarget = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matForTarget.setTexture("DiffuseMap", assetManager.loadTexture("Textures/target.png"));
        target1.setMaterial(matForTarget);

        target2 = new Geometry("target2", new Sphere(50, 50, 0.45f));
        matForTarget.setTexture("DiffuseMap", assetManager.loadTexture("Textures/target.png"));
        target2.setMaterial(matForTarget);

        target3 = new Geometry("target3", new Sphere(50, 50, 0.6f));
        matForTarget.setTexture("DiffuseMap", assetManager.loadTexture("Textures/target.png"));
        target3.setMaterial(matForTarget);

        //Attach them to node.
        rootNode.attachChild(target1);
        rootNode.attachChild(target2);
        rootNode.attachChild(target3);

        target4 = new Geometry("target4", new Sphere(50, 50, 0.35f));
        matForTarget.setTexture("DiffuseMap", assetManager.loadTexture("Textures/target.png"));
        target4.setMaterial(matForTarget);
        target4.setLocalTranslation(10, 10, 10);
        rootNode.attachChild(target4);

        target5 = new Geometry("target5", new Sphere(50, 50, 0.45f));
        matForTarget.setTexture("DiffuseMap", assetManager.loadTexture("Textures/target.png"));
        target5.setMaterial(matForTarget);
        target5.setLocalTranslation(10, 10, 10);
        rootNode.attachChild(target5);

        target6 = new Geometry("target6", new Sphere(50, 50, 0.35f));
        matForTarget.setTexture("DiffuseMap", assetManager.loadTexture("Textures/target.png"));
        target6.setMaterial(matForTarget);
        target6.setLocalTranslation(10, 10, 10);
        rootNode.attachChild(target6);
    }

    private void initialtable() {
        // Create the table.
        table = (Node) assetManager.loadModel("Models/table.j3o");
        Material matFortable = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matFortable.setTexture("DiffuseMap", assetManager.loadTexture("Textures/table.jpg"));
        table.setMaterial(matFortable);
        rootNode.attachChild(table);
    }

    private void initialpaddle() {
        // Create the paddle.
        paddle = (Node) assetManager.loadModel("Models/paddle.j3o");
        Material matForpaddle = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        matForpaddle.setTexture("DiffuseMap", assetManager.loadTexture("Textures/paddle2.jpg"));
        paddle.setMaterial(matForpaddle);//Set material.
        paddle.scale(1, 1, 0.7f);//Set scale.
        rootNode.attachChild(paddle);//Attach to node.
    }

    private void initialLight() {
        //Initial the light.
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, -1, -1));
        rootNode.addLight(sun);
        //set up a point light
        PointLight myLight = new PointLight();
        myLight.setColor(ColorRGBA.White);
        myLight.setPosition(new Vector3f(0, 2, 2));
        myLight.setRadius(20);
        rootNode.addLight(myLight);

        // Casting shadow.
        ball.setShadowMode(RenderQueue.ShadowMode.Cast);
        // The table can both cast and receive
        table.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // setting up the renderers.
        PointLightShadowRenderer plsr = new PointLightShadowRenderer(assetManager, 512);

        plsr.setLight(myLight);
        plsr.setFlushQueues(false);

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 512, 2);
        dlsr.setLight(sun);

        // Make us see.
        viewPort.addProcessor(plsr);
        viewPort.addProcessor(dlsr);

        //Set camera location to what we see.
        cam.setLocation(new Vector3f(0, 17, 6));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.clearViewportChanged();
    }

    //Check ball collison with paddle.
    private void CheckpaddleCollision(CollisionResults resultBallPaddle) {
        if (resultBallPaddle.size() > 0) {

            CollisionSound.playInstance();
            velocity.setZ(-FastMath.abs(velocity.getZ()));
            //Friction on x axis.
            if (paddle.getLocalTranslation().getX() < 0) {
                velocity.setX(velocity.getX() + 0.5f);

            } else if (paddle.getLocalTranslation().getX() > 0) {
                velocity.setX(velocity.getX() - 0.5f);

            }

            //Change type of paddle to give collison feedback.
            Material matForpaddle = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            if (paddlecheck == 0) {
                matForpaddle.setTexture("DiffuseMap", assetManager.loadTexture("Textures/paddle1.jpg"));
                paddle.setMaterial(matForpaddle);
                paddlecheck = 1;
            } else {
                matForpaddle.setTexture("DiffuseMap", assetManager.loadTexture("Textures/paddle2.jpg"));
                paddle.setMaterial(matForpaddle);
                paddlecheck = 0;
            }

        }
    }

    //Check ball collison with table.
    private void checkCollsiontable() {
        CollisionResults resultTop = new CollisionResults();
        CollisionResults resultDown = new CollisionResults();
        CollisionResults resultLeft = new CollisionResults();
        CollisionResults resultRight = new CollisionResults();

        ball.collideWith(up, resultTop);
        ball.collideWith(down, resultDown);
        ball.collideWith(left, resultLeft);
        ball.collideWith(right, resultRight);
        if (resultTop.size() > 0) {

            velocity.setZ(FastMath.abs(velocity.getZ()));
            velocity = velocity.mult(1.005f);
            CollisionSound.playInstance();
        } else if (resultDown.size() > 0) {
            //If the ball out of field, the game go end.
            GG();

        } else if (resultLeft.size() > 0) {
            //Check left side table.
            velocity.setX(FastMath.abs(velocity.getX()));
            velocity = velocity.mult(1.005f);
            CollisionSound.playInstance();
        } else if (resultRight.size() > 0) {
            //Check right side table.
            velocity.setX(-FastMath.abs(velocity.getX()));
            velocity = velocity.mult(1.005f);
            CollisionSound.playInstance();
        }
    }

    private void checkcollisionTarget(CollisionResults resultBalltarget, Geometry target) {
        if (resultBalltarget.size() > 0) {

            CollisionSound2.playInstance();
            score += 1;
            //normal vector from ball to t1
            Vector3f norm = new Vector3f((target.getLocalTranslation().subtract(ball.getLocalTranslation())).normalize());
            //length of projection on norm
            float projVal = velocity.dot(norm);
            //vector projrction
            Vector3f projection = norm.mult(projVal);
            //parall vector
            Vector3f parall = velocity.subtract(projection);
            velocity = parall.subtract(projection);
            target.move(10, 10, 10);// Move the target to garbage.

        }
    }

    //Create void to make game go end.
    protected void GG() {
        rotationSpeed = 0;
        velocity = new Vector3f(0, 0, 0);
        //Set ball away.
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 2.5));
        //Set the paddle away to end the game.
        paddle.setLocalTranslation(0, 0, 10);
        // Make the interface of lost appear in front of stats view
        p.move(0, 0, 1);
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/GG.jpg", false);
        BGM.pause();

    }

    //Create the situation of first level. 
    protected void Level_1() {
        level = 1;
        score = 0;
        //Set up the position of all targets.
        target1.setLocalTranslation(-2f, 0, -2f);
        target2.setLocalTranslation(1f, 0, -2f);
        target3.setLocalTranslation(3f, 0, -2f);
        //Set up paddle.
        paddle.setLocalTranslation(0, 0, 6);
        //Set up ball's position.
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 3));

        //Set up ball's postition and speed. Shot randomly.
        rotationSpeed = 2;
        float num = new Random().nextFloat() * 2 - 1;
        velocity = new Vector3f(num, 0, -1);
        velocity = velocity.mult(5f);
        shout = false;
    }

    //Create the situation of second level. 
    protected void Level_2() {

        level = 2;
        score = 0;
        BGM.setVolume(0.15f);

        //set up new position of all targets, ball and the paddle.
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 3));
        target1.setLocalTranslation(-3f, 0, -2f);
        target2.setLocalTranslation(1f, 0, -2f);
        target3.setLocalTranslation(-2f, 0, 2f);
        target4.setLocalTranslation(2f, 0, 2f);
        target5.setLocalTranslation(1f, 0, -3f);
        paddle.setLocalTranslation(0, 0, 6);

        //Set up ball's postition and speed. Shot randomly.
        rotationSpeed = 3;
        float num = new Random().nextFloat() * 2 - 1;
        velocity = new Vector3f(num, 0, -1);
        velocity = velocity.mult(7f);
        shout = false;
    }

    //Create the situation of third level. 
    protected void Level_3() {

        level = 3;
        score = 0;
        BGM.setVolume(0.2f);

        //Set up all the position of the ball, targets and the paddle.
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 3));
        target1.setLocalTranslation(0, 0, 3f);
        target2.setLocalTranslation(-3f, 0, 0);
        target3.setLocalTranslation(2f, 0, 0);
        target4.setLocalTranslation(-1f, 0, -3f);
        target5.setLocalTranslation(2f, 0, -3f);
        target6.setLocalTranslation(0, 0, 4f);
        paddle.setLocalTranslation(0, 0, 6);

        //Set up ball's postition and speed. Shot randomly.
        rotationSpeed = 4;
        float num = new Random().nextFloat() * 2 - 1;
        velocity = new Vector3f(num, 0, -1);
        velocity = velocity.mult(12f);
        paddle.setLocalTranslation(0, 0, 6);
        shout = false;
    }

    protected void Win() {

        // Show the interface of winning.
        p.move(0, 0, 1);
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/win.jpg", false);

        BGM.pause();
        velocity = velocity.mult(0f);
    }

    //The loop and check collision for the game/
    @Override
    public void simpleUpdate(float tpf) {

        text.setText("Level " + level + "\n" + "Your Score: " + (score));
        if (GameRun == true) {
            if (shout) {
                ball.rotate(0, rotationSpeed * FastMath.PI * tpf, 0);
                //initial move!
                ball.move(velocity.mult(tpf));
            }
            //Check if paddle collision with the table.

            CollisionResults resultsPaddle = new CollisionResults();
            BoundingVolume bvBord = paddle.getWorldBound();
            table.collideWith(bvBord, resultsPaddle);

            if (resultsPaddle.size() > 0) {
                //Restrict the move range of the paddle. 
                if (paddle.getLocalTranslation().x < 0) {
                    paddle.setLocalTranslation((float) -3.65, 0, 6);
                } else {
                    paddle.setLocalTranslation((float) 3.65, 0, 6);
                }
            }

            //Ball collision with table.
            checkCollsiontable();

            //Ball collision with paddle.
            CollisionResults resultBallPaddle = new CollisionResults();
            BoundingVolume bvBall = ball.getWorldBound();
            paddle.collideWith(bvBall, resultBallPaddle);
            CheckpaddleCollision(resultBallPaddle);

            //Ball collision with target1.
            CollisionResults resultBalltarget_1 = new CollisionResults();
            BoundingVolume bvBall_2 = target1.getWorldBound();
            ball.collideWith(bvBall_2, resultBalltarget_1);
            checkcollisionTarget(resultBalltarget_1, target1);

            //Ball collision with target2.
            CollisionResults resultBalltarget_2 = new CollisionResults();
            BoundingVolume bvBall_3 = target2.getWorldBound();
            ball.collideWith(bvBall_3, resultBalltarget_2);
            checkcollisionTarget(resultBalltarget_2, target2);

            //Ball collision with target3.
            CollisionResults resultBalltarget_3 = new CollisionResults();
            BoundingVolume bvBall_4 = target3.getWorldBound();
            ball.collideWith(bvBall_4, resultBalltarget_3);
            checkcollisionTarget(resultBalltarget_3, target3);

            //Ball collision with target4.
            CollisionResults resultBalltarget_4 = new CollisionResults();
            BoundingVolume bvBall_5 = target4.getWorldBound();
            ball.collideWith(bvBall_5, resultBalltarget_4);
            checkcollisionTarget(resultBalltarget_4, target4);

            //Ball collision with target5.
            CollisionResults resultBalltarget_5 = new CollisionResults();
            BoundingVolume bvBall_6 = target5.getWorldBound();
            ball.collideWith(bvBall_6, resultBalltarget_5);
            checkcollisionTarget(resultBalltarget_5, target5);

            //Ball collision with target6.
            CollisionResults resultBalltarget_6 = new CollisionResults();
            BoundingVolume bvBall_7 = target6.getWorldBound();
            ball.collideWith(bvBall_7, resultBalltarget_6);
            checkcollisionTarget(resultBalltarget_6, target6);
        }
        //Loop the game.
        if (score == 3 && level == 1) {
            Level_2();
        }
        if (score == 5 && level == 2) {
            Level_3();
        }
        if (score == 6 && level == 3) {
            Win();
        }
    }

    public static void main(String[] args) {
        app = new Main();
        app.showSettings = false;
        initialSettings();
        app.setSettings(mySettings);
        //Start the game.
        app.start();
    }
}
