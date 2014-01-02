/*
 * Copyright 2013 MovingBlocks
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

import java.awt.Shape;
import java.util.Set;

import javax.vecmath.Point2i;

import org.terasology.cities.common.CachingFunction;
import org.terasology.cities.common.Orientation;
import org.terasology.cities.common.UnorderedPair;
import org.terasology.cities.generator.CityConnector;
import org.terasology.cities.generator.CityPlacerRandom;
import org.terasology.cities.generator.DefaultTownWallGenerator;
import org.terasology.cities.generator.LotGeneratorRandom;
import org.terasology.cities.generator.RoadGeneratorSimple;
import org.terasology.cities.generator.RoadModifierRandom;
import org.terasology.cities.generator.RoadShapeGenerator;
import org.terasology.cities.generator.SimpleChurchGenerator;
import org.terasology.cities.generator.SimpleFenceGenerator;
import org.terasology.cities.generator.SimpleHousingGenerator;
import org.terasology.cities.generator.TownWallShapeGenerator;
import org.terasology.cities.model.City;
import org.terasology.cities.model.Junction;
import org.terasology.cities.model.MedievalTown;
import org.terasology.cities.model.Road;
import org.terasology.cities.model.Sector;
import org.terasology.cities.model.SimpleBuilding;
import org.terasology.cities.model.SimpleChurch;
import org.terasology.cities.model.SimpleFence;
import org.terasology.cities.model.SimpleLot;
import org.terasology.cities.model.TownWall;
import org.terasology.cities.terrain.HeightMap;
import org.terasology.engine.CoreRegistry;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

/**
 * Provides many different getters to rasterize a world
 * @author Martin Steiger
 */
public class WorldFacade {

    private CachingFunction<Sector, Set<City>> decoratedCities;

    private Function<City, Set<City>> connectedCities;

    private Function<Sector, Set<UnorderedPair<City>>> sectorConnections;

    private Function<Point2i, Junction> junctions;

    private Function<Sector, Set<Road>> roadMap;

    private Function<Sector, Shape> roadShapeFunc;

