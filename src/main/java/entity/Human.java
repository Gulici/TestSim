package entity;

import ai.FollowPath;
import ai.FollowPathToExit;
import ai.FollowToHuman;
import controller.EntityController;
import java.lang.Comparable;
import core.Motion;
import core.Size;
import sim.Sim;
import core.Group;
import core.Position;
import java.awt.*;

public class Human extends Agent implements Comparable<Human> {

    FollowPath followPath;
    Group group;
    private Color color;
    private int ticksPathChange;
    private double speed = 1;
    private String state;
    private int knockOverCounter;
    private int pushCounter;
    public Human(Sim sim, EntityController entityController) {
        super(entityController);
        setSize(new Size(6,6));
        setPosition(sim.getMap().getRandomPosition());
        color = Color.CYAN;
        pushCounter = 0;
        knockOverCounter = 0;
        state = "FollowPath";
        this.motion = new Motion(this.speed);
        followPath = new FollowPathToExit();
        group = new Group(this);
    }

    @Override
    public int compareTo(Human h) {
        return followPath.getLength() - h.getPathLength();
    }

    @Override
    public void update(Sim sim) {
        if(ticksPathChange == 300) {
            ticksPathChange = 0;
            if(group.getLeader() != this) {
                followPath = new FollowToHuman(group.getLeader());
            }
        }
        if (ticksPathChange%30 == 0 && group.getLeader() == this) {
              Position mean = group.meanPosition();
              double distance = this.getCenterPosition().distanceTo(mean);
              double new_speed = this.speed - 1/60 *distance;
              if (new_speed > 0) {
                  this.motion = new Motion(new_speed);
              }
              else
                  this.motion = new Motion(0);
        }
    updateState();
        switch (state) {
            case "FollowPath" -> {
                followPath.update(this, sim); handleMotion();
                handleCollisions(sim);
                apply(motion);
            }
            case "KnockOver" -> {}
            case "Trampled" -> sim.addToKillList(this);
        }
        followPath.update(this, sim);
        super.update(sim);
    }

    private void updateState() {
        switch (state) {
            case "FollowPath" -> {
                if (pushCounter >= 5) {
                    state = "KnockOver";
                    knockOverCounter = 60;
                    color = Color.YELLOW;
                }
            }
            case "KnockOver" -> {
                if (pushCounter >= 20) {
                    state = "Trampled";
                    break;
                }
                if (knockOverCounter > 0) {
                    knockOverCounter --;
                }
                else {
                    state = "FollowPath";
                    pushCounter = 0;
                    color = Color.cyan;
                }
            }
            default -> state = "FollowPath";
        }
    }

    @Override
    public void handleCollision(Entity other, Sim sim) {
        super.handleCollision(other, sim);
        if(other instanceof Human) {
            Human other_human = (Human)other;
            color = Color.BLUE;
            if(!this.group.contains(other_human)) {
                Group merged = group.merge(other_human.getGroup());
                this.group = merged;
                other_human.setGroup(merged);
            }
        }
    }


    public void setGroup(Group g) {
        group = g;
    }
    public Group getGroup() {
        return group;
    }

    @Override
    public void draw(Graphics2D graphics2D) {
        graphics2D.setColor(color);
        graphics2D.fillRect(getCenterPosition().intX(), getCenterPosition().intY(), size.getWidth(), size.getHeight());
    }
    public void increasePushCounter() {
        pushCounter++;
    }
    public int getPathLength() {
        return followPath.getLength();
    }
}
