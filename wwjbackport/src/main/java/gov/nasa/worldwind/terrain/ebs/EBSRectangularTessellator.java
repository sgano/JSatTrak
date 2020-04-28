/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.terrain.ebs;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * @author Jim Miller
 * @version $Id: EBSRectangularTessellator.java 8723 2009-02-03 03:57:57Z jmiller $
 */
public class EBSRectangularTessellator extends WWObjectImpl implements Tessellator
{
    // FOR RAIL EDGE TEXTURE COORDINATE LOGGING:    <--- search for this string
    //public static java.io.PrintWriter[] sectorPW;
    //public static java.io.PrintWriter curSectorPW = null;
    //static
    //{
    //String base = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "EBS_";
    //sectorPW=new java.io.PrintWriter[9];
    //try
    //{
    //sectorPW[0] = new java.io.PrintWriter(base + "0_LatLonLatLon.txt");
    //sectorPW[1] = new java.io.PrintWriter(base + "1_LatLatLonLon.txt");
    //sectorPW[2] = new java.io.PrintWriter(base + "2_LatGEGELon.txt");
    //sectorPW[3] = new java.io.PrintWriter(base + "3_LatLonGEGE.txt");
    //sectorPW[4] = new java.io.PrintWriter(base + "4_LatLatGEGE.txt");
    //sectorPW[5] = new java.io.PrintWriter(base + "5_LonLonGEGE.txt");
    //sectorPW[6] = new java.io.PrintWriter(base + "6_LatGEGEGE.txt");
    //sectorPW[7] = new java.io.PrintWriter(base + "7_LonGEGEGE.txt");
    //sectorPW[8] = new java.io.PrintWriter(base + "8_GEGEGEGE.txt");
    //}
    //catch (Exception e) {}
    //}

    //

    protected static class RenderInfo
    {
        private final int density;
        private final int numVerticesDroppedPerRow;
        private final Vec4 referenceCenter;
        private final DoubleBuffer vertices;
        private final DoubleBuffer texCoords;
        private final IntBuffer indices;
        private final long time;

        private RenderInfo(int density, int numVerticesDroppedPerRow,
            DoubleBuffer vertices, DoubleBuffer texCoords, Vec4 refCenter)
        {
            this.density = density;
            this.numVerticesDroppedPerRow = numVerticesDroppedPerRow;
            this.vertices = vertices;
            this.texCoords = texCoords;
            this.referenceCenter = refCenter;
            this.indices = getIndices(this.density, this.numVerticesDroppedPerRow);
            this.time = System.currentTimeMillis();
        }

        private long getSizeInBytes()
        {
            // Texture coordinates are shared among all tiles of the same density, so do not count towards size.
            // 8 references, doubles in buffer.
            return 8 * 4 + (this.vertices.limit()) * Double.SIZE / 8;
        }
    }

    private static class RectTile implements SectorGeometry
    {
        private final EBSRectangularTessellator tessellator;
        private final int level;
        private final EdgeBoundedSector sector;
        private final int density;
        private final double log10CellSize;
        private Extent extent; // extent of sector in object coordinates
        private RenderInfo ri;

        private int minColorCode = 0;
        private int maxColorCode = 0;

        public RectTile(EBSRectangularTessellator tessellator, Extent extent, int level, int density,
            EdgeBoundedSector sector,
            double cellSize)
        {
            this.tessellator = tessellator;
            this.level = level;
            this.density = density;
            this.sector = sector;
            this.extent = extent;
            this.log10CellSize = Math.log10(cellSize);
        }

        public DoubleBuffer makeTextureCoordinates(GeographicTextureCoordinateComputer computer)
        {
            return null; // TODO: implement this method
        }

        public EdgeBoundedSector getSector()
        {
            return this.sector;
        }

        public Extent getExtent()
        {
            return this.extent;
        }

        public void renderMultiTexture(DrawContext dc, int numTextureUnits)
        {
            this.tessellator.renderMultiTexture(dc, this, numTextureUnits);
        }

        public void render(DrawContext dc)
        {
            this.tessellator.render(dc, this);
        }

        public void renderWireframe(DrawContext dc, boolean showTriangles, boolean showTileBoundary)
        {
            this.tessellator.renderWireframe(dc, this, showTriangles, showTileBoundary);
        }

        public void renderBoundingVolume(DrawContext dc)
        {
            this.tessellator.renderBoundingVolume(dc, this);
        }

        public void renderTileID(DrawContext dc)
        {
        }

        public PickedObject[] pick(DrawContext dc, List<? extends Point> pickPoints)
        {
            return this.tessellator.pick(dc, this, pickPoints);
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            this.tessellator.pick(dc, this, pickPoint);
        }

        public Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset)
        {
            return this.tessellator.getSurfacePoint(this, latitude, longitude, metersOffset);
        }

        public double getResolution()
        {
            return this.sector.getDeltaLatRadians() / this.density;
        }

        public Intersection[] intersect(Line line)
        {
            return this.tessellator.intersect(this, line);
        }

        public Intersection[] intersect(double elevation)
        {
            return this.tessellator.intersect(this, elevation);
        }

        public ExtractedShapeDescription getIntersectingTessellationPieces(Plane[] p)
        {
            return this.tessellator.getIntersectingTessellationPieces(this, p);
        }

