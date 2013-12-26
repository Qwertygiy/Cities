/*
 * Copyright 2013 MovingBlocks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terasology.cities.raster.standard;

import java.awt.Rectangle;

import org.terasology.cities.BlockTypes;
import org.terasology.cities.model.HipRoof;
import org.terasology.cities.raster.Brush;
import org.terasology.cities.raster.Rasterizer;
import org.terasology.cities.raster.TerrainInfo;
import org.terasology.cities.terrain.HeightMap;
import org.terasology.cities.terrain.HeightMapAdapter;
import org.terasology.cities.terrain.OffsetHeightMap;
import org.terasology.math.TeraMath;

/**
 * Converts a {@link HipRoof} into blocks
 * @author Martin Steiger
 */
public class HipRoofRasterizer implements Rasterizer<HipRoof> {
    
    @Override
    public void raster(Brush brush, TerrainInfo ti, final HipRoof roof) {
        final Rectangle area = roof.getArea();

        if (!brush.affects(area)) {
            return;
        }
        
        // this is the ground truth
        // maxHeight = baseHeight + Math.min(cur.width, cur.height) * pitch / 2;
        
        HeightMap hm = new HeightMapAdapter() {

            @Override
            public int apply(int x, int z) {
                int rx = x - area.x;
                int rz = z - area.y;

                // distance to border of the roof
                int borderDistX = Math.min(rx, area.width - 1 - rx);
                int borderDistZ = Math.min(rz, area.height - 1 - rz);

                int dist = Math.min(borderDistX, borderDistZ);

                int y = TeraMath.floorToInt(roof.getBaseHeight() + dist * roof.getPitch());
                return Math.min(y, roof.getMaxHeight());
            }
        };

        brush.fillRect(area, hm, new OffsetHeightMap(hm, (int) roof.getPitch()), BlockTypes.ROOF_HIP);
    }

}