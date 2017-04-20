/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008-2016, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.sdmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.sdmx.SDMXDataStore;
import org.geotools.data.sdmx.SDMXDataStoreFactoryTest;
import org.geotools.data.sdmx.SDMXDataStore;
import org.geotools.data.sdmx.SDMXFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.UnsupportedImplementationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.geotools.util.UnsupportedImplementationException;

import org.powermock.modules.junit4.PowerMockRunner;

import com.vividsolutions.jts.geom.Geometry;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SDMXDataStore.class, HttpURLConnection.class })
public class SDMXDataStoreTest {

  public static String TYPENAME1 = "FERTILITY_AGE_STATE";
  public static String TYPENAME2 = "ATSI_FERTILITY";
  public static String TYPENAME3 = "ABS_ABORIGINAL_POPPROJ_INDREGION";

  private SDMXDataStore dataStore;

  private HttpURLConnection clientMock;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() throws Exception {

    this.clientMock = PowerMockito.mock(HttpURLConnection.class);
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK);
    when(clientMock.getInputStream())
    .thenReturn(Helper.readXMLAsStream("test-data/abs.xml"));
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK);
    when(clientMock.getInputStream())
    .thenReturn(Helper.readXMLAsStream("test-data/abs-seifa-lga.xml"));

    this.dataStore = (SDMXDataStore) Helper.createDefaultSDMXTestDataStore();
    List<Name> names = this.dataStore.createTypeNames();

    assertEquals(304, names.size());
    assertEquals(TYPENAME1, names.get(0).getLocalPart());
    assertEquals(Helper.NAMESPACE, names.get(0).getNamespaceURI());

    assertNotNull(
        this.dataStore.getEntry(new NameImpl(Helper.NAMESPACE, TYPENAME1)));

    assertTrue(true);
  }
}
