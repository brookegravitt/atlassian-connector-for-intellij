/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.connector.intellij.bamboo;

import com.atlassian.theplugin.commons.bamboo.BuildStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @autrhor pmaruszak
 * @date Jun 25, 2010
 */
class BambooBuildGroup {
      List<BambooBuildAdapter> groupBuilds;
       Map<String, BambooBuildAdapter> buildStatuses = new HashMap<String, BambooBuildAdapter>(0);



    public BambooBuildGroup(Collection<BambooBuildAdapter> newBuildStatuses) {
          this.groupBuilds = new ArrayList<BambooBuildAdapter>();
          for (BambooBuildAdapter build : newBuildStatuses) {
              if (build.isGrouped()) {
                  groupBuilds.add(build);
              }
          }
      }

      public boolean isGroup() {
          return groupBuilds.size() > 0;
      }


    public Map<String, BambooBuildAdapter> getBuildStatuses() {
        return buildStatuses;
    }

    public List<BambooBuildAdapter> getGroupBuilds() {
          return groupBuilds;
      }

      public BuildStatus getStatus() {
          BuildStatus calculatedStatus = BuildStatus.UNKNOWN;
          
          if (isGroup()) {
              calculatedStatus = groupBuilds.get(0).getStatus();
              for (int i = 0, buildStatusesSize = groupBuilds.size(); i < buildStatusesSize; i++) {
                  BambooBuildAdapter build = groupBuilds.get(i);
                  if (calculatedStatus != build.getStatus()) {
                      if (calculatedStatus == BuildStatus.SUCCESS) {
                          calculatedStatus = build.getStatus();
                      }
                      break;
                  }
              }
          }
          return calculatedStatus;
      }

//      class LocalComparator implements Comparator<BambooBuildAdapter> {
//          public int compare(BambooBuildAdapter b, BambooBuildAdapter b1) {
//
//              if (b.isGrouped() == b1.isGrouped()) {
//                  return 0;
//              } else if (b.isGrouped() && !b1.isGrouped()) {
//                  return -1;
//              } else {
//                  return 1;
//              }
//          }
//      }


      @Override
      public boolean equals(Object o) {
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;

          BambooBuildGroup that = (BambooBuildGroup) o;

          if (groupBuilds != null ? !groupBuilds.equals(that.groupBuilds) : that.groupBuilds != null)
              return false;

          return true;
      }

      @Override
      public int hashCode() {
          return groupBuilds != null ? groupBuilds.hashCode() : 0;
      }
  }
