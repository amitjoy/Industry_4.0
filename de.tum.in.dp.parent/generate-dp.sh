#*******************************************************************************
# Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
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
#!/bin/bash

# Declaring all the Deployment Package Project Names to facilitate easy finding of 
# its directory (as the directory name is as same as the project name)

declare -a arr=("de.tum.in.dp.bluetooth" "de.tum.in.dp.opcua" "de.tum.in.dp.socket" "de.tum.in.dp.cache" "de.tum.in.dp.commons" "de.tum.in.dp.dependencies")

# The build file filename extension
b="_build.xml"

# The Directory Separator
c="/"

# The root directory of all the aforementioned projects.
home_dir="/Users/AMIT/Industry_4.0/"

# The DP file extension
ext=".dp"

# The destination directory to copy all deployment packages
cp_dir="/Users/AMIT/Downloads/"

for i in "${arr[@]}"
do
   echo "Generating Deployment Package for $i"
   
   # Run ant on the build file
   /usr/local/bin/ant -d -buildfile  $home_dir$i$c$i$b
   
   # Move the deployment package to destination folder
   mv $home_dir$i$c$i$ext $cp_dir
done