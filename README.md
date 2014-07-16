koala
=====

Android插件平台 KOALA

该平台目前：<br>
1、支持Activity（显式，隐式启动），Service（显式，隐式启动），BroadcastReceiver（动态注册，静态注册）组件。<br>
2、支持插件间互调。<br>
3、支持本地库。<br>
4、支持插件包资源访问。<br>
5、支持插件自己的data目录。<br>
<br>
一共有3个项目：<br>
1、koala：是插件的sdk，包含的插件的安装，启动，卸载等功能，该项目是不可运行的Android项目，libs目录下的frameworks-classes-full-debug.jar和libcore-classes-full-debug是基于Android4.4.2源码编译成userdebug版SDK，主要用于访问hide的API<br>
2、MainApp：使用插件sdk的项目，调用插件的API来管理插件，asset目录下有测试的插件demo，项目运行时会将其拷贝到/sdcard/koala目录下。<br>
3、PluginApp：上个项目中插件demo的源码，该项目开发时需要把koala SDK作为外部jar包导入。<br>

你可以通过运行MainApp，来启动插件demo。<br>

注意事项：<br>
1、插件中弹出Toast必须使用SDK中的showToast方法，否则不显示。<br>
2、插件中有本地库加载的话，由于虚拟机没有提供本地库卸载的功能，所以卸载的时候只能重启应用。<br>
3、兼容Android2.3到4.4
