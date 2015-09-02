/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.cities;

import java.util.Collections;

import org.terasology.cities.sites.Settlement;
import org.terasology.cities.sites.SettlementFacet;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.spawner.AbstractSpawner;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;

/**
 * Spawns entities at the center of the closest settlement.
 * Requires a {@link SettlementFacet} provider.
 */
public class CitySpawner extends AbstractSpawner {

    private final int searchRadius = 16;

    @Override
    public Vector3f getSpawnPosition(World world, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        Vector3f pos = location.getWorldPosition();
        Vector2i pos2d = new Vector2i(pos.x, pos.z);
        Region3i region = Region3i.createFromCenterExtents(pos, new Vector3f(32, 1, 32));
        Region data = world.getWorldData(region);
        SettlementFacet settlementFacet = data.getFacet(SettlementFacet.class);
        Vector2i searchPos;
        if (!settlementFacet.getSettlements().isEmpty()) {
            Settlement closest = Collections.min(settlementFacet.getSettlements(),
                    (a, b) -> a.getPos().distanceSquared(pos2d) - b.getPos().distanceSquared(pos2d));

            searchPos = closest.getPos();
        } else {
            searchPos = pos2d;
        }
        Vector3f realPos = findSpawnPosition(world, searchPos, searchRadius);
        return realPos;
    }

}