        public ExtractedShapeDescription getIntersectingTessellationPieces(Vec4 Cxyz,
            Vec4 uHat, Vec4 vHat, double uRadius, double vRadius)
        {
            return this.tessellator.getIntersectingTessellationPieces(this, Cxyz,
                uHat, vHat, uRadius, vRadius);
        }
    }

    private static class CacheKey
    {
        private final EdgeBoundedSector sector;
        private final int density;
        private final Object globeStateKey;

        public CacheKey(DrawContext dc, EdgeBoundedSector sector, int density)
        {
            this.sector = sector;
            this.density = density;
            this.globeStateKey = dc.getGlobe().getStateKey(dc);
        }

        @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            CacheKey cacheKey = (CacheKey) o; // Note: no check of class type equivalence, for performance

            if (density != cacheKey.density)
                return false;
            if (globeStateKey != null ? !globeStateKey.equals(cacheKey.globeStateKey) : cacheKey.globeStateKey != null)
                return false;
            //noinspection RedundantIfStatement
            if (sector != null ? !sector.equals(cacheKey.sector) : cacheKey.sector != null)
                return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            long temp;
            result = (sector != null ? sector.hashCode() : 0);
            result = 31 * result + density;
            result = 31 * result + (globeStateKey != null ? globeStateKey.hashCode() : 0);
            return result;
        }
    }

    // TODO: Make all this configurable
    private static final double DEFAULT_LOG10_RESOLUTION_TARGET = 1.3;
    private static final int DEFAULT_MAX_LEVEL = 17;
    private static final int DEFAULT_DENSITY = 20;
    private static final String CACHE_NAME = "Terrain";
    private static final String CACHE_ID = EBSRectangularTessellator.class.getName();

    // Tri-strip indices and texture coordinates. These depend only on density and can therefore be statically cached.
    private static final HashMap<Integer, DoubleBuffer> parameterizations = new HashMap<Integer, DoubleBuffer>();
    private static final HashMap<Integer, IntBuffer> indexLists = new HashMap<Integer, IntBuffer>();

    private ArrayList<RectTile> topLevels;
    private PickSupport pickSupport = new PickSupport();
    private SectorGeometryList currentTiles = new SectorGeometryList();
    private Frustum currentFrustum;
    private Sector currentCoverage; // union of all tiles selected during call to render()
    private boolean makeTileSkirts = true;
    private int currentLevel;
    private int maxLevel = DEFAULT_MAX_LEVEL;
    private Globe globe;
    private int density = DEFAULT_DENSITY;

    // cube-based tessellation parameters
    private static double latitudeCutoffInDegrees = 40.0;

    public SectorGeometryList tessellate(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getView() == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        if (!WorldWind.getMemoryCacheSet().containsCache(CACHE_ID))
        {
            long size = Configuration.getLongValue(AVKey.SECTOR_GEOMETRY_CACHE_SIZE, 20000000L);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName(CACHE_NAME);
            WorldWind.getMemoryCacheSet().addCache(CACHE_ID, cache);
        }

        this.maxLevel = Configuration.getIntegerValue(AVKey.RECTANGULAR_TESSELLATOR_MAX_LEVEL, DEFAULT_MAX_LEVEL);

        EdgeBoundedSector.recordGlobe(dc.getGlobe());
        if (this.topLevels == null)
            this.topLevels = this.createTopLevelTiles(dc);

        this.currentTiles.clear();
        this.currentLevel = 0;
        this.currentCoverage = null;

        this.currentFrustum = dc.getView().getFrustumInModelCoordinates();
        for (RectTile tile : this.topLevels)
        {
            this.selectVisibleTiles(dc, tile);
        }

        this.currentTiles.setSector(this.currentCoverage);

        for (SectorGeometry tile : this.currentTiles)
        {
            this.makeVerts(dc, (RectTile) tile);
        }

        return this.currentTiles;
    }

    private ArrayList<RectTile> createTopLevelTiles(DrawContext dc)
    {
        this.globe = dc.getGlobe();
        boolean forceExtraInitialSubdivisionInPolarRegion = true;

        // the 4 lateral cube faces are divided into 4 sectors each ( 16)
        // the top and bottom faces are divided into 16 each
        // (when forceExtraInitialSubdivisionInPolarRegion==true)   (+32)
        ArrayList<RectTile> tops = new ArrayList<RectTile>(48);

        EdgeBoundedSector[] s = EdgeBoundedSector.initialCubeMapping(Angle.fromDegrees(latitudeCutoffInDegrees));

        for (int i = 0; i < 6; i++)
        {
            // Subdivide all the original ones once (partly to avoid having tiles in the polar
            // regions with deltaLatitude = 0). This first subdivision will produce tiles
            // subtending 45 degree angles. (Two subdivisions would produce ones subtending 22.5
            // degrees.) By comparison, the original RectangularTessellator started with 36 degree
            // top level tiles.
            EdgeBoundedSector[] si = (EdgeBoundedSector[]) s[i].subdivide();
            for (EdgeBoundedSector sij : si)
            {
                // We further subdivide the tiles in the polar regions one more time. We derive
                // no benefit at the poles until we do that.  This will make the initial polar tiles
                // much smaller (roughly 1/2 x 1/2), but to compensate, we will make the size of
                // the actual triangles comparable by halving the triangle resolution in these two
                // polar regions.
                int[] result = analyzeEdgeBoundedSector(sij);
                if ((result == null) || !forceExtraInitialSubdivisionInPolarRegion)
                    // lateral sector - add as is
                    tops.add(this.createTile(dc, sij, 0, this.density));
                else // polar region sector. Force one more initial subdivision
                {
                    EdgeBoundedSector[] sijSubDiv = (EdgeBoundedSector[]) sij.subdivide();
                    for (EdgeBoundedSector sijSubDiv_k : sijSubDiv)
                    // note we halve the density here to compensate for the smaller polar sectors
                    {
                        tops.add(this.createTile(dc, sijSubDiv_k, 0, this.density / 2));
                    }
                }
            }
        }

        return tops;
    }

    private RectTile createTile(DrawContext dc, EdgeBoundedSector tileSector, int level, int triangleDensity)
    {
        Extent extent = Sector.computeBoundingCylinder(dc.getGlobe(), dc.getVerticalExaggeration(), tileSector);
        double cellSize = tileSector.getDeltaLatRadians() * dc.getGlobe().getRadius() / triangleDensity;

        return new RectTile(this, extent, level, triangleDensity, tileSector, cellSize);
    }

    public static double getLatitudeCutoffInDegrees()
    {
        return latitudeCutoffInDegrees;
    }

    public static void setLatitudeCutoffInDegrees(double d)
    {
        if (d < 0.0)
            d = -d;
        if ((d >= 5.0) && (d <= 85.0))
            latitudeCutoffInDegrees = d;
    }

    public boolean isMakeTileSkirts()
    {
        return makeTileSkirts;
    }

    public void setMakeTileSkirts(boolean makeTileSkirts)
    {
        this.makeTileSkirts = makeTileSkirts;
    }

    public long getUpdateFrequency() // TODO
    {
        return 0;
    }

    public void setUpdateFrequency(long updateFrequency)
    {
    }

    private void selectVisibleTiles(DrawContext dc, RectTile tile)
    {
        Extent extent = tile.getExtent();
        if (extent != null && !extent.intersects(this.currentFrustum))
            return;

        if (this.currentLevel < this.maxLevel - 1 && this.needToSplit(dc, tile))
        {
            ++this.currentLevel;
            RectTile[] subtiles = this.split(dc, tile);
            for (RectTile child : subtiles)
            {
                this.selectVisibleTiles(dc, child);
            }
            --this.currentLevel;
            return;
        }
        this.currentCoverage = tile.getSector().union(this.currentCoverage);
        this.currentTiles.add(tile);
    }

    private boolean needToSplit(DrawContext dc, RectTile tile)
    {
        Vec4[] corners = tile.sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
        Vec4 centerPoint = tile.sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());

        View view = dc.getView();
        double d1 = view.getEyePoint().distanceTo3(corners[0]);
        double d2 = view.getEyePoint().distanceTo3(corners[1]);
        double d3 = view.getEyePoint().distanceTo3(corners[2]);
        double d4 = view.getEyePoint().distanceTo3(corners[3]);
        double d5 = view.getEyePoint().distanceTo3(centerPoint);

        double minDistance = d1;
        if (d2 < minDistance)
            minDistance = d2;
        if (d3 < minDistance)
            minDistance = d3;
        if (d4 < minDistance)
            minDistance = d4;
        if (d5 < minDistance)
            minDistance = d5;

        double logDist = Math.log10(minDistance);
        boolean useTile = tile.log10CellSize <= (logDist - DEFAULT_LOG10_RESOLUTION_TARGET);

        return !useTile;
    }

    private RectTile[] split(DrawContext dc, RectTile tile)
    {
        EdgeBoundedSector[] sectors = (EdgeBoundedSector[]) tile.sector.subdivide();

        RectTile[] subTiles = new RectTile[4];
        subTiles[0] = this.createTile(dc, sectors[0], tile.level + 1, tile.density);
        subTiles[1] = this.createTile(dc, sectors[1], tile.level + 1, tile.density);
        subTiles[2] = this.createTile(dc, sectors[2], tile.level + 1, tile.density);
        subTiles[3] = this.createTile(dc, sectors[3], tile.level + 1, tile.density);

        return subTiles;
    }

    private EBSRectangularTessellator.CacheKey createCacheKey(DrawContext dc, RectTile tile)
    {
        return new CacheKey(dc, tile.sector, tile.density);
    }

    private void makeVerts(DrawContext dc, RectTile tile)
    {
        // First see if the vertices have been previously computed and are in the cache. Since the elevation model
        // can change between frames, regenerate and re-cache vertices every second.
        MemoryCache cache = WorldWind.getMemoryCache(CACHE_ID);
        CacheKey cacheKey = this.createCacheKey(dc, tile);
        tile.ri = (RenderInfo) cache.getObject(cacheKey);
        if (tile.ri != null && tile.ri.time >= System.currentTimeMillis() - 1000) // Regenerate cache after one second
            return;

        tile.ri = this.buildVerts(dc, tile, this.makeTileSkirts);
        if (tile.ri != null)
        {
            cacheKey = this.createCacheKey(dc, tile);
            cache.add(cacheKey, tile.ri, tile.ri.getSizeInBytes());
        }
    }

    private int[] analyzeEdgeBoundedSector(EdgeBoundedSector s)
    {
        // This routine is critical to the functioning of sector vertex generation, hence we
        // document much of the overall process here.

        /* There are nine different types of edge-bounded sectors that can arise. These are
             characterized by the types of edges that appear and their ordering around the sector.
             Using a notation that starts with the first latitude encountered in a CCW traversal
             (or the first longitude, if there are no latitudes involved, or finally the first
             Great Ellipse edge otherwise), we can list the nine options as shown below. The first
             (LatLonLatLon) appears on the lateral sides of the globe between the +/- latitude
             limit lines; all the others appear only in latitudes above (below) the latitude limit
             corresponding to the top (bottom) four vertices of the cube (i.e., appear only in the
             polar regions).

          LatLonLatLon: A lateral sector which can be handled in the conventional way.
          LatLatLonLon: Only appears at the coarsest subdivision level. There will be exactly 4 around each pole.
          LatGEGELon:   Appears immediately adjacent to one of the original 8 cube vertices (mirror image of next)
          LatLonGEGE:   Appears immediately adjacent to one of the original 8 cube vertices (mirror image of previous)
          LatLatGEGE:   Appears immediately adjacent to a midpoint of one of the original 8 cube vertices. ("Remnant"
                        of LatLatLonLon at subsequent levels of subdivision.)
          LonLonGEGE:   Appears immediately adjacent to the north (south) pole.
          LatGEGEGE:    Appears along the edges of quadrants in the two polar regions.
          LonGEGEGE:    Appears along the edges of quadrants in the two polar regions.
          GEGEGEGE:     Appears in the interior of quadrants in the two polar regions.

          The first (LatLonLatLon) case can be handled exactly as in the original RectangularTessellator.buildVerts.
          For the other 8 cases, we generate vertices along transverse arcs between pairs of "rail edges". Outside of
          this routine, it is not relevant which of the 8 cases we are dealing with. Detecting the eight cases is
          only necessary here in order to determine (i) which pair of edges are used as the "rails", and (ii) to
          determine the nature of the transverse arc. Relevant details:

          (i) rail edges: The higher level code assumes vertices are generated in rows with increasing longitude
          across a row and with latitudes increasing from row to row. Hence we return the rail edge indices
          ordered "min longitude" to "max longitude" (quotes because an edge does not, in general, have constant
          latitude or longitude), and successive rows with "increasing latitude" are generated as the "rail_t"
          parameter used in buildVertsBetweenRailEdges varies from 0 to 1.
          (ii) transverse arc: Ideally, transverse vertices would always be generated along great ellipse arcs.
          However, the tessellation must smoothly meet the top latitude limit line corresponding to the top four
          and bottom four cube vertices. This line is a curve of constant latitude, not a great ellipse. This is
          only relevant for edge-bounded sectors that have at least one edge which is a line of constant latitude.

          METHOD RETURN VALUE:
              (*) null if the edge bounded sector is LatLonLatLon (i.e., a transverse sector away from the polar region)
          -OR-
              (*) an array of length three: the first two positions hold the edge indices specifying
                  rail edge 1 and rail edge 2, respectively, ordered as indicated above.  The third position
                  holds the number of constant latitude edges.
          */

        // We start by counting the number of edges of each type and remembering
        // where the first two (if any) of each type are:
        int nGE = 0,            // there will be 0, 2, 3, or 4 (can't be just 1)
            nLat = 0, nLon = 0; // there will be 0, 1, or 2 (can't be more than 2)
        int firstGE = -1, firstLat = -1, firstLon = -1;
        int secondGE = -1, secondLon = -1;
        for (int i = 0; i < 4; i++)
        {
            Edge.EdgeType et = s.getEdge(i).getEdgeType();
            if (et == Edge.EdgeType.ConstantLatitude)
            {
                if (++nLat == 1)
                    firstLat = i;
            }
            else if (et == Edge.EdgeType.ConstantLongitude)
            {
                if (++nLon == 1)
                    firstLon = i;
                else
                    secondLon = i;
            }
            else
            {
                if (++nGE == 1)
                    firstGE = i;
                else if (nGE == 2)
                    secondGE = i;
            }
        }

        // Next we determine which of the nine cases we have so that we can identify
        // the two rail edges. (We will order them at the end.)
        int[] edgeIndices = new int[3];
        edgeIndices[2] = nLat;
        if ((nLat == 2) && (nLon == 2))
        {
            // if the first latitude is both preceded and followed by a longitude edge,
            // then the sector is a lateral one:
            int n = (firstLat + 1) % 4;
            if ((n == firstLon) || (n == secondLon))
            {
                n = firstLat - 1;
                if (n < 0)
                    n = 3;
                if ((n == firstLon) || (n == secondLon))
                {
                    /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
                         curSectorPW=sectorPW[0]; */
                    // lateral sector:
                    return null;
                }
            }
            // must be LatLatLonLon
            /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
               curSectorPW=sectorPW[1]; */
            edgeIndices[0] = firstLon;
            edgeIndices[1] = secondLon;
        }

        // There are at least two GE edges. (It is impossible to have a sector with 1 GE edge.)
        else if (nLon == 1)
        {
            // either LatGEGELon, LatLonGEGE, or LonGEGEGE: go from longitude to opposite GE
            /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
               if((firstLat+1)%4==firstLon) curSectorPW=sectorPW[3];
               else if (nLat>0) curSectorPW=sectorPW[2]; else curSectorPW=sectorPW[7]; */
            edgeIndices[0] = firstLon;
            edgeIndices[1] = (firstLon + 2) % 4;
        }
        else if (nLat == 2) // LatLatGEGE: go between GEs:
        {
            /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
               curSectorPW=sectorPW[4]; */
            edgeIndices[0] = firstGE;
            edgeIndices[1] = secondGE;
        }
        else if (nLat == 1) // LatGEGEGE; use preceding and following GEs:
        {
            /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
               curSectorPW=sectorPW[6]; */
            if (firstLat == 0)
                edgeIndices[0] = 3;
            else
                edgeIndices[0] = firstLat - 1;
            edgeIndices[1] = (firstLat + 1) % 4;
        }
        else if (nLon == 2) // LonLonGEGE: go from min Lon to opposite GE
        {
            /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
               curSectorPW=sectorPW[5]; */
            if (s.getEdge(firstLon).maxLongitudeDegrees() > s.getEdge(secondLon).maxLongitudeDegrees())
                edgeIndices[0] = secondLon;
            else
                edgeIndices[0] = firstLon;
            edgeIndices[1] = (edgeIndices[0] + 2) % 4;
        }
        else // must be GEGEGEGE: go from arbitrary GE to opposite GE:
        {
            /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
               curSectorPW=sectorPW[8]; */
            edgeIndices[0] = firstGE;
            edgeIndices[1] = (firstGE + 2) % 4;
        }

        // finally, order the two rail edges before returning them
        boolean northPolarRegion = (s.getEdge(0).getStartPoint().getLatitude().getDegrees() > 0.0);
        if (northPolarRegion)
        {
            if (s.getEdge(edgeIndices[0]).maxLongitudeDegrees() >
                s.getEdge(edgeIndices[1]).maxLongitudeDegrees())
            {
                int t = edgeIndices[0];
                edgeIndices[0] = edgeIndices[1];
                edgeIndices[1] = t;
            }
        }
        else
        {
            if (s.getEdge(edgeIndices[0]).maxLongitudeDegrees() <
                s.getEdge(edgeIndices[1]).maxLongitudeDegrees())
            {
                int t = edgeIndices[0];
                edgeIndices[0] = edgeIndices[1];
                edgeIndices[1] = t;
            }
        }

        return edgeIndices;
    }

    public RenderInfo buildVerts(DrawContext dc, RectTile tile, boolean makeSkirts)
    {
        int[] oppEdges = analyzeEdgeBoundedSector(tile.getSector());
        if (oppEdges == null)
            // lateral sector; build vertices in standard way
            return buildVertsOnLateralTile(dc, tile, makeSkirts);
        return buildVertsBetweenRailEdges(dc, tile, makeSkirts, oppEdges);
    }

    private RenderInfo buildVertsBetweenRailEdges(DrawContext dc, RectTile tile, boolean makeSkirts,
        int[] railEdges)    // Build vertices along GreatEllipseEdges between railEdges[0] and railEdges[1]
    {
        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);
        DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 3);
        DoubleBuffer texCoords = BufferUtil.newDoubleBuffer(numVertices * 2);
        double nominal_delta_t = 1.0 / density;
        ArrayList<LatLon> latlons = this.computeLocationsBetweenRailEdges(tile, railEdges, nominal_delta_t, texCoords);
        /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
		curSectorPW.print("density = " + tile.density + "; level = " + tile.level + "\n"); */
        double[] elevations = new double[latlons.size()];
        dc.getGlobe().getElevations(tile.sector, latlons, tile.getResolution(), elevations);

        int iv = 0;
        double verticalExaggeration = dc.getVerticalExaggeration();
        double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation() * verticalExaggeration : 0;

        LatLon centroid = tile.sector.averageOfCorners();
        Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);

        int ie = 0;
        Iterator<LatLon> latLonIter = latlons.iterator();
        int densityThisRow = density, numVerticesDroppedPerRow = 0;
        if (railEdges[2] == 2)
        {
            // see comments below in computeLocationsBetweenRailEdges
            numVerticesDroppedPerRow = 2;
            densityThisRow = 2 * density + numVerticesDroppedPerRow;
        }

        for (int j = 0; j <= density + 2; j++)
        {
            int i = 0;
            while (i <= densityThisRow + 2)
            {
                LatLon latlon = latLonIter.next();
                double elevation = verticalExaggeration * elevations[ie++];
                if (j == 0 || j >= tile.density + 2 || i == 0 || i >= densityThisRow + 2)
                {   // use abs to account for negative elevation.
                    elevation -= exaggeratedMinElevation >= 0 ? exaggeratedMinElevation : -exaggeratedMinElevation;
                }

                Vec4 p = globe.computePointFromPosition(latlon.getLatitude(), latlon.getLongitude(), elevation);
                verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y).put(iv++, p.z - refCenter.z);
                i++;
            }
            densityThisRow -= numVerticesDroppedPerRow;
        }
        /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
          curSectorPW.print("=====================================\n\n"); curSectorPW.flush(); */
        return new RenderInfo(density, numVerticesDroppedPerRow,
            verts,
//			getPolarRegionTextureCoordinates(density,s,verts),
            texCoords,
            refCenter);
    }

    private ArrayList<LatLon> computeLocationsBetweenRailEdges(RectTile tile, int[] railEdges,
        double nominal_delta_t, DoubleBuffer texCoords)
    // Build vertices and texture coordinates along GreatEllipseEdges between railEdges[0]
    // and railEdges[1]
    {
        boolean sectorBordersLatLimit = (railEdges[2] > 0);
        boolean sectorHasLatLatBottom = (railEdges[2] == 2); // effectively a triangular patch. See below.

        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);
        if (sectorHasLatLatBottom) // see comments below
            numVertices = (2 * density) * (2 * density) / 4 + 3 * (2 * density) + 9; // Hmm. actually same as (d+3)^2...

        // One notable special case arises from the original LatLatLonLon case. As stated in analyzeEdgeBoundedSector,
        // a sector of this type appears only at the coarsest subdivision. However, subsequent subdivisions "push" this
        // special case out towards the midpoints of the original 8 vertices via LatLatGEGE sectors. The original
        // LatLatLonLon and LatLatGEGE sectors have in common (i) a degeneracy at the top since the two rail edges
        // will share a vertex there, and (ii) while the sector is technically 4-sided, the "LatLat" part of it
        // forms one geometrically continuous edge, making the sector geometrically triangular. There are two
        // relevant implications of this: (ii.a) we want to generate a triangular array of vertices to better match
        // the sector shape, and (ii.b) we need to blend between great ellipse transverse arcs near the vertex
        // shared by the two rail edges and the line of constant latitude (which is not a great ellipse) at the bottom
        // (LatLat) portion of the sector. Issue (ii.a) is handled in part by the use of "numVerticesDroppedPerRow" and
        // in part by "getTriangularIndices" (as called from "getIndices"). Issue (ii.b) is handled in the loop below: See
        // the use of "sectorBordersLatLimit".
        EdgeBoundedSector s = tile.getSector();
        Edge re0 = s.getEdge(railEdges[0]);
        Edge re1 = s.getEdge(railEdges[1]);
        double rail_t = 1.0;
        double rail_delta_t = -nominal_delta_t;

        int densityThisRow = density, numVerticesDroppedPerRow = 0;
        if (sectorHasLatLatBottom)
        {
            // this sector has a triangular shape. It is either LatLatLonLon at coarsest
            // subdivision, or LatLatGEGE after one or more additional levels of subdivision.
            // We need to match the triangle pattern along the bottom (LatLat) end so that
            // it matches the triangle density of the same subdivision level immediately
            // below. But we want to drop off triangles from row to row, leaving a single
            // triangle at the top so that we get better triangle aspect ratios throughout.

            // we double the number of points on the bottom row so that it will correctly meet
            // the two sectors below it. But we need to drop vertices from row to row so that
            // we are left with a single vertex att he top. Since this needs to be done in
            // "density" (not 2*density) steps, we need to drop two vertices per row:
            numVerticesDroppedPerRow = 2;
            // "+ numVerticesDroppedPerRow" makes the skirt while allowing the density of the first
            // "real" row to be what it needs to be (2*density) so that it matches the sectors below.
            densityThisRow = 2 * density + numVerticesDroppedPerRow;
        }

        double sectorLonDelta = s.getDeltaLonRadians();
        double sectorLatDelta = s.getDeltaLatRadians();
        double sectorLonMin = s.getMinLongitude().radians;
        double sectorLatMin = s.getMinLatitude().radians;

        int ivTexCoords = 0;
        ArrayList<LatLon> latlons = new ArrayList<LatLon>(numVertices);

        for (int j = 0; j <= density + 2; j++)
        {
            LatLon re0Point = re0.arcLengthPointOnEdge(rail_t);
            LatLon re1Point = re1.arcLengthPointOnEdge(1.0 - rail_t);
            GreatEllipseEdge ge = new GreatEllipseEdge(globe, re0Point, re1Point);
            double transverse_t = 0.0;
            double transverse_delta_t = 1.0 / densityThisRow;
            int i = 0;
            while (i <= densityThisRow + 2)
            {
                LatLon transverse_pnt = ge.arcLengthPointOnEdge(transverse_t);

                if (sectorBordersLatLimit)
                {
                    // blend with point computed based on linear blend in lat-lon
                    // space of two rail points. This converges to a line of constant
                    // latitude where the sector meets the latitude cutoff point.
                    double re0LatRadians = re0Point.getLatitude().getRadians();
                    double re0LonRadians = re0Point.getLongitude().getRadians();
                    double re1LatRadians = re1Point.getLatitude().getRadians();
                    double re1LonRadians = re1Point.getLongitude().getRadians();
                    double LinBlendPointLat = (1.0 - transverse_t) * re0LatRadians + transverse_t * re1LatRadians;
                    double LinBlendPointLon = (1.0 - transverse_t) * re0LonRadians + transverse_t * re1LonRadians;

                    double tpLat = transverse_pnt.getLatitude().getRadians();
                    double tpLon = transverse_pnt.getLongitude().getRadians();
                    double alpha = 1.0 - rail_t;
                    double bLat = (1.0 - alpha) * LinBlendPointLat + alpha * tpLat;
                    double bLon = (1.0 - alpha) * LinBlendPointLon + alpha * tpLon;

                    transverse_pnt = LatLon.fromRadians(bLat, bLon);
                }

                Angle lat = transverse_pnt.getLatitude();
                Angle lon = transverse_pnt.getLongitude();
                latlons.add(new LatLon(lat, lon));
                double sTexCoord = (lon.radians - sectorLonMin) / sectorLonDelta;
                double tTexCoord = (lat.radians - sectorLatMin) / sectorLatDelta;
                // avoid bad numerical roundoff
                if (sTexCoord < 0.0)
                    sTexCoord = 0.0;
                else if (sTexCoord > 1.0)
                    sTexCoord = 1.0;
                if (tTexCoord < 0.0)
                    tTexCoord = 0.0;
                else if (tTexCoord > 1.0)
                    tTexCoord = 1.0;
                /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
                    curSectorPW.print(sTexCoord + " , " + tTexCoord + "\n"); */
                texCoords.put(ivTexCoords++, sTexCoord);
                texCoords.put(ivTexCoords++, tTexCoord);

                if (i >= densityThisRow)
                    transverse_t = 1.0;
                else if (i != 0)
                    transverse_t += transverse_delta_t;
                i++;
            }
            densityThisRow -= numVerticesDroppedPerRow;

            if (j >= density)
                rail_t = 0.0;
            else if (j != 0)
                rail_t += rail_delta_t;
        }
        /* FOR RAIL EDGE TEXTURE COORDINATE LOGGING:
          curSectorPW.print("=====================================\n\n"); curSectorPW.flush(); */
        return latlons;
    }

    // The following is the original RectangularTessellator.buildVerts:
    public RenderInfo buildVertsOnLateralTile(DrawContext dc, RectTile tile, boolean makeSkirts)
    {
        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);
        DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 3);
        ArrayList<LatLon> latlons = this.computeLocationsOnLateralTile(tile);
        double[] elevations = new double[latlons.size()];
        dc.getGlobe().getElevations(tile.sector, latlons, tile.getResolution(), elevations);

        int iv = 0;
        double verticalExaggeration = dc.getVerticalExaggeration();
        double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation() * verticalExaggeration : 0;

        LatLon centroid = tile.sector.getCentroid();
        Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);

        int ie = 0;
        Iterator<LatLon> latLonIter = latlons.iterator();
        for (int j = 0; j <= density + 2; j++)
        {
            for (int i = 0; i <= density + 2; i++)
            {
                LatLon latlon = latLonIter.next();
                double elevation = verticalExaggeration * elevations[ie++];

                if (j == 0 || j >= tile.density + 2 || i == 0 || i >= tile.density + 2)
                {   // use abs to account for negative elevation.
                    elevation -= exaggeratedMinElevation >= 0 ? exaggeratedMinElevation : -exaggeratedMinElevation;
                }

                Vec4 p = globe.computePointFromPosition(latlon.getLatitude(), latlon.getLongitude(), elevation);
                verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y).put(iv++, p.z - refCenter.z);
            }
        }

        return new RenderInfo(density, 0, verts, getLateralSideTextureCoordinates(density), refCenter);
    }

    // The following is the original RectangularTessellator.computeLocations:
    private ArrayList<LatLon> computeLocationsOnLateralTile(RectTile tile)
    {
        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);

        Angle latMax = tile.sector.getMaxLatitude();
        Angle dLat = tile.sector.getDeltaLat().divide(density);
        Angle lat = tile.sector.getMinLatitude();

        Angle lonMin = tile.sector.getMinLongitude();
        Angle lonMax = tile.sector.getMaxLongitude();
        Angle dLon = tile.sector.getDeltaLon().divide(density);

        ArrayList<LatLon> latlons = new ArrayList<LatLon>(numVertices);
        for (int j = 0; j <= density + 2; j++)
        {
            Angle lon = lonMin;
            for (int i = 0; i <= density + 2; i++)
            {
                latlons.add(new LatLon(lat, lon));

                if (i > density)
                    lon = lonMax;
                else if (i != 0)
                    lon = lon.add(dLon);

                if (lon.degrees < -180)
                    lon = Angle.NEG180;
                else if (lon.degrees > 180)
                    lon = Angle.POS180;
            }

            if (j > density)
                lat = latMax;
            else if (j != 0)
                lat = lat.add(dLat);
        }

        return latlons;
    }

    private void renderMultiTexture(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (numTextureUnits < 1)
        {
            String msg = Logging.getMessage("generic.NumTextureUnitsLessThanOne");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.render(dc, tile, numTextureUnits);
    }

    private void render(DrawContext dc, RectTile tile)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.render(dc, tile, 1);
    }

    private long render(DrawContext dc, RectTile tile, int numTextureUnits)
    {
        if (tile.ri == null)
        {
            String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

        GL gl = dc.getGL();
        gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_DOUBLE, 0, tile.ri.vertices.rewind());

        for (int i = 0; i < numTextureUnits; i++)
        {
            gl.glClientActiveTexture(GL.GL_TEXTURE0 + i);
            gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL.GL_DOUBLE, 0, tile.ri.texCoords.rewind());
        }

        gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP, tile.ri.indices.limit(),
            javax.media.opengl.GL.GL_UNSIGNED_INT, tile.ri.indices.rewind());

        gl.glPopClientAttrib();

        dc.getView().popReferenceCenter(dc);

        return tile.ri.indices.limit() - 2; // return number of triangles rendered
    }

    private void renderWireframe(DrawContext dc, RectTile tile, boolean showTriangles, boolean showTileBoundary)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tile.ri == null)
        {
            String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        java.nio.IntBuffer indices = getIndices(tile.ri.density, tile.ri.numVerticesDroppedPerRow);
        indices.rewind();

        dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

        javax.media.opengl.GL gl = dc.getGL();
        gl.glPushAttrib(
            GL.GL_DEPTH_BUFFER_BIT | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
        gl.glEnable(javax.media.opengl.GL.GL_CULL_FACE);
        gl.glCullFace(javax.media.opengl.GL.GL_BACK);
        gl.glDisable(javax.media.opengl.GL.GL_TEXTURE_2D);
        gl.glColor4d(1d, 1d, 1d, 0.2);
        gl.glPolygonMode(javax.media.opengl.GL.GL_FRONT, javax.media.opengl.GL.GL_LINE);

        if (showTriangles)
        {
            gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
            gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

            gl.glVertexPointer(3, GL.GL_DOUBLE, 0, tile.ri.vertices.rewind());
            gl.glDrawElements(javax.media.opengl.GL.GL_TRIANGLE_STRIP, indices.limit(),
                javax.media.opengl.GL.GL_UNSIGNED_INT, indices.rewind());

            gl.glPopClientAttrib();
        }

        dc.getView().popReferenceCenter(dc);

        if (showTileBoundary)
            this.renderPatchBoundary(dc, tile, gl);

        gl.glPopAttrib();
    }

    private void renderPatchBoundary(DrawContext dc, RectTile tile, GL gl)
    {
        // TODO: Currently only works if called from renderWireframe because no state is set here.
        // TODO: Draw the boundary using the vertices along the boundary rather than just at the corners.
        gl.glColor4d(1d, 0, 0, 1d);
        Vec4[] corners = tile.sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());

        gl.glBegin(javax.media.opengl.GL.GL_QUADS);
        gl.glVertex3d(corners[0].x, corners[0].y, corners[0].z);
        gl.glVertex3d(corners[1].x, corners[1].y, corners[1].z);
        gl.glVertex3d(corners[2].x, corners[2].y, corners[2].z);
        gl.glVertex3d(corners[3].x, corners[3].y, corners[3].z);
        gl.glEnd();
    }

    private void renderBoundingVolume(DrawContext dc, RectTile tile)
    {
        Extent extent = tile.getExtent();
        if (extent == null)
            return;

        if (extent instanceof Cylinder)
            ((Cylinder) extent).render(dc);
    }

    private PickedObject[] pick(DrawContext dc, RectTile tile, List<? extends Point> pickPoints)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (pickPoints.size() == 0)
            return null;

        if (tile.ri == null)
            return null;

        PickedObject[] pos = new PickedObject[pickPoints.size()];
        this.renderTrianglesWithUniqueColors(dc, tile);
        for (int i = 0; i < pickPoints.size(); i++)
        {
            pos[i] = this.resolvePick(dc, tile, pickPoints.get(i));
        }

        return pos;
    }

    private void pick(DrawContext dc, RectTile tile, Point pickPoint)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tile.ri == null)
            return;

        renderTrianglesWithUniqueColors(dc, tile);
        PickedObject po = this.resolvePick(dc, tile, pickPoint);
        if (po != null)
            dc.addPickedObject(po);
    }

    private void renderTrianglesWithUniqueColors(DrawContext dc, RectTile tile)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (tile.ri.vertices == null)
            return;

        tile.ri.vertices.rewind();
        tile.ri.indices.rewind();

        javax.media.opengl.GL gl = dc.getGL();

        if (null != tile.ri.referenceCenter)
            dc.getView().pushReferenceCenter(dc, tile.ri.referenceCenter);

        tile.minColorCode = dc.getUniquePickColor().getRGB();
        int trianglesNum = tile.ri.indices.capacity() - 2;

        int[] indices = new int[3];
        double[] coords = new double[3];

        gl.glBegin(GL.GL_TRIANGLES);
        for (int i = 0; i < trianglesNum; i++)
        {
            java.awt.Color color = dc.getUniquePickColor();
            gl.glColor3ub((byte) (color.getRed() & 0xFF),
                (byte) (color.getGreen() & 0xFF),
                (byte) (color.getBlue() & 0xFF));

            tile.ri.indices.position(i);
            tile.ri.indices.get(indices);

            tile.ri.vertices.position(3 * indices[0]);
            tile.ri.vertices.get(coords);
            gl.glVertex3d(coords[0], coords[1], coords[2]);
//            int vIndex = 3 * tile.ri.indices.get(i);
//            gl.glVertex3d(tile.ri.vertices.get(vIndex), tile.ri.vertices.get(vIndex + 1), tile.ri.vertices.get(
//                vIndex + 2));

            tile.ri.vertices.position(3 * indices[1]);
            tile.ri.vertices.get(coords);
            gl.glVertex3d(coords[0], coords[1], coords[2]);
//            vIndex = 3 * tile.ri.indices.get(i + 1);
//            gl.glVertex3d(tile.ri.vertices.get(vIndex), tile.ri.vertices.get(vIndex + 1), tile.ri.vertices.get(
//                vIndex + 2));

            tile.ri.vertices.position(3 * indices[2]);
            tile.ri.vertices.get(coords);
            gl.glVertex3d(coords[0], coords[1], coords[2]);
//            vIndex = 3 * indices[2];//tile.ri.indices.get(i + 2);
//            gl.glVertex3d(tile.ri.vertices.get(vIndex), tile.ri.vertices.get(vIndex + 1), tile.ri.vertices.get(
//                vIndex + 2));
        }
        gl.glEnd();
        tile.maxColorCode = dc.getUniquePickColor().getRGB();

        if (null != tile.ri.referenceCenter)
            dc.getView().popReferenceCenter(dc);
    }

    private PickedObject resolvePick(DrawContext dc, RectTile tile, Point pickPoint)
    {
        int colorCode = this.pickSupport.getTopColor(dc, pickPoint);
        if (colorCode < tile.minColorCode || colorCode > tile.maxColorCode)
            return null;

        double EPSILON = (double) 0.00001f;

        int triangleIndex = colorCode - tile.minColorCode - 1;

        if (tile.ri.indices == null || triangleIndex >= (tile.ri.indices.capacity() - 2))
            return null;

        double centerX = tile.ri.referenceCenter.x;
        double centerY = tile.ri.referenceCenter.y;
        double centerZ = tile.ri.referenceCenter.z;

        int[] indices = new int[3];
        tile.ri.indices.position(triangleIndex);
        tile.ri.indices.get(indices);

//        int vIndex = 3 * tile.ri.indices.get(triangleIndex);
        double[] coords = new double[3];
        tile.ri.vertices.position(3 * indices[0]);
        tile.ri.vertices.get(coords);
        Vec4 v0 = new Vec4(coords[0] + centerX, coords[1] + centerY, coords[2] + centerZ);
//        Vec4 v0 = new Vec4((tile.ri.vertices.get(vIndex++) + centerX),
//            (tile.ri.vertices.get(vIndex++) + centerY),
//            (tile.ri.vertices.get(vIndex) + centerZ));

//        vIndex = 3 * tile.ri.indices.get(triangleIndex + 1);
        tile.ri.vertices.position(3 * indices[1]);
        tile.ri.vertices.get(coords);
        Vec4 v1 = new Vec4(coords[0] + centerX, coords[1] + centerY, coords[2] + centerZ);
//        Vec4 v1 = new Vec4((tile.ri.vertices.get(vIndex++) + centerX),
//            (tile.ri.vertices.get(vIndex++) + centerY),
//            (tile.ri.vertices.get(vIndex) + centerZ));

//        vIndex = 3 * tile.ri.indices.get(triangleIndex + 2);
        tile.ri.vertices.position(3 * indices[2]);
        tile.ri.vertices.get(coords);
        Vec4 v2 = new Vec4(coords[0] + centerX, coords[1] + centerY, coords[2] + centerZ);
//        Vec4 v2 = new Vec4((tile.ri.vertices.get(vIndex++) + centerX),
//            (tile.ri.vertices.get(vIndex++) + centerY),
//            (tile.ri.vertices.get(vIndex) + centerZ));

        // get triangle edge vectors and plane normal
        Vec4 e1 = v1.subtract3(v0);
        Vec4 e2 = v2.subtract3(v0);
        Vec4 N = e1.cross3(e2);  // if N is 0, the triangle is degenerate, we are not dealing with it

        Line ray = dc.getView().computeRayFromScreenPoint(pickPoint.getX(), pickPoint.getY());

        Vec4 w0 = ray.getOrigin().subtract3(v0);
        double a = -N.dot3(w0);
        double b = N.dot3(ray.getDirection());
        if (java.lang.Math.abs(b) < EPSILON) // ray is parallel to triangle plane
            return null;                    // if a == 0 , ray lies in triangle plane
        double r = a / b;

        Vec4 intersect = ray.getOrigin().add3(ray.getDirection().multiply3(r));
        Position pp = dc.getGlobe().computePositionFromPoint(intersect);

        // Draw the elevation from the elevation model, not the geode.
        double elev = dc.getGlobe().getElevation(pp.getLatitude(), pp.getLongitude());
        elev *= dc.getVerticalExaggeration();
        Position p = new Position(pp.getLatitude(), pp.getLongitude(), elev);

        return new PickedObject(pickPoint, colorCode, p, pp.getLatitude(), pp.getLongitude(), elev, true);
    }

    /**
     * Determines if and where a ray intersects a <code>RectTile</code> geometry.
     *
     * @param tile the <Code>RectTile</code> which geometry is to be tested for intersection.
     * @param line the ray for which an intersection is to be found.
     *
     * @return the <Vec4> point closest to the ray origin where an intersection has been found or null if no
     *         intersection was found.
     */
    private Intersection[] intersect(RectTile tile, Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tile.ri.vertices == null)
            return null;

        // Compute 'vertical' plane perpendicular to the ground, that contains the ray
        Vec4 normalV = line.getDirection().cross3(globe.computeSurfaceNormalAtPoint(line.getOrigin()));
        Plane verticalPlane = new Plane(normalV.x(), normalV.y(), normalV.z(), -line.getOrigin().dot3(normalV));
        if (!tile.getExtent().intersects(verticalPlane))
            return null;

        // Compute 'horizontal' plane perpendicular to the vertical plane, that contains the ray
        Vec4 normalH = line.getDirection().cross3(normalV);
        Plane horizontalPlane = new Plane(normalH.x(), normalH.y(), normalH.z(), -line.getOrigin().dot3(normalH));
        if (!tile.getExtent().intersects(horizontalPlane))
            return null;

        Intersection[] hits;
        ArrayList<Intersection> list = new ArrayList<Intersection>();

        int[] indices = new int[tile.ri.indices.limit()];
        double[] coords = new double[tile.ri.vertices.limit()];
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();
        tile.ri.indices.get(indices, 0, indices.length);
        tile.ri.vertices.get(coords, 0, coords.length);
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();

        int trianglesNum = tile.ri.indices.capacity() - 2;
        double centerX = tile.ri.referenceCenter.x;
        double centerY = tile.ri.referenceCenter.y;
        double centerZ = tile.ri.referenceCenter.z;

        // Compute maximum cell size based on tile delta lat, density and globe radius
        double cellSide = tile.getSector().getDeltaLatRadians() * globe.getRadius() / density;
        double maxCellRadius = Math.sqrt(cellSide * cellSide * 2) / 2;   // half cell diagonal

        // Compute maximum elevation difference
        double elevationSpan = tile.getExtent().getDiameter();

        // TODO: ignore back facing triangles?
        // Loop through all tile cells - triangle pairs
        int startIndice = (density + 2) * 2 + 6; // skip firts skirt row and a couple degenerate cells
        int endIndice = trianglesNum - startIndice; // ignore last skirt row and a couple degenerate cells
        int k = -1;
        for (int i = startIndice; i < endIndice; i += 2)
        {
            // Skip skirts and degenerate triangle cells - based on indice sequence.
            k = k == density - 1 ? -4 : k + 1; // density x terrain cells interleaved with 4 skirt and degenerate cells.
            if (k < 0)
                continue;

            // Triangle pair diagonal - v1 & v2
            int vIndex = 3 * indices[i + 1];
            Vec4 v1 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 2];
            Vec4 v2 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            Vec4 cellCenter = Vec4.mix3(.5, v1, v2);

            // Test cell center distance to vertical plane
            if (Math.abs(verticalPlane.distanceTo(cellCenter)) > maxCellRadius)
                continue;

            // Test cell center distance to horizontal plane
            if (Math.abs(horizontalPlane.distanceTo(cellCenter)) > elevationSpan)
                continue;

            // Prepare to test triangles - get other two vertices v0 & v3
            Vec4 p;
            vIndex = 3 * indices[i];
            Vec4 v0 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 3];
            Vec4 v3 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            // Test triangle 1 intersection w ray
            Triangle t = new Triangle(v0, v1, v2);
            if ((p = t.intersect(line)) != null)
            {
                list.add(new Intersection(p, false));
            }

            // Test triangle 2 intersection w ray
            t = new Triangle(v1, v2, v3);
            if ((p = t.intersect(line)) != null)
            {
                list.add(new Intersection(p, false));
            }
        }

        int numHits = list.size();
        if (numHits == 0)
            return null;

        hits = new Intersection[numHits];
        list.toArray(hits);

        final Vec4 origin = line.getOrigin();
        Arrays.sort(hits, new Comparator<Intersection>()
        {
            public int compare(Intersection i1, Intersection i2)
            {
                if (i1 == null && i2 == null)
                    return 0;
                if (i2 == null)
                    return -1;
                if (i1 == null)
                    return 1;

                Vec4 v1 = i1.getIntersectionPoint();
                Vec4 v2 = i2.getIntersectionPoint();
                double d1 = origin.distanceTo3(v1);
                double d2 = origin.distanceTo3(v2);
                return Double.compare(d1, d2);
            }
        });

        return hits;
    }

    /**
     * Determines if and where a <code>RectTile</code> geometry intersects the globe ellipsoid at a given elevation. The
     * returned array of <code>Intersection</code> describes a list of individual segments - two
     * <code>Intersection</code> for each, corresponding to each geometry triangle that intersects the given elevation.
     *
     * @param tile      the <Code>RectTile</code> which geometry is to be tested for intersection.
     * @param elevation the elevation for which intersection points are to be found.
     *
     * @return an array of <code>Intersection</code> pairs or null if no intersection was found.
     */
    private Intersection[] intersect(RectTile tile, double elevation)
    {
        if (tile.ri.vertices == null)
            return null;

        // Check whether the tile includes the intersection elevation - assume cylinder as Extent
        if (tile.getExtent() instanceof Cylinder)
        {
            Cylinder cylinder = ((Cylinder) tile.getExtent());
            if (!(globe.isPointAboveElevation(cylinder.getBottomCenter(), elevation)
                ^ globe.isPointAboveElevation(cylinder.getTopCenter(), elevation)))
                return null;
        }

        Intersection[] hits;
        ArrayList<Intersection> list = new ArrayList<Intersection>();

        int[] indices = new int[tile.ri.indices.limit()];
        double[] coords = new double[tile.ri.vertices.limit()];
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();
        tile.ri.indices.get(indices, 0, indices.length);
        tile.ri.vertices.get(coords, 0, coords.length);
        tile.ri.indices.rewind();
        tile.ri.vertices.rewind();

        int trianglesNum = tile.ri.indices.capacity() - 2;
        double centerX = tile.ri.referenceCenter.x;
        double centerY = tile.ri.referenceCenter.y;
        double centerZ = tile.ri.referenceCenter.z;

        // Loop through all tile cells - triangle pairs
        int startIndice = (density + 2) * 2 + 6; // skip firts skirt row and a couple degenerate cells
        int endIndice = trianglesNum - startIndice; // ignore last skirt row and a couple degenerate cells
        int k = -1;
        for (int i = startIndice; i < endIndice; i += 2)
        {
            // Skip skirts and degenerate triangle cells - based on indice sequence.
            k = k == density - 1 ? -4 : k + 1; // density x terrain cells interleaved with 4 skirt and degenerate cells.
            if (k < 0)
                continue;

            // Get the four cell corners
            int vIndex = 3 * indices[i];
            Vec4 v0 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 1];
            Vec4 v1 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 2];
            Vec4 v2 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            vIndex = 3 * indices[i + 3];
            Vec4 v3 = new Vec4(
                coords[vIndex++] + centerX,
                coords[vIndex++] + centerY,
                coords[vIndex] + centerZ);

            Intersection[] inter;
            // Test triangle 1 intersection
            if ((inter = globe.intersect(new Triangle(v0, v1, v2), elevation)) != null)
            {
                list.add(inter[0]);
                list.add(inter[1]);
            }

            // Test triangle 2 intersection
            if ((inter = globe.intersect(new Triangle(v1, v2, v3), elevation)) != null)
            {
                list.add(inter[0]);
                list.add(inter[1]);
            }
        }

        int numHits = list.size();
        if (numHits == 0)
            return null;

        hits = new Intersection[numHits];
        list.toArray(hits);

        return hits;
    }

    private Vec4 getSurfacePoint(RectTile tile, Angle latitude, Angle longitude, double metersOffset)
    {
        Vec4 result = this.getSurfacePoint(tile, latitude, longitude);
        if (metersOffset != 0 && result != null)
            result = applyOffset(this.globe, result, metersOffset);

        return result;
    }

    /**
     * Offsets <code>point</code> by <code>metersOffset</code> meters.
     *
     * @param globe        the <code>Globe</code> from which to offset
     * @param point        the <code>Vec4</code> to offset
     * @param metersOffset the magnitude of the offset
     *
     * @return <code>point</code> offset along its surface normal as if it were on <code>globe</code>
     */
    private static Vec4 applyOffset(Globe globe, Vec4 point, double metersOffset)
    {
        Vec4 normal = globe.computeSurfaceNormalAtPoint(point);
        point = Vec4.fromLine3(point, metersOffset, normal);
        return point;
    }

    private Vec4 getSurfacePoint(RectTile tile, Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!tile.sector.contains(latitude, longitude))
        {
            // not on this geometry
            return null;
        }

        if (tile.ri == null)
            return null;

        if (Math.abs(latitude.getDegrees()) > latitudeCutoffInDegrees)
            return getSurfacePointPolarRegion(tile, latitude, longitude);
        return getSurfacePointLateralSides(tile, latitude, longitude);
    }

    private Vec4 getSurfacePointLateralSides(RectTile tile, Angle latitude, Angle longitude)
    {
        // guaranteed that none of the input parameters are null
        double lat = latitude.getDegrees();
        double lon = longitude.getDegrees();

        double bottom = tile.sector.getMinLatitude().getDegrees();
        double top = tile.sector.getMaxLatitude().getDegrees();
        double left = tile.sector.getMinLongitude().getDegrees();
        double right = tile.sector.getMaxLongitude().getDegrees();

        double leftDecimal = (lon - left) / (right - left);
        double bottomDecimal = (lat - bottom) / (top - bottom);

        int row = (int) (bottomDecimal * (tile.density));
        int column = (int) (leftDecimal * (tile.density));

        double l = createPosition(column, leftDecimal, tile.ri.density);
        double h = createPosition(row, bottomDecimal, tile.ri.density);

        Vec4 result = interpolate(row, column, l, h, tile.ri);
        result = result.add3(tile.ri.referenceCenter);

        return result;
    }

    /**
     * Computes from a column (or row) number, and a given offset ranged [0,1] corresponding to the distance along the
     * edge of this sector, where between this column and the next column the corresponding position will fall, in the
     * range [0,1].
     *
     * @param start   the number of the column or row to the left, below or on this position
     * @param decimal the distance from the left or bottom of the current sector that this position falls
     * @param density the number of intervals along the sector's side
     *
     * @return a decimal ranged [0,1] representing the position between two columns or rows, rather than between two
     *         edges of the sector
     */
    private static double createPosition(int start, double decimal, int density)
    {
        double l = ((double) start) / (double) density;
        double r = ((double) (start + 1)) / (double) density;

        return (decimal - l) / (r - l);
    }

    /**
     * Calculates a <code>Point</code> that sits at <code>xDec</code> offset from <code>column</code> to <code>column +
     * 1</code> and at <code>yDec</code> offset from <code>row</code> to <code>row + 1</code>. Accounts for the
     * diagonals.
     *
     * @param row    represents the row which corresponds to a <code>yDec</code> value of 0
     * @param column represents the column which corresponds to an <code>xDec</code> value of 0
     * @param xDec   constrained to [0,1]
     * @param yDec   constrained to [0,1]
     * @param ri     the render info holding the vertices, etc.
     *
     * @return a <code>Point</code> geometrically within or on the boundary of the quadrilateral whose bottom left
     *         corner is indexed by (<code>row</code>, <code>column</code>)
     */
    private static Vec4 interpolate(int row, int column, double xDec, double yDec, RenderInfo ri)
    {
        row++;
        column++;

        int numVerticesPerEdge = ri.density + 3;

        int bottomLeft = row * numVerticesPerEdge + column;

        bottomLeft *= 3;

        int numVertsTimesThree = numVerticesPerEdge * 3;

        double[] a = new double[6];
        ri.vertices.position(bottomLeft);
        ri.vertices.get(a);
        Vec4 bL = new Vec4(a[0], a[1], a[2]);
        Vec4 bR = new Vec4(a[3], a[4], a[5]);
//        Vec4 bL = new Vec4(ri.vertices.get(bottomLeft), ri.vertices.get(bottomLeft + 1), ri.vertices.get(
//            bottomLeft + 2));
//        Vec4 bR = new Vec4(ri.vertices.get(bottomLeft + 3), ri.vertices.get(bottomLeft + 4),
//            ri.vertices.get(bottomLeft + 5));

        bottomLeft += numVertsTimesThree;

        ri.vertices.position(bottomLeft);
        ri.vertices.get(a);
        Vec4 tL = new Vec4(a[0], a[1], a[2]);
        Vec4 tR = new Vec4(a[3], a[4], a[5]);
//        Vec4 tL = new Vec4(ri.vertices.get(bottomLeft), ri.vertices.get(bottomLeft + 1), ri.vertices.get(
//            bottomLeft + 2));
//        Vec4 tR = new Vec4(ri.vertices.get(bottomLeft + 3), ri.vertices.get(bottomLeft + 4),
//            ri.vertices.get(bottomLeft + 5));

        return interpolate(bL, bR, tR, tL, xDec, yDec);
    }

    /**
     * Calculates the point at (xDec, yDec) in the two triangles defined by {bL, bR, tL} and {bR, tR, tL}. If thought of
     * as a quadrilateral, the diagonal runs from tL to bR. Of course, this isn't a quad, it's two triangles.
     *
     * @param bL   the bottom left corner
     * @param bR   the bottom right corner
     * @param tR   the top right corner
     * @param tL   the top left corner
     * @param xDec how far along, [0,1] 0 = left edge, 1 = right edge
     * @param yDec how far along, [0,1] 0 = bottom edge, 1 = top edge
     *
     * @return the point xDec, yDec in the co-ordinate system defined by bL, bR, tR, tL
     */
    private static Vec4 interpolate(Vec4 bL, Vec4 bR, Vec4 tR, Vec4 tL, double xDec, double yDec)
    {
        double pos = xDec + yDec;
        if (pos == 1)
        {
            // on the diagonal - what's more, we don't need to do any "oneMinusT" calculation
            return new Vec4(
                tL.x * yDec + bR.x * xDec,
                tL.y * yDec + bR.y * xDec,
                tL.z * yDec + bR.z * xDec);
        }
        else if (pos > 1)
        {
            // in the "top right" half

            // vectors pointing from top right towards the point we want (can be thought of as "negative" vectors)
            Vec4 horizontalVector = (tL.subtract3(tR)).multiply3(1 - xDec);
            Vec4 verticalVector = (bR.subtract3(tR)).multiply3(1 - yDec);

            return tR.add3(horizontalVector).add3(verticalVector);
        }
        else
        {
            // pos < 1 - in the "bottom left" half

            // vectors pointing from the bottom left towards the point we want
            Vec4 horizontalVector = (bR.subtract3(bL)).multiply3(xDec);
            Vec4 verticalVector = (tL.subtract3(bL)).multiply3(yDec);

            return bL.add3(horizontalVector).add3(verticalVector);
        }
    }

    private Vec4 getSurfacePointPolarRegion(RectTile tile, Angle latitude, Angle longitude)
    {
        // guaranteed that none of the input parameters are null
        Vec4 idealPos = this.globe.computePointFromPosition(latitude, longitude, 0);
        tile.ri.vertices.rewind();
        tile.ri.indices.rewind();

        Vec4 offset = tile.ri.referenceCenter;
        if (offset == null)
            offset = new Vec4(0.0);

        int trianglesNum = tile.ri.indices.capacity() - 2;

        int[] indices = new int[3];
        double[] coords = new double[3];

        for (int i = 0; i < trianglesNum; i++)
        {
            tile.ri.indices.position(i);
            tile.ri.indices.get(indices);

            if ((indices[0] == indices[1]) || (indices[0] == indices[2]) ||
                (indices[1] == indices[2]))
                // degenerate triangle
                continue;
            Vec4[] triVerts = new Vec4[3];
            for (int j = 0; j < 3; j++)
            {
                tile.ri.vertices.position(3 * indices[j]);
                tile.ri.vertices.get(coords);
                triVerts[j] = new Vec4(coords[0] + offset.getX(),
                    coords[1] + offset.getY(),
                    coords[2] + offset.getZ(), 1.0);
            }
            // project "idealPos" onto the plane of this triangle and
            // see if it is in the interior.
            Vec4 n = triVerts[1].subtract3(triVerts[0]).cross3(triVerts[2].subtract3(triVerts[0])).normalize3();
            double d = idealPos.subtract3(triVerts[0]).dot3(n);
            Vec4 projPnt = idealPos.subtract3(n.multiply3(-d));
            double[] b0b1b2 = baryCentricCoordsRequireInside(projPnt, triVerts);
            if (b0b1b2 != null)
                return projPnt;
        }
        return null;
    }

    private static double[] baryCentricCoordsRequireInside(Vec4 pnt, Vec4[] V)
    {
        // if pnt is in the interior of the triangle determined by V, return its
        // barycentric coordinates with respect to V. Otherwise return null.

        // b0:
        final double tol = 1.0e-4;
        double[] b0b1b2 = new double[3];
        double triangleHeight =
            distanceFromLine(V[0], V[1], V[2].subtract3(V[1]));
        double heightFromPoint =
            distanceFromLine(pnt, V[1], V[2].subtract3(V[1]));
        b0b1b2[0] = heightFromPoint / triangleHeight;
        if (Math.abs(b0b1b2[0]) < tol)
            b0b1b2[0] = 0.0;
        else if (Math.abs(1.0 - b0b1b2[0]) < tol)
            b0b1b2[0] = 1.0;
        if (b0b1b2[0] < 0.0 || b0b1b2[0] > 1.0)
            return null;

        // b1:
        triangleHeight = distanceFromLine(V[1], V[0], V[2].subtract3(V[0]));
        heightFromPoint = distanceFromLine(pnt, V[0], V[2].subtract3(V[0]));
        b0b1b2[1] = heightFromPoint / triangleHeight;
        if (Math.abs(b0b1b2[1]) < tol)
            b0b1b2[1] = 0.0;
        else if (Math.abs(1.0 - b0b1b2[1]) < tol)
            b0b1b2[1] = 1.0;
        if (b0b1b2[1] < 0.0 || b0b1b2[1] > 1.0)
            return null;

        // b2:
        b0b1b2[2] = 1.0 - b0b1b2[0] - b0b1b2[1];
        if (Math.abs(b0b1b2[2]) < tol)
            b0b1b2[2] = 0.0;
        else if (Math.abs(1.0 - b0b1b2[2]) < tol)
            b0b1b2[2] = 1.0;
        if (b0b1b2[2] < 0.0)
            return null;
        return b0b1b2;
    }

    private static double distanceFromLine(Vec4 pnt, Vec4 P, Vec4 u)
    {
        // Return distance from pnt to line(P,u)
        // Pythagorean theorem approach: c^2 = a^2 + b^2. The
        // The square of the distance we seek is b^2:
        Vec4 toPoint = pnt.subtract3(P);
        double cSquared = toPoint.dot3(toPoint);
        double aSquared = u.normalize3().dot3(toPoint);
        aSquared *= aSquared;
        double distSquared = cSquared - aSquared;
        if (distSquared < 0.0)
            // must be a tiny number that really ought to be 0.0
            return 0.0;
        return Math.sqrt(distSquared);
    }

    private static DoubleBuffer getLateralSideTextureCoordinates(int density)
    {
        if (density < 1)
            density = 1;

        // Approximate 1 to avoid shearing off of right and top skirts in SurfaceTileRenderer.
        // TODO: dig into this more: why are the skirts being sheared off?
        final double one = 0.999999;

        DoubleBuffer p = parameterizations.get(density);
        if (p != null)
            return p;

        int coordCount = (density + 3) * (density + 3);
        p = BufferUtil.newDoubleBuffer(2 * coordCount);
        double delta = 1d / density;
        int k = 2 * (density + 3);
        for (int j = 0; j < density; j++)
        {
            double v = j * delta;

            // skirt column; duplicate first column
            p.put(k++, 0d);
            p.put(k++, v);

            // interior columns
            for (int i = 0; i < density; i++)
            {
                p.put(k++, i * delta); // u
                p.put(k++, v);
            }

            // last interior column; force u to 1.
            p.put(k++, one);//1d);
            p.put(k++, v);

            // skirt column; duplicate previous column
            p.put(k++, one);//1d);
            p.put(k++, v);
        }

        // Last interior row
        //noinspection UnnecessaryLocalVariable
        double v = one;//1d;
        p.put(k++, 0d); // skirt column
        p.put(k++, v);

        for (int i = 0; i < density; i++)
        {
            p.put(k++, i * delta); // u
            p.put(k++, v);
        }
        p.put(k++, one);//1d); // last interior column
        p.put(k++, v);

        p.put(k++, one);//1d); // skirt column
        p.put(k++, v);

        // last skirt row
        int kk = k - 2 * (density + 3);
        for (int i = 0; i < density + 3; i++)
        {
            p.put(k++, p.get(kk++));
            p.put(k++, p.get(kk++));
        }

        // first skirt row
        k = 0;
        kk = 2 * (density + 3);
        for (int i = 0; i < density + 3; i++)
        {
            p.put(k++, p.get(kk++));
            p.put(k++, p.get(kk++));
        }

        parameterizations.put(density, p);

        return p;
    }

