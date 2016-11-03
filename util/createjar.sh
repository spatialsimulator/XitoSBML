#*******************************************************************************
# Copyright 2015 Kaito Ii
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*******************************************************************************
/bin/cp ./plugins.config ./factId>maven-assembly-plugin</artifactId>
        <configuration>
                  <archive>
                              <manifest>
                                            <mainClass>org.sbml.layoutconverter.LayoutConverter</mainClass>
                                                        </manifest>
                                                                  </archive>
                                                                            <descriptorRefs>
                                                                                        <descriptorRef>jar-with-dependencies</descriptorRef>
                                                                                                  </descriptorRefs>
                                                                                                          </configuration>
                                                                                                                  <executions>
                                                                                                                            <execution>
                                                                                                                                        <id>make-assembly</id> <!-- this is used for inheritance merges -->
                                                                                                                                                    <phase>package</phase> <!-- bind to the packaging phase -->
                                                                                                                                                                <goals>
                                                                                                                                                                              <goal>single</goal>
                                                                                                                                                                                          </goals>
                                                                                                                                                                                                    </execution>
                                                                                                                                                                                                            </executions>
                                                                                                                                                                                                                  </plugin>
                                                                                                                                                                                                                   src/*.java ./bin/*.class ./zip && cd zip && LC_ALL="C" jar cvf Spatial_SBML.jar plugins.config *.class *.java
