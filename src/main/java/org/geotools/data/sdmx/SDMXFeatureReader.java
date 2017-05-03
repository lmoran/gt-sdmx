/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2016, Open Source Geospatial Foundation (OSGeo)
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
 *
 */

package org.geotools.data.sdmx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.api.PortableTimeSeries;
import it.bancaditalia.oss.sdmx.client.SdmxClientHandler;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;

/**
 * Feature reader of SDMX tuples
 * 
 * @author lmorandini
 *
 */
public class SDMXFeatureReader
    implements FeatureReader<SimpleFeatureType, SimpleFeature> {

  protected SimpleFeatureType featureType;
  protected Logger LOGGER;
  protected GenericSDMXClient client;
  protected Iterator<PortableTimeSeries> tsIter;
  protected boolean empty;
  protected int featIndex = 0;

  public SDMXFeatureReader(GenericSDMXClient clientIn,
      SimpleFeatureType featureTypeIn, Dataflow dataflowIn,
      DataFlowStructure dfStructureIn, String sdmxConstraints, Logger logger)
      throws IOException {

    this.featureType = featureTypeIn;
    this.featIndex = 0;
    this.LOGGER = logger;
    this.client = clientIn;
    this.empty = false;

    try {
      this.tsIter = this.client.getTimeSeries(dataflowIn, dfStructureIn,
          sdmxConstraints, null, null,
          false, null, false).iterator();
    } catch (SdmxException e) {
      if (e instanceof SdmxResponseException && ((SdmxResponseException) e)
          .getResponseCode() == SDMXDataStore.ERROR_NORESULTS) {
        this.empty = true;
      } else {
        logger.log(Level.SEVERE, e.getMessage(), e);
        throw new IOException(e);
      }
    }
  }

  /**
   * @see FeatureReader#getFeatureType()
   */
  @Override
  public SimpleFeatureType getFeatureType() {
    if (this.featureType == null) {
      throw new IllegalStateException(
          "No features were retrieved, shouldn't be calling getFeatureType()");
    }
    return this.featureType;
  }

  /**
   * @see FeatureReader#hasNext()
   */
  @Override
  public boolean hasNext() {

    if (this.empty == true) {
      return false;
    }

    return this.tsIter.hasNext();
  }

  /**
   * @throws IOException
   * @see FeatureReader#next()
   */
  @Override
  public SimpleFeature next() throws NoSuchElementException, IOException {

    if (this.empty == true) {
      return null;
    }

    // TODO
    List<Object> values = new ArrayList();
    values.add(null);

    PortableTimeSeries ts = this.tsIter.next();
    ts.getObservations().forEach((value) -> {
      values.add(new Double(value));
    });

    return new SimpleFeatureImpl(values, this.featureType,
        new FeatureIdImpl(String.valueOf(ts.hashCode())));
  }

  @Override
  public void close() {
    // TODO
  }

}
