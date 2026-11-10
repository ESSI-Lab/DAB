"""
Builds the TRIGGER sea mask (trigger/sea_mask_osm.geojson) used by COGS3SyncProcessor and CIMAForecastProcessor.

The land polygons are rasterized on the 2.5 km EPSG:3857 output grid of the COGs (all touched pixels: every pixel touching land
is land) and the sea pixels are polygonized. The polygons follow the output pixels, so the tasks reproduce this mask exactly with
a plain "gdal_rasterize -burn -9999" (see README.md).

Requirements: GDAL command line tools (ogr2ogr, gdal_create, gdal_rasterize) and a Python environment with rasterio and shapely
(e.g. python -m venv venv && venv/Scripts/python -m pip install rasterio shapely).

Usage: python make_sea_mask.py <land polygons> <output.geojson> [reference.tif]
  <land polygons>: land polygons in EPSG:3857, e.g. land-polygons-split-3857/land_polygons.shp from
  https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip
  [reference.tif]: optional EPSG:3857 raster whose grid is used instead of the 2.5 km grid of the tasks (e.g. a historical CIMA COG)
"""

import json
import os
import subprocess
import sys
import tempfile

# The GDAL command line tools keep the original environment; rasterio ships its own GDAL and PROJ and fails if PROJ_LIB/PROJ_DATA
# point to the proj.db of another installation (e.g. the GDAL tools), so they are removed for this Python process only
CLI_ENV = dict(os.environ)
for var in ("PROJ_LIB", "PROJ_DATA"):
    os.environ.pop(var, None)

import rasterio
from rasterio.features import shapes
from shapely.geometry import mapping, shape

# Output grid of the COGs: 2500 m EPSG:3857 aligned with gdalwarp -tap, covering the ECMWF domain (25W-45E, ~29.9N-72N).
# Pixels are multiples of 2500 m, so the mask is also aligned with any other 2500 m -tap grid inside this extent (e.g. CIMA d03).
# The ECMWF COGs reach y = 3500000 (29.97N): the grid goes down to 3490000, a few rows below, so their last rows are masked too.
RESOLUTION = 2500
XMIN, YMAX = -2785000, 11755000
WIDTH, HEIGHT = 3121, 3306
XMAX, YMIN = XMIN + WIDTH * RESOLUTION, YMAX - HEIGHT * RESOLUTION


def run(*args):
    print(" ".join(str(a) for a in args))
    subprocess.run([str(a) for a in args], check=True, env=CLI_ENV)


def use_reference_grid(reference):
    """Takes the grid (north-up, EPSG:3857, also non-square pixels) from a reference raster"""
    global XMIN, YMAX, XMAX, YMIN, WIDTH, HEIGHT
    with rasterio.open(reference) as ref:
        if ref.crs is None or ref.crs.to_epsg() != 3857:
            sys.exit("the reference raster must be in EPSG:3857")
        t = ref.transform
        if t.b != 0 or t.d != 0 or t.e >= 0:
            sys.exit("the reference raster must be north-up without rotation")
        WIDTH, HEIGHT = ref.width, ref.height
        XMIN, YMAX = t.c, t.f
        XMAX, YMIN = XMIN + WIDTH * t.a, YMAX + HEIGHT * t.e
    print("grid from %s: %dx%d, pixel %.6f x %.6f m" % (reference, WIDTH, HEIGHT, t.a, -t.e))


def main(land, output, reference=None):
    if reference:
        use_reference_grid(reference)
    work = tempfile.mkdtemp(prefix="sea_mask_")
    domain = os.path.join(work, "land_domain.gpkg")
    mask = os.path.join(work, "land_mask.tif")
    sea_3857 = os.path.join(work, "sea_3857.geojson")

    # 1. land polygons touching the output grid
    run("ogr2ogr", "-q", "-overwrite", "-f", "GPKG", domain, land, "-spat", XMIN, YMIN, XMAX, YMAX, "-nln", "land")

    # 2. land mask on the output grid, all touched pixels (the raster is created first: gdal_rasterize reprojects the polygons only
    #    when burning into an existing raster)
    run("gdal_create", "-q", "-of", "GTiff", "-ot", "Byte", "-outsize", WIDTH, HEIGHT, "-a_srs", "EPSG:3857", "-a_ullr", XMIN, YMAX, XMAX,
        YMIN, "-burn", 0, "-co", "COMPRESS=DEFLATE", mask)
    run("gdal_rasterize", "-q", "-at", "-burn", 1, domain, mask)

    # 3. sea pixels -> polygons following the pixel edges
    with rasterio.open(mask) as src:
        land_px = src.read(1)
        geoms = [shape(s) for s, v in shapes(land_px, mask=(land_px == 0), transform=src.transform) if v == 0]
    print("land pixels:", int(land_px.sum()), "sea polygons:", len(geoms))
    with open(sea_3857, "w") as f:
        json.dump({"type": "FeatureCollection",
                   "features": [{"type": "Feature", "properties": {}, "geometry": mapping(g)} for g in geoms]}, f)

    # 4. GeoJSON in EPSG:4326 (RFC 7946), 5 decimals (~1 m: far below the 1250 m from the pixel centres)
    run("ogr2ogr", "-q", "-overwrite", "-f", "GeoJSON", output, sea_3857, "-s_srs", "EPSG:3857", "-t_srs", "EPSG:4326", "-lco",
        "COORDINATE_PRECISION=5", "-lco", "RFC7946=YES", "-nln", "sea")

    # 5. check: burning the sea polygons the way the tasks do must reproduce the mask exactly
    check = os.path.join(work, "check.tif")
    run("gdal_create", "-q", "-of", "GTiff", "-ot", "Float32", "-outsize", WIDTH, HEIGHT, "-a_srs", "EPSG:3857", "-a_ullr", XMIN, YMAX,
        XMAX, YMIN, "-burn", 1, check)
    run("gdal_rasterize", "-q", "-burn", -9999, output, check)
    with rasterio.open(check) as src:
        burned = src.read(1)
    different = int(((burned != -9999).astype("uint8") != land_px).sum())
    print("pixels different from the mask:", different)
    if different:
        sys.exit("the sea polygons do not reproduce the mask")
    print("written", output, round(os.path.getsize(output) / 1048576, 2), "MB; temporary files in", work)


if __name__ == "__main__":
    if len(sys.argv) not in (3, 4):
        sys.exit(__doc__)
    main(*sys.argv[1:])
