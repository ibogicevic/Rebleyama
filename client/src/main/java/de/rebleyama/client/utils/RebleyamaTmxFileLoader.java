package de.rebleyama.client.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import de.rebleyama.lib.game.TileType;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class RebleyamaTmxFileLoader extends TmxMapLoader {
    @Override
    protected void loadTileSet(TiledMap map, XmlReader.Element element, FileHandle tmxFile, ImageResolver imageResolver) {
        super.loadTileSet(map, element, tmxFile, imageResolver);

        if (element.getName().equals("tileset")) {
            String source = element.getAttribute("source", null);
            if (source != null) {
                FileHandle tsx = getRelativeFileHandle(tmxFile, source);
                element = xml.parse(tsx);
            }

            XmlReader.Element terrains = element.getChildByName("terrain");

            if (terrains != null) {
                //Generate a terrain to TileType mapping
                Map<Integer, TileType> terrainMapping =
                        ClientTileStructureGenerator.readTerrainIdToTileTypeMappingFromTSX(element);
                TiledMapTileSet tileSet = map.getTileSets().getTileSet(element.get("name", null));
                tileSet.getProperties().put("rebleyamaTerrainMapping", terrainMapping);

                //Give every tile a TileType
                tileSet.forEach((tile) -> {
                    MapProperties tileProp = tile.getProperties();
                    String terrainProp = tileProp.get("terrain", null);
                    if (terrainProp != null) {
                        String[] splitTiledTerrain = terrainProp.split(",");
                        Map<Integer, Integer> countInts = new HashMap<>(6, 0.8f);

                        for (String terr : splitTiledTerrain) {
                            int terrToInt = Integer.parseInt(terr);
                            if (countInts.get(terrToInt) != null) {
                                countInts.replace(terrToInt, countInts.get(terrToInt),
                                        countInts.get(terrToInt) + 1);
                            } else {
                                countInts.put(terrToInt, 1);
                            }
                        }

                        int terrainId = Collections.max(countInts.entrySet(),
                                Comparator.comparingInt(Map.Entry<Integer, Integer>::getValue)).getKey();
                        tileProp.put("rebleyamaTerrain", terrainMapping.get(terrainId));
                    }
                });
            }
        }
    }

    public TiledMapTileSet loadTSX(FileHandle file) {
        XmlReader.Element root = xml.parse(file);
        TiledMap map = new TiledMap();

        ObjectMap<String, FileHandle> tileSetTextureFileMap = loadImageFromTSX(root, file);
        ObjectMap<String, Texture> tileSetTextureMap = new ObjectMap<>();
        for (ObjectMap.Entry<String, FileHandle> entry : tileSetTextureFileMap) {
            tileSetTextureMap.put(entry.key, new Texture(entry.value));
        }

        ImageResolver.DirectImageResolver imageResolver = new ImageResolver.DirectImageResolver(tileSetTextureMap);

        loadTileSet(map, root, file, imageResolver);

        return map.getTileSets().getTileSet(0);

    }

    protected ObjectMap<String, FileHandle> loadImageFromTSX(XmlReader.Element root, FileHandle tsxFile) {
        ObjectMap<String, FileHandle> result = new ObjectMap<>();
        if (root.getName().equals("tileset")) {
            XmlReader.Element image = root.getChildByName("image");
            if (image != null) {
                String imageSource = image.getAttribute("source", null);
                if (imageSource != null) {
                    result.put(imageSource, getRelativeFileHandle(tsxFile, imageSource));
                }
            }
        }

        return result;
    }
}
