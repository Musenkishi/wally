Wally
=====

Wally is a fast and efficient open source wallpaper application for Android.

![](assets/wally_logo.png)

Wally gets its source of images from [Wallhaven][1]. By scraping the website, it provides the user
with a fast and smooth experience filled with subtle animations and a minimal design. The main goal
of Wally is to provide the same functionality as the website but in a more mobile friendly way.

Development
-----------
Wally is a gradle project built with Android Studio 0.8.x. To get started, import the project in
Android Studio by choosing the <code>build.gradle</code> located in the root folder.

However, you might notice that it won't build right away. This is because you have to provide it
with/generate your own release- and debug keystore. The debug keystore can be generated the same
way as a release keystore. Put the release- and debug keystore files in a directory of your choice
and reference them in a <code>local.properties</code> file in the root folder of this project.

Architecture
------------
Wally is divided into multiple modules; Models, Data Provider, and the main module Wally (UI). This
architecture allows a project to more easily expand to other platforms (e.g. Android Wear
or Android Auto).

License
-------
Apache 2.0 where applicable. See LICENSE file for details.

Contributing
------------
Pull requests are welcome!

Thanks
------
* All alpha testers.
* Everyone who has contributed ideas and reported issues!

Author
------
Freddie Lust-Hed - @musenkishi

Disclaimer
---------
This is not an official Wallhaven product.

[1]: http://alpha.wallhaven.cc