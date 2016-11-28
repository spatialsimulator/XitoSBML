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
 * A plugin should implement this interface for minimizing a single-valued function
 * or fitting a curve with a custom fit function.
 */
public interface UserFunction {
    
    /**
     * A user-supplied function.
     *
     * @param params    When minimizing, array of variables.
     *                  For curve fit array of fit parameters.
     *                  The array contents should not be modified.
     *                  Note that the function can get an array with more
     *                  elements then needed to specify the parameters.
     *                  Ignore the rest (and don't modify them).
     * @param x         For a fit function, the independent variable of the function.
     *                  Ignore it when using the minimizer.
     * @return          The result of the function.
     */
    public double userFunction(double[] params, double x);
}

