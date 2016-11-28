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
package ij.macro;

// TODO: Auto-generated Javadoc
/**
 * The Interface MacroExtension.
 */
public interface MacroExtension {
  
  /** The Constant ARG_STRING. */
  public static final int ARG_STRING = 0x01;
  
  /** The Constant ARG_NUMBER. */
  public static final int ARG_NUMBER = 0x02;
  
  /** The Constant ARG_ARRAY. */
  public static final int ARG_ARRAY  = 0x04;
  
  /** The Constant ARG_OUTPUT. */
  public static final int ARG_OUTPUT = 0x10;
  
  /** The Constant ARG_OPTIONAL. */
  public static final int ARG_OPTIONAL = 0x20;

  /**
   * Handle extension.
   *
   * @param name the name
   * @param args the args
   * @return the string
   */
  public String handleExtension(String name, Object[] args);
  
  /**
   * Gets the extension functions.
   *
   * @return the extension functions
   */
  public ExtensionDescriptor[] getExtensionFunctions();
}
