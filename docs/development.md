## Development

The plugin is organised to allow you to use a fork and deploy your own custom version of it
to a Maven repository.

### Using plugin locally from another project

A `settings.gradle` file is included at the root level in order to allow you to use the plugin
in a multi-project build. Specifically this allows you to symlink the root of the plugin
from the `buildSrc/` directory of another project so you can reference multiple custom plugins
in other projects.

To do this, `cd` to the project where you would like to use your own checkout of the plugin and:

```bash
mkdir buildSrc
cd buildSrc
ln -s /path/to/gradle-aws-plugin ./
```

Then add the following files to the `buildSrc/` directory:

_`buildSrc/build.gradle`_
```groovy
apply plugin: "java"

repositories {
  jcenter()
  mavenCentral()
}

dependencies {
  runtime subprojects.findAll { it.getTasksByName("jar", false) }
}

```
_`buildSrc/settings.gradle`_
```groovy
rootProject.name = 'my-project-build'

include 'gradle-aws-plugin'
```

This will make all subprojects in `buildSrc/` 
with jar archives available as plugins to your main project provided you add the appropriate include
directive in `settings.gradle` (see: [gradle forum post](http://forums.gradle.org/gradle/topics/is_it_possible_to_create_a_multi_project_setup_for_plugins_in_the_buildsrc_directory)).

### Deploying plugin artifact to Maven

If you would like to push your own version of the plugin to a Maven repository you can do so
by changing the group property to your own Maven group name in the `build.gradle` of the plugin
and creating a file in `deploy/` named `${group}.gradle`.

Implement the logic to publish your archive in that file, then run the command to publish.

See for example:

[jp.classmethod.aws.gradle](deploy/jp.classmethod.aws.gradle) 

Or using maven-publish:
 
[TouchType.gradle](deploy/TouchType.gradle).
