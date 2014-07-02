koala
=====

Android插件平台

一共有3个项目：
1、koala：是插件的sdk，包含的插件的安装，启动，卸载等功能，该项目的开发需要把classes-full-debug.jar作为外部jar包导入，因为使用了PackageParser的相关功能。
2、MainApp：使用插件sdk的项目，调用插件的API来管理插件，asset目录下有测试的插件demo，项目运行时会将其拷贝到/sdcard/koala目录下。
3、PluginApp：上个项目中插件demo的源码，该项目开发时需要把koala SDK作为外部jar包导入。

你可以通过运行MainApp，来启动插件demo。

注意事项：
1、插件中的service必须要在主项目的manifest声明，activity则不需要。
2、插件中有本地库加载的话，由于虚拟机没有提供本地库卸载的功能，所以卸载的时候只能重启应用。

