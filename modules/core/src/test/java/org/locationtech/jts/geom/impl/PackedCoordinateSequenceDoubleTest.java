/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.geom.impl;

import org.locationtech.jts.geom.CoordinateSequenceFactory;

import junit.textui.TestRunner;

/**
 * Test {@link PackedCoordinateSequence.Double}
 * using the {@link CoordinateSequenceTestBase}
 * @version 1.7
 */
public class PackedCoordinateSequenceDoubleTest
    extends CoordinateSequenceTestBase
{
  public static void main(String args[]) {
    TestRunner.run(PackedCoordinateSequenceDoubleTest.class);
  }

  public PackedCoordinateSequenceDoubleTest(String name)
  {
    super(name);
  }

  @Override
  CoordinateSequenceFactory getCSFactory() {
    return PackedCoordinateSequenceFactory.DOUBLE_FACTORY;
  }

}