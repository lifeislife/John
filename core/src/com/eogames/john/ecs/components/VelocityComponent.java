package com.eogames.john.ecs.components;

import com.badlogic.ashley.core.Component;

public class VelocityComponent implements Component {
  public float maxX = 150.0f;
  public float maxY = 1500.0f;
  public float x = 0.0f;
  public float y = 0.0f;
  public float gravity = 750.0f;
}
