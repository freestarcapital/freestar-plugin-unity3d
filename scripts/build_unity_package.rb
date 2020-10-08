#!/usr/bin/env ruby

# Author: Lev Trubov
# © 2019 Vdopia, Inc.

require 'json'

PROJ_DIR = 'UnityProjectFiles'
UNITY_DIR = '/Applications/Unity/Hub/Editor/'
UNITY_VER = '2019.4.10f1'
UNITY_EX_PATH = '/Unity.app/Contents/MacOS/Unity'
UNITY_EX = "#{UNITY_DIR}#{UNITY_VER}#{UNITY_EX_PATH}"
PKG_NAME = 'FreestarMediation.unitypackage'

def build
  cmd = "(cd #{PROJ_DIR} && "
  cmd += "#{UNITY_EX} -nographics -batchmode "
  cmd += "-importPackage ../libs/external-dependency-manager-1.2.159.unitypackage "
  cmd += "-quit -projectPath `pwd` "
  cmd += "-exportPackage Assets/Plugins Assets/FreestarMediation "
  cmd += "Assets/ExternalDependencyManager #{PKG_NAME} "
  cmd += ")"
  puts cmd
  system(cmd)
end

## -- Running script --

build

versions = JSON.parse(File.read("scripts/versions.json"))
ver = versions["plugin-unity"]

pkg_name_comps = PKG_NAME.split('.')
system("mv #{PROJ_DIR}/#{PKG_NAME} UnityPackage/#{pkg_name_comps[0]}_v#{ver}.#{pkg_name_comps[1]}")