    /**
     * @param seed the seed value
     * @param heightMap the height map to use
     * @param config the world configuration
     */
    public WorldFacade(String seed, final HeightMap heightMap, final CityWorldConfig config) {

        final Function<Sector, SectorInfo> sectorInfos = CachingFunction.wrap(new Function<Sector, SectorInfo>() {

            @Override
            public SectorInfo apply(Sector input) {
                return new SectorInfo(input, config, heightMap);
            }
        }); 
                
        junctions = new Function<Point2i, Junction>() {

            @Override
            public Junction apply(Point2i input) {
                return new Junction(input);
            }
            
        };
        junctions = CachingFunction.wrap(junctions);
        
        int minCitiesPerSector = config.getMinCitiesPerSector();
        int maxCitiesPerSector = config.getMaxCitiesPerSector();
        int minSize = config.getMinCityRadius();
        int maxSize = config.getMaxCityRadius();
        
        CityPlacerRandom cpr = new CityPlacerRandom(seed, sectorInfos, minCitiesPerSector, maxCitiesPerSector, minSize, maxSize);
        final Function<Sector, Set<City>> cityMap = CachingFunction.wrap(cpr);
        
        double maxDist = config.getMaxConnectedCitiesDistance();
        connectedCities = new CityConnector(cityMap, maxDist);
        connectedCities = CachingFunction.wrap(connectedCities);
        
        sectorConnections = new SectorConnector(cityMap, connectedCities);
        sectorConnections = CachingFunction.wrap(sectorConnections);

        Function<UnorderedPair<City>, Road> rg = new Function<UnorderedPair<City>, Road>() {
            private RoadGeneratorSimple rgs = new RoadGeneratorSimple(junctions);
            private RoadModifierRandom rmr = new RoadModifierRandom(10);

            @Override
            public Road apply(UnorderedPair<City> input) {
                Road road = rgs.apply(input);
                rmr.apply(road);
                return road;
            }
            
        };
        
        final Function<UnorderedPair<City>, Road> cachedRoadgen = CachingFunction.wrap(rg);

        roadMap = new Function<Sector, Set<Road>>() {

            @Override
            public Set<Road> apply(Sector sector) {
                Set<Road> allRoads = Sets.newHashSet();
                
                Set<UnorderedPair<City>> localConns = sectorConnections.apply(sector);
                Set<UnorderedPair<City>> allConns = Sets.newHashSet(localConns);
                
                // add all neighbors, because their roads might be passing through
                for (Orientation dir : Orientation.values()) {
                    Sector neighbor = sector.getNeighbor(dir);

                    allConns.addAll(sectorConnections.apply(neighbor));
                }

                for (UnorderedPair<City> conn : allConns) {
                    Road road = cachedRoadgen.apply(conn);
                    allRoads.add(road);
                }

                return allRoads;
            }
        };
        
        roadMap = CachingFunction.wrap(roadMap);

        roadShapeFunc = new RoadShapeGenerator(roadMap);
        roadShapeFunc = CachingFunction.wrap(roadShapeFunc);
        
        final DefaultTownWallGenerator twg = new DefaultTownWallGenerator(seed, heightMap);
        final LotGeneratorRandom housingLotGenerator = new LotGeneratorRandom(seed);
        final LotGeneratorRandom churchLotGenerator = new LotGeneratorRandom(seed, 25d, 40d, 1, 100);
        final SimpleHousingGenerator blgGenerator = new SimpleHousingGenerator(seed, heightMap);
        final SimpleFenceGenerator sfg = new SimpleFenceGenerator(seed);
        final SimpleChurchGenerator sacg = new SimpleChurchGenerator(seed, heightMap);

        decoratedCities = CachingFunction.wrap(new Function<Sector, Set<City>>() {
            
            @Override
            public Set<City> apply(Sector input) {
                
                SectorInfo si = sectorInfos.apply(input);
                Set<City> cities = cityMap.apply(input);
                
                Shape roadShape = roadShapeFunc.apply(input);
                si.addBlockedArea(roadShape);

                for (City city : cities) {
                    
                    if (city instanceof MedievalTown) {
                        MedievalTown town = (MedievalTown) city;
                        TownWall tw = twg.generate(city, si);
                        town.setTownWall(tw);

                        TownWallShapeGenerator twsg = new TownWallShapeGenerator();
                        Shape townWallShape = twsg.computeShape(tw);
                        si.addBlockedArea(townWallShape);
                    }

                    if (city instanceof MedievalTown) {
                        Set<SimpleLot> lots = churchLotGenerator.generate(city, si);
                        if (!lots.isEmpty()) {
                            SimpleLot lot = lots.iterator().next();
                            SimpleChurch church = sacg.generate(lot);
                            lot.addBuilding(church);
                            city.add(lot);
                        }
                    }
                    
                    Set<SimpleLot> lots = housingLotGenerator.generate(city, si);
                    
                    for (SimpleLot lot : lots) {
                        city.add(lot);
                        
                        for (SimpleBuilding bldg : blgGenerator.apply(lot)) {
                            lot.addBuilding(bldg);
                            SimpleFence fence = sfg.createFence(city, lot.getShape());
                            lot.setFence(fence);
                        }
                    }
                }
                return cities;
            }
        });
        
        CoreRegistry.putPermanently(WorldFacade.class, this);
    }
    
    /**
     * Clears the caches
     */
    public void expungeCache() {
        decoratedCities.invalidateAll();
    }

    /**
     * @param sector the sector
     * @return a shape that describes the area of all roads
     */
    public Shape getRoadArea(Sector sector) {
        return roadShapeFunc.apply(sector);
    }

    /**
     * @param sector the sector
     * @return all cities in that sector
     */
    public Set<City> getCities(Sector sector) {
        return decoratedCities.apply(sector);
    }

}
