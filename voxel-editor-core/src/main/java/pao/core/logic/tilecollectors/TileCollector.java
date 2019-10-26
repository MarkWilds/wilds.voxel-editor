package pao.core.logic.tilecollectors;

import pao.core.models.Map;

import wilds.rune.geometry.Plane;
import wilds.rune.geometry.Ray;

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * @author Mark "Wilds" van der Wal
 * @since 21-3-2018
 */
public interface TileCollector {

    IntSet getTileIndices();

    void setup(Plane plane);

    void start(Map map, Ray ray);

    void move(Map map, Ray ray);

    void stop(Map map, Ray ray);

    void teardown();
}
