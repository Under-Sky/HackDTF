package ru.itlab.hackdtf.Characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;

import ru.itlab.hackdtf.CreateFixture;
import ru.itlab.hackdtf.Screens.GameScreen;
import ru.itlab.hackdtf.Weapons.Gun;

public class Enemy extends Actor {

    Texture texture;
    public Fixture body;
    boolean isSlowLast = false;
    int speed, health = 2;
    Player player;
    long lastShoot = 0;
    World world;
    Gun gun;
    Stage stage;

    public Enemy(Stage stage, World world, Player player) {
        this.world = world;
        this.stage = stage;
        this.player = player;
        speed = player.speed / 10;
        texture = new Texture(Gdx.files.internal("enemy.png"));
        body = CreateFixture.createCircle(world, new Vector2(320, 180), 10, false, "enemy", (short) 2);
        body.getBody().setTransform(new Vector2(200, 300), 0);
        gun = new Gun(stage, world, 1, true, player);
        player.guns.add(gun);
        stage.addActor(gun);
        player.enemies.add(this);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(isSlowLast != GameScreen.isSlow)
            useBraking(GameScreen.isSlow);

        body.getBody().setLinearVelocity((float) Math.cos(body.getBody().getAngle()) * speed * delta,
                (float) Math.sin(body.getBody().getAngle()) * speed * delta);
        body.getBody().setAngularVelocity(0);
        body.getBody().setAngularDamping(0);
        gun.updatePos(body.getBody().getPosition(), (float) Math.toDegrees(body.getBody().getAngle()), body.getShape().getRadius());
        //body.getBody().getTransform().setRotation((float) Math.atan2(x, y));
        //TODO logic of enemies (use player + logic of UFOB enemies)

        float xp = player.body.getBody().getPosition().x;
        float yp = player.body.getBody().getPosition().y;
        float xe = body.getBody().getPosition().x;
        float ye = body.getBody().getPosition().y;
        double distance = Math.sqrt((xp - xe) * (xp - xe) + (yp - ye) * (yp - ye));
        float angleRadian = (float) (Math.atan2((yp - ye) / distance, (xp - xe) / distance));
        body.getBody().setTransform(body.getBody().getPosition(), angleRadian);
        gun.updatePos(body.getBody().getPosition(), (float) Math.toDegrees(body.getBody().getAngle()), body.getShape().getRadius());
        int tmp = 1;
        if (isSlowLast) {
            tmp = GameScreen.braker / 10;
        }
        if (TimeUtils.millis() - lastShoot > Math.random() * 80000 * tmp) {
            gun.shoot();
            lastShoot = TimeUtils.millis();
        }

        if (health <= 0) {
            destroy();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(texture,
                body.getBody().getPosition().x - body.getShape().getRadius(),
                body.getBody().getPosition().y - body.getShape().getRadius(),
                body.getShape().getRadius() + 0, //center for rotation and scaling x
                body.getShape().getRadius() + 0, //center for rotation and scaling y
                body.getShape().getRadius() * 2,
                body.getShape().getRadius() * 2,
                1, 1, //scale from center
                (float) Math.toDegrees(body.getBody().getAngle()) + 0,
                0, 0, //coordinates in image file
                texture.getWidth() + 0, //size in image file
                texture.getHeight() + 0,
                false, false);
    }

    public void damaged() {
        health--;
    }

    public void destroy() {
        gun.isDropped = true;
        gun.isEnemy = false;
        player.enemies.removeValue(this, true);
        world.destroyBody(body.getBody());
        stage.getActors().removeValue(this, true);
    }

    @Override
    public float getRotation() {
        return body.getBody().getAngle();
    }

    public void useBraking(boolean isSlow) {
        if (isSlow) speed /= GameScreen.braker;
        else speed *= GameScreen.braker;
        isSlowLast = isSlow;
    }

    public boolean equals(Fixture f){
        return f.equals(body);
    }
}
