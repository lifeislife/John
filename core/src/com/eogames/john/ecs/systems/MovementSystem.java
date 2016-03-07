package com.eogames.john.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.eogames.john.ecs.components.PhysicComponent;
import com.eogames.john.ecs.components.TransformComponent;
import com.eogames.john.ecs.components.VelocityComponent;

public class MovementSystem extends IteratingSystem {
  private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
  private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
  private ComponentMapper<PhysicComponent> physicMapper = ComponentMapper.getFor(PhysicComponent.class);

  private Pool<Rectangle> rectPool = new Pool<Rectangle>()
  {
    @Override
    protected Rectangle newObject()
    {
      return new Rectangle();
    }
  };
  private Array<Rectangle> tiles = new Array<Rectangle>();

  private TiledMapTileLayer wallsLayer;
  private int tileHeight;
  private int tileWidth;

  public MovementSystem(TiledMap tiledMap) {
    super(Family.all(TransformComponent.class, VelocityComponent.class, PhysicComponent.class).get());
    this.wallsLayer = (TiledMapTileLayer) tiledMap.getLayers().get("walls");
    this.tileHeight = (int) wallsLayer.getTileHeight();
    this.tileWidth = (int) wallsLayer.getTileWidth();
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    TransformComponent transform = tm.get(entity);
    VelocityComponent velocity = vm.get(entity);
    PhysicComponent physic = physicMapper.get(entity);

    moveX(transform, velocity, physic, deltaTime);
    moveY(transform, velocity, physic, deltaTime);
  }

  private void moveX(TransformComponent transform, VelocityComponent velocity,
                     PhysicComponent physic, float deltaTime) {
    Rectangle entityRect = new Rectangle(transform.pos.x, transform.pos.y, physic.width, physic.height);
    int startX, endX, startY, endY;

    if (velocity.x == 0) {
      return;
    }
    else if (velocity.x > 0) {
      startX = Math.round(entityRect.x + entityRect.width);
      endX = Math.round(entityRect.x + entityRect.width + velocity.x * deltaTime);
    }
    else {
      startX = Math.round(entityRect.x + velocity.x * deltaTime);
      endX = Math.round(entityRect.x);
    }
    startY = Math.round(entityRect.y);
    endY = Math.round(entityRect.y + entityRect.height);
    getTiles(startX / tileWidth, startY / tileHeight, endX / tileWidth, endY / tileHeight, tiles);
    entityRect.x += velocity.x * deltaTime;

    for (Rectangle tile : tiles) {
      if (entityRect.overlaps(tile)) {
        if (velocity.x > 0) {
          transform.pos.x = tile.x - physic.width;
        }
        else {
          transform.pos.x = tile.x + tile.width;
        }
        velocity.x = 0.0f;
        break;
      }
    }
    transform.pos.x += velocity.x * deltaTime;
  }

  private void moveY(TransformComponent transform, VelocityComponent velocity,
                     PhysicComponent physic, float deltaTime) {
    Rectangle entityRect = new Rectangle(transform.pos.x, transform.pos.y, physic.width, physic.height);
    int startX, endX, startY, endY;

    if (velocity.y - velocity.gravity > 0.0f) {
      startY = Math.round(entityRect.y + physic.height + (velocity.y - velocity.gravity) * deltaTime);
      endY = Math.round(entityRect.y + physic.height);
    }
    else {
      startY = Math.round(entityRect.y + (velocity.y - velocity.gravity) * deltaTime);
      endY = Math.round(entityRect.y);
    }
    startX = Math.round(entityRect.x);
    endX = Math.round(entityRect.x + entityRect.getWidth());
    getTiles(startX / tileWidth, startY / tileHeight, endX / tileWidth, endY / tileHeight, tiles);
    entityRect.y += (velocity.y - velocity.gravity) * deltaTime;

    for (Rectangle tile : tiles) {
      if (entityRect.overlaps(tile)) {
        if (velocity.y - velocity.gravity > 0)
        {
          transform.pos.y = tile.y - physic.height;
        }
        else if (velocity.y - velocity.gravity < 0)
        {
          transform.pos.y = tile.y + tile.height + 1;
        }
        velocity.y = velocity.gravity;
        return;
      }
    }
    transform.pos.y += (velocity.y - velocity.gravity) * deltaTime;
    velocity.y *= 0.95f;
  }

  private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles)
  {
    rectPool.freeAll(tiles);
    tiles.clear();
    for (int y = startY; y <= endY; y++)
    {
      for (int x = startX; x <= endX; x++)
      {
        TiledMapTileLayer.Cell cell = this.wallsLayer.getCell(x, y);
        if (cell != null)
        {
          Rectangle rect = rectPool.obtain();
          rect.set(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
          tiles.add(rect);
        }
      }
    }
  }
}