//    private static DoubleBuffer getPolarRegionTextureCoordinates(int density,
//		EdgeBoundedSector s, java.nio.DoubleBuffer verts)
//    {
//        if (density < 1)
//            density = 1;
//
//        // Approximate 1 to avoid shearing off of right and top skirts in SurfaceTileRenderer.
//        // TODO: dig into this more: why are the skirts being sheared off?
//        final double one = 0.999999;
//
//		int key = 2048 + density;
//        DoubleBuffer p = parameterizations.get(key);
//        if (p != null)
//            return p;
//
//        int coordCount = (density + 3) * (density + 3);
//        p = BufferUtil.newDoubleBuffer(2 * coordCount);
//        double delta = 1d / density;
//        int k = 2 * (density + 3);
///*
//		// the sector does not have boundaries of constant latitude/longitude, so we need
//		// to explicitly compute (s,t) based on the bounding rectangle:
//        double sectorWidth  = s.getDeltaLonRadians();
//        double sectorHeight = s.getDeltaLatRadians();
//        double sectorXmin   = s.getMinLongitude().radians;
//        double sectorYmin   = s.getMinLatitude().radians;
//		// store each (s,t) here:
//		double[] st = new double[2]; // (s,t) texture coordinates
//*/
//        for (int j = 0; j < density; j++)
//        {
//            double v = j * delta;
//
//            // skirt column; duplicate first column
//            p.put(k++, 0d);
//            p.put(k++, v);
//
//            // interior columns
//            for (int i = 0; i < density; i++)
//            {
//                p.put(k++, i * delta); // u
//                p.put(k++, v);
//            }
//
//            // last interior column; force u to 1.
//            p.put(k++, one);//1d);
//            p.put(k++, v);
//
//            // skirt column; duplicate previous column
//            p.put(k++, one);//1d);
//            p.put(k++, v);
//        }
//
//        // Last interior row
//        //noinspection UnnecessaryLocalVariable
//        double v = one;//1d;
//        p.put(k++, 0d); // skirt column
//        p.put(k++, v);
//
//        for (int i = 0; i < density; i++)
//        {
//            p.put(k++, i * delta); // u
//            p.put(k++, v);
//        }
//        p.put(k++, one);//1d); // last interior column
//        p.put(k++, v);
//
//        p.put(k++, one);//1d); // skirt column
//        p.put(k++, v);
//
//        // last skirt row
//        int kk = k - 2 * (density + 3);
//        for (int i = 0; i < density + 3; i++)
//        {
//            p.put(k++, p.get(kk++));
//            p.put(k++, p.get(kk++));
//        }
//
//        // first skirt row
//        k = 0;
//        kk = 2 * (density + 3);
//        for (int i = 0; i < density + 3; i++)
//        {
//            p.put(k++, p.get(kk++));
//            p.put(k++, p.get(kk++));
//        }
//
//        parameterizations.put(key, p);
//
//        return p;
//    }

    protected static IntBuffer getIndices(int density, int numVerticesDroppedPerRow)
    {
        if (density < 1)
            density = 1;

        // return a pre-computed buffer if possible.
        int hashKey = 1024 * numVerticesDroppedPerRow + density;
        java.nio.IntBuffer buffer = indexLists.get(hashKey);
        if (buffer != null)
            return buffer;

        if (numVerticesDroppedPerRow == 0)
            buffer = getRectangularIndices(density);
        else
            buffer = getTriangularIndices(density, numVerticesDroppedPerRow);

        indexLists.put(hashKey, buffer);
        return buffer;
    }

    private static IntBuffer getRectangularIndices(int density)
    {
        int sideSize = density + 2;

        int indexCount = 2 * sideSize * sideSize + 4 * sideSize - 2;
        java.nio.IntBuffer buffer = BufferUtil.newIntBuffer(indexCount);
        int k = 0;
        // The vertex array is (sideSize+1) x (sideSize+1). (Indices run
        // 0..sideSize; comments below refer to indices). On i-th trip through
        // loop, we triangulate between column i and column (i+1).
        for (int i = 0; i < sideSize; i++) // step across columns
        {
            buffer.put(k);
            if (i > 0)
            {
                buffer.put(++k);
                buffer.put(k);
            }

            if (i % 2 == 0) // even
            {
                buffer.put(++k);
                // triangulate between positions on row j and row (j+1)
                for (int j = 0; j < sideSize; j++)
                {
                    k += sideSize; // jump to one spot earlier on row (j+1)
                    buffer.put(k);
                    buffer.put(++k);
                }
            }
            else // odd
            {
                buffer.put(--k);
                // triangulate between positions on row (sideSize-j) and row (sideSize-j-1)
                for (int j = 0; j < sideSize; j++)
                {
                    k -= sideSize; // jump to one spot later on row (sideSize-j-1)
                    buffer.put(k);
                    buffer.put(--k);
                }
            }
        }
        return buffer;
    }

    private static IntBuffer getTriangularIndices(int densityIn, int numVerticesDroppedPerRow)
    {
        // density along the bottom of a triangular patch is really 2*density. Moreover, the
        // bottom skirt row is "numVerticesDroppedPerRow" longer, and we have to add two (one
        // on each end) for the rest of the skirt. Finally, since these are really "numGaps",
        // we add one more to get num vertices required:
        int numOnBottomRow = 2 * densityIn + numVerticesDroppedPerRow + 3;
        int indexCount = numOnBottomRow * numOnBottomRow + 6 * numOnBottomRow + 8; // TODO: check derivation. Loose?
        java.nio.IntBuffer buffer = BufferUtil.newIntBuffer(indexCount);
        int firstIndexOnBottomRow = 0;
        int dir = 1;
        while (numOnBottomRow > numVerticesDroppedPerRow)
        {
            int numOnSecondRow = numOnBottomRow - numVerticesDroppedPerRow;
            int firstIndexOnSecondRow = firstIndexOnBottomRow;
            if (dir == 1)
                firstIndexOnSecondRow += numOnBottomRow;
            else
                firstIndexOnSecondRow += numOnSecondRow;
            int lastIndexWrittenOnSecondRow = -1;
            int nWrittenOnSecondRow = 0;
            buffer.put(firstIndexOnBottomRow);
            if (firstIndexOnBottomRow != 0)
                buffer.put(firstIndexOnBottomRow);
            for (int i = 1; i < numOnBottomRow; i++)
            {
                buffer.put(firstIndexOnBottomRow + i * dir);
                if (nWrittenOnSecondRow < numOnSecondRow)
                {
                    lastIndexWrittenOnSecondRow = firstIndexOnSecondRow + nWrittenOnSecondRow * dir;
                    buffer.put(lastIndexWrittenOnSecondRow);
                    nWrittenOnSecondRow++;
                }
            }
            // get ready to go other direction
            dir = -dir;
            numOnBottomRow = numOnSecondRow;
            firstIndexOnBottomRow = lastIndexWrittenOnSecondRow;
        }
        return buffer;
    }

    private SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(RectTile tile, Plane[] planes)
    {
        tile.ri.vertices.rewind();
        tile.ri.indices.rewind();

        Vec4 offset = tile.ri.referenceCenter;
        if (offset == null)
            offset = new Vec4(0.0);

        int trianglesNum = tile.ri.indices.capacity() - 2;

        int[] indices = new int[3];
        double[] coords = new double[3];

        SectorGeometry.ExtractedShapeDescription clippedTriangleList = null;
        for (int i = 0; i < trianglesNum; i++)
        {
            tile.ri.indices.position(i);
            tile.ri.indices.get(indices);

            if ((indices[0] == indices[1]) || (indices[0] == indices[2]) ||
                (indices[1] == indices[2]))
                // degenerate triangle
                continue;
            Vec4[] triVerts = new Vec4[3];
            for (int j = 0; j < 3; j++)
            {
                tile.ri.vertices.position(3 * indices[j]);
                tile.ri.vertices.get(coords);
                triVerts[j] = new Vec4(coords[0] + offset.getX(),
                    coords[1] + offset.getY(),
                    coords[2] + offset.getZ(), 1.0);
            }
            clippedTriangleList = addClippedPolygon(triVerts, planes, clippedTriangleList);
        }
        return clippedTriangleList;
    }

    private SectorGeometry.ExtractedShapeDescription addClippedPolygon(Vec4[] triVerts, Plane[] planes,
        SectorGeometry.ExtractedShapeDescription l)
    {
        // Clip the polygon defined by polyVerts to the region defined by the intersection of
        // the negative halfspaces in 'planes'. If there is a non-empty clipped result, then
        // add it to the given list.
        // This routine is (currently) only used to clip triangles in the current tessellation,
        // but it is actually general enough for n-sided polygons. Best results will be
        // obtained if the polygon is convex.

        // ignore triangles on skirts
        if (isSkirt(triVerts))
            return l;

        // We use a multi-pass Sutherland-Hodgman-style clipping algorithm.
        // There is one pass for each clipping plane. We begin by copying the
        // original vertices to local working storage.
        Vec4[] polyVerts = new Vec4[3];
        System.arraycopy(triVerts, 0, polyVerts, 0, 3);

        for (Plane p : planes)
        {
            polyVerts = doSHPass(p, polyVerts);
            if (polyVerts == null)
                // the polygon has been totally clipped away
                return l;
        }
        // some part of the polygon survived. Store it in the list.
        if (l == null)
            l = new SectorGeometry.ExtractedShapeDescription(
                new ArrayList<Vec4[]>(), new ArrayList<SectorGeometry.BoundaryEdge>());
        l.interiorPolys.add(polyVerts);
        addBoundaryEdges(polyVerts, triVerts, l.shapeOutline);

        return l;
    }

    private boolean isSkirt(Vec4[] triVerts)
    {
        Vec4 normal = globe.computeSurfaceNormalAtPoint(triVerts[0]);
        // try to minimize numerical roundoff. The three triangle vertices
        // are going to have coordinates with roughly the same magnitude,
        // so we just sample triVerts[0].
        double maxC = Math.max(Math.abs(triVerts[0].x), Math.abs(triVerts[0].y));
        maxC = Math.max(maxC, Math.abs(triVerts[0].z));
        Vec4 v0 = triVerts[0].divide3(maxC);
        Vec4 u = triVerts[1].divide3(maxC).subtract3(v0);
        Vec4 v = triVerts[triVerts.length - 1].divide3(maxC).subtract3(v0);
        Vec4 w = u.cross3(v).normalize3();
        return (Math.abs(w.dot3(normal)) < 0.0001);
    }

    private Vec4[] doSHPass(Plane p, Vec4[] polyVerts)
    {
        // See comments in addClippedPolygon. Also note that, even if the
        // original polygon is a triangle, the polygon here may have
        // more than three vertices, depending on how it cuts the various
        // planes whose volumetric intersection defines the clipping region.
        ArrayList<Vec4> workingStorage = new ArrayList<Vec4>();
        Vec4 startPnt = polyVerts[0];
        boolean startPntIn = (p.dot(startPnt) < 0.0);
        for (int i = 1; i <= polyVerts.length; i++)
        {
            if (startPntIn)
                workingStorage.add(startPnt);
            Vec4 endPnt = polyVerts[i % polyVerts.length];
            boolean endPntIn = (p.dot(endPnt) < 0.0);
            if (startPntIn != endPntIn)
            {
                // compute and store the intersection of this edge with p
                Vec4[] clippedPnts;
                if (startPntIn)
                    clippedPnts = p.clip(startPnt, endPnt);
                else
                    clippedPnts = p.clip(endPnt, startPnt);
                if (clippedPnts != null)
                    workingStorage.add(clippedPnts[0]);
            }
            // prepare for next edge
            startPnt = endPnt;
            startPntIn = endPntIn;
        }
        if (workingStorage.size() == 0)
            return null;
        Vec4[] verts = new Vec4[workingStorage.size()];
        return workingStorage.toArray(verts);
    }

    private void addBoundaryEdges(Vec4[] polyVerts, Vec4[] triVerts,
        ArrayList<SectorGeometry.BoundaryEdge> beList)
    {
        // each edge of polyVerts not coincident with an edge of the original
        // triangle (triVerts) belongs to the outer boundary.
        for (int i = 0; i < polyVerts.length; i++)
        {
            int j = (i + 1) % polyVerts.length;
            if (!edgeOnTriangle(polyVerts[i], polyVerts[j], triVerts))
                beList.add(new SectorGeometry.BoundaryEdge(polyVerts, i, j));
        }
    }

    private boolean edgeOnTriangle(Vec4 a, Vec4 b, Vec4[] tri)
    {
        final double tol = 1.0e-4;
        double[] coords_a = baryCentricCoordsRequireInside(a, tri);
        double[] coords_b = baryCentricCoordsRequireInside(b, tri);
        if ((coords_a == null) || (coords_b == null))
            // mathematically not possible because 'a' and 'b' are
            // known to be on edges of the triangle 'tri'.
            return true;
        for (int i = 0; i < 3; i++)
        {
            if ((coords_a[i] < tol) && (coords_b[i] < tol))
                // 'a' and 'b' are on the same edge
                return true;
        }
        return false;
    }

    private SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(RectTile tile, Vec4 Cxyz,
        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius)
    {
        tile.ri.vertices.rewind();
        tile.ri.indices.rewind();

        Vec4 offset = tile.ri.referenceCenter;
        if (offset == null)
            offset = new Vec4(0.0);

        int trianglesNum = tile.ri.indices.capacity() - 2;

        int[] indices = new int[3];
        double[] coords = new double[3];

        SectorGeometry.ExtractedShapeDescription clippedTriangleList = null;
        for (int i = 0; i < trianglesNum; i++)
        {
            tile.ri.indices.position(i);
            tile.ri.indices.get(indices);

            if ((indices[0] == indices[1]) || (indices[0] == indices[2]) ||
                (indices[1] == indices[2]))
                // degenerate triangle
                continue;
            Vec4[] triVerts = new Vec4[3];
            for (int j = 0; j < 3; j++)
            {
                tile.ri.vertices.position(3 * indices[j]);
                tile.ri.vertices.get(coords);
                triVerts[j] = new Vec4(coords[0] + offset.getX(),
                    coords[1] + offset.getY(),
                    coords[2] + offset.getZ(), 1.0);
            }
            clippedTriangleList = addClippedPolygon(triVerts,
                Cxyz, uHat, vHat, uRadius, vRadius, clippedTriangleList);
        }
        return clippedTriangleList;
    }

    private SectorGeometry.ExtractedShapeDescription addClippedPolygon(Vec4[] polyVerts, Vec4 Cxyz,
        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius, SectorGeometry.ExtractedShapeDescription l)
    {
        // ignore triangles on skirts
        if (isSkirt(polyVerts))
            return l;

        int i = 0, nInNegHalfspace = 0, locIn = -1, locOut = -1;
        for (Vec4 vtx : polyVerts)
        {
            Vec4 vMinusC = vtx.subtract3(Cxyz);
            double xd = vMinusC.dot3(uHat);
            double yd = vMinusC.dot3(vHat);
            double halfspaceEqn = (xd * xd) / (uRadius * uRadius) + (yd * yd) / (vRadius * vRadius) - 1.0;
            if (halfspaceEqn <= 0.0)
            {
                locIn = i++;
                nInNegHalfspace++;
            }
            else
                locOut = i++;
        }
        SectorGeometry.BoundaryEdge be = new SectorGeometry.BoundaryEdge(null, -1, -1);
        switch (nInNegHalfspace)
        {
            case 0: // check for edge intersections
                polyVerts = checkForEdgeCylinderIntersections(polyVerts, Cxyz, uHat, vHat,
                    uRadius, vRadius);
                break;
            case 1: // compute and return a trimmed triangle
                if (locIn != 0)
                {
                    Vec4 h1 = polyVerts[locIn];
                    polyVerts[locIn] = polyVerts[0];
                    polyVerts[0] = h1;
                }
                polyVerts = computeTrimmedPoly(polyVerts, Cxyz, uHat, vHat, uRadius,
                    vRadius, nInNegHalfspace, be);
                break;
            case 2: // compute and return a trimmed quadrilateral
                if (locOut != 0)
                {
                    Vec4 h2 = polyVerts[locOut];
                    polyVerts[locOut] = polyVerts[0];
                    polyVerts[0] = h2;
                }
                polyVerts = computeTrimmedPoly(polyVerts, Cxyz, uHat, vHat, uRadius,
                    vRadius, nInNegHalfspace, be);
                break;
            case 3: // triangle completely inside cylinder, so store it
                break;
        }
        if (polyVerts == null)
            return l;
        if (l == null)
            l = new SectorGeometry.ExtractedShapeDescription(new ArrayList<Vec4[]>(100),
                new ArrayList<SectorGeometry.BoundaryEdge>(50));
        l.interiorPolys.add(polyVerts);
        if (be.vertices != null)
            l.shapeOutline.add(be);
        return l;
    }

    private Vec4[] checkForEdgeCylinderIntersections(Vec4[] polyVerts, Vec4 Cxyz,
        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius)
    {
        // no triangle vertices are inside the cylinder; see if there are edge intersections
        // this will only be the case if the cylinder's size is roughly the same as a triangle
        // in the current tessellation and may not be worth the extra computation to check.
        return null;
    }

    private Vec4[] computeTrimmedPoly(Vec4[] polyVerts, Vec4 Cxyz,
        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius, int nInside,
        SectorGeometry.BoundaryEdge be)
    {
        // Either 1 or 2 vertices are inside the ellipse. If exactly 1 is inside, it is in position 0
        // of the array. If exactly 1 is outside, it is in position 0 of the array.
        // We therefore compute the points of intersection between the two edges [0]-[1] and [0]-[2]
        // with the cylinder and return either a triangle or a quadrilateral.
        Vec4 p1 = intersectWithEllCyl(polyVerts[0], polyVerts[1], Cxyz, uHat, vHat, uRadius, vRadius);
        Vec4 p2 = intersectWithEllCyl(polyVerts[0], polyVerts[2], Cxyz, uHat, vHat, uRadius, vRadius);
        Vec4 midP1P2 = p1.multiply3(0.5).add3(p2.multiply3(0.5));
        if (nInside == 1)
        {
            polyVerts[1] = p1;
            polyVerts[2] = p2;
            be.vertices = polyVerts;
            be.i1 = 1;
            be.i2 = 2;
            be.toMidPoint = midP1P2.subtract3(polyVerts[0]);
            return polyVerts;
        }
        Vec4[] ret = new Vec4[4];
        ret[0] = p1;
        ret[1] = polyVerts[1];
        ret[2] = polyVerts[2];
        ret[3] = p2;
        be.vertices = ret;
        be.i1 = 0;
        be.i2 = 3;
        be.toMidPoint = polyVerts[0].subtract3(midP1P2);
        return ret;
    }

    private Vec4 intersectWithEllCyl(Vec4 v0, Vec4 v1, Vec4 Cxyz,
        Vec4 uHat, Vec4 vHat, double uRadius, double vRadius)
    {
        // Entry condition: one of (v0, v1) is inside the elliptical cylinder, and one is
        // outside. We find 0<t<1 such that (1-t)*v0 + t*v1 is on the cylinder. We then return
        // the corresponding point.

        // First project v0 and v1 onto the plane of the ellipse
        Vec4 v0MinusC = v0.subtract3(Cxyz);
        double v0x = v0MinusC.dot3(uHat);
        double v0y = v0MinusC.dot3(vHat);
        Vec4 v1MinusC = v1.subtract3(Cxyz);
        double v1x = v1MinusC.dot3(uHat);
        double v1y = v1MinusC.dot3(vHat);

        // Then compute the coefficients of the quadratic equation describing where
        // the line segment (v0x,v0y)-(v1x,v1y) intersects the ellipse in the plane:
        double v1xMinusV0x = v1x - v0x;
        double v1yMinusV0y = v1y - v0y;
        double uRsquared = uRadius * uRadius;
        double vRsquared = vRadius * vRadius;

        double a = v1xMinusV0x * v1xMinusV0x / uRsquared + v1yMinusV0y * v1yMinusV0y / vRsquared;
        double b = 2.0 * (v0x * v1xMinusV0x / uRsquared + v0y * v1yMinusV0y / vRsquared);
        double c = v0x * v0x / uRsquared + v0y * v0y / vRsquared - 1.0;

        // now solve it
        // if the entry condition is satsfied, the diuscriminant will not be negative...
        double disc = Math.sqrt(b * b - 4.0 * a * c);
        double t = (-b + disc) / (2.0 * a);
        if ((t < 0.0) || (t > 1.0))
            // need the other root
            t = (-b - disc) / (2.0 * a);

        // the desired point is obtained by using the computed t with the original points
        // v0 and v1:
        return v0.multiply3(1.0 - t).add3(v1.multiply3(t));
    }

    // TODO: The following method was brought over from BasicRectangularTessellator and is unchecked.
