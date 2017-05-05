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
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.geotools.data.sdmx.SDMXDataStore;
import org.geotools.feature.NameImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.type.Name;
import java.net.HttpURLConnection;
import java.net.URL;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;

import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RestSdmxClient.class, HttpURLConnection.class, URL.class })
public class SDMXDataStoreTest {

  private SDMXDataStore dataStore;
  private URL urlMock;
  private HttpURLConnection clientMock;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testTypeName() throws Exception {

    this.urlMock = PowerMockito.mock(URL.class);
    this.clientMock = PowerMockito.mock(HttpURLConnection.class);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(this.urlMock);
    PowerMockito.when(this.urlMock.openConnection())
        .thenReturn(this.clientMock);
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK).thenReturn(HttpStatus.SC_OK);
    when(clientMock.getInputStream())
        .thenReturn(Helper.readXMLAsStream("test-data/abs.xml"))
        .thenReturn(
            Helper.readXMLAsStream("test-data/abs-census2011-t04-abs.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/abs-seifa-lga.xml"));

    this.dataStore = (SDMXDataStore) Helper.createDefaultSDMXTestDataStore();
    List<Name> names = this.dataStore.createTypeNames();

    assertEquals(2, names.size());
    assertEquals(Helper.T04, names.get(0).getLocalPart());
    assertEquals(Helper.NAMESPACE, names.get(0).getNamespaceURI());
    assertNotNull(
        this.dataStore.getEntry(new NameImpl(Helper.NAMESPACE, Helper.T04)));
    assertEquals(Helper.SEIFA_LGA, names.get(1).getLocalPart());
    assertNotNull(this.dataStore
        .getEntry(new NameImpl(Helper.NAMESPACE, Helper.SEIFA_LGA)));
  }

  @Test
  public void testSchema() throws Exception {

    this.urlMock = PowerMockito.mock(URL.class);
    this.clientMock = PowerMockito.mock(HttpURLConnection.class);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(this.urlMock);
    PowerMockito.when(this.urlMock.openConnection())
        .thenReturn(this.clientMock);
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK).thenReturn(HttpStatus.SC_OK);
    when(clientMock.getInputStream())
        .thenReturn(Helper.readXMLAsStream("test-data/abs.xml"))
        .thenReturn(
            Helper.readXMLAsStream("test-data/abs-census2011-t04-abs.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/abs-seifa-lga.xml"));

    this.dataStore = (SDMXDataStore) Helper.createDefaultSDMXTestDataStore();
    assertEquals(2, this.dataStore.createTypeNames().size());

    assertNotNull(this.dataStore.getFeatureSource(Helper.T04).getSchema());
    assertEquals(11, this.dataStore.getFeatureSource(Helper.T04).getSchema()
        .getAttributeCount());
    assertNotNull(this.dataStore.getFeatureSource(Helper.SEIFA_LGA).getSchema());
    assertEquals(16, this.dataStore.getFeatureSource(Helper.SEIFA_LGA).getSchema()
        .getAttributeCount());
  }

}
