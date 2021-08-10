# Juppiter ![workflow status](https://github.com/CloudNetService/juppiter/actions/workflows/build.yml/badge.svg)

**Juppiter** is a simple Gradle plugin for CloudNet 3 which automatically generates the module.json file for your
CloudNet module. Properties can automatically get detected as well as being specified in the build.gradle of your module
project.

## Usage

Simply add the Juppiter plugin to your build.gradle as follows:

```groovy
plugins {
  id 'eu.cloudnetservice.juppiter' version '0.1.2'
}
```

and configure it at least like this:

```groovy
moduleJson {
  main = 'eu.cloudnetservice.cloudnet.ext.signs.node.CloudNetSignsModule'
}
```

Now just run your build using gradle and the module.json will be generated and added to your output jar file.

## Full configuration

Here is a full overview of all configuration options and their default settings:

```groovy
moduleJson {
  // main is the only setting which can not get automatically set
  // you have to define it yourself
  main = 'eu.cloudnetservice.cloudnet.ext.signs.node.CloudNetSignsModule'
  // Sets whether is module is a runtime module or not. Runtime modules
  // will not get reloaded when using 'reload confirm'. For changing their
  // configuration you have to restart CloudNet or provide a custom config
  // reload command.
  runtimeModule = false
  // Sets if this module stores sensitive data. If this is set to true
  // the module configuration will for example not get appended to the
  // output of a node report module paste.
  storesSensitiveData = false
  // Sets the name of this module. This defaults to the project name
  // if not specified.
  name = 'CloudNet-Signs'
  // Specifies the group of this module. This default to the project
  // group if not specified.
  group = 'eu.cloudnetservice.cloudnet.modules'
  // Specified the author of this module. This defaults to 'Anonymous'
  // if not specified.
  author = 'CloudNetService'
  // Specified the version of this module. This defaults to the project
  // version if not specified.
  version = '2.0'
  // Specified the website of this module. This can for example be your
  // wiki page for this module. If not specified this setting defaults
  // to 'https://cloudnetservice.eu'
  website = 'https://cloudnetservice.eu'
  // Specifies the data folder of this module. This will override the
  // default data folder configuration of CloudNet which is based on the
  // base path of the module provider and the name of the module. In this
  // case it would be 'modules/CloudNet-Signs' for the default module provider.
  dataFolder = 'modules/IWantMyCustomDataFolder'
  // Specified the description of this module. This defaults to the
  // project description and if the project description is not set
  // it will fall back to 'Just another CloudNet3 module'
  description = 'CloudNet extension which adds sign connector support for Bukkit, Nukkit and Sponge'
  // Specifies the minimum java version required in the runtime in order to 
  // run this module. If this property is not set it's assumed that the module
  // can run on every version starting from java version 8. The same rule applies
  // if the module defines a version which is older than version 8. If the runtime
  // version does not support the provided java version, a warning will be printed
  // and the module will not get loaded.
  minJavaVersionId = JavaVersion.VERSION_11
  // You can define custom properties for your module which will be accessible
  // in the runtime in a seperated JsonDocument: ModuleConfiguration#getProperties.
  // The properties object is a map, all keys must be a string the values can be
  // anything you want (in this case a string and a boolean).
  properties.put("Build-Number", System.getenv('BUILD_NUMBER').toString())
  properties.put("Development-Build", project.version.endsWith('-SNAPSHOT'))
  // You can define custom dependencies which will get added to your
  // module.json. However any dependency in the dependency block
  // which uses the configuration 'moduleLibrary' will be resolved against
  // all repositories defined and added when a matching repository is found.
  // If the repository is not yet defined, it will be added automatically
  // to the build output. Below is an example how to add a custom dependencies.
  dependencies {
    // This case demonstrates how to depend on the bridge module without putting it 
    // into the dependency handler block of your build.gradle (See below for more 
    // information on this). The bridge module now gets loaded before this module.
    // No repository is defined, so the module must be loaded already for this to work.
    // The module version which runs in the runtime must match the following convention:
    //    - the major version must match exactly the defined version major
    //    - the minor version must be higher or equal to the defined version minor
    //    - the patch version is ignored as it should never contain breaking changes
    // Note that this version checking only works for modules which follow the semantic
    // versioning 2: https://semver.org/
    'CloudNet-Bridge' {
      version = '1.2'
      group = 'de.dytanic.cloudnet.modules'
    }
    // This case demonstrates how to depend on the rest module which now gets 
    // loaded before this module. The download url of the module is defined
    // so it will be loaded from there if the dependency is not yet available
    // on the local machine. The url property will always force the node
    // by default to use this url, so defining a repo has no effect. Note
    // that in this example the rest module is not dependency as a module
    // but as a normal maven dependency (so the rest module will not get loaded
    // before this module but downloaded and added to the module ucp).
    'CloudNet-Rest' {
      version = '1.0'
      group = 'de.dytanic.cloudnet.modules'
      url = 'https://cloudnetservice.eu/cloudnet/updates/versions/3.4.0-RELEASE/cloudnet-rest.jar'
    }
    // This case demonstrates how to depend on gson without putting it into the
    // dependency handler block of your build.gradle (See below for more information
    // on this). Please note that for this example to work a repository with the
    // name 'Maven-Central' has to be added to the project. If the repository is
    // not defined the node will just silently ignore the dependency. If the
    // repository is present the dependency will be downloaded like a normal
    // maven dependency. For more information on this please check out the docs:
    // https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
    'Gson' {
      version = '2.8.7'
      group = 'com.google.code.gson'
      repo = 'Maven-Central'
    }
  }
  // On the other hand you are able to configure custom repositories if
  // you need them. And repository needed by your defined dependencies
  // will be added as well.
  repositories {
    'Another Jitpack Repo' {
      url = 'https://jitpack.io'
    }
  }
}
```

