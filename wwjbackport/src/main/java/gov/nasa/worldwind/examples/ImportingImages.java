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
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWIO;
import org.w3c.dom.Document;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * This example demonstrates how to import image data into a World Wind {@link FileStore} by converting the image into
 * the World Wind Java cache format, then constructing a {@link gov.nasa.worldwind.layers.Layer} which can render the
 * converted image on a {@link gov.nasa.worldwind.globes.Globe}.
 * <p/>
 * Image data is imported into a FileStore by executing the following steps: <ol> <li>Choose the FileStore location into
 * to import the image into. This example uses the FileStore's install location, which indicates the imported image
 * should never be removed. </li> <li>Compute a unique <strong>cache name</strong> for the image. In this example, the
 * cache name defined as "Examples/ImageName", where "ImageName" is the image's display name, stripped of any illegal
 * filename characters.</li> <li>Convert the image to the World Wind Java cache format. This produces a pyramid of image
 * tiles in the FileStore. We accomplish this by configuring a {@link gov.nasa.worldwind.data.TiledImageProducer} with
 * the file to process and the location to put the processed image tiles.</li> <li>The image's processed form is
 * described by a configuration {@link org.w3c.dom.Document}, which we use to construct a Layer via the {@link Factory}
 * method {@link gov.nasa.worldwind.Factory#createFromConfigSource(Object, gov.nasa.worldwind.avlist.AVList)}.</li>
 * </ol>
 *
 * @author tag
 * @version $Id: ImportingImages.java 14220 2010-12-10 19:02:38Z tgaskins $
 */
public class ImportingImages extends ApplicationTemplate
{
    protected static final String BASE_CACHE_PATH = "Examples/";
    protected static final String IMAGE_URL =
        "http://worldwind.arc.nasa.gov/java/demos/data/wa-precip-24hmam-rgb.tif.zip";

    // Overrides ApplicationTemplate.AppFrame's constructor to import a surface image.
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Download the source file, and get a reference to the FileStore which we'll import the image into.
            File sourceFile = downloadAndUnzipToTempFile(WWIO.makeURL(IMAGE_URL), ".tif");
            FileStore fileStore = WorldWind.getDataFileStore();

            // Import the image into the FileStore by converting it to the World Wind Java cache format.
            Layer layer = importSurfaceImage("WA Annual Precipitation", sourceFile, fileStore);
            if (layer != null)
                insertBeforePlacenames(this.getWwd(), layer);

            // Update the layer panel to display the imported surface image layer.
            this.getLayerPanel().update(this.getWwd());
        }
    }

    protected static Layer importSurfaceImage(String displayName, Object imageSource, FileStore fileStore)
    {
        // Use the FileStore's install location as the destination for the imported image tiles. The install location
        // is an area in the data file store for permanently resident data.
        File fileStoreLocation = DataImportUtil.getDefaultImportLocation(fileStore);
        // Create a unique cache name that specifies the FileStore path to the imported image.
        String cacheName = BASE_CACHE_PATH + WWIO.replaceIllegalFileNameCharacters(displayName);

        // Create a parameter list that specifies where the image is imported to, and the name associated with it.
        AVList params = new AVListImpl();
        params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
        params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
        params.setValue(AVKey.DATASET_NAME, displayName);

        // Create a TiledImageProducer to transforms the source image to a pyramid of images tiles in the World Wind
        // Java cache format.
        TiledImageProducer producer = new TiledImageProducer();
        try
        {
            // Configure the TiledImageProducer with the parameter list and the image source.
            producer.setStoreParameters(params);
            producer.offerDataSource(imageSource, null);
            // Import the source image into the FileStore by converting it to the World Wind Java cache format.
            producer.startProduction();
        }
        catch (Exception e)
        {
            producer.removeProductionState();
            e.printStackTrace();
            return null;
        }

        // Extract the data configuration document from the production results. If production successfully completed, the
        // TiledImageProducer should always contain a document in the production results, but we test the results
        // anyway.
        Iterable<?> results = producer.getProductionResults();
        if (results == null || results.iterator() == null || !results.iterator().hasNext())
            return null;

        Object o = results.iterator().next();
        if (o == null || !(o instanceof Document))
            return null;

        // Construct a Layer by passing the data configuration document to a LayerFactory.
        Layer layer = (Layer) BasicFactory.create(AVKey.LAYER_FACTORY, ((Document) o).getDocumentElement());
        layer.setEnabled(true); // TODO: BasicLayerFactory creates layers which are initially disabled
        return layer;
    }

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
        // Configure the initial view parameters to start looking at Washington State, USA from a distance of 1300 km.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 47.49112060327775);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -121.04940944067332);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 1300000);

        ApplicationTemplate.start("World Wind Image Import", ImportingImages.AppFrame.class);
    }
}
