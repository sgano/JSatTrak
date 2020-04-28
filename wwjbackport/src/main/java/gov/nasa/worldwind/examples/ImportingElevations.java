/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import org.w3c.dom.Document;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * This example demonstrates how to import elevation data into a World Wind {@link gov.nasa.worldwind.cache.FileStore}
 * by converting the elevations into the World Wind Java cache format, then constructing a {@link
 * gov.nasa.worldwind.globes.ElevationModel} which can display the elevations as part of the {@link
 * gov.nasa.worldwind.globes.Globe} surface.
 * <p/>
 * Elevation data is imported into a FileStore by executing the following steps: <ol> <li>Choose the FileStore location
 * into to import the elevations into. This example uses the FileStore's install location, which indicates the imported
 * elevations should never be removed. </li> <li>Compute a unique <strong>cache name</strong> for the elevations. In
 * this example, the cache name defined as "Examples/ElevationsName", where "ElevationsName" is the elevation data's
 * display name, stripped of any illegal filename characters.</li> <li>Convert the elevation data to the World Wind Java
 * cache format. This produces a pyramid of elevation tiles in the FileStore. We accomplish this by configuring a {@link
 * gov.nasa.worldwind.data.TiledElevationProducer} with the file to process and the location to put the processed
 * elevation tiles.</li> <li>The elevation data's processed form is described by a configuration {@link
 * org.w3c.dom.Document}, which we use to constrct an ElevationModel via the {@link gov.nasa.worldwind.Factory} method
 * {@link gov.nasa.worldwind.Factory#createFromConfigSource(Object, gov.nasa.worldwind.avlist.AVList)}.</li> </ol>
 *
 * @author tag
 * @version $Id: ImportingElevations.java 13466 2010-06-17 20:44:29Z tgaskins $
 */
public class ImportingElevations extends ApplicationTemplate
{
    protected static final String BASE_CACHE_PATH = "Examples/";
    protected static final String ELEVATIONS_URL =
        "http://worldwind.arc.nasa.gov/java/demos/data/wa-snohomish-dtm-16bit.tif.zip";

    // Overrides ApplicationTemplate.AppFrame's constructor to import an elevation dataset.
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Download the source file and create the FileStore which we'll import the elevation data into.
            File sourceFile = downloadAndUnzipToTempFile(WWIO.makeURL(ELEVATIONS_URL), ".tif");
            FileStore fileStore = WorldWind.getDataFileStore();

            // Import the elevation data into the FileStore by converting it to the World Wind Java cache format.
            ElevationModel em = importElevationData("Snohomish WA LiDAR DTM", sourceFile, fileStore);
            if (em != null)
            {
                CompoundElevationModel model
                    = (CompoundElevationModel) this.getWwd().getModel().getGlobe().getElevationModel();
                model.addElevationModel(em);
            }
        }
    }

    protected static ElevationModel importElevationData(String displayName, Object elevationSource, FileStore fileStore)
    {
        // Use the FileStore's install location as the destination for the imported elevation tiles. The install
        // location is an area in the data file store for permenantly resident data.
        File fileStoreLocation = DataImportUtil.getDefaultImportLocation(fileStore);
        // Create a unique cache name which defines the FileStore path to the imported elevations.
        String cacheName = BASE_CACHE_PATH + WWIO.replaceIllegalFileNameCharacters(displayName);

        // Create a parameter list which defines where the elevation data is imported, the name associated with it,
        // and default extreme elevations for the ElevationModel using the minimum and maximum elevations on
        // Earth.
        AVList params = new AVListImpl();
        params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
        params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
        params.setValue(AVKey.DATASET_NAME, displayName);

        // Create a TiledElevationProducer to transforms the source elevation data to a pyramid of elevation tiles in
        // the World Wind Java cache format.
        TiledElevationProducer producer = new TiledElevationProducer();
        try
        {
            // Configure the TiledElevationProducer with the parameter list and the elevation data source.
            producer.setStoreParameters(params);
            producer.offerDataSource(elevationSource, null);
            // Import the source elevation data into the FileStore by converting it to the World Wind Java cache format.
            producer.startProduction();
        }
        catch (Exception e)
        {
            producer.removeProductionState();
            e.printStackTrace();
            return null;
        }

        // Extract the data configuration document from the production results. If production sucessfully completed, the
        // TiledElevationProducer should always contain a document in the production results, but we test the results
        // anyway.
        Iterable<?> results = producer.getProductionResults();
        if (results == null || results.iterator() == null || !results.iterator().hasNext())
            return null;

        Object o = results.iterator().next();
        if (o == null || !(o instanceof Document))
            return null;

        // Construct an ElevationModel by passing the data configuration document to an ElevationModelFactory.
        return (ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY, ((Document) o).getDocumentElement());
    }

    // Temporary method for downloading and inflating a remote zip file. We'll replace this with a simple two-line URL
    // download when the GeoTIFF reader provides support for reading zipped files directly.
    public static File downloadAndUnzipToTempFile(URL url, String suffix)
    {
        try
        {
            ByteBuffer buffer = WWIO.readURLContentToBuffer(url);
            File file = WWIO.saveBufferToTempFile(buffer, WWIO.getFilename(url.toString()));

            buffer = WWIO.readZipEntryToBuffer(file, null);
            return WWIO.saveBufferToTempFile(buffer, suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters to start looking down a valley in Snohomish, WA, USA which clearly
        // demonstrates the imported elevation data.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 47.916674844546904);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.08177639628207);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 800);
        Configuration.setValue(AVKey.INITIAL_HEADING, 25);
        Configuration.setValue(AVKey.INITIAL_PITCH, 80);
        // Configure a default vertical exaggeration of 2x to exaggerate the imported elevation data.
        Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, 2d);

        ApplicationTemplate.start("World Wind Elevation Import", ImportingElevations.AppFrame.class);
    }
}
