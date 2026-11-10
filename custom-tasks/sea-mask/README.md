# TRIGGER sea mask

`custom-tasks/src/main/resources/trigger/sea_mask_osm.geojson` sets the sea pixels of the TRIGGER COGs to NoData (-9999), so that
the sea is transparent on the map. It is used by:

- `COGS3SyncProcessor` (ECMWF indices: shiwe, 2t, 2r, utci)
- `CIMAForecastProcessor` (CIMA WRF forecast, d03 Italy domain)

`land_mask.geojson` (Natural Earth 1:50m land) in the same folder is a different, older mask: it is used only by the optional sea
masking of `CAMSAirQualityProcessor` (task option `MASK_SEA`, default false) and by the local conversion of the historical CIMA COGs.

## Source and license

- OpenStreetMap land polygons, `land-polygons-split-3857.zip` with data of 2026-05-25 (regenerated 2026-11-10; the 2026-10-23 snapshot used before differs by 39 isolated pixels), from
  <https://osmdata.openstreetmap.de/data/land-polygons.html>
- Data © OpenStreetMap contributors, available under the Open Database License (ODbL). The maps showing these COGs should credit
  "© OpenStreetMap contributors" (the OSM base map of the demos and of the TRIGGER dashboard already does).

OSM was preferred to Natural Earth 1:10m (coast errors of 1-2 km, many missing islands) and to GSHHG 2.3.7 (2017): on the output
grid OSM and GSHHG differ by 0.5% of the land pixels, but the OSM coast matches the OSM base map of the clients.

## How it is built

The mask is not a copy of the land polygons: it is computed on the output grid of the COGs, so it must be regenerated if that grid
changes.

1. The land polygons are rasterized on the output grid: 2500 m EPSG:3857 pixels aligned with `gdalwarp -tap`, extent
   -2785000, 3490000, 5017500, 11755000 (3121 x 3306 pixels, ECMWF domain 25W-45E, ~29.9N-72N; the ECMWF COGs reach y 3500000, so the grid goes a few rows lower to mask their last rows). All touched pixels: every pixel touching
   land is land, so the coast and the small islands are always covered (data spill over the sea by at most one pixel).
2. The sea pixels are polygonized: the polygons follow the pixel edges, so `gdal_rasterize -burn -9999 sea_mask_osm.geojson` on any
   2500 m `-tap` grid inside that extent reproduces the mask exactly (checked by the script).
3. The polygons are written in EPSG:4326 (RFC 7946 GeoJSON), 5 decimals.

The tasks burn the **sea** polygons with a plain `gdal_rasterize`. Do not burn land polygons with `gdal_rasterize -i`: the inverse
geometry turns the islands inside lagoons (holes inside holes, e.g. the Arabat spit in Crimea) into sea. Do not use
`gdalwarp -cutline` either when the output grid differs from the source grid: GDAL rasterizes the cutline on the source grid (coast
in source-cell steps) and it shifted the nearest-neighbour sampling of 25% of the pixels in a test with the ECMWF 0.1° grid.

## Regenerate

Requirements: GDAL command line tools and Python with `rasterio` and `shapely` (a temporary virtual environment is enough;
rasterio ships its own GDAL and PROJ, the script keeps it separate from the PROJ of the command line tools).

```bash
curl -L -o land-polygons-split-3857.zip https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip   # ~900 MB
unzip -q land-polygons-split-3857.zip
python -m venv venv && venv/Scripts/python -m pip install rasterio shapely
venv/Scripts/python make_sea_mask.py land-polygons-split-3857/land_polygons.shp \
    ../src/main/resources/trigger/sea_mask_osm.geojson
```

The script prints the number of land pixels and sea polygons and fails if the polygons do not reproduce the mask. To change the
grid (e.g. a new ECMWF domain or a different `TARGET_RESOLUTION` in the processors), edit `RESOLUTION`, `XMIN`, `YMAX`, `WIDTH`,
`HEIGHT` at the top of `make_sea_mask.py`: take them from `gdalinfo` of a COG produced by the task.

Reference values (2026-11-10): 4,807,068 land pixels, 2,420 sea polygons, 1.98 MB.

## Other grids: historical CIMA COGs

The historical CIMA COGs (2023-2025, `E:\TRIGGER\CIMA\<VAR>\output_cog`, produced by `NetCDFToCOGProcessor`) have their own grid
(1641 x 1354 pixels of ~4224 x 4221 m), so they need a mask computed on that grid. Pass one of them as third argument: the grid
(also with non-square pixels) is taken from it.

```bash
venv/Scripts/python make_sea_mask.py land-polygons-split-3857/land_polygons.shp E:/TRIGGER/CIMA/sea_mask_osm_cima.geojson \
    E:/TRIGGER/CIMA/WCT/output_cog/wct_20230102T000000Z.tif
```

Reference values (2026-10-28): 1,342,083 land pixels, 419 sea polygons, 0.54 MB. The mask is applied to each COG with an identity
`gdalwarp` forcing the source grid (`-te`/`-ts`: without them gdalwarp recomputes a slightly different grid) and `-dstnodata -9999`,
then `gdal_rasterize -burn -9999` with the sea polygons and `gdal_translate -of COG`: the land values are unchanged.
