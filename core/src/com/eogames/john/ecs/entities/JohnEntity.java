package com.eogames.john.ecs.entities;

import com.badlogic.ashley.core.Entity;
import com.eogames.john.ecs.components.AnimationComponent;
import com.eogames.john.ecs.components.PhysicComponent;
import com.eogames.john.ecs.components.PositionComponent;
import com.eogames.john.ecs.components.VelocityComponent;

public class JohnEntity extends Entity {

  public JohnEntity() {
    add(new PositionComponent());
    add(new VelocityComponent());
    add(new AnimationComponent());
    add(new PhysicComponent());
  }
}
