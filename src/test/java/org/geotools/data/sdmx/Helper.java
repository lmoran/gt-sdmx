package org.geotools.data.sdmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;

public class Helper {

//  public static String URL = "http://stat.data.abs.gov.au/restsdmx/sdmx.ashx/GetDataStructure/ABS_SEIFA_LGA";
  public static String URL = "http://stat.data.abs.gov.au/sdmx-json/data";
  public static String NAMESPACE = "http://aurin.org.au";
  public static String USER = "testuser";
  public static String PASSWORD = "testpassword";

  public Helper() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Helper method to read a XMl file into a stream
   * 
   * @param fileName
   *          File name to load
   * @return XML content of the file
   * @throws FileNotFoundException
   */
  public static InputStream readXMLAsStream(String fileName)
      throws FileNotFoundException {
    return new FileInputStream(new File(
        Helper.class.getResource(fileName).getFile()));
  }

  /**
   * Helper method to create a default test data store with Open Data catlaog
   */
  public static DataStore createDefaultSDMXTestDataStore() throws IOException {

    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put(SDMXDataStoreFactory.NAME_PARAM.key, "ABS");
    params.put(SDMXDataStoreFactory.NAMESPACE_PARAM.key, NAMESPACE);
    params.put(SDMXDataStoreFactory.URL_PARAM.key, URL);
    params.put(SDMXDataStoreFactory.USER_PARAM.key, USER);
    params.put(SDMXDataStoreFactory.PASSWORD_PARAM.key, PASSWORD);
    return (new SDMXDataStoreFactory()).createDataStore(params);
  }

  /**
   * Helper method to create a data store
   * 
   * @param namespace
   * @param url
   * @param user
   * @param password
   * @return
   * @throws IOException
   */
  public static DataStore createDataStore(final String namespace, final String url,
      final String user, final String password) throws IOException {

    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put(SDMXDataStoreFactory.NAME_PARAM.key, "ABS");
    params.put(SDMXDataStoreFactory.NAMESPACE_PARAM.key, namespace);
    params.put(SDMXDataStoreFactory.URL_PARAM.key, url);
    params.put(SDMXDataStoreFactory.USER_PARAM.key, user);
    params.put(SDMXDataStoreFactory.PASSWORD_PARAM.key, password);
    return (new SDMXDataStoreFactory()).createDataStore(params);
  }

}
