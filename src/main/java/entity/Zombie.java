package entity;
import ai.FollowPath;
import ai.FollowPathToExit;
import ai.FollowToHuman;
import controller.EntityController;
import java.awt.Color;
import sim.Sim;
import core.Size;
import core.Position;
import entity.Human;
import java.awt.*;

public class Zombie extends Human {
    FollowPath followPath;
    private int range = 50;
    private Human target = null;
    int ticks = 0;
    public Zombie(Sim sim, EntityController entityController) {
        super(sim, entityController);
        followPath = new FollowPathToExit();
        color = Color.GREEN;
    }


    public int getRange() {
        return range;
    }

    @Override
    public void update(Sim sim) {
        if (ticks == 30) {
            sim.chaseInRangeEntities(this);
            ticks = 0;
        }
        ticks++;
        followPath.update(this, sim);
        handleMotion();
        handleCollisions(sim);
        sim.removeFromRegion(this);
        apply(motion);
        sim.updateRegion(this);
    }

    @Override
    public void handleCollision(Entity other, Sim sim) {
        if (other instanceof Human && !(other instanceof Zombie))
            ((Human)other).damage(150);
    }

    public boolean start_chasing(Entity en) {
        if (en instanceof Human && !(en instanceof Zombie) && getPosition().distanceTo(en.getPosition()) < range) {
            followPath = new FollowToHuman((Human)en);
            target = (Human)en;
            //System.out.println("Selected target");
            return true;
        }
        return false;
    }
    @Override
    public void draw(Graphics2D graphics2D) {
        super.draw(graphics2D);
        graphics2D.setColor(Color.PINK);
        for(Position p : followPath.getPath())
            graphics2D.fillRect(p.intX(), p.intY(), size.getWidth(), size.getHeight());
        graphics2D.drawArc(this.getCenterPosition().intX() - range/2 + size.getWidth()/2, this.getCenterPosition().intY() - range/2 + size.getHeight()/2, range, range, 0, 360);
    }
}