## Defining dependencies

You can still define any dependency you need for your build. However, any dependency notated as `moduleLibrary` will be
automatically added the `compileOnly` class path and later added to the dependencies in the generated module.json. Here
is an example:

```groovy
dependencies {
  // will be added to the fat jar by for example the shadow plugin
  implementation group: 'com.zaxxer', name: 'HikariCP', version: '5.0.0'
  // these are only available during the compile and will be added to the
  // module.json file and loaded in the runtime. These dependencies must be
  // resolvable in a repository in order to work.
  moduleLibrary group: 'io.netty', name: 'netty-handler', version: '4.1.66.Final'
  moduleLibrary group: 'com.github.juliarn', name: 'npc-lib', version: 'development-SNAPSHOT'
  // This defines a dependency on another module. These will not be associated with
  // any repository during the build but must be present for the module provider in
  // the runtime in order to work. There is no way that CloudNet loads module dependencies
  // automatically. The classes of the module are available during the compile process
  // and will not get shaded into the jar. See above for information about strict
  // module version checking.
  moduleDependency group: 'de.dytanic.cloudnet.modules', name: 'CloudNet-Bridge', version: '1.2'
  moduleDependency group: 'eu.cloudnetservice.cloudnet.modules', name: 'CloudNet-Signs', version: '2.0'
  // other dependencies you may need
  testImplementation 'org.junit.jupiter:junit-jupiter-api:5.7.0'
  testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.7.0'
}
```

These files are coming from the maven central repo. The plugin will find any transitive dependency and their
repositories and adds all of them to the module.json file.

## Customize the generation task

In the [Full configuration](#full-configuration) section the configuration of the module extension was explained. Below
is a small description on how to modify the generation task. By default, there is no need to configure any property of
the task settings.

```groovy
genModuleJson {
  // Sets the output file name. By default this is set to 'module.json' as
  // the default CloudNet module loader will only recognize this type of
  // module configuration.
  fileName.set("module.yml")
  // There is another option which allows you do set the module configuration
  // class instance which gets generated. By default you should only use the
  // extension which allows you to configure the module configuration. This
  // is only here because it exists.
  moduleConfiguration.set(ModuleConfigration.newInstance(project))
  // The output directory property defines the output directory of the generated
  // file and the inclusion point for the jar task. Please note this comment of
  // the gradle documentation: "This will cause the task to be considered 
  // out-of-date when the directory path or task output to that directory has 
  // been modified since the task was last run.". This property defaults to
  // a sub directory of the build directory named 'generated/module-json'.
  outputDirectory.set(project.layout.buildDirectory.dir("generated/module-yaml"))
}
```

## Support & Issues

If you need help using this plugin or found an issue feel free to join
our [Discord Server](https://discord.cloudnetservice.eu/) or open
an [issue](https://github.com/CloudNetService/juppiter/issues/new) on GitHub.

**Happy developing!**
