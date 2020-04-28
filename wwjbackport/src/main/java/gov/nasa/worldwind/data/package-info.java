/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * Provides classes for converting raw data sources into a form which can be used by standard World Wind components,
 * such as {@link gov.nasa.worldwind.layers.Layer} and {@link gov.nasa.worldwind.globes.ElevationModel}. The
 * gov.nasa.worldwind.data package contains two key interfaces: DataRaster, and DataStoreProducer. Older versions of the
 * gov.nasa.worldwind.data package included DataDescriptor, which has been removed from World Wind. This section
 * describes the role of each interface. A guide to updating code which uses DataDescriptor can be found
 * <a href="#Section_DataDescriptorPortingGuide">here</a>.
 * <p>
 * {@link gov.nasa.worldwind.data.DataRaster} encapsulates the dimensions, geographic
 * {@link gov.nasa.worldwind.geom.Sector}, and data of a two-dimensional raster grid. DataRaster provides an interface
 * to draw one geographic raster into another, according to each raster's geographic bounds. There are three 
 * concrete implementations of DataRaster:
 * <ul>
 * <li>{@link gov.nasa.worldwind.data.BufferedImageRaster} - uses a {@link java.awt.image.BufferedImage} as a source of
 * geographically referenced image data.</li>
 * <li>{@link gov.nasa.worldwind.data.BufferWrapperRaster} - uses a {@link gov.nasa.worldwind.util.BufferWrapper} as a
 * source of geographically referenced scalar data. BufferWrapperRaster typically represents integer or floating point
 * elevation data.</li>
 * <li>{@link gov.nasa.worldwind.data.ReadableDataRaster} - a wrapper implementation of DataRaster which lazily loads a
 * DataRaster from a specified source. The lazily loaded DataRaster is kept in a
 * {@link gov.nasa.worldwind.cache.MemoryCache}, which can be shared across multiple ReadableDataRasters.
 * ReadableDataRaster is useful when an application needs to operate on a number of raster data sources which do not fit
 * into main memory.</li>
 * </ul>
 * Additionally, there are two interfaces for performing I/O operations on DataRaster:
 * <ul>
 * <li>{@link gov.nasa.worldwind.data.DataRasterReader} - implementations can read image formats accepted by
 * {@link javax.imageio.ImageIO}, GeoTIFF images, GeoTIFF elevations, Raster Product Format (RPF) imagery, and Band
 * Interleaved by Line (BIL) formatted data.</li>
 * <li>{@link gov.nasa.worldwind.data.DataRasterWriter} - implementations can write image formats accepted by ImageIO,
 * GeoTIFF images, GeoTIFF elevations, compressed Direct Draw Surface (DDS) images, and Band Interleaved by Line (BIL)
 * formatted data.</li>
 * </ul>
 * <p>
 * {@link gov.nasa.worldwind.data.DataStoreProducer} provides a common interface for converting raw data sources into a
 * form which can be used by standard World Wind components. There are three concrete implementations of
 * DataStoreProducer:
 * <ul>
 * <li>{@link gov.nasa.worldwind.data.TiledElevationProducer} - converts georeferenced image files into the World Wind
 * tile cache structure, which can be rendered on the globe by a {@link gov.nasa.worldwind.layers.TiledImageLayer}.</li>
 * <li>{@link gov.nasa.worldwind.data.TiledImageProducer} - converts georeferenced elevation data files into the World
 * Wind tile cache structure, which can become part of the globe's surface terrain by using a
 * {@link gov.nasa.worldwind.terrain.BasicElevationModel}.</li>
 * <li>{@link gov.nasa.worldwind.data.WWDotNetLayerSetConverter} - converts data in the World Wind .NET tile cache
 * structure into the World Wind Java tile cache structure.</li>
 * </ul>
 * <p>
 * <strong>Data Configuration Documents</strong> are a common mechanism and file format for describing a World Wind
 * component's configuration. While data configuration documents are not part of the gov.nasa.worldwind.data package,
 * they are used as a configuration exchange mechanism by the classes in gov.nasa.worldwind.data. For example,
 * DataStoreProducer returns a DOM Document describing the data it produces. Understanding how to use data configuration
 * documents is important to leveraging the functionality of the gov.nasa.worldwind.data package. The section
 * <a href="#Section_UseCaseExamples">Common Use Case Examples</a> provides examples of how to use data configuration
 * documents to manage the data produced by classes in this package.
 * <p>
 * <strong>DataDescriptor</strong> defined an interface for representing meta information about World Wind cache data.
 * It has been replaced with data configuration documents, which provide a common mechanism to describe a component's
 * configuration. For information how to update code which uses DataDescriptor, see the <a href="#Section_DataDescriptorPortingGuide">DataDescriptor Porting Guide</a>.
 *
 * <!--**************************************************************-->
 * <!--********************  Use Case Examples  *********************-->
 * <!--**************************************************************-->
 * <h2><a name="Section_UseCaseExamples">Common Use Case Examples</a></h2>
 * The following examples demonstrate the most common use cases which the classes in gov.nasa.worldwind.data are
 * designed to address. Additionally, several examples demonstrate data management use cases using data configuration
 * documents. These examples constitute an overview of how to convert raw data sources into a form which can be consumed
 * by World Wind components, then manage the data in its converted form.
 * <p>
 * <!-- Example 1 -->
 * <strong><a name="Example_1">Example 1: Converting Georeferenced Imagery to the World Wind Tile Structure</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a source image, and a path to a folder in the local file system which receives the image tiles, configure a
 * // TiledImageProducer to create a pyramid of images tiles in the World Wind Java cache format.
 * String imagePath = ...;
 * String tiledImagePath = ...;
 * String tiledImageDisplayName = ...;
 *
 * // Create a parameter list which defines where the image is imported, and the name associated with it.
 * AVList params = new AVListImpl();
 * params.setValue(AVKey.FILE_STORE_LOCATION, WWIO.getParentFilePath(tiledImagePath));
 * params.setValue(AVKey.DATA_CACHE_NAME, WWIO.getFilename(tiledImagePath));
 * params.setValue(AVKey.DATASET_NAME, tiledImageDisplayName);
 *
 * // Create a TiledImageProducer to transforms the source image to a pyramid of images tiles in the World Wind
 * // Java cache format.
 * DataStoreProducer producer = new TiledImageProducer();
 * try
 * {
 *     // Configure the TiledImageProducer with the parameter list and the image source.
 *     producer.setStoreParameters(params);
 *     producer.offerDataSource(new File(imagePath), null);
 *     // Import the source image into the FileStore by converting it to the World Wind Java cache format. This throws
 *     // an exception if production fails for any reason.
 *     producer.startProduction();
 * }
 * catch (Exception e)
 * {
 *     // Exception attempting to create the image tiles. Revert any change made during production.
 *     producer.removeProductionState();
 * }
 *
 * // Extract the data configuration document from the production results. If production sucessfully completed, the
 * // TiledImageProducer's production results contain a Document describing the converted imagery as a data
 * // configuration document.
 * Iterable<?> results = producer.getProductionResults();
 * if (results == null || results.iterator() == null || !results.iterator().hasNext())
 *     return;
 *
 * Object o = results.iterator().next();
 * if (o == null || !(o instanceof Document))
 *     return;
 *
 * Document dataConfigDoc = (Document) o;
 * </code>
 * </pre>
 * </blockquote>
 * <p>
 * <!-- Example 2 -->
 * <strong><a name="Example_2">Example 2: Converting Georeferenced Elevation Data to the World Wind Tile Structure</a>
 * </strong>
 * <blockquote>
 * Converting georeferenced elevation data can be accomplished by referring to
 * <a href="#Example_1">Example 1: Converting Georeferenced Imagery to the World Wind Tile Structure</a>, and replacing
 * {@link gov.nasa.worldwind.data.TiledImageProducer} with {@link gov.nasa.worldwind.data.TiledElevationProducer}.
 * </blockquote>
 * <p>
 * <!-- Example 3 -->
 * <strong><a name="Example_3">Example 3: Converting World Wind .NET LayerSets to the World Wind Java Tile Structure</a>
 * </strong>
 * <blockquote>
 * Converting World Wind .NET LayerSets can be accomplished by referring to
 * <a href="#Example_1">Example 1: Converting Georeferenced Imagery to the World Wind Tile Structure</a>, and replacing
 * {@link gov.nasa.worldwind.data.TiledImageProducer} with {@link gov.nasa.worldwind.data.WWDotNetLayerSetConverter}.
 * </blockquote>
 * <p>
 * <!-- Example 4 -->
 * <strong><a name="Example_4">Example 4: Reading Data Configuration Documents from the File System</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a string path to a data configuration file in the local file system, read the file as a DOM document, which
 * // can be consumed by World Wind's Layer and ElevationModel factories. This code is backward compatible with
 * // DataDescriptor files. The method DataConfigurationUtils.convertToStandardDataConfigDocument automatically detects
 * // and transforms DataDescriptor documents into standard Layer and ElevationModel configuration documents.
 * String dataConfigPath = ...;
 * Document dataConfigDoc = WWXML.openDocument(dataConfigPath);
 * dataConfigDoc = DataConfigurationUtils.convertToStandardDataConfigDocument(dataConfigDoc);
 * </code>
 * </pre>
 * </blockquote>
 * <p>
 * <!-- Example 5 -->
 * <strong><a name="Example_5">Example 5: Reading Data Configuration Documents from the World Wind FileStore</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a path to a data configuration file in the World Wind FileStore, read the file as a DOM document, which can
 * // be consumed by World Wind's Layer and ElevationModel factories. This code is backward compatible with
 * // DataDescriptor files. The method DataConfigurationUtils.convertToStandardDataConfigDocument automatically detects
 * // and transforms DataDescriptor documents into standard Layer or ElevationModel configuration documents.
 * FileStore fileStore = ...;
 * String dataConfigPath = ...;
 *
 * // Look for the data configuration file in the local file cache, but not in the class path.
 * URL url = fileStore.findFile(dataConfigPath, false);
 * if (url == null)
 * {
 *     // The specified path name does not exist in the file store.
 *     return;
 * }
 *
 * Document dataConfigDoc = WWXML.openDocument(url);
 * dataConfigDoc = DataConfigurationUtils.convertToStandardDataConfigDocument(dataConfigDoc);
 * </code>
 * </pre>
 * </blockquote>
 * <p>
 * <!-- Example 6 -->
 * <strong><a name="Example_6">Example 6: Writing Data Configuration Documents</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a path to the data configuration file's destination in the local file system, and a parameter list
 * // describing the data, create a standard data configuration document and write it to the file system. This code is
 * // not forward compatible with applications still using DataDescriptor. Code which uses FileStore.findDataDescriptors
 * // to find data configuration files, or DataDesccriptorReader to read data configuration files will not be able to
 * // find or read the file produced by this example.
 * String dataConfigPath = ...;
 * AVList params = ...;
 *
 * // Create a data configuration document from the specified parameters.
 * Document dataConfigDoc;
 * if (isTiledImageryData)
 * {
 *     // If you're writing a data configuration file for tiled imagery, use the following:
 *     dataConfigDoc = LayerConfiguration.createTiledImageLayerDocument(params);
 * }
 * else if (isTiledElevationData)
 * {
 *     // If you're writing a data configuration file for tiled elevations, use the following:
 *     dataConfigDoc = ElevationModelConfiguration.createBasicElevationModelDocument(params);
 * }
 * else
 * {
 *     // Otherwise, you'll need to create your own document. World Wind currently provides support for creating data
 *     // configuration files for tiled imagery and tiled elevations. Write custom code to create a data configuration
 *     // document which corresponds with your data. Use LayerConfiguration and ElevationModelConfiguration as
 *     // references, and use the methods in WWXML to construct your document in memory.
 * }
 *
 * // Write the data configuration document to the file system.
 * WWXML.saveDocumentToFile(dataConfigDoc, dataConfigPath);
 * </code>
 * </pre>
 * </blockquote>
 * <p>
 * <!-- Example 7 -->
 * <strong><a name="Example_7">Example 7: Searching for Data Configuration Documents in the File System</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a search path in the local file system to look for data configuration files, return a list of file paths
 * // representing the matches closest to the root: data configuration files who's ancestor directories contain another
 * // data configuration file are ignored. The search path cannot be null. This code is backward compatible with
 * // DataDescriptor files. The class DataConfigurationFilter accepts standard Layer and ElevationModel configuration
 * // files, DataDescriptor files, and World Wind .NET LayerSet files.
 * String searchPath = ...;
 * String[] filePaths = WWIO.listDescendantFilenames(searchPath, new DataConfigurationFilter());
 *
 * if (filePaths == null || filePaths.length == 0)
 * {
 *     // No data configuration files found in the file system.
 *     return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 * <p>
 * <!-- Example 8 -->
 * <strong><a name="Example_8">Example 8: Searching for Data Configuration Documents in the World Wind FileStore</a></strong>
 * <blockquote>
 * There are two methods of searching for data configuration files in the World Wind FileStore. The first method
 * individually searches each FileStore location using the method
 * {@link gov.nasa.worldwind.util.WWIO#listDescendantFilenames(java.io.File, java.io.FileFilter, boolean)}. This method
 * is equivalent to calling the method <code>FileStore.findDataDescriptors(String)</code> (which has been removed), and
 * should be used by applications porting from DataDescriptor. Use this method when your application needs to control
 * the FileStore locations it searches for data configuration files.
 * <pre>
 * <code>
 * // Given a World Wind FileStore location in which to look for data configuration files, return a list of cache names
 * // representing the matches closest to the root: data configuration files who's ancestor directories contain another
 * // data configuration file are ignored. This code is backward compatible with DataDescriptor files. The class
 * // DataConfigurationFilter accepts standard Layer and ElevationModel configuration files, DataDescriptor files, and
 * // World Wind .NET LayerSet files.
 * String fileStoreLocation = ...;
 * String[] cacheNames = WWIO.listDescendantFilenames(fileStoreLocation, new DataConfigurationFilter(), false);
 *
 * if (cacheNames == null || cacheNames.length == 0)
 * {
 *     // No data configuration files found in the FileStore.
 *     return;
 * }
 * </code>
 * </pre>
 * The second method searches the entire World Wind FileStore under a relative file store path. Use this method when
 * your application doesn't care which FileStore location it searches for data configuration files, but may care what
 * relative file store path it searches under.
 * <pre>
 * <code>
 * // Given a search path in the World Wind FileStore to look for data configuration files, return a list of cache names
 * // representing the matches closest to the root: data configuration files who's ancestor directories contain another
 * // data configuration file are ignored. If the search path is null, the method FileStore.listTopFileNames searches
 * // the entire FileStore, excluding the class path. This code is backward compatible with DataDescriptor files. The
 * // class DataConfigurationFilter accepts standard Layer and ElevationModel configuration files, DataDescriptor files,
 * // and World Wind .NET LayerSet files.
 * FileStore fileStore = ...;
 * String fileStoreSearchPath = ...;
 * String[] cacheNames = fileStore.listTopFileNames(fileStoreSearchPath, new DataConfigurationFilter());
 *
 * if (cacheNames == null || cacheNames.length == 0)
 * {
 *     // No data configuration files found in the FileStore.
 *     return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 * <p>
 * <!-- Example 9 -->
 * <strong><a name="Example_9">Example 9: Creating World Wind Components from Data Configuration Documents</a></strong>
 * <blockquote>
 * <pre>
 * <code>
 * // Given a data configuration document which describes tiled imagery or tiled elevations in the World Wind FileStore,
 * // create a World Wind Layer or ElevationModel according to the contents of the data configuration document. This
 * // code is backward compatible with DataDescriptor files if the data configuration file was opened as shown in <a href="#Example_5">Example 5</a> or <a href="#Example_8">Example 8</a>.
 * Document dataConfigDoc = ...;
 * AVList params = ...;
 * String filename = ...; // The data configuration's filename, relative to a World Wind file store.
 *
 * // If the data configuration doesn't define a cache name, then compute one using the file's path relative to its file
 * // store directory.
 * String s = dataConfig.getString("DataCacheName");
 * if (s == null || s.length() == 0)
 *     DataConfigurationUtils.getDataConfigCacheName(filename, params);
 *
 * String type = DataConfigurationUtils.getDataConfigType(domElement);
 * if (type != null && type.equalsIgnoreCase("Layer"))
 * {
 *     Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
 *     Layer layer = (Layer) factory.createFromConfigSource(dataConfig, params);
 * }
 * else if (type != null && type.equalsIgnoreCase("ElevationModel"))
 * {
 *     // If the data configuration doesn't define the data's extreme elevations, provide default values using the
 *     // minimum and maximum elevations of Earth.
 *     if (dataConfig.getDouble("ExtremeElevations/@min") == null)
 *         params.setValue(AVKey.ELEVATION_MIN, -11000d); // Depth of Mariana trench.
 *     if (dataConfig.getDouble("ExtremeElevations/@max") == null)
 *         params.setValue(AVKey.ELEVATION_MAX, 8500d); // Height of Mt. Everest.
 *
 *     Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
 *     ElevationModel elevationModel = (ElevationModel) factory.createFromConfigSource(dataConfig, params);
 * }
 * else
 * {
 *     // Currently, World Wind supports factory construction of Layers and ElevationModels from data configuration
 *     // documents. If your data configuration document describes another type of component, you'll need to write your
 *     // own construction routine. Use BasicLayerFactory and BasicElevationModelFactory as references.
 *     return;
 * }
 * </code>
 * </pre>
 * </blockquote>
 *
 * <!--**************************************************************-->
 * <!--********************  DataDescriptor Porting Guide  **********-->
 * <!--**************************************************************-->
 * <h2><a name="Section_DataDescriptorPortingGuide">DataDescriptor Porting Guide</a></h2>
 * DataDescriptor has been replaced with data configuration documents. This guide explains why DataDescriptor has been
 * replaced, provides information on backward compatability with data configuration, and outlines how to update code
 * which uses DataDescriptor.
 * <p>
 * <strong>What Happened to DataDescriptor?</strong>
 * <br/>
 * Older versions of the gov.nasa.worldwind.data package included the DataDescriptor interface, along with its
 * associated DataDescriptorReader and DataDescriptorWriter. DataDescriptor defined an interface and an XML file format
 * for representing meta information about World Wind cache data. The XML files were called "data descriptor" files, and
 * were typically named "dataDescriptor.xml". Applications used these files to discover processed data in the World Wind
 * file store, create an in-memory DataDescriptor from the file, then create either a Layer or an ElevationModel from
 * the DataDescriptor. World Wind needed a common mechanism to describe a component's configuration, but
 * DataDescriptor had two problems which prevented its use as a common mechanism: (1) it presented all information as a
 * flat list of key-value pairs, making it difficult to represent heirarchical information, and (2) it decoded complex
 * properties (for example lists, angles, colors) at read time based on the property name, making it impossible to
 * decode complex objects with unknown names. For these reasons DataDescriptor was replaced with data configuration
 * documents. Data configuration documents provide a standard XML document structure to describe a component's
 * configuration. They support heirarchical data, and enable data to be decoded after read time by any component which
 * consumes the data configuration document.
 * <p>
 * <strong>Backward Compatibility with Data Configuration Documents</strong>
 * <br/>
 * Data configuration documents have supporting utilities which are designed to read and transform DataDescriptor files
 * written by older versions of World Wind. Applications which port usage of DataDescriptor to data configuration
 * documents can maintain backwards compatibility with the DataDescriptor files written by older versions of World Wind.
 * However, there is no mechanism for forward compatibililty with data configuration documents. Applications which still
 * use DataDescriptor files will not be able to recognize or read data configuration files.
 * <p>
 * The section <a href="#Section_UseCaseExamples">Common Use Case Examples</a> provides usage examples of data
 * configuration which are backward compatible with DataDescriptor. <a href="#Example_4">Example 4</a> and
 * <a href="#Example_5">Example 5</a> demonstrate how to read both data configuration files and DataDescriptor files.
 * <a href="#Example_7">Example 7</a> and <a href="#Example_8">Example 8</a> demonstrate how to search for
 * data configuration files and DataDescriptor files in the file system or the World Wind FileStore.
 * <a href="#Example_9">Example 9</a> demonstrates how to create a World Wind Layer or ElevationModel from a
 * data configuration file, in a way which is backward compatible with DataDescriptor files.
 * <p>
 * The data configuration files created in <a href="#Example_1">Example 1</a>, <a href="#Example_2">Example 2</a>, and
 * <a href="#Example_3">Example 3</a> are not forward compatible with DataDescriptor. Likewise, neither are the
 * data configuration files written in <a href="#Example_6">Example 6</a>. Applications which still use DataDescriptor
 * will not be able to recognize or read these files.
 * <p>
 * <strong>Updating Usage of DataDescriptor</strong>
 * <br/>
 * Data configuration documents are designed to replace DataDescriptor as a mechanism for communicating configuration
 * metadata about World Wind components. World Wind provides utilities to read and write data configuration files,
 * search for data configuration files in the local file system or World Wind file store, and create World Wind
 * components from a data configuration document. The following section outlines how to replace usage of DataDescriptor
 * with data configuration files.
 * <ul>
 * <li><strong>Reading DataDescriptor Files from the File System</strong>
 * <br/>
 * The following code snippet is an example of how DataDescriptorReader was used to open a DataDescriptor file from the
 * local file system. <a href="#Example_4">Example 4</a> and <a href="#Example_5">Example 5</a> show how to replace this
 * with usage of data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a string path to a DataDescriptor file in the local file system, read the file as a DataDescriptor.
 * String dataDescriptorPath = ...;
 *
 * DataDescriptorReader reader = new BasicDataDescriptorReader();
 * reader.setSource(new File(dataDescriptorPath));
 *
 * if (!reader.canRead())
 * {
 *     // File path does not point to a DataDescriptor file.
 *     return;
 * }
 *
 * DataDescriptor dataDescriptor = reader.read();
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * <li><strong>Writing DataDescriptor Files</strong>
 * <br/>
 * The following code snippet is an example of how DataDescriptorWriter was used to save a DataDescriptor to the local
 * file system. <a href="#Example_6">Example 6</a> shows how to replace this with usage of data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a path to the DataDescriptor file's destination in the local file system, and a parameter list describing
 * // the data, create a DataDescriptor and write it to the file system.
 * String dataDescriptorPath = ...;
 * AVList params = ...;
 *
 * DataDescriptor descriptor = new BasicDataDescriptor();
 *
 * Object o = params.getValue(AVKey.FILE_STORE_LOCATION);
 * if (o != null)
 *     descriptor.setFileStoreLocation(new java.io.File(o.toString()));
 *
 * o = params.getValue(AVKey.DATA_CACHE_NAME);
 * if (o != null)
 *     descriptor.setFileStorePath(o.toString());
 *
 * o = params.getValue(AVKey.DATASET_NAME);
 * if (o != null)
 *     descriptor.setName(o.toString());
 *
 * o = params.getValue(AVKey.DATA_TYPE);
 * if (o != null)
 *     descriptor.setType(o.toString());
 *
 * for (java.util.Map.Entry<String, Object> avp : params.getEntries())
 * {
 *     String key = avp.getKey();
 *
 *     // Skip key-value pairs that the DataDescriptor specially manages.
 *     if (key.equals(AVKey.FILE_STORE_LOCATION)
 *         || key.equals(AVKey.DATA_CACHE_NAME)
 *         || key.equals(AVKey.DATASET_NAME)
 *         || key.equals(AVKey.DATA_TYPE))
 *     {
 *         continue;
 *     }
 *
 *     descriptor.setValue(key, avp.getValue());
 * }
 *
 * DataDescriptorWriter writer = new BasicDataDescriptorWriter();
 * writer.setDestination(installLocation);
 * writer.write(descriptor);
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * <li><strong>Searching for DataDescriptor Files in the World Wind FileStore</strong>
 * <br/>
 * The following code snippet is an example of how FileStore.findDataDescriptors was used to search for DataDescriptor
 * files in the World Wind FileStore. <a href="#Example_8">Example 8</a> shows how to replace this with usage of
 * data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a World Wind FileStore location in which to look for DataDescriptor files, return a list of cache names
 * // representing the matches closest to the root: DataDescriptor files who's ancestor directories contain another
 * // DataDescriptor file are ignored. The search location must not be null.
 * String fileStoreLocation = ...;
 * FileStore fileStore = ...;
 *
 * List&lt;? extends DataDescriptor&gt; dataDescriptors = fileStore.findDataDescriptors(fileStoreLocation);
 * if (dataDescriptors == null || dataDescriptors.size() == 0)
 * {
 *     // No DataDescriptor files found in the specified FileStore location.
 *     return;
 * }
 *
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * <li><strong>Creating World Wind Components from DataDescriptor</strong>
 * <br/>
 * The following code snippet is an example of how the AVList parameters attached to DataDescriptor were used to
 * construct World Wind Layers and ElevationModels. <a href="#Example_9">Example 9</a> shows how to replace this with
 * usage of data configuration documents.
 * <blockquote>
 * <pre>
 * <code>
 * // Given a reference to a DataDescriptor which describes tiled imagery or elevations in the World Wind FileStore,
 * // create a World Wind Layer or ElevationModel according to the contents of the DataDescriptor.
 * DataDescriptor dataDescriptor = ...;
 *
 * if (dataDescriptor.getType().equals(AVKey.TILED_IMAGERY))
 * {
 *     BasicTiledImageLayer layer = new BasicTiledImageLayer(dataDescriptor);
 *     layer.setNetworkRetrievalEnabled(false);
 *     layer.setUseTransparentTextures(true);
 *
 *     if (dataDescriptor.getName() != null)
 *     {
 *         layer.setName(dataDescriptor.getName());
 *     }
 * }
 * else if (dataDescriptor.getType().equals(AVKey.TILED_ELEVATIONS))
 * {
 *     // DataDescriptor files do not contain properties describing an ElevationModel's extreme elevations. Give those
 *     // properties default values using the known extreme elevations on Earth.
 *     dataDescriptor.setValue(AVKey.ELEVATION_MIN, -11000);
 *     dataDescriptor.setValue(AVKey.ELEVATION_MAX, 8500);
 *
 *     // DataDescriptor files contain the property key "gov.nasa.worldwind.avkey.MissingDataValue", which must be
 *     // translated to AVKey.MISSING_DATA_SIGNAL so it will be understood by BasicElevationModel.
 *     if (dataDescriptor.hasKey("gov.nasa.worldwind.avkey.MissingDataValue"))
 *     {
 *         Object missingDataSignal = dataDescriptor.getValue("gov.nasa.worldwind.avkey.MissingDataValue");
 *         dataDescriptor.removeKey("gov.nasa.worldwind.avkey.MissingDataValue");
 *         dataDescriptor.setValue(AVKey.MISSING_DATA_SIGNAL, missingDataSignal);
 *     }
 *
 *     BasicElevationModel elevationModel = new BasicElevationModel(dataDescriptor);
 *     elevationModel.setNetworkRetrievalEnabled(false);
 *
 *     if (dataDescriptor.getName() != null)
 *     {
 *         elevationModel.setName(dataDescriptor.getName());
 *     }
 * }
 * </code>
 * </pre>
 * </blockquote>
 * </li>
 * </ul>
 *
 * @author dcollins
 * @version $Id: package-info.java 13358 2010-04-30 18:31:10Z dcollins $
 */
package gov.nasa.worldwind.data;
