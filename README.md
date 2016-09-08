# Tag

A 3D tag game. In development.

## Licenses

Code is licensed under the MIT License (found in the LICENSE.MD file). Art
assets are licensed under Creative Commons 4.0 (file CC_LICENSE.MD). The art
license applies to anything inside the following folders:

* Assets/
* Client/Assets/Tag/Models/
* Client/Assets/Tag/Textures/
* Server/levels/

## Dependencies

Some dependencies are not included in the repository since I do not own them,
they are still required if you want to download the project and compile it
locally.

### Art dependencies

The client uses the [Cartoon BigHead Starter](https://www.assetstore.unity3d.com/en/#!/content/15991)
with animations from [Mixamo](https://www.mixamo.com/). The animations are:
* Fall Flat
* Soccer Tackle
* Rejected
* Idle
* Running
* Change Direction
* Bboy Hip Hop move

### Code dependencies

The client uses the SmartFox2X plugin to connect to the server, it is usually
located in the Client/Assets/Plugins folder. It is not included in the
repository but can be found easily in the SmartFox2X website.

The server requires [jBullet](http://jbullet.advel.cz/) and vecmath (which comes
included with the jBullet distribution). It also requires the SmartFox2X
installation and the correct .jar files, the instructions can be found in the
SFSX website. Right now the project also requires lwjgl.jar and lwjgl_util.jar,
though these are debug dependencies and will be removed in the future.

## Credits

Font [Scorpion](http://www.dafont.com/scorpion.font) by [Geronimo](http://www.dafont.com/profile.php?user=756192)
