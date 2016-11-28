/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.measure;

// TODO: Auto-generated Javadoc
/**
 * The Interface Measurements.
 */
public interface Measurements {
	
	/** The Constant ADD_TO_OVERLAY. */
	public static final int AREA=1,MEAN=2,STD_DEV=4,MODE=8,MIN_MAX=16,
		CENTROID=32,CENTER_OF_MASS=64,PERIMETER=128, LIMIT = 256, RECT=512,
		LABELS=1024,ELLIPSE=2048,INVERT_Y=4096,CIRCULARITY=8192,
		SHAPE_DESCRIPTORS=8192,FERET=16384,INTEGRATED_DENSITY=0x8000,
		MEDIAN=0x10000, SKEWNESS=0x20000, KURTOSIS=0x40000, AREA_FRACTION=0x80000, 
		SLICE=0x100000, STACK_POSITION=0x100000, SCIENTIFIC_NOTATION=0x200000,
		ADD_TO_OVERLAY=0x400000;
		
	/**  Maximum number of calibration standard (20). */
	public static final int MAX_STANDARDS = 20;

}