//    // Compute normals for a strip
//    protected static java.nio.DoubleBuffer getNormals(int density, DoubleBuffer vertices,
//        java.nio.IntBuffer indices, Vec4 referenceCenter)
//    {
//        int numVertices = (density + 3) * (density + 3);
//        int sideSize = density + 2;
//        int numFaces = indices.limit() - 2;
//        double centerX = referenceCenter.x;
//        double centerY = referenceCenter.y;
//        double centerZ = referenceCenter.z;
//        // Create normal buffer
//        java.nio.DoubleBuffer normals = BufferUtil.newDoubleBuffer(numVertices * 3);
//        // Create per vertex normal lists
//        ArrayList<ArrayList<Vec4>> normalLists = new ArrayList<ArrayList<Vec4>>(numVertices);
//        for (int i = 0; i < numVertices; i++)
//            normalLists.set(i, new ArrayList<Vec4>());
//        // Go through all faces in the strip and store normals in lists
//        for (int i = 0; i < numFaces; i++)
//        {
//            int vIndex = 3 * indices.get(i);
//            Vec4 v0 = new Vec4((vertices.get(vIndex++) + centerX),
//                (vertices.get(vIndex++) + centerY),
//                (vertices.get(vIndex) + centerZ));
//
//            vIndex = 3 * indices.get(i + 1);
//            Vec4 v1 = new Vec4((vertices.get(vIndex++) + centerX),
//                (vertices.get(vIndex++) + centerY),
//                (vertices.get(vIndex) + centerZ));
//
//            vIndex = 3 * indices.get(i + 2);
//            Vec4 v2 = new Vec4((vertices.get(vIndex++) + centerX),
//                (vertices.get(vIndex++) + centerY),
//                (vertices.get(vIndex) + centerZ));
//
//            // get triangle edge vectors and plane normal
//            Vec4 e1 = v1.subtract3(v0);
//            Vec4 e2 = v2.subtract3(v0);
//            Vec4 N = e1.cross3(e2).normalize3();  // if N is 0, the triangle is degenerate
//
//            // Store the face's normal for each of the vertices that make up the face.
//            // TODO: Clear up warnings here
//            normalLists.get(indices.get(i)).add(N);
//            normalLists.get(indices.get(i + 1)).add(N);
//            normalLists.get(indices.get(i + 2)).add(N);
//            //System.out.println("Normal: " + N);
//        }
//
//        // Now loop through each vertex, and average out all the normals stored.
//        int idx = 0;
//        for (int i = 0; i < numVertices; i++)
//        {
//            Vec4 normal = Vec4.ZERO;
//            // Sum
//            for (int j = 0; j < normalLists.get(i).size(); ++j)
//                normal = normal.add3(normalLists.get(i).get(j));
//            // Average
//            normal = normal.multiply3(1.0f / normalLists.get(i).size()).normalize3();
//            // Fill normal buffer
//            normals.put(idx++, normal.x);
//            normals.put(idx++, normal.y);
//            normals.put(idx++, normal.z);
//            //System.out.println("Normal: " + normal + " - " + normalLists[i].size());
//            //System.out.println("Normal buffer: " + normals.get(idx - 3) + ", " + normals.get(idx - 2) + ", " + normals.get(idx - 1));
//        }
//
//        return normals;
//    }

    //
    // Exposes aspects of the RectTile.
    //

    public static class RectGeometry
    {
        private RectTile tile;
        private double rowFactor;
        private double colFactor;

        public RectGeometry(RectTile tile)
        {
            this.tile = tile;
            // Precompute as much as possible; computation in this class is a hot spot...
            rowFactor = getNumRows() / tile.sector.getDeltaLatDegrees();
            colFactor = getNumCols() / tile.sector.getDeltaLonDegrees();
        }

        public int getColAtLon(double longitude)
        {
            return (int) Math.floor((longitude - tile.sector.getMinLongitude().degrees) * colFactor);
        }

        public int getRowAtLat(double latitude)
        {
            return (int) Math.floor((latitude - tile.sector.getMinLatitude().degrees) * rowFactor);
        }

        public double getLatAtRow(int row)
        {
            return tile.sector.getMinLatitude().degrees + row / rowFactor;
        }

        public double getLonAtCol(int col)
        {
            return tile.sector.getMinLongitude().degrees + col / colFactor;
        }

        /*
         * Bilinearly interpolate XYZ coords from the grid patch that contains the given lat-lon.
         *
         * Note:  The returned point is clamped along the nearest border if the given lat-lon is outside the
         * region spanned by this tile.
         *
         */
        public double[] getPointAt(double lat, double lon)
        {
            int col = getColAtLon(lon);
            if (col < 0)
            {
                col = 0;
                lon = getMinLongitude();
            }
            else if (col > getNumCols())
            {
                col = getNumCols();
                lon = getMaxLongitude();
            }

            int row = getRowAtLat(lat);
            if (row < 0)
            {
                row = 0;
                lat = getMinLatitude();
            }
            else if (row > getNumRows())
            {
                row = getNumRows();
                lat = getMaxLatitude();
            }

            double[] c0 = new double[3];
            this.tile.ri.vertices.position(getVertexIndex(row, col));
            this.tile.ri.vertices.get(c0);
            double[] c1 = new double[3];
            this.tile.ri.vertices.position(getVertexIndex(row, col + 1));
            this.tile.ri.vertices.get(c1);
            double[] c2 = new double[3];
            this.tile.ri.vertices.position(getVertexIndex(row + 1, col));
            this.tile.ri.vertices.get(c2);
            double[] c3 = new double[3];
            this.tile.ri.vertices.position(getVertexIndex(row + 1, col + 1));
            this.tile.ri.vertices.get(c3);
            double[] refCenter = new double[3];
            this.tile.ri.referenceCenter.toArray3(refCenter, 0);

            // calculate our parameters u and v...
            double minLon = getLonAtCol(col);
            double maxLon = getLonAtCol(col + 1);
            double minLat = getLatAtRow(row);
            double maxLat = getLatAtRow(row + 1);
            double u = (lon - minLon) / (maxLon - minLon);
            double v = (lat - minLat) / (maxLat - minLat);

            double[] ret = new double[3];
            // unroll the loop...this method is a definite hotspot!
            ret[0] = c0[0] * (1. - u) * (1 - v) + c1[0] * (u) * (1. - v) + c2[0] * (1. - u) * (v) + c3[0] * u * v
                + refCenter[0];
            ret[1] = c0[1] * (1. - u) * (1 - v) + c1[1] * (u) * (1. - v) + c2[1] * (1. - u) * (v) + c3[1] * u * v
                + refCenter[1];
            ret[2] = c0[2] * (1. - u) * (1 - v) + c1[2] * (u) * (1. - v) + c2[2] * (1. - u) * (v) + c3[2] * u * v
                + refCenter[2];
            return ret;
        }

        public double getMinLongitude()
        {
            return this.tile.sector.getMinLongitude().degrees;
        }

        public double getMaxLongitude()
        {
            return this.tile.sector.getMaxLongitude().degrees;
        }

        public double getMinLatitude()
        {
            return this.tile.sector.getMinLatitude().degrees;
        }

        public double getMaxLatitude()
        {
            return this.tile.sector.getMaxLatitude().degrees;
        }

        public int getNumRows()
        {
            return this.tile.density;
        }

        public int getNumCols()
        {
            return this.tile.density;
        }

        private int getVertexIndex(int row, int col)
        {
            // The factor of 3 accounts for the 3 doubles that make up each node...
            // The 3 added to density is 2 tile-skirts plus 1 ending column...
            return (this.tile.density + 3) * (row + 1) * 3 + (col + 1) * 3;
        }
    }

    public static RectGeometry getTerrainGeometry(SectorGeometry tile)
    {
        if (tile == null || !(tile instanceof RectTile))
            // TODO: I*N this
            throw new IllegalArgumentException("SectorGeometry instance not of type RectTile");

        return new RectGeometry((RectTile) tile);
    }
}
