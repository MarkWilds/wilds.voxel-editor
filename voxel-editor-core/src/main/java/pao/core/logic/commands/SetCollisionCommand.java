package pao.core.logic.commands;

import pao.core.models.Map;
import pao.core.services.MapService;

/**
 * @author Mark "Wilds" van der Wal
 * @since 1-2-2018
 */
public class SetCollisionCommand implements ICommand {
    private MapService mapService;
    private int x;
    private int y;
    private int collisionId;
    private int oldCollisionId;

    public SetCollisionCommand(int x, int y, int id, MapService mapService) {
        this.mapService = mapService;
        this.collisionId = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getDescription() {
        return String.format("Set collision data! Coordinate: %s, %s collisionId: %s", x, y, collisionId);
    }

    @Override
    public boolean execute() {
        Map map = mapService.getMap();

        if (x < 0 || y < 0
                || x >= map.getWidth() || y >= map.getHeight()) {
            return false;
        }

        int currentTile = mapService.getCollisionId(x, y);
        if (currentTile != collisionId) {
            oldCollisionId = mapService.setCollisionId(x, y, collisionId);
            return true;
        }

        return false;
    }

    @Override
    public void undo() {
        collisionId = mapService.setCollisionId(x, y, oldCollisionId);
    }
}
