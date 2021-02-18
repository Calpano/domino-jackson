/*
 * Copyright © 2019 Dominokit
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
 */
package org.dominokit.jacksonapt.processor.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.dominokit.jacksonapt.annotation.JSONMapper;

@JSONMapper
public class CreatorParent {

  private int id;
  private String name;
  private CreatorChild child;

  @JsonCreator
  public CreatorParent(
      @JsonProperty("id") int id,
      @JsonProperty("name") String name,
      @JsonProperty("child") CreatorChild child) {
    this.id = id;
    this.name = name;
    this.child = child;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public CreatorChild getChild() {
    return child;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreatorParent creatorParent = (CreatorParent) o;
    return id == creatorParent.id
        && Objects.equals(name, creatorParent.name)
        && Objects.equals(child, creatorParent.child);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, child);
  }
}
