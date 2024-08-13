/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.tasks

import com.facebook.react.model.ModelAutolinkingConfigJson
import com.facebook.react.model.ModelAutolinkingDependenciesJson
import com.facebook.react.model.ModelAutolinkingDependenciesPlatformAndroidJson
import com.facebook.react.model.ModelAutolinkingDependenciesPlatformJson
import com.facebook.react.tests.createTestTask
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GenerateAutolinkingNewArchitecturesFileTaskTest {

  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun generatePackageListTask_groupIsSetCorrectly() {
    val task = createTestTask<GenerateAutolinkingNewArchitecturesFileTask> {}
    Assert.assertEquals("react", task.group)
  }

  @Test
  fun generatePackageListTask_staticInputs_areSetCorrectly() {
    val outputFolder = tempFolder.newFolder("build")
    val inputFile = tempFolder.newFile("config.json")

    val task =
        createTestTask<GenerateAutolinkingNewArchitecturesFileTask> {
          it.generatedOutputDirectory.set(outputFolder)
          it.autolinkInputFile.set(inputFile)
        }

    Assert.assertEquals(inputFile, task.inputs.files.singleFile)
    Assert.assertEquals(outputFolder, task.outputs.files.singleFile)
  }

  @Test
  fun filterAndroidPackages_withNull_returnsEmpty() {
    val task = createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
    val result = task.filterAndroidPackages(null)
    Assert.assertEquals(emptyList<ModelAutolinkingDependenciesPlatformAndroidJson>(), result)
  }

  @Test
  fun filterAndroidPackages_withEmptyObject_returnsEmpty() {
    val task = createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
    val result = task.filterAndroidPackages(ModelAutolinkingConfigJson("1000.0.0", null, null))
    Assert.assertEquals(emptyList<ModelAutolinkingDependenciesPlatformAndroidJson>(), result)
  }

  @Test
  fun filterAndroidPackages_withNoAndroidObject_returnsEmpty() {
    val task = createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
    val result =
        task.filterAndroidPackages(
            ModelAutolinkingConfigJson(
                reactNativeVersion = "1000.0.0",
                dependencies =
                    mapOf(
                        "a-dependency" to
                            ModelAutolinkingDependenciesJson(
                                root = "./a/directory",
                                name = "a-dependency",
                                platforms =
                                    ModelAutolinkingDependenciesPlatformJson(android = null))),
                project = null))
    Assert.assertEquals(emptyList<ModelAutolinkingDependenciesPlatformAndroidJson>(), result)
  }

  @Test
  fun filterAndroidPackages_withValidAndroidObject_returnsIt() {
    val task = createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
    val android =
        ModelAutolinkingDependenciesPlatformAndroidJson(
            sourceDir = "./a/directory/android",
            packageImportPath = "import com.facebook.react.aPackage;",
            packageInstance = "new APackage()",
            buildTypes = emptyList(),
        )

    val result =
        task.filterAndroidPackages(
            ModelAutolinkingConfigJson(
                reactNativeVersion = "1000.0.0",
                dependencies =
                    mapOf(
                        "a-dependency" to
                            ModelAutolinkingDependenciesJson(
                                root = "./a/directory",
                                name = "a-dependency",
                                platforms =
                                    ModelAutolinkingDependenciesPlatformJson(android = android))),
                project = null))
    Assert.assertEquals(1, result.size)
    Assert.assertEquals(android, result.first())
  }

  @Test
  fun generateCmakeFileContent_withNoPackages_returnsEmpty() {
    val output =
        createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
            .generateCmakeFileContent(emptyList())
    // language=cmake
    Assert.assertEquals(
        """
      # This code was generated by [React Native](https://www.npmjs.com/package/@react-native/gradle-plugin)
      cmake_minimum_required(VERSION 3.13)
      set(CMAKE_VERBOSE_MAKEFILE on)
      
      
      
      set(AUTOLINKED_LIBRARIES
        
      )
    """
            .trimIndent(),
        output)
  }

  @Test
  fun generateCmakeFileContent_withPackages_returnsImportCorrectly() {
    val output =
        createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
            .generateCmakeFileContent(testDependencies)
    // language=cmake
    Assert.assertEquals(
        """
      # This code was generated by [React Native](https://www.npmjs.com/package/@react-native/gradle-plugin)
      cmake_minimum_required(VERSION 3.13)
      set(CMAKE_VERBOSE_MAKEFILE on)
      
      add_subdirectory(./a/directory/ aPackage_autolinked_build)
      add_subdirectory(./another/directory/ anotherPackage_autolinked_build)
      add_subdirectory(./another/directory/cxx/ anotherPackage_cxxmodule_autolinked_build)
      
      set(AUTOLINKED_LIBRARIES
        react_codegen_aPackage
        react_codegen_anotherPackage
      another_cxxModule
      )
    """
            .trimIndent(),
        output)
  }

  @Test
  fun generateCppFileContent_withNoPackages_returnsEmpty() {
    val output =
        createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
            .generateCppFileContent(emptyList())
    // language=cpp
    Assert.assertEquals(
        """
      /**
       * This code was generated by [React Native](https://www.npmjs.com/package/@react-native/gradle-plugin).
       *
       * Do not edit this file as changes may cause incorrect behavior and will be lost
       * once the code is regenerated.
       *
       */

      #include "autolinking.h"


      namespace facebook {
      namespace react {

      std::shared_ptr<TurboModule> autolinking_ModuleProvider(const std::string moduleName, const JavaTurboModule::InitParams &params) {

        return nullptr;
      }

      std::shared_ptr<TurboModule> autolinking_cxxModuleProvider(const std::string moduleName, const std::shared_ptr<CallInvoker>& jsInvoker) {

        return nullptr;
      }

      void autolinking_registerProviders(std::shared_ptr<ComponentDescriptorProviderRegistry const> providerRegistry) {

        return;
      }

      } // namespace react
      } // namespace facebook
    """
            .trimIndent(),
        output)
  }

  @Test
  fun generateCppFileContent_withPackages_returnsImportCorrectly() {
    val output =
        createTestTask<GenerateAutolinkingNewArchitecturesFileTask>()
            .generateCppFileContent(testDependencies)
    // language=cpp
    Assert.assertEquals(
        """
      /**
       * This code was generated by [React Native](https://www.npmjs.com/package/@react-native/gradle-plugin).
       *
       * Do not edit this file as changes may cause incorrect behavior and will be lost
       * once the code is regenerated.
       *
       */

      #include "autolinking.h"
      #include <aPackage.h>
      #include <anotherPackage.h>
      #include <react/renderer/components/anotherPackage/ComponentDescriptors.h>
      #include <AnotherCxxModule.h>

      namespace facebook {
      namespace react {

      std::shared_ptr<TurboModule> autolinking_ModuleProvider(const std::string moduleName, const JavaTurboModule::InitParams &params) {
      auto module_aPackage = aPackage_ModuleProvider(moduleName, params);
      if (module_aPackage != nullptr) {
      return module_aPackage;
      }
      auto module_anotherPackage = anotherPackage_ModuleProvider(moduleName, params);
      if (module_anotherPackage != nullptr) {
      return module_anotherPackage;
      }
        return nullptr;
      }

      std::shared_ptr<TurboModule> autolinking_cxxModuleProvider(const std::string moduleName, const std::shared_ptr<CallInvoker>& jsInvoker) {
      if (moduleName == AnotherCxxModule::kModuleName) {
      return std::make_shared<AnotherCxxModule>(jsInvoker);
      }
        return nullptr;
      }

      void autolinking_registerProviders(std::shared_ptr<ComponentDescriptorProviderRegistry const> providerRegistry) {
      providerRegistry->add(concreteComponentDescriptorProvider<AnotherPackageComponentDescriptor>());
        return;
      }

      } // namespace react
      } // namespace facebook
    """
            .trimIndent(),
        output)
  }

  private val testDependencies =
      listOf(
          ModelAutolinkingDependenciesPlatformAndroidJson(
              sourceDir = "./a/directory",
              packageImportPath = "import com.facebook.react.aPackage;",
              packageInstance = "new APackage()",
              buildTypes = emptyList(),
              libraryName = "aPackage",
              componentDescriptors = emptyList(),
              cmakeListsPath = "./a/directory/CMakeLists.txt",
          ),
          ModelAutolinkingDependenciesPlatformAndroidJson(
              sourceDir = "./another/directory",
              packageImportPath = "import com.facebook.react.anotherPackage;",
              packageInstance = "new AnotherPackage()",
              buildTypes = emptyList(),
              libraryName = "anotherPackage",
              componentDescriptors = listOf("AnotherPackageComponentDescriptor"),
              cmakeListsPath = "./another/directory/CMakeLists.txt",
              cxxModuleCMakeListsPath = "./another/directory/cxx/CMakeLists.txt",
              cxxModuleHeaderName = "AnotherCxxModule",
              cxxModuleCMakeListsModuleName = "another_cxxModule",
          ))
}
