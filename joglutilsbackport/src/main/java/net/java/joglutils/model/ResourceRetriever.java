package net.java.joglutils.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Utility class that allows transparent reading of files from the current 
 * working directory or from the classpath.
 * <p>
 * Based on code from author Pepijn Van Eeckhoudt
 */
public class ResourceRetriever {

    /**
     * Retrieves a resource as a URL that corresponds to the file specified
     * by the passed in filename.
     * 
     * @param filename The name of the file to retrieve as a URL
     * @throws IOException
     * @return URL that corresponds to the filename
     */
    public static URL getResource( final String filename ) throws IOException {
        // Try to load resource from jar
        URL url = ResourceRetriever.class.getClassLoader().getResource(filename);
        
        // If not found in jar, then load from disk
        if (url == null) {
            return new URL("file", "localhost", filename);
        } else {
            return url;
        }
    }

    /**
     * Retrieves a resource as an InputStream that corresponds to the file 
     * specified by the passed in filename.
     * 
     * @param filename The name of the file to retrieve as an InputStream
     * @throws IOException
     * @return InputStream that corresponds to the filename     
     */    
    public static InputStream getResourceAsStream( final String filename ) throws IOException {
        // Try to load resource from jar
        String convertedFileName = filename.replace('\\', '/');
        InputStream stream = ResourceRetriever.class.getClassLoader().getResourceAsStream(convertedFileName);
        // If not found in jar, then load from disk
        if (stream == null) {
            return new FileInputStream(convertedFileName);
        } else {
            return stream;
        }
    }
    
    /**
     * Retrieves a resource as a URL that corresponds to the file specified
     * by the passed in filename.
     * 
     * @param filename The name of the file to retrieve as a URL
     * @throws IOException
     * @return URL that corresponds to the filename
     */    
    public static URL getResourceAsUrl( final String filename ) throws IOException {
        URL result;
        
        try {
            result = new URL(filename);
        } catch (MalformedURLException e) {     
            // When the string was not a valid URL, try to load it as a resource using
            // an anonymous class in the tree.
            Object objectpart = new Object() { };
            Class classpart = objectpart.getClass();
            ClassLoader loaderpart = classpart.getClassLoader();
            result = loaderpart.getResource(filename);
            
            if (result == null) {
                result = new URL("file", "localhost", filename);
            }
        }    
        
        return result;
    }
    
    /**
     * Retrieves a resource as an InputStream that corresponds to the file 
     * specified by the passed in filename.
     * 
     * @param filename The name of the file to retrieve as an InputStream
     * @throws IOException
     * @return InputStream that corresponds to the filename     
     */      
    public static InputStream getResourceAsInputStream( final String filename ) throws IOException {        
        URL result;
        
        try {
            result = getResourceAsUrl(filename);                                  
        } catch(IOException e) {
            return new FileInputStream(filename);    
        }
        
        if (result == null) {
            return new FileInputStream(filename);
        } else {
            return result.openStream();
        }        
    